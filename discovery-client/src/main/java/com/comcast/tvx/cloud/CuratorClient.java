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

import com.google.common.base.Throwables;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CuratorClient helper class.
 */
public class CuratorClient {

    private static Logger log = LoggerFactory.getLogger(CuratorClient.class);

    public static final int DEFAULT_MAX_SLEEP_MS = 60000;

    /**
     * Gets the curator framework.
     *
     * @param   zkConnectionString  the zoo keeper connection string
     * @return  the curator framework
     */
    public static CuratorFramework getCuratorFramework(String zkConnectionString) {
        return getCuratorFramework(getCuratorBuilder(zkConnectionString));
    }

    /**
     * Get a framework using a builder argument.
     * 
     * @param builder The builder to use.
     * @return the curator framework
     */
    public static CuratorFramework getCuratorFramework(CuratorFrameworkFactory.Builder builder){
        CuratorFramework curatorFramework = builder.build();
        curatorFramework.start();

        try {
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        return curatorFramework;
    }

    /**
     * Get a builder object and allow user to override specific parameters.
     * 
     * @param zkConnectionString Zookeeper connection string.
     * @return A builder.
     */
    public static CuratorFrameworkFactory.Builder getCuratorBuilder(String zkConnectionString) {
        return CuratorFrameworkFactory.builder()
                .connectionTimeoutMs(10 * 1000)
                .retryPolicy(new ExponentialBackoffRetry(10, 20, DEFAULT_MAX_SLEEP_MS))
                .connectString(zkConnectionString);
    }

    public static void registerForChanges(CuratorFramework curatorFramework,
                                          final RegistrationChangeHandler<MetaData> handler,
                                          String... basePaths) {

        CuratorZookeeperClient client = null;

        if (curatorFramework.getState() != CuratorFrameworkState.STARTED) {
            curatorFramework.start();
        }

        try {
            client = curatorFramework.getZookeeperClient();
            client.blockUntilConnectedOrTimedOut();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        for (String basePath : basePaths) {

            log.debug("Adding watched path: " + basePath);

            try {
                new EnsurePath(basePath).ensure(client);
                CuratorEventListener<MetaData> eventBridge = new CuratorEventListener<MetaData>(handler, basePath);
                curatorFramework.getCuratorListenable().addListener(eventBridge);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }

        log.debug("exiting registerForChanges");
    }

}
