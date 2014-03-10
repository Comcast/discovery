/* 
 * ===========================================================================
 * This file is the intellectual property of Comcast Corp.  It
 * may not be copied in whole or in part without the express written
 * permission of Comcast or its designees.
 * ===========================================================================
 * Copyright (c) 2012 Comcast Corp. All rights reserved.
 * ===========================================================================
 */
package com.comcast.tvx.cloud.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * This is a builder for our subclass of the Curator implementation of ServiceDiscovery
 */
public class EasyDiscoveryBuilder<T> {

    private CuratorFramework        client;
    private String                  basePath;
    private InstanceSerializer<T>   serializer;
    private ServiceInstance<T>      thisInstance;

    /**
     * Return a new builder. The builder will be defaulted with a {@link JsonInstanceSerializer}.
     *
     * @param payloadClass the class of the payload of your service instance (you can use {@link Void}
     * if your instances don't need a payload)
     * @return new builder
     */
    public static<T> EasyDiscoveryBuilder<T>     builder(Class<T> payloadClass)
    {
        return new EasyDiscoveryBuilder<T>(payloadClass).serializer(new JsonInstanceSerializer<T>(payloadClass));
    }

    /**
     * Build a new service discovery with the currently set values
     *
     * @return new service discovery
     */
    public ServiceDiscovery<T> build() {
        return new EasyDiscoveryImpl<T>(client, basePath, serializer, thisInstance);
    }

    /**
     * Required - set the client to use
     *
     * @param client client
     * @return this
     */
    public EasyDiscoveryBuilder<T>   client(CuratorFramework client)
    {
        this.client = client;
        return this;
    }

    /**
     * Required - set the base path to store in ZK
     *
     * @param basePath base path
     * @return this
     */
    public EasyDiscoveryBuilder<T>   basePath(String basePath)
    {
        this.basePath = basePath;
        return this;
    }

    /**
     * optional - change the serializer used (the default is {@link JsonInstanceSerializer}
     *
     * @param serializer the serializer
     * @return this
     */
    public EasyDiscoveryBuilder<T>   serializer(InstanceSerializer<T> serializer)
    {
        this.serializer = serializer;
        return this;
    }

    /**
     * Optional - instance that represents the service that is running. The instance will get auto-registered
     *
     * @param thisInstance initial instance
     * @return this
     */
    public EasyDiscoveryBuilder<T>   thisInstance(ServiceInstance<T> thisInstance)
    {
        this.thisInstance = thisInstance;
        return this;
    }

    EasyDiscoveryBuilder(Class<T> payloadClass)
    {
    }

}
