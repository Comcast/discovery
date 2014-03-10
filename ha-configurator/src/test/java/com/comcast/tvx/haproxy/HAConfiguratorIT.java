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
import java.util.HashMap;
import java.util.List;

import com.comcast.tvx.cloud.Constants;
import com.comcast.tvx.cloud.CuratorClient;
import com.comcast.tvx.cloud.DiscoveryClient;
import com.comcast.tvx.cloud.RegistrationClient;
import com.comcast.tvx.cloud.ServiceDiscoveryManagerImpl;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HAConfiguratorIT {

    Logger logger = LoggerFactory.getLogger(HAConfiguratorIT.class);

    private static final String region = "region1";
    private static final String availabilityZone = "zonedOut";
    private static final String flavor = "yummy";
    private static final String ip = "127.0.0.1";
    private static final String basePath = new StringBuilder().append("/services").append("/").append(region)
            .append("/").append(availabilityZone).toString();
    final CuratorFramework curatorFramework = CuratorClient.getCuratorFramework(getConnectionString());
    private List<RegistrationClient> clients = new ArrayList<RegistrationClient>();

    private String getConnectionString() {
        return System.getProperty("zookeeper.connection", "localhost:2181");
    }

    @Test(groups = { "scaleUp" })
    public void testScaleUp() throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            String serviceSpec = "foo:100" + i;
            clients.add(new RegistrationClient(curatorFramework, basePath, flavor, ip, serviceSpec)
                    .advertiseAvailability());
        }
        Thread.sleep(3000);

    }

    @Test(dependsOnGroups = "scaleUp")
    public void testScaleDown() throws InterruptedException {
        HashMap<Integer, String> mappings = new HashMap<Integer, String>();
        mappings.put(Integer.valueOf(81), "/services/[a-zA-Z0-9]*/[a-zA-Z0-9]*/[a-zA-Z0-9]*/foo/.*:/healthcheck");
        List<String> filters = new ArrayList<String>();
        filters.add("region1/**");
        final DiscoveryClient discoClient = new DiscoveryClient(curatorFramework, Constants.DEFAULT_REGISTRATION_ROOT,
                filters, new ServiceDiscoveryManagerImpl(curatorFramework));

        String result = "";
        for (Integer key : mappings.keySet()) {
            result = key + ":" + mappings.get(key);
        }
        logger.debug(result);

        HAProxyService haproxy = mock(HAProxyServiceController.class);
        when(haproxy.reload()).thenReturn(0);
        MappingsProvider mockProvider = mock(MappingsProvider.class);
        when(mockProvider.getMappings()).thenReturn(mappings);

        ZkEventHandler zkEventHandler = new ZkEventHandler(discoClient, mockProvider,
                "target/config.cfg", haproxy);

        HAServersConfiguration rules = zkEventHandler.constructRules(discoClient.findInstances(), mappings);
        RegistrationClient client = clients.remove(0);
        client.deAdvertiseAvailability();
        Thread.sleep(3 * 1000);

        HAServersConfiguration rulesDelta = zkEventHandler.constructRules(discoClient.findInstances(), mappings);

        Assert.assertNotEquals(rules, rulesDelta);

    }

    public void testLoadMappings() {

    }

}
