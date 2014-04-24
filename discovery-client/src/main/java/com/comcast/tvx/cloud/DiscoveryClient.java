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

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Throwables;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client used to query the central server for registered services.
 */
public class DiscoveryClient {

    /** The curator framework. */
    private CuratorFramework curatorFramework;

    /** The base path. */
    private String basePath;

    /** The filters. */
    private List<String> filters;

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(DiscoveryClient.class);
    
    private ServiceDiscoveryManager discoveryManager ;
    
    /** The discovery cache. */
   
    /**
     * Create a new instance of the client. This is the same as<br/>
     * <code>new DiscoveryClient(curatorFramework, basePath, Arrays.asList(&quot;**&quot;));</code>
     *
     * @param curatorFramework the curator framework
     * @param basePath the base path
     */
    public DiscoveryClient(CuratorFramework curatorFramework, String basePath,ServiceDiscoveryManager discoveryManager) {
        this(curatorFramework, basePath, Arrays.asList("**"),discoveryManager);
    }

    /**
     * This method accepts a list of string expressions representing paths that the user is
     * interested in. Each element is used to construct a search path relative to the base path. Two
     * regular expressions are supported. One is the use of the wildcard character '*' in place of
     * an entire path segment. (e.g. &quot;west1/*\/my_service&quot;). The other is the the Ant
     * style glob '**', but is only supported as the last path segment in an expression to indicate
     * all sub nodes. (e.g. &quot;west1/zone1/**&quot;)
     *
     * @param curatorFramework the curator framework
     * @param basePath the base path
     * @param filters the filters
     */
    public DiscoveryClient(CuratorFramework curatorFramework, String basePath, List<String> filters,
            ServiceDiscoveryManager discoveryManager) {
        this.curatorFramework = curatorFramework;
        this.basePath = basePath;
        this.filters = filters;
        this.discoveryManager = discoveryManager;
    }

    /**
     * Find instances based on the filters used to create this object.
     *
     * @return  A sorted map of full paths to a node, along with the MetaData stored in that node.
     */
    public Map<String, MetaData> findInstances() {
        Map<String, MetaData> instances = new TreeMap<String, MetaData>();

        try {

            for (String path : filters) {
                logger.debug("checking entries with path filter: " + path);

                // We need to clean up the input for base path and filter.  Trim trailing slash on
                // base path. Leading slash on filter needs to be removed or a split() will produce
                // an empty element.
                String currentRoot = basePath.endsWith("/") ? basePath.substring(0, basePath.length() - 1) : basePath;
                String[] segments = path.split("/+");

                if (segments.length > 0 && "".equalsIgnoreCase(segments[0])) {
                    segments = Arrays.copyOfRange(segments, 1, segments.length);
                }

                processPath(instances, currentRoot, segments);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return instances;
    }

    /**
     * Initialize this client instance.
     */
    protected void init() {

        // Initialize framework.
        if (curatorFramework.getState() != CuratorFrameworkState.STARTED) {
            curatorFramework.start();
        }
    }

    /**
     * Process the path given.
     *
     * @param instances the instances
     * @param currentRoot the current root
     * @param segments the segments
     * @throws Exception the exception
     */
    protected void processPath(Map<String, MetaData> instances, String currentRoot, String[] segments)
        throws Exception {

        for (int i = 0; i < segments.length; currentRoot += "/" + segments[i++]) {

            if (segments[i].equalsIgnoreCase("**")) {
                findSubNodes(instances, currentRoot);

                break;
            } else if (segments[i].equalsIgnoreCase("*")) {
                List<String> dirs = findDirectories(currentRoot);

                for (String dir : dirs) {
                    processPath(instances, dir, Arrays.copyOfRange(segments, i + 1, segments.length));
                }

                break;
            } else if (i + 1 == segments.length) {

                // Last segment
                findChildren(instances, currentRoot += "/" + segments[i]);
            }
        }
    }

    /**
     * Find all nodes and sub-nodes and add them to the supplied map. This method recursively calls
     * itself using a depth first algorithm.
     *
     * @param instances the instances
     * @param currentRoot the current root
     * @throws Exception the exception
     */
    protected void findSubNodes(Map<String, MetaData> instances, String currentRoot) throws Exception {
        List<String> children = new ArrayList<String>();

        try {
            children = curatorFramework.getChildren().forPath(currentRoot);
        } catch (Exception e) {
            logger.info("got exception : " + e.getMessage() +
                        "  finding children, cannot continue with this traversal, returning");

            return;
        }

        for (String child : children) {
            String childPath = currentRoot + "/" + child;

            if (curatorFramework.getChildren().forPath(childPath).size() > 0) {
                findSubNodes(instances, childPath);
            } // else do nothing to add children, delegate to "findChildren" for this dir
        }

        findChildren(instances, currentRoot);
    }

    /**
     * Find and return all sub-directories in the current directory.
     *
     * @param directory the directory
     * @return  A list of full paths to discovered directories.
     * @throws Exception the exception
     */
    protected List<String> findDirectories(String directory) throws Exception {
        List<String> dirs = new ArrayList<String>();
        List<String> children = new ArrayList<String>();

        try {
            children = curatorFramework.getChildren().forPath(directory);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage() + " getting children, returning");

            return dirs;
        }

        for (String child : children) {

            // In the Discovery lib, this node is a directory only if it has great-grand-children.
            String childPath = directory + "/" + child;
            List<String> grandChildren = curatorFramework.getChildren().forPath(childPath);

            for (String grandChild : grandChildren) {
                String grandChildPath = childPath + "/" + grandChild;
                List<String> greatGrandChildren = curatorFramework.getChildren().forPath(grandChildPath);

                if (greatGrandChildren.size() > 0) {
                    dirs.add(childPath);

                    break;
                }
            }
        }

        return dirs;
    }

    /**
     * Find services that are direct children of the currentRoot. Services will be added to the
     * supplied map.
     *
     * @param instances the instances
     * @param directory the directory
     * @throws Exception the exception
     */
    protected void findChildren(Map<String, MetaData> instances, String directory) throws Exception {
        
        ServiceDiscovery<MetaData> discovery =  discoveryManager.getDiscovery( directory );
        
        for (String name :discovery.queryForNames()) {

            try {

                for (ServiceInstance<MetaData> instance : discovery.queryForInstances(name)) {
                    instances.put(directory + "/" + name + "/" + instance.getId(), instance.getPayload());
                }
            } catch (EOFException ignore) {
                // I would consider this a bug in Curator.  This Exception is thrown if a node has
                // no data.
            }
        }
        /*
         * Call discoveryManager.sync to release any ServiceDiscovery instances
         * that are referencing dead paths
         */
        discoveryManager.prune();
    }

}
