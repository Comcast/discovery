#!/bin/bash

NAME=reagent

[ -e /etc/sysconfig/$NAME ] && . /etc/sysconfig/$NAME

APP_HOME=$INSTALL_PREFIX/$NAME
CONF_DIR=$APP_HOME/conf
RUN_DIR=/var/run/$NAME
LOG_DIR=/var/log/$NAME

build_params=""

# source properties via facter if not defined
if [ -z "$zookeeper_connection" ]; then
    zookeeper_connection=`facter zookeeper_connection`
fi
if [ -n "$zookeeper_connection" ]; then
    build_params="$build_params -z $zookeeper_connection"
fi

if [ -z "$ipaddress" ]; then
    ipaddress=`facter ipaddress`
fi
if [ -n "$ipaddress" ]; then
    build_params="$build_params -i $ipaddress"
fi

if [ -z "$service_spec" ]; then
    service_spec=`facter service_spec`
fi
if [ -n "$service_spec" ]; then
    build_params="$build_params -s $service_spec"
fi

if [ -z "$flavor" ]; then
    flavor=`facter flavor`
fi
if [ -n "$flavor" ]; then
    build_params="$build_params -f $flavor"
fi

if [ -z "$region" ]; then
    region=`facter region`
fi
if [ -n "$region" ]; then
    build_params="$build_params -r $region"
fi

if [ -z "$availability_zone" ]; then
    availability_zone=`facter availability_zone`
fi
if [ -n "$availability_zone" ]; then
    build_params="$build_params -a $availability_zone"
fi

# If JAVA_OPTS are set, then it will completely override defaults.
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms64M -Xmx64M"
fi

if [ "$1" != "--nodaemon" ]; then
    nohup java $JAVA_OPTS \
        -cp $CONF_DIR:$APP_HOME/lib/* \
        com.comcast.tvx.cloud.RegistrationMain \
        $build_params \
        > $LOG_DIR/$NAME.out 2>&1 &

    echo $! > $RUN_DIR/$NAME.pid
else
    # run in foreground
    java $JAVA_OPTS \
        -cp $CONF_DIR:$APP_HOME/lib/* \
        com.comcast.tvx.cloud.RegistrationMain \
        $build_params

fi
