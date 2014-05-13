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

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import junit.framework.Assert;

/**
 * Test class.
 */
public class DiscoveryClientTest {

    private String basePath;
    private Map<String, MetaData> instances = new HashMap<String, MetaData>();

    @BeforeClass
    public void setup() {
        basePath = System.getProperty("test.resources", "src/test/resources");
    }

    @BeforeTest
    public void reset() {
        instances.clear();
    }

    @Test
    public void testFindInstances() {
        DiscoveryClient client =
            new TestableDiscoveryClient(null, "/some/slashes/", "///xx/x") {

                @Override
                protected void processPath(Map<String, MetaData> instances, String currentRoot, String[] segments) {
                    Assert.assertFalse(currentRoot.endsWith("/"));

                    List<String> list = Arrays.asList(segments);
                    Assert.assertFalse(list.contains(""));
                }
            };
        client.findInstances();
    }

    @Test
    public void testFindAllGlob() {
        DiscoveryClient client = new TestableDiscoveryClient(null)
            .usingBasePath(basePath).withCriteria("/a/**").withCriteria("/b/**");
        instances = client.findInstances();
        Assert.assertEquals(10, instances.size());
    }

    @Test
    public void testFindDirGlob() {
        DiscoveryClient client = new TestableDiscoveryClient(null, basePath, "/*/zz");
        instances = client.findInstances();
        Assert.assertEquals(2, instances.size());
    }

    @Test
    public void testFindSingleDir() {
        DiscoveryClient client = new TestableDiscoveryClient(null, basePath, "/b/bb");
        instances = client.findInstances();
        Assert.assertEquals(2, instances.size());
        Assert.assertTrue(instances.containsKey(basePath + "/" + "b/bb/bbb"));
    }

   

    class TestableDiscoveryClient extends DiscoveryClient {

        public TestableDiscoveryClient(CuratorFramework curatorFramework) {
            super(curatorFramework);
        }

        public TestableDiscoveryClient(CuratorFramework curatorFramework, String basePath, String filter) {
            super(curatorFramework);
            usingBasePath(basePath).withCriteria(filter);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void init() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void findSubNodes(Map<String, MetaData> instances, String currentRoot) {
            File start = new File(currentRoot);
            File[] list = start.listFiles();

            for (File found : list) {

                if (found.isDirectory()) {
                    findSubNodes(instances, currentRoot + "/" + found.getName());
                } else {
                    instances.put(currentRoot + "/" + found.getName(), null);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected List<String> findDirectories(String currentRoot) {
            List<String> dirs = new ArrayList<String>();
            File start = new File(currentRoot);

            for (File found : start.listFiles()) {

                if (found.isDirectory()) {
                    dirs.add(currentRoot + "/" + found.getName());
                }
            }

            return dirs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void findChildren(Map<String, MetaData> instances, String currentRoot) {
            File start = new File(currentRoot);

            for (File found : start.listFiles()) {

                if (found.isFile()) {
                    instances.put(currentRoot + "/" + found.getName(), null);
                }
            }
        }

    }
}
