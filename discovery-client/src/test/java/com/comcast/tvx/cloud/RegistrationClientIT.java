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

package com.comcast.tvx.cloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * The Class RegistrationClientIT.
 */
@Test(groups = "RegistrationClientIT")
public class RegistrationClientIT extends AbstractITBase {

    /**
     * Test simple registration.
     *
     * @throws  Exception  the exception
     */
    @Test
    public void testSimpleRegistration() throws Exception {
        RegistrationClient workerAdvertiser =
            new RegistrationClient(getCurator(), basePath, "vanilla", "127.0.0.1", "foo:2181").advertiseAvailability();

        ServiceDiscovery<MetaData> serviceDiscovery =
            ServiceDiscoveryBuilder.builder(MetaData.class).client(getCurator()).basePath(basePath + "/vanilla").build();

        Collection<ServiceInstance<MetaData>> services = serviceDiscovery.queryForInstances("foo");
        log.debug("All services: " + services.toString());
        assertEquals(services.size(), 1);

        for (ServiceInstance<MetaData> worker : services) {
            assertEquals(worker.getAddress(), "127.0.0.1");
            assertEquals(worker.getPort(), new Integer(2181));
            assertEquals(worker.getName(), "foo");
        }

        workerAdvertiser.deAdvertiseAvailability();

        assertTrue(ServiceDiscoveryBuilder.builder(MetaData.class).client(getCurator()).basePath(basePath).build()
                   .queryForInstances("foo").size() == 0);

    }

    /**
     * Test cluster registration of same type.
     *
     * @throws  Exception  the exception
     */
    @Test
    public void testClusterRegistrationOfSameType() throws Exception {
        List<RegistrationClient> workers = new ArrayList<RegistrationClient>();

        workers.add(new RegistrationClient(getCurator(), basePath, "x", "192.168.1.100", "guide:10004")
                    .advertiseAvailability());

        workers.add(new RegistrationClient(getCurator(), basePath, "x", "192.168.1.101", "dayview:10022")
                    .advertiseAvailability());

        workers.add(new RegistrationClient(getCurator(), basePath, "x", "192.168.1.102", "dayview:10022")
                    .advertiseAvailability());

        ServiceDiscovery<MetaData> serviceDiscovery =
            ServiceDiscoveryBuilder.builder(MetaData.class).client(getCurator()).basePath(basePath + "/x").build();

        Collection<String> names = serviceDiscovery.queryForNames();
        assertEquals(names.size(), 2);
        assertTrue(names.containsAll(Arrays.asList("guide", "dayview")));

        Collection<ServiceInstance<MetaData>> guides = serviceDiscovery.queryForInstances("guide");
        assertEquals(guides.size(), 1);

        ServiceInstance<MetaData> guide = guides.iterator().next();
        assertEquals(guide.getName(), "guide");
        assertEquals(guide.getAddress(), "192.168.1.100");
        assertEquals(guide.getPort(), new Integer(10004));

        Collection<ServiceInstance<MetaData>> dayviews = serviceDiscovery.queryForInstances("dayview");
        assertEquals(dayviews.size(), 2);

        for (RegistrationClient worker : workers) {
            worker.deAdvertiseAvailability();
        }

    }

    /**
     * Test should enforce unique name address port.
     */
    @Test(expectedExceptions = RuntimeException.class,
        expectedExceptionsMessageRegExp = "^Duplicate service being registered.*")
    public void testShouldEnforceUniqueNameAddressPort() {
        List<RegistrationClient> workers = new ArrayList<RegistrationClient>();

        workers.add(new RegistrationClient(getCurator(), basePath, "y", "192.168.1.101", "dayview:10022")
                    .advertiseAvailability());

        workers.add(new RegistrationClient(getCurator(), basePath, "y", "192.168.1.101", "dayview:10022")
                    .advertiseAvailability());

        for (RegistrationClient worker : workers) {
            worker.deAdvertiseAvailability();
        }
    }

    /**
     * Shutdown.
     */
    @AfterClass
    public void shutdown() {

        if (curatorFramework != null) {
            curatorFramework.close();
        }
    }
}
