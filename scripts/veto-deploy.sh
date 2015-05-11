#!/bin/sh
#
# Dette er et felles-skript for å deploye SUAF-applikasjoner til
# veto-dev.  Det er stygt, men det gjør susen :)  Og det skal fungere
# både på Linux og i cygwin.  Særiteter som sånne Macs kan vi ikke
# garantere for.
# 
# Vær forsiktig: hvis man endrer på skriptet på ett prosjekt blir
# endringene synlige i de andre også (det er shared gjennom samme
# repository).
#   
# Skriptet forutsetter at man enten kan jboss sitt passord på veto-dev eller
# at man har lagret pub-nøkkelen sin der (og da kjører det passordløst)
#
# Hilsen, Matija & Alen 


#<jboss.home>C:\jbdevstudio\jboss-6</jboss.home>

APP=$1
shift
CMD=" $@"

if [ "$CMD" = " " ]; then
	echo -e "Usage: $0 <application> <command [command2...]>\n"
	echo 'Valid applications: evuweb, fagpersonweb, soknadsweb'
	echo 'Valid commands:     build, undeploy, deploy, restart (or "all" for all 4)'
	exit
fi
if [[ "$CMD" == *all* ]]; then
	CMD="build undeploy deploy restart"
fi


if [ $APP = "evuweb" ]; then
	DIR="/www/var/data/evuweb/app"
	JBAPP="evuweb"
	IS_ANT=1
elif [ $APP = "epn" ] || [ $APP = "kid" ]; then
	#DIR="/www/var/data/epn/app"
	DIR="/tmp/jboss-6.0.0.Final/server/default/deploy"
	JBAPP="epn"
	IS_ANT=0
else
	DIR="/www/var/data/fs-test/app"
	JBAPP="fs-test"
	IS_ANT=1
fi


if [[ "$CMD" == *build* ]]; then
    echo "Building..."

    # Ant
    if [ $IS_ANT = "1" ]; then

        ANT=`ant -version 2>/dev/null`
        if [ $? = 0 ]; then
                PWD=`pwd`
                cd ..
                ant deploy
                cd -
        fi
        
    # Maven
    else
    	cd ..
    	mvn -Dmaven.test.skip=true -npu -o package jboss:unpack
    	cd -

    fi
fi



if [[ "$CMD" == *undeploy* ]]; then
    echo "Removing old version..."
    ssh jboss@veto-dev rm -rf $DIR/$APP.[ew]ar
fi



if [[ "$CMD" == *\ deploy* ]]; then

    # Ant
    if [ $IS_ANT = "1" ]; then
        BUILDP='build.properties'
        if [ ! -e $BUILDP ]; then
            BUILDP="../$BUILDP"
        fi

	export deployd=`grep jboss.home $BUILDP | grep -v "#" | awk '{ sub("^.*=",""); print $0"/server/default/deploy/'$APP'.ear"}' | sed -e 's/C\\\:/cygdrive\/c/'`
    else
    	cd ..
        export deployd=`mvn jboss:unpack | grep 'Doing unpack' | sed -e 's/^.* to //g' | sed -e 's/C\:/\/cygdrive\/c/' | sed -e 's/\\\\/\//g'`
        cd -
    fi

    echo "Making sure permissions are ok..."
    find $deployd -type d | xargs chmod 755 2>/dev/null
    find $deployd -type f | xargs chmod 644
    echo "Copying files to veto-dev..."
    scp -qr $deployd jboss@veto-dev:$DIR/
fi


if [[ "$CMD" == *restart* ]]; then
	echo "Restarting jboss..."
	ssh jboss@veto-dev /usit/veto-dev/www/sbin/jbossctl -p $JBAPP restart
fi
