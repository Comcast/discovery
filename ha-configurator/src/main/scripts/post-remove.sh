##  %postun scriptlet

# Do not remove anything if this is not an uninstall
if [ $1 = 0 ]; then
    # This may not exist if haproxy is also removed.
    if [ -f /etc/init.d/haproxy ]; then
        sed -i -e 's% -f /etc/$prog/discovery.cfg%%g' haproxy
    fi
fi
