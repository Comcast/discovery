#!/bin/bash

NAME=ha-configurator

[ -e /etc/sysconfig/$NAME ] && . /etc/sysconfig/$NAME

APP_HOME=$INSTALL_PREFIX/$NAME
CONF_DIR=$APP_HOME/conf
RUN_DIR=/var/run/$NAME
LOG_DIR=/var/log/$NAME

# source properties via facter if not defined
if [ -z "$zookeeper_connection" ]; then
    zookeeper_connection=`facter zookeeper_connection`
fi
#if mappings root is not set, then set it from facter
#if not set in facter, set it to nothing (it is an optional param)
if [ -z "$mappings_root" ]; then
    mappings_root=`facter mappings_root`
    if [ -z "$mappings_root" ]; then
        mappings_root=""
    else
        mappings_root="-x $mappings_root"
    fi
fi


# If JAVA_OPTS are set, then it will completely override defaults.
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms256M -Xmx384M"
fi

if [ "$1" != "--nodaemon" ]; then
    nohup java $JAVA_OPTS \
        -cp $CONF_DIR:$APP_HOME/lib/* \
        com.comcast.tvx.haproxy.ConfiguratorMain \
        -z $zookeeper_connection  $mappings_root -f $CONF_DIR/filters.conf -m $CONF_DIR/mappings.conf -o /etc/haproxy/discovery.cfg \
        > $LOG_DIR/$NAME.out 2>&1 &

    echo $! > $RUN_DIR/$NAME.pid
else
    # run in foreground
    java $JAVA_OPTS \
        -cp $CONF_DIR:$APP_HOME/lib/* \
        com.comcast.tvx.haproxy.ConfiguratorMain \
        -z $zookeeper_connection  $mappings_root -f $CONF_DIR/filters.conf -m $CONF_DIR/mappings.conf -o /etc/haproxy/discovery.cfg 

fi
