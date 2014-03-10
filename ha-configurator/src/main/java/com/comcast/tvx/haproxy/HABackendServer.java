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

/*
 * Encapsulates this: 
 * server server1 127.0.0.1:81 check
 */
public class HABackendServer extends HARenderer {

    private Integer port;
    private Boolean healthCheck;
    private String ipAddress;
    private String protocol;

    public HABackendServer(String ipAddress, Integer port, Boolean healthCheck, String protocol) {
        super();
        this.ipAddress = ipAddress;
        this.port = port;
        this.healthCheck = healthCheck;
        this.protocol = protocol;
    }

    public String toString() {
        // acl is_80 path_beg -i /healthCheck/80
        StringBuilder result = new StringBuilder().append(" server ").append(makeName(ipAddress)).append(" ")
                .append(ipAddress + ":" + port.toString());
        // hack... if xre server, DON'T specify check interval, it won't work
        if (!getProtocol().equalsIgnoreCase("xre"))
            result.append(" check inter 5000");
        return result.toString();
    }

    private String getProtocol() {

        return (protocol == null) ? "tcp" : protocol;
    }

    private String makeName(String networkName) {
        return networkName.replace('.', '_');
    }

    @Override
    public int compareTo(HAConfigurationSection o) {
        if (!(o instanceof HABackendServer))
            return 1;
        HABackendServer other = (HABackendServer) o;

        if (other.healthCheck != this.healthCheck)
            return -1;
        if (other.port != this.port)
            return -1;
        if (!other.ipAddress.equalsIgnoreCase(this.ipAddress))
            return -1;

        return 0;
    }
}
