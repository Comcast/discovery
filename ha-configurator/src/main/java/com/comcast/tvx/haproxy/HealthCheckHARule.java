/*
 * Copyright 2014 Comcast Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.tvx.haproxy;

public class HealthCheckHARule extends BasicHARule {

    public HealthCheckHARule(String name, int externalPort, String httpchk) {
        super(name, externalPort, httpchk);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        int count = 0;

        StringBuilder buff = new StringBuilder().append("backend healtcheck_" + externalPort);
        // new
        // StringBuilder().append("listen ").append(name).append(" *:").append(String.valueOf(externalPort)).append(
        // "\n").append("  mode tcp\n");

        if ((httpchk != null) && (httpchk.trim().length() > 0)) {
            buff.append("  option httpchk " + httpchk + "\n");
        }

        // Show address of calling client to backend
        // Disabled for now, requires 'mode http' -we're currently 'mode tcp'
        // buff.append("  option forwardfor\n");

        // describes itself
        buff.append("  balance roundrobin\n");

        // set cookie to establish server affinity - might be useful for XRE
        // apps, but may also break scale-down

        // Disabled for now, requires 'mode http' -we're currently 'mode tcp'
        // buff.append("  cookie SERVERID insert indirect\n");

        for (String server : servers) {
            buff.append("  server server").append(String.valueOf(++count)).append(" ").append(server)
                    .append(" check\n");
        }

        buff.append("\n");

        return buff.toString();
    }

}
