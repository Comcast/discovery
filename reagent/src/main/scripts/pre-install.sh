##  %pre scriptlet

# If app user does not exist, create
id %name >/dev/null 2>&1
if [ $? != 0 ]; then
    /usr/sbin/groupadd -r %name >/dev/null 2>&1
    /usr/sbin/useradd -d /var/run/%name -r -g %name %name >/dev/null 2>&1
fi
