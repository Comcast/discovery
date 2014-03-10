#!/bin/bash

NAME=ha-configurator

[ -e /etc/sysconfig/$NAME ] && . /etc/sysconfig/$NAME

APP_HOME=$INSTALL_PREFIX/$NAME
CONF_DIR=$APP_HOME/conf
RUN_DIR=/var/run/$NAME
LOG_DIR=/var/log/$NAME
if [ "$#" -ne 3 ]; then
    echo "usage: mappings-loader.sh <zookeeper_connection> <zookeeper root to load to> <file to load>"
    echo "example: mappings-loader.sh localhost:2181 /awesome_services/bubblegum /opt/ha-configurator/conf/mappings.conf"
    exit 1
fi

# If JAVA_OPTS are set, then it will completely override defaults.
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms256M -Xmx384M"
fi

    # run in foreground
    java $JAVA_OPTS \
        -cp $CONF_DIR:$APP_HOME/lib/* \
        com.comcast.tvx.haproxy.MappingsLoaderMain \
        -z $1 -r $2 -m $3

