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

public class HABackend extends HARenderer {

    protected Integer port;
    protected String healthCheck;
    protected List<HABackendServer> servers;
    private String protocol;

    public HABackend(Integer port, String healthCheck, List<HABackendServer> servers, String protocol) {
        super();
        this.port = port;
        this.healthCheck = healthCheck;
        this.servers = servers;
        this.protocol = protocol;
    }

    public String getName() {
        return "backend_" + port.toString();
    }

    public String getMode() {
        return ((protocol != null) && (!protocol.equalsIgnoreCase("xre"))) ? protocol : "tcp";
    }

    public String toString() {
        StringBuilder result = new StringBuilder().append(wrap("backend " + getName()))
                .append(wrap(" mode " + getMode())).append(wrap(" balance roundrobin"));

        if (getMode().equalsIgnoreCase("http"))
            result.append(wrap(" option forwardfor "));

        if ((healthCheck != null) && (healthCheck.trim().length() != 0) && (getMode().equalsIgnoreCase("http"))) {
            result.append(wrap(" option httpchk GET " + healthCheck));
        }

        for (HABackendServer server : servers) {
            result.append(wrap(server.render()));
        }

        return result.toString();
    }

    @Override
    public int compareTo(HAConfigurationSection o) {
        if (!(o instanceof HABackend))
            return 1;
        HABackend other = (HABackend) o;

        if (!other.servers.containsAll(this.servers))
            return 1;
        if (!this.servers.containsAll(this.servers))
            return 1;

        if (!this.port.equals(other.port))
            return 1;
        if (!this.healthCheck.equalsIgnoreCase(other.healthCheck))
            return 1;

        return 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof HABackend))
            return false;
        if (o == this)
            return true;
        HABackend other = (HABackend) o;

        if (!other.servers.containsAll(this.servers))
            return false;
        if (!this.servers.containsAll(this.servers))
            return false;

        if (!this.port.equals(other.port))
            return false;
        if (!this.healthCheck.equalsIgnoreCase(other.healthCheck))
            return false;

        return true;
    }

}
