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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapping provider that pulls data from the Zookeeper, and thus the ability
 * to dynamically reconfigure mappings on all HAProxy instances.
 */
class ZkMappingsProvider extends AbstractMappingsProvider {

    private static Logger logger = LoggerFactory.getLogger(ZkMappingsProvider.class);

    private CuratorFramework curatorFramework;
    private String basePath;

    ZkMappingsProvider(CuratorFramework curatorFramework, String basePath) {
        this.curatorFramework = curatorFramework;
        this.basePath = basePath;
    }

    @Override
    public Map<Integer, String> getMappings() {
        try {
            String data = new String(curatorFramework.getData().forPath(basePath + "/mappings.conf"));
            logger.debug("read data from ZK: " + data);
            return parseMappings(new BufferedReader(new StringReader(data)));
        } catch (Exception e) {
            logger.error("Error reading from ZK path:" + basePath + "/mappings.conf: " + e.getMessage()) ;
            throw new RuntimeException( e );
        }
        
    }

    @Override
    public BufferedReader acquire() throws IOException {
        try {
            byte[] data = curatorFramework.getData().forPath(basePath + "/mappings.conf");
            logger.debug("read data from ZK: " + data);
            return new BufferedReader(new StringReader(new String(data)));
        } catch (Exception e) {
            logger.debug("Error reading from ZK path:" + basePath + "/mappings.conf");
            throw new IOException(e);
        }
    }

}
