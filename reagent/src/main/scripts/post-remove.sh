##  %postun scriptlet

# Do not remove anything if this is not an uninstall
if [ $1 = 0 ]; then
    /usr/sbin/userdel -r %name >/dev/null 2>&1
    /usr/sbin/groupdel %name >/dev/null 2>&1
    # Ignore errors from above
    true
fi
