#! /bin/bash

file=$1
[[ -r $file ]]  || { echo $file is not readable ; exit 1; }
[[ "x$JAVA_HOME" == "x" ]] || { echo JAVA_HOME is not set; }

DEBUG=${DEBUG:-"false"}

nexus_user=user
nexus_password=password
cli_user=user
cli_password=password


function is_absolute_path {
        return $(echo $file | grep "^/" >/dev/null)
}

tmpdir=$(mktemp -d)

if [[ "$DEBUG" == "true" ]]
then
	echo working in $tmpdir
	echo absolute filename is $file
fi

cp $file $tmpdir
file=$(basename $file)
cd $tmpdir

zip="https://repo.usit.uio.no/nexus/content/repositories/internal-thirdparty/no/usit/sun/cli-deployer/0.2/cli-deployer-0.2.jar"
wget --user=$nexus_user --password=$nexus_password -Omyzip.zip $zip
unzip myzip.zip > /dev/null

JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=com.sun.java.swing "
java $JAVA_OPTS \
    -jar "jboss-modules.jar" \
    -mp "modules" \
     org.jboss.as.cli \
     --controller=w3utv-jb01.uio.no:9999 --connect --user=$cli_user --password=$cli_password --command="deploy $file --force "


cd -

rm -r $tmpdir
