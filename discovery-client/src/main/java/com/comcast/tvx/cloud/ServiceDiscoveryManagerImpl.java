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
import java.util.HashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;

/**
 * Implementation that performs cache maintenance.
 */
public class ServiceDiscoveryManagerImpl implements ServiceDiscoveryManager {

    protected HashMap<String, ServiceDiscovery<MetaData>> oldCache = new HashMap<String, ServiceDiscovery<MetaData>>();
    protected HashMap<String, ServiceDiscovery<MetaData>> newCache = new HashMap<String, ServiceDiscovery<MetaData>>();
    private CuratorFramework curatorFramework;

    public ServiceDiscoveryManagerImpl(CuratorFramework curatorFramework) {
        super();
        this.curatorFramework = curatorFramework;
    }

    /* (non-Javadoc)
     * @see com.comcast.tvx.cloud.ServiceDiscoveryManager#prune()
     */
    public void prune() throws IOException {
        /*
         * Clean up a bit... close things that are not referenced to plug connection
         * leaks, and set cache up
         */
        for (String key : oldCache.keySet()) {
            oldCache.get(key).close();
        }
        oldCache.clear();
        oldCache.putAll(newCache);
        newCache.clear();
    }

    /* (non-Javadoc)
     * @see com.comcast.tvx.cloud.ServiceDiscoveryManager#getDiscovery(java.lang.String)
     */
    public ServiceDiscovery<MetaData> getDiscovery(String directory) throws Exception {

        ServiceDiscovery<MetaData> discovery = oldCache.get(directory);
        /*
         * Not found in old cache
         */
        if (discovery == null) {
            // If we don't already have an instance...
            if (newCache.get(directory) == null) {
                newCache.put(directory, ServiceUtil.getDiscovery(directory, curatorFramework));
            }
        } else {
            oldCache.remove(directory);
            newCache.put(directory, discovery);
        }
        return newCache.get(directory);
    }
}
