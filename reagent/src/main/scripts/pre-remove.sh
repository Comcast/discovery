##  %preun scriptlet

# Stop service if running
if [ -e /etc/init.d/%name ]; then
    /sbin/service %name stop > /dev/null 2>&1
    true
fi

# If not an upgrade, then delete
if [ $1 = 0 ]; then
    /sbin/chkconfig --del %name > /dev/null 2>&1
    true
fi
