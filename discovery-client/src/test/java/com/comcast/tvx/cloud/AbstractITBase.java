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

import org.apache.curator.framework.CuratorFramework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class AbstractITBase {

    /** The zk connection string. */
    String zkConnectionString = System.getProperty("zookeeper.host") + ":" + System.getProperty("zookeeper.port");

    /** The logger. */
    Logger log = LoggerFactory.getLogger(this.getClass());

    /** The curator framework. */
    CuratorFramework curatorFramework;

    String basePath = "/cvs/" + getClass().getName();

    /**
     * Gets the curator.
     *
     * @return  the curator
     */
    CuratorFramework getCurator() {
        log.debug("ZK connection string: " + zkConnectionString);

        return CuratorClient.getCuratorFramework(zkConnectionString);
    }

}
