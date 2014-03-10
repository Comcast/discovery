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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.comcast.tvx.cloud.DiscoveryClient;
import com.comcast.tvx.cloud.MetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ZkEventHandlerTest {

    Logger logger = LoggerFactory.getLogger(ZkEventHandlerTest.class);

    @Test
    public void testConstructRules() throws IOException {
        HashMap<String, MetaData> services = new HashMap<String, MetaData>();

        services.put("/services/1/2/http/127.0.0.1:81",
                     new MetaData(UUID.randomUUID(), "127.0.0.1", 81, "http"));
        services.put("/services/1/2/http/127.0.0.2:81",
                     new MetaData(UUID.randomUUID(), "127.0.0.2", 81, "http"));
        services.put("/services/11/22/http/127.0.0.3:81",
                     new MetaData(UUID.randomUUID(), "127.0.0.3", 81, "http"));

        services.put("/services/1/2/https/127.0.0.1:444",
                     new MetaData(UUID.randomUUID(), "127.0.0.1", 444, "https"));
        services.put("/services/1/2/https/127.0.0.2:444",
                     new MetaData(UUID.randomUUID(), "127.0.0.2", 444, "https"));

        services.put("/services/1/2/xre/127.0.0.1:10004",
                     new MetaData(UUID.randomUUID(), "127.0.0.1", 10004, "xre"));
        services.put("/services/1/2/xre/127.0.0.2:10004",
                     new MetaData(UUID.randomUUID(), "127.0.0.2", 10004, "xre"));

        final Map<Integer, String> mappings = new HashMap<Integer, String>();
        mappings.put(Integer.valueOf(81), "/services/1/2/http/.*:/healthcheck");
        mappings.put(Integer.valueOf(444), "/services/.*/https/.*:/checkHealth");
        mappings.put(Integer.valueOf(10004), "/services/.*/xre/.*");

        DiscoveryClient client = mock(DiscoveryClient.class);
        HAProxyService haproxy = mock(HAProxyServiceController.class);
        when(haproxy.reload()).thenReturn(0);
        MappingsProvider mockProvider = mock(MappingsProvider.class);
        when(mockProvider.getMappings()).thenReturn(mappings);
        ZkEventHandler zkEventHandler = new ZkEventHandler(client, mockProvider, "target/haproxy.cfg", haproxy);

        HAServersConfiguration rules = zkEventHandler.constructRules(services, mappings);

        logger.debug(rules.toString());
        List<HAConfigurationSection> sections = rules.getSections();
        // There should be 9 sections.  3 backends, 3 frontends, 2 healthcheck backends, 1 healthcheck frontend
        assertEquals(sections.size(), 9);

        assertTrue(sections.get(0) instanceof HABackend, "Expected backend section not found.");
        assertTrue(sections.get(1) instanceof HAFrontEnd, "Expected frontend section not found.");
        assertTrue(sections.get(2) instanceof HAHealthCheckBackEnd, "Expected healthcheck section not found.");
        assertTrue(sections.get(3) instanceof HABackend, "Expected backend section not found.");
        assertTrue(sections.get(4) instanceof HAFrontEnd, "Expected frontend section not found.");
        assertTrue(sections.get(5) instanceof HAHealthCheckBackEnd, "Expected healthcheck section not found.");
        assertTrue(sections.get(6) instanceof HABackend, "Expected backend section not found.");
        assertTrue(sections.get(7) instanceof HAFrontEnd, "Expected frontend section not found.");
        assertTrue(sections.get(8) instanceof HAHealthCheckFrontEnd, "Expected healthcheck section not found.");
    }

}
