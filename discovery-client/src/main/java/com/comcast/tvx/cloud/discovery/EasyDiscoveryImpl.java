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
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.ServiceDiscoveryImpl;

/**
 * 
 */
public class EasyDiscoveryImpl<T> extends ServiceDiscoveryImpl<T> {

    public EasyDiscoveryImpl(CuratorFramework client, String basePath, InstanceSerializer<T> serializer,
                             ServiceInstance<T> thisInstance) {
        super(client, basePath, serializer, thisInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

}
