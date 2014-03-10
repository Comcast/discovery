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
 * frontend healthcheck
 * #all checks are on 80
 * bind 0.0.0.0:80
 * #acls determine whick backend to route to based on url start
 * acl is_80 path_beg -i  /healthCheck/80
 * acl is_14013 path_beg -i  /healthCheck/14013
 * acl is_18181 path_beg -i  /healthCheck/18181
 *
 * use_backend healthCheck_80 if is_80
 * use_backend healthCheck_14013 if is_14013
 * use_backend healthCheck_18181 if is_18181
 */
public class HAHealthCheckFrontEnd extends HARenderer {
    private Integer port;

    private List<HAACL> acls;
    private List<HACondition> conditions;

    public HAHealthCheckFrontEnd(Integer port, List<HAACL> acls, List<HACondition> conditions) {
        this.port = port;
        this.acls = acls;
        this.conditions = conditions;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(wrap("frontend frontend_healthCheck_" + port.toString()));
        result.append(wrap("bind 0.0.0.0:" + port.toString()));
        result.append(wrap("stats enable"));
        result.append(wrap("stats uri /haproxy?stats"));

        for (HAACL acl : acls) {
            result.append(wrap(acl.toString()));
        }

        for (HACondition condition : conditions) {
            result.append(wrap(condition.toString()));
        }

        return result.toString();

    }

    @Override
    public int compareTo(HAConfigurationSection o) {
        if (!(o instanceof HAHealthCheckFrontEnd))
            return 1;
        HAHealthCheckFrontEnd that = (HAHealthCheckFrontEnd) o;
        if (that.port != this.port)
            return 1;

        if (!that.acls.containsAll(this.acls))
            return 1;
        if (!this.acls.containsAll(that.acls))
            return 1;

        if (!that.conditions.containsAll(this.conditions))
            return 1;
        if (!this.conditions.containsAll(that.conditions))
            return 1;
        return 0;

    }

}
