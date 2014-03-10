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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.comcast.tvx.cloud.DiscoveryClient;
import com.comcast.tvx.cloud.MetaData;
import com.comcast.tvx.cloud.RegistrationChangeHandler;
import com.google.common.base.Throwables;

import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Event handler to deal with update events.  Current this implementation
 * doesn't really act on Zookeeper events due to a missing feature in the
 * library to get events for recursive nodes.
 *
 */
public class ZkEventHandler implements RegistrationChangeHandler<MetaData> {

    private static Logger logger = LoggerFactory.getLogger(ZkEventHandler.class);

    private DiscoveryClient client;
    private String outputFile;
    private HAServersConfiguration currentRules = null;
    private MappingsProvider mappingsProvider = null;
    private HAProxyService haProxyService;

    public ZkEventHandler(DiscoveryClient client, MappingsProvider mappingsProvider,
            String outputFile, HAProxyService haProxyService) {
         super();
         this.client = client;
         this.outputFile = outputFile;
         this.mappingsProvider = mappingsProvider;
         this.haProxyService = haProxyService;
    }

    public ZkEventHandler(DiscoveryClient client, MappingsProvider mappingsFileProvider,
            String outputFile) {
        this(client,mappingsFileProvider, outputFile,new HAProxyServiceController());
    }

    @Override
    public void handleChange(Collection<ServiceInstance<MetaData>> instances) {
        // TODO: This event is ignored for now.
    }

    @Override
    public void handleChange(String basePath) {
        logger.info("Received change event for basePath: " + basePath);

        Map<String, MetaData> instances = client.findInstances();

        logger.info("processing a change for basepath:  " + basePath
                + " .  This will result in a new discovery.cfg being generated");

        try {
            writeToOutput(outputFile, constructRules(instances, mappingsProvider.getMappings()));
        } catch (IOException e) {
            logger.error("An error occurred writing to outfile: " + outputFile, e);
            Throwables.propagate(e);
        }

        try {
            reloadHaProxy();
        } catch (IOException e) {
            logger.error("An error occurred reloading HAProxy. ", e);
            Throwables.propagate(e);
        }
    }

    /*
     * process...
     */
    public void process() throws IOException {
        Map<String, MetaData> instances = client.findInstances();
        HAServersConfiguration newRules = constructRules(instances, mappingsProvider.getMappings());

        if (!newRules.equals(currentRules)) {
            logger.info("New rules added, rebuilding and reloading");
            writeToOutput(outputFile, newRules);
            reloadHaProxy();
            currentRules = newRules;
            logger.info("reload and rebuild complete");
        
        } else {
            logger.info("no new rules detected, this is a noop");
        }
    }

    private void reloadHaProxy() throws IOException {
        haProxyService.reload();
    }

    /*
     * Given services and mappings, construct a valid haproxy config. This is
     * run as the result of receiving a change event from DiscoveryClient
     */
    protected HAServersConfiguration constructRules(Map<String, MetaData> services, Map<Integer, String> mappings) {
        // Need to build up unique list of service names for member definitions
        Set<String> serviceNames = new HashSet<String>();

        for (Entry<String, MetaData> e : services.entrySet()) {
            serviceNames.add(e.getValue().getServiceName());
        }

        Map<String, Integer> names2externalPorts = new TreeMap<String, Integer>();
        Map<String, String> serviceNames2HealthChecks = new TreeMap<String, String>();
        Map<String, String> serviceNames2Protocols = new TreeMap<String, String>();
        Map<Integer,String> orderedMappings = new TreeMap<Integer, String>();
        for (Entry<Integer, String> port : mappings.entrySet()){
            orderedMappings.put(port.getKey(),port.getValue());
        }

        /*
         * Build up map of serviceName -> external port
         */
        for (Entry<Integer,String> port: orderedMappings.entrySet()) {
            for (String serviceName : serviceNames) {
                // for now, be dumb..
                if (port.getValue().contains("/" + serviceName + "/")) {
                    logger.info("Adding external mapping: " + serviceName + " at port:" + port.getKey());
                    names2externalPorts.put(serviceName, port.getKey());
                    String[] mappingParts = port.getValue().split(":");

                    if (mappingParts.length >1 ) {
                        serviceNames2HealthChecks.put(serviceName,
                                                      mappingParts[1]);
                        logger.info("adding healthCheck for service: " +
                                                      serviceName + " :" +mappingParts[1]);
                    }
                    /* need to get optional protocol */
                    if (mappingParts.length == 3 ) {
                        serviceNames2Protocols.put(serviceName,
                                                      mappingParts[2]);
                        logger.info("adding protocol for service: " +
                                serviceName + " :" +mappingParts[2]);
                    }
                }
            }
        }

        /*
         * iterate over services, and extract needed stuff to construct HARules
         */
        List<HAConfigurationSection> sections = new ArrayList<HAConfigurationSection>();
        List<HAACL> acls = new ArrayList<HAACL>();
        List<HACondition> conditions = new ArrayList<HACondition>();
        
        for (Entry<String, Integer> e : names2externalPorts.entrySet()) {
            List<HABackendServer> servers = new ArrayList<HABackendServer>();
            String serviceName = e.getKey();
            int externalPort = e.getValue();

            String healthCheckUrl = serviceNames2HealthChecks.get(e.getKey());
            boolean healthCheck = ((healthCheckUrl == null) || (healthCheckUrl.trim().length() == 0)) ? false : true;

            for (Entry<String, MetaData> s : services.entrySet()) {
                MetaData backEndService = s.getValue();

                if (backEndService.getServiceName().trim().equalsIgnoreCase(serviceName)) {
                    servers.add(new HABackendServer(backEndService.getListenAddress(), backEndService.getListenPort(),
                            healthCheck, serviceNames2Protocols.get(serviceName)));
                }
            }

            HABackend backend = new HABackend(externalPort, healthCheckUrl, servers, (serviceNames2Protocols.get(e
                    .getKey()) != null) ? serviceNames2Protocols.get(e.getKey()) : null);
            sections.add(backend);
            sections.add(new HAFrontEnd(externalPort, backend));
            String externalHealthCheckUrl = "/healthCheck";
            // add healthCheck backend, but only if there is a healthCheck defined
            if (healthCheck) {
                acls.add(new HAACL(externalPort, externalHealthCheckUrl));
                conditions.add(new HACondition(HAHealthCheckBackEnd.makeName(externalPort), HACondition
                        .makeIsCondition(Integer.toString(externalPort))));
                sections.add(new HAHealthCheckBackEnd(externalPort, healthCheckUrl, servers));
            }
        }

        HAHealthCheckFrontEnd healthCheckFrontEnd = new HAHealthCheckFrontEnd(80, acls, conditions);
        sections.add(healthCheckFrontEnd);
        HAServersConfiguration configuration = new HAServersConfiguration(sections);

        return configuration;
    }

    protected void writeToOutput(String outputFile, HAServersConfiguration config) throws IOException {
        // Writing in place.  We're the only writer after all.  Right?
        File f = new File(outputFile);
        BufferedWriter output = new BufferedWriter(new FileWriter(f));
        output.write(config.render());
        output.flush();
        output.close();
    }

}
