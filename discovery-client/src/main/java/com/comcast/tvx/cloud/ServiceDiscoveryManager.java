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

import org.apache.curator.x.discovery.ServiceDiscovery;

/**
 * Manager interface to the ServiceDiscovery routines.  Abstracts house cleaning.
 */
public interface ServiceDiscoveryManager {

    /**
     * Get a ServiceDiscovery instance, retrieving from cache if in cache, or
     * creating a new instance if needed, and then caching it.
     * 
     * @param directory the directory to search
     * @return the Discovered Service
     * @throws Exception if an Exception occurs executing the remote call.
     */
    public ServiceDiscovery<MetaData> getDiscovery(String directory) throws Exception;

    /**
     * Perform any pruning or clean up as necessary.
     * @throws IOException
     */
    public void prune() throws IOException;

}
