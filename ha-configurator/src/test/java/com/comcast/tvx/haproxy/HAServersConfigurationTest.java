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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class HAServersConfigurationTest {

    private static Logger logger = LoggerFactory.getLogger(HAServersConfigurationTest.class);

    @Test
    public void testEquals() {
        String healthCheck = "/whatsnew/healthCheck";
        Integer xrePort = 81;
        List<HABackendServer> servers = new ArrayList<HABackendServer>();

        servers.add(new HABackendServer("127.0.0.1", xrePort, true, "xre"));
        HABackend backend = new HABackend(xrePort, healthCheck, servers, null);

        List<HAACL> acls = new ArrayList<HAACL>();
        acls.add(new HAACL(xrePort, "healthCheck"));

        List<HACondition> conditions = new ArrayList<HACondition>();

        HAHealthCheckBackEnd checkBackEnd = new HAHealthCheckBackEnd(xrePort, "/healthCheck", servers);
        conditions.add(new HACondition(checkBackEnd.getName(), HACondition.makeIsCondition(xrePort.toString())));

        HAHealthCheckFrontEnd healthCheckFrontEnd = new HAHealthCheckFrontEnd(80, acls, conditions);

        HAFrontEnd haFrontEnd = new HAFrontEnd(xrePort, backend);
        List<HAConfigurationSection> sections = new ArrayList<HAConfigurationSection>();
        sections.add(healthCheckFrontEnd);
        sections.add(backend);
        sections.add(checkBackEnd);
        sections.add(haFrontEnd);

        List<HAConfigurationSection> othersections = new ArrayList<HAConfigurationSection>();
        othersections.add(backend);
        othersections.add(checkBackEnd);

        HAServersConfiguration configuration = new HAServersConfiguration(sections);

        HAServersConfiguration other = new HAServersConfiguration(othersections);

        assertFalse(configuration.equals(other));
        assertFalse(configuration.equals(other));

        other = new HAServersConfiguration(sections);

        assertTrue(other.equals(configuration));
        assertTrue(other.equals(configuration));

        assertTrue(configuration.equals(configuration));
        assertTrue(configuration.equals(configuration));

        logger.debug(configuration.render());
    }
}
