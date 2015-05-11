#!/bin/bash

[[ -r "$1" ]] || { echo "Usage: $0 simulation_script_file.sim ARG1 .. ARGN"; exit 1 ; }

source config

# params
sim_script_file=$1

args="${@:2}"
substitutions=""
# constants
tmp_path="/dev/shm"
status_file="$tmp_path/$0.$$.status"
#response_file="$tmp_path/$0.$$.response"
cookies_file="$tmp_path/$0.$$.cookies"
headers_file="$tmp_path/$0.$$.headers"
wget_opts="-sS -L -i -m $timeout --retry 1 -c $cookies_file -b $cookies_file "
#wget_opts="-sS -L -m $timeout --retry 1 -c $cookies_file "
jsf_viewstate_file="$tmp_path/$0.$$.viewstate"
params_file="$tmp_path/$0.$$.params"
time_opts="--format %e"
timer="/usr/bin/time"
bc="/usr/bin/bc"
tp_rst=$(tput sgr0)
tp_red=$(tput setaf 1)
tp_grn=$(tput setaf 2)

# dependency checks
[[ -x "$timer" ]] || { echo "We need GNU time to be installed ($timer)"; exit 1 ; }
[[ -x "$bc" ]] || { echo "We need GNU bc to be installed ($bc)"; exit 1 ; }

# global vars
total_cumulative_waiting_time_ms=15
last_elapsed_ms=0
last_elapsed_secs=0
declare -a executestack=(true)

function cleanup {
	rm -f $tmp_path/$0.$$.*
}

function update_elapsed_time {
	echo $last_elapsed_secs |grep "exited with non-zero status" > /dev/null
	if [ "$?" == "0" ] ; then
		last_elapsed_secs=$timeout
		echo WGET_ERROR $last_elapsed_secs > $response_file
		echo $(cat $status_file)
	fi
	elapsed_ms=$(echo "$last_elapsed_secs * 1000"| $bc)
	rounded_elapsed_ms=$(printf %0.f $elapsed_ms)
	last_elapsed_ms=$rounded_elapsed_ms
	let total_cumulative_waiting_time_ms=$total_cumulative_waiting_time_ms+$rounded_elapsed_ms 
}

function do_get {
	# params
	url=$(echo $1|sed s/\"//g)

        response_file="$tmp_path/$0.$$.GET.$sim_line_number.response.html"

	rm -f $response_file
	echo "$base_url$url" > $response_file
	last_elapsed_secs=$($timer $time_opts curl $wget_opts -o $response_file "$base_url$url"  2>&1)
	update_elapsed_time
	command_result=$last_elapsed_ms
        find_current_viewstate $response_file
}

function do_post {
	# params
	url=$(echo $1|sed s/\"//g)
	post_data=$(echo $2|sed  s/\"//g)
	if [[ ${#substitutions} -gt 0  ]]; then
		post_data=$(echo $post_data|sed $substitutions)
	fi
	viewstate=$(cat $jsf_viewstate_file)
	post_data="$post_data&$viewstate"
	# echo do_post har post_data $post_data

        response_file="$tmp_path/$0.$$.POST.$sim_line_number.response.html"

	rm -f $response_file
	last_elapsed_secs=$($timer $time_opts curl $wget_opts -o $response_file -d "$post_data" "$base_url$url" 2>&1)
	echo "$base_url$url" >> $response_file
	echo "posted data $post_data" >> $response_file
	update_elapsed_time
	command_result=$last_elapsed_ms
        find_current_viewstate $response_file
}


function do_fileupload {
	# params
	params=("${@:4}")
	#for p in ${params[@]}
	#do
	#	echo her er param $p
	#done
	
	url=$(echo $3|sed s/\"//g)
	form_data="${params[@]}"
	#if [[ ${#substitutions} -gt 0  ]]; then
	#	post_data=$(echo $post_data|sed $substitutions)
	#fi
	viewstate=$(cat $jsf_viewstate_file)
	form_data="$form_data -F$viewstate"

        response_file="$tmp_path/$0.$$.FILEUPLOAD.$sim_line_number.response.html"

	rm -f $response_file
	echo curl $wget_opts -o $response_file "$form_data" "$base_url$url"
	last_elapsed_secs=$($timer $time_opts curl $wget_opts -o $response_file $form_data "$base_url$url" 2>&1)
	echo "$base_url$url" >> $response_file
	echo "posted data $form_data" >> $response_file
	# FIXME ADD ELAPSED TIME
	#update_elapsed_time
	command_result=$last_elapsed_ms
        find_current_viewstate $response_file
}

function run_expect {
	# params
	parameters=("${@}")

	unset parameters[0]
	command=${parameters[2]}
	unset parameters[2]
	# line_number=${parameters[1]}
	unset parameters[1]
	parameters=${parameters[*]}

	matcher_output=$(echo $(cat $response_file) | ./matchers/$command $parameters)
	result=$PIPESTATUS	

	if [ "$result" == "0" ] ; then
		command_result="${tp_grn}PASSED${tp_rst}"
	else
		command_result="${tp_red}FAILED${tp_rst} $matcher_output "
                failpath="$tmp_path/FAIL-$(basename $response_file)"
		echo $args > $failpath
		echo $command_result >> $failpath
		cat $response_file >> $failpath
	fi
}


function run_iftest {
	# params
	parameters=("${@}")

	unset parameters[0]
	command=${parameters[2]}
	unset parameters[2]
	# line_number=${parameters[1]}
	unset parameters[1]
	parameters=${parameters[*]}

	matcher_output=$(echo $(cat $response_file) | ./matchers/$command $parameters)
	result=$PIPESTATUS	

	if [ "$result" == "0" ] ; then
		command_result="${tp_grn}PASSED${tp_rst} IF $matcher_output"
	else
		command_result="${tp_grn}FAILED${tp_rst} IF $matcher_output "
	fi
}


function find_current_viewstate () {
	file=$1
	grep -m1 "input.*hidden.*ViewState.*j_id" $file | sed 's|.*id="javax.faces.ViewState" value="\(j_id[0-9]*\).*|javax.faces.ViewState=\1|' > $jsf_viewstate_file
	
}


function execute_line {
	# params
	sim_line_number=$1
	sim_command=$2
	argument1=$3
	argument2=$4
	all_arguments="${@}"


	# is it a comment?
	if [ "${sim_command:0:1}" == "#" ] ; then
		return 0
	fi
		
	case $sim_command in
		WAIT)
			sleep $argument1
			return 0
		;;
		GET)
			do_get $argument1 $argument2
		;;
		POST)
			do_post $argument1 $argument2
		;;
		FILEUPLOAD)
			do_fileupload $all_arguments
		;;
		EXPECT)
			run_expect $all_arguments
		;;
		IF)
			run_iftest $all_arguments
		;;
		TERMINATE)
			echo TERMINATING line $sim_line_number
			return 10
		;;
		*)
			return 1
		;;		
	esac
	

	echo -e $$:$sim_command-$sim_line_number: $command_result
}

function is_if_line {
	return $(echo $@ | grep "^IF " >/dev/null)
}

function is_fi_line {
	return $(echo $@ | grep -w "^FI" >/dev/null)
}


function is_else_line {
	return $(echo $@ | grep "^ELSE" >/dev/null)
}

function is_terminate_line {
	return $(echo $@ | grep "^TERMINATE" >/dev/null)
}

function command_passed {
	return $(echo $command_result | grep "PASSED" >/dev/null)
}

function pop_exec_stack() {
	old=${executestack[0]}
	executestack=("${executestack[@]:1}")
	# echo popped $old off of execstack, new ${executestack[@]}
}

function push_exec_stack {
	#echo command passed, will add $1 to exstack ${executestack[@]}
	executestack=($1 ${executestack[@]})
	#echo new execstack ${executestack[@]}
}

function revert_top_element_of_stack {
	old=${executestack[0]}
	if [[ $old == "true" ]]
	then
		executestack=("false"  ${executestack[@]:1})
	else
		executestack=("true" ${executestack[@]:1})
	fi
	# echo reverting new executestack ${executestack[@]}
}

function execute_mode {
	if [[ ${executestack[0]} == "true" ]]; then
		return "0"
	fi
	return "1"
}

function emptyline_or_comment {
	if [[ -z $1 ]] || [[ "#" == ${1:0:1} ]]
	then
		return "0"
	fi
	return "1"
}

function execute_every_line_in_script {
	script_file=$1
	line_number=1
	while read line; do
		line=$(echo "${line}" | sed -e 's/^[ \t]*//')
		if emptyline_or_comment $line; then let line_number=$line_number+1; continue; fi
		if is_if_line $line
		then
			execute_line $line_number $line
			if command_passed
			then
				push_exec_stack true
			else
				push_exec_stack false
			fi
		elif is_fi_line $line
		then
			pop_exec_stack
		elif is_else_line $line ; then
			revert_top_element_of_stack
			
		else # normal case
			if execute_mode; then
				execute_line $line_number $line
				if [[ $? -gt 0 ]]; then
					break
				fi
			fi
		fi
		let line_number=$line_number+1
	done < <(cat $script_file)
	# Assert that stack is back to normal
	if [[ "${executestack[@]}" != "true" ]]; then
		echo WARNSTACKFAIL ${executestack[@]}
	fi
}

raw_urlencode() {
  local string="${1}"
  local strlen=${#string}
  local encoded=""

  for (( pos=0 ; pos<strlen ; pos++ )); do
     c=${string:$pos:1}
     case "$c" in
        [-_.~a-zA-Z0-9] ) o="${c}" ;;
        * )               printf -v o '%%%02x' "'$c"
     esac
     encoded+="${o}"
  done
  echo "${encoded}"
}



# function main ()

for a in $args
do 
	var=$(echo $a | cut -f1 -d=)
	sub=$(raw_urlencode $(echo $a | cut -f2 -d=))
	#sub=$(echo $a | cut -f2 -d=)
	if [[ ${#substitutions} -eq 0 ]]
	then
		substitutions="s/$var/$sub/g"
#		substitutions="$(echo $a |sed 's|\(.*\)=\(.*\)|s/\1/\2/g|')"
	else
		substitutions+=";s/$var/$sub/g"
#		substitutions+=";$(echo $a |sed 's|\(.*\)=\(.*\)|s/\1/\2/g|')"
	fi
done
# echo got substitutions $substitutions



echo $@> $params_file
echo $$ $@
execute_every_line_in_script "$sim_script_file"
echo $$:CUMULATIVE: $total_cumulative_waiting_time_ms

if [[ $DEBUG != "true" ]];
then
	cleanup
fi

exit 0
