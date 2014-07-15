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

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Throwables;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RegistrationClient is a client for performing registration on your behalf, by providing a
 * canonical view of services. This client helps you register, verify, and de-register services
 * but does not provide any type of polling capabilities. If this is required, you should provide
 * your own execution thread and periodically call {@link #verifyRegistrations()} method.
 */
public final class RegistrationClient {

    /** The Curator framework. */
    private final CuratorFramework curatorFramework;

    /** Registration base path. */
    private final String basePath;

    /** Application flavor. */
    private final String flavor;

    /** The listen address. */
    private final String listenAddress;

    /** Map of services to register. */
    private final Map<String, Integer> services;

    /** Map of discovery objects and instances since there is a one to one correlation. */
    private Map<ServiceDiscovery<MetaData>, ServiceInstance<MetaData>> discoveryMap;

    /** Maintain client state of what was called by clients of this object. */
    private AtomicBoolean active = new AtomicBoolean(false);

    /** The log. */
    private Logger log = LoggerFactory.getLogger(this.getClass());

    /** Payload parameters." */
    private Map<String, String> parameters;

    /**
     * Instantiates a new registration client.
     *
     * @param  curatorFramework  the curator framework
     * @param  basePath          Zookeeper registration path
     * @param  flavor            Application flavor of this client.
     * @param  listenAddress     Local IP address
     * @param  serviceSpec       A list of services and corresponding ports.
     * @param  parameters        A map of optional payload parameters.
     */
    public RegistrationClient(CuratorFramework curatorFramework, String basePath,
                              String flavor, String listenAddress,
                              String serviceSpec,
                              Map<String, String> parameters) {
        this(curatorFramework, basePath, flavor, listenAddress, serviceSpec);
        this.parameters = parameters;
    }

    /**
     * Instantiates a new registration client.
     *
     * @param  curatorFramework  the curator framework
     * @param  basePath          Zookeeper registration path
     * @param  flavor            Application flavor of this client.
     * @param  listenAddress     Local IP address
     * @param  serviceSpec       A list of services and corresponding ports.
     */
    public RegistrationClient(CuratorFramework curatorFramework, String basePath,
                              String flavor, String listenAddress,
                              String serviceSpec) {
        this(curatorFramework, basePath, flavor, listenAddress, ServiceUtil.parseServiceSpec(serviceSpec));
    }


    /**
     * Instantiates a new registration client.
     *
     * @param  curatorFramework  the curator framework
     * @param  basePath          Zookeeper registration path
     * @param  flavor            Application flavor of this client.
     * @param  listenAddress     Local IP address
     * @param  services          A Map of services and corresponding ports.
     */
    public RegistrationClient(CuratorFramework curatorFramework, String basePath, String flavor, String listenAddress,
                              Map<String, Integer> services) {
        this.curatorFramework = curatorFramework;
        this.basePath = basePath;
        this.flavor = flavor;
        this.listenAddress = listenAddress;
        this.services = services;
        discoveryMap = new HashMap<ServiceDiscovery<MetaData>, ServiceInstance<MetaData>>();
    }

    /**
     * Advertise availability.
     *
     * @return  the registration client
     */
    public RegistrationClient advertiseAvailability() {

        if (active.getAndSet(true)) {
            throw new IllegalStateException("This client instance is already advertising.");
        }

        try {
            if (curatorFramework.getState() != CuratorFrameworkState.STARTED) {
                curatorFramework.start();
            }

            for (Map.Entry<String, Integer> entry : services.entrySet()) {
                String serviceName = entry.getKey();
                String regPath = constructRegistrationPath(basePath, flavor);
                int port = entry.getValue().intValue();
                ServiceDiscovery<MetaData> discovery = ServiceUtil.getDiscovery(regPath, curatorFramework);
                ServiceInstance<MetaData> service = ServiceUtil.getServiceInstance(serviceName, port, listenAddress, parameters);

                /*
                 * Having >1 instance with of the same name with same listenAddress + listPort is
                 * bad. Incur some overhead to look for duplicates and explode appropriately
                 */
                Collection<ServiceInstance<MetaData>> candidates = discovery.queryForInstances(serviceName);

                for (ServiceInstance<MetaData> worker : candidates) {
                    if ((worker.getAddress().equals(service.getAddress())) && (worker.getPort() == port)) {
                        log.error("An instance of " + service + " already exists at: " +
                                  service.getAddress() + ":" + port);
                        throw new IllegalStateException("Duplicate service being registered. for service: " +
                                                        serviceName + " at: " + regPath);
                    }
                }

                log.debug("registering service: " + serviceName);
                discovery.registerService(service);
                discoveryMap.put(discovery, service);
                log.info("registered service: " + serviceName);

            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return this;
    }

    /**
     * Verify services are registered.
     *
     * @throws  Exception
     */
    public void verifyRegistrations() throws Exception {

        for (Map.Entry<ServiceDiscovery<MetaData>, ServiceInstance<MetaData>> entry : discoveryMap.entrySet()) {
            ServiceInstance<MetaData> instance = entry.getValue();
            ServiceInstance<MetaData> found = entry.getKey().queryForInstance(instance.getName(), instance.getId());

            if (found == null) {
                throw new RuntimeException("There is no instance for: " + instance.getName() + ":" + instance.getId() +
                                           " registered ");
            }

            log.debug(found.getName() + " is verified at: " + found.getAddress() + ":" + found.getPort());
        }
    }

    /**
     * De advertise availability.
     *
     * @return  the registration client
     */
    public RegistrationClient deAdvertiseAvailability() {

        active.set(false);

        for (Map.Entry<ServiceDiscovery<MetaData>, ServiceInstance<MetaData>> entry : discoveryMap.entrySet()) {
            ServiceInstance<MetaData> instance = entry.getValue();

            try {
                entry.getKey().unregisterService(instance);
            } catch (Exception e) {
                log.error("Unregistration exception: ", e);
            } finally {
                try {
                    entry.getKey().close();
                } catch (IOException ignore) {
                }
            }
        }

        return this;
    }

    /**
     * Build registration path.
     *
     * @param   basePath
     * @param   flavor
     *
     * @return
     */
    protected static String constructRegistrationPath(String basePath, String flavor) {
        StringBuilder buff = new StringBuilder().append(basePath).append("/").append(flavor);

        return buff.toString();
    }
}
