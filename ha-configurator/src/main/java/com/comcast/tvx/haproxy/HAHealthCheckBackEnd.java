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

import java.util.List;

/*
 * backend healthCheck_80
 * mode http
 * balance roundrobin  # Load Balancing algorithm
 * option forwardfor
 * option httpchk /healthCheck
 * reqrep ^([^\ ]*\ /)healthCheck\/80[/]?(.*)     \1healthCheck\2
 * server server1 127.0.0.1:81 check
 *
 * This class is specialized to build "fall through" health check
 */
public class HAHealthCheckBackEnd extends HABackend {

    public HAHealthCheckBackEnd(Integer port, String healthCheck, List<HABackendServer> servers) {
        super(port, healthCheck, servers, "http");

    }

    public String toString() {
        if ((healthCheck == null) || (healthCheck.trim().length() == 0)) {
            logger.info("Cannot create healthcheck back for port: " + port + " because no healthCheck is specified");
        }
        StringBuilder result = new StringBuilder()
                .append(wrap("backend " + getName() + " "))
                .append(wrap("mode http"))
                .append(wrap("balance roundrobin"))
                .append(wrap("option forwardfor "))
                .append(wrap("option httpchk GET " + healthCheck))
                // reqrep ^([^\ ]*\ /)healthCheck\/80[/]?(.*) \1healthCheck\2
                .append(wrap("reqrep ^([^\\ ]*\\ /)healthCheck\\/" + port.toString() + "[/]?(.*)    \\1"
                        + healthCheckIfy(healthCheck) + "\\2"));
        for (HABackendServer server : servers) {
            result.append(wrap(server.toString()));
        }

        return result.toString();
    }

    /*
     * Remove leading slash, only used to regex above, which is special case
     */
    private String healthCheckIfy(String healthCheck) {
        if (healthCheck.startsWith("/"))
            return healthCheck.substring(1);
        return healthCheck;
    }

    public String getName() {

        return "healthCheck_" + port.toString();
    }

    public static String makeName(Integer port) {

        return "healthCheck_" + port.toString();
    }

}
