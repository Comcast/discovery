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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class HAHealthCheckFrontEndTest {
    @Test
    public void testEquality() {
        Integer backEndHealthCheckPort = 8080;
        HAACL anAcl = new HAACL(backEndHealthCheckPort, "/healthCheck");
        HABackendServer backendServer = new HABackendServer("192.168.201.2", 44, true, "xre");

        List<HABackendServer> servers = new ArrayList<HABackendServer>();
        servers.add(backendServer);
        HAHealthCheckBackEnd healthCheckBackEnd = new HAHealthCheckBackEnd(backEndHealthCheckPort, "healthCheck",
                servers);

        HACondition haConditional = new HACondition(healthCheckBackEnd.getName(), "is_" + backEndHealthCheckPort);
        List<HAACL> acls = new ArrayList<HAACL>();
        acls.add(anAcl);
        List<HACondition> conditions = new ArrayList<HACondition>();
        conditions.add(haConditional);
        HAHealthCheckFrontEnd healthCheckFrontEnd = new HAHealthCheckFrontEnd(80, acls, conditions);
        HAHealthCheckFrontEnd otherCheckFrontEnd = new HAHealthCheckFrontEnd(80, acls, conditions);

        assertEquals(healthCheckFrontEnd.compareTo(otherCheckFrontEnd), 0);

    }

    @Test
    public void testInEquality() {
        Integer backEndHealthCheckPort = 8080;
        HAACL anAcl = new HAACL(backEndHealthCheckPort, "/healthCheck");

        List<HABackendServer> servers = new ArrayList<HABackendServer>();
        servers.add(new HABackendServer("192.168.201.2", 44, true, "xre"));
        servers.add(new HABackendServer("192.168.201.3", 44, true, "xre"));
        servers.add(new HABackendServer("192.168.201.4", 44, true, "xre"));

        String backendName = "foo";
        HACondition haConditional = new HACondition(backendName, "is_" + backEndHealthCheckPort);
        List<HAACL> acls = new ArrayList<HAACL>();
        acls.add(anAcl);
        List<HACondition> conditions = new ArrayList<HACondition>();
        conditions.add(haConditional);
        HAHealthCheckFrontEnd healthCheckFrontEnd = new HAHealthCheckFrontEnd(80, acls, conditions);
        HAHealthCheckFrontEnd otherCheckFrontEnd = new HAHealthCheckFrontEnd(81, acls, conditions);

        assertEquals(healthCheckFrontEnd.compareTo(otherCheckFrontEnd), 1);
        List<HAACL> otherAcls = new ArrayList<HAACL>();
        otherCheckFrontEnd = new HAHealthCheckFrontEnd(80, otherAcls, conditions);

        assertEquals(healthCheckFrontEnd.compareTo(otherCheckFrontEnd), 1);

    }

}
