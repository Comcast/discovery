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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Integration tests.
 */
@Test(dependsOnGroups = { "RegistrationClientIT" })
public class DiscoveryClientIT extends AbstractITBase {

    @Test
    public void testFindSubNodes() throws Exception {
        List<RegistrationClient> workers = new ArrayList<RegistrationClient>();
        workers.add(new RegistrationClient(getCurator(), basePath + "/a/b", "z", "127.0.0.1", "http:80")
                    .advertiseAvailability());
        workers.add(new RegistrationClient(getCurator(), basePath + "/a/b/c", "y", "127.0.0.1", "http:80")
                    .advertiseAvailability());
        workers.add(new RegistrationClient(getCurator(), basePath + "/a/b/c", "y", "127.0.0.2", "http:80")
                    .advertiseAvailability());
        
        DiscoveryClient discovery = new DiscoveryClient(getCurator()).usingBasePath(basePath);
        TreeMap<String, MetaData> services = new TreeMap<String, MetaData>();
        discovery.findSubNodes(services, basePath);
        assertEquals(services.size(), 3);
        assertTrue(services.containsKey(basePath + "/a/b/z/http/127.0.0.1:80"));
        assertTrue(services.containsKey(basePath + "/a/b/c/y/http/127.0.0.1:80"));
        assertTrue(services.containsKey(basePath + "/a/b/c/y/http/127.0.0.2:80"));

        for (RegistrationClient worker : workers) {
            worker.deAdvertiseAvailability();
        }
    }

    @Test
    public void testFindDirectories() throws Exception {
        List<RegistrationClient> workers = new ArrayList<RegistrationClient>();
        workers.add(new RegistrationClient(getCurator(), basePath + "/a/b", "z", "127.0.0.1", "http:80")
                    .advertiseAvailability());
        workers.add(new RegistrationClient(getCurator(), basePath + "/a/b/c", "y", "127.0.0.1", "http:80")
                    .advertiseAvailability());
        DiscoveryClient discovery = new DiscoveryClient(getCurator()).usingBasePath(basePath).withCriteria("a");
        List<String> dirs = discovery.findDirectories(basePath + "/a");
        assertEquals(dirs.size(), 1);
        assertTrue(dirs.contains(basePath + "/a/b"));

        dirs = discovery.findDirectories(basePath + "/a/b");
        assertEquals(dirs.size(), 2);
        assertTrue(dirs.contains(basePath + "/a/b/c"));
        assertTrue(dirs.contains(basePath + "/a/b/z"));

        dirs = discovery.findDirectories(basePath + "/a/b/c");
        assertEquals(dirs.size(), 1);
        assertTrue(dirs.contains(basePath + "/a/b/c/y"));

        for (RegistrationClient worker : workers) {
            worker.deAdvertiseAvailability();
        }
    }

    @Test
    public void testFindChildren() throws Exception {
        List<RegistrationClient> workers = new ArrayList<RegistrationClient>();
        workers.add(new RegistrationClient(getCurator(), basePath, "vanilla", "127.0.0.1", "http:80")
                    .advertiseAvailability());
        workers.add(new RegistrationClient(getCurator(), basePath, "vanilla", "127.0.0.2", "http:80")
                    .advertiseAvailability());
        workers.add(new RegistrationClient(getCurator(), basePath + "/x", "vanilla", "127.0.0.1", "http:80")
                    .advertiseAvailability());

        Map<String, MetaData> instances = new TreeMap<String, MetaData>();
        DiscoveryClient discovery = new DiscoveryClient(getCurator()).usingBasePath(basePath).withCriteria("vanilla");
        discovery.findChildren(instances, basePath + "/vanilla");
        assertEquals(instances.size(), 2);

        instances.clear();
        discovery.findChildren(instances, basePath + "/chocolate");
        assertEquals(instances.size(), 0);

        instances.clear();
        discovery.findChildren(instances, basePath + "/x");
        assertEquals(instances.size(), 0);

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
