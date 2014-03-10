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

public class HAFrontEnd extends HARenderer {

    private Integer port;
    private HABackend backend;

    public HAFrontEnd(Integer port, HABackend backend) {
        super();
        this.port = port;
        this.backend = backend;
    }

    @Override
    public int compareTo(HAConfigurationSection o) {

        if (!(o instanceof HAFrontEnd))
            return 1;

        HAFrontEnd other = (HAFrontEnd) o;
        if (!other.port.equals(this.port))
            return 1;

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(wrap("frontend frontend_" + port.toString() + " *:" + port.toString()));
        result.append(wrap("mode " + backend.getMode()));
        result.append(wrap("default_backend " + backend.getName()));
        return result.toString();
    }

    public void validate() {

        super.validate();

        if ((port == 80) || (port == 443)) {
            throw new RuntimeException(
                    "Ports 80 and 443 are reserved for healthcheck and cannot be used for non healthcheck front/backends");
        }
    }
}
