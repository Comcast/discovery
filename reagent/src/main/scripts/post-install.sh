##  %post scriptlet

# Set the installation directory and name in configuration file
sed -i  -e "s^@REPLACE_RPM_PREFIX@^$RPM_INSTALL_PREFIX^" \
        -e "s^@REPLACE_PKG_NAME@^%name^" \
        /etc/sysconfig/%name

if [ $1 = 1 ]; then
    /sbin/chkconfig --add %name
fi
