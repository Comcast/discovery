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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class CuratorClient.
 */
public class CuratorClient {

    private static Logger log = LoggerFactory.getLogger(CuratorClient.class);

    /**
     * Gets the curator framework.
     *
     * @param   zooKeeperConnectionString  the zoo keeper connection string
     * @param   basePath                   the base path
     *
     * @return  the curator framework
     */
    public static CuratorFramework getCuratorFramework(String zooKeeperConnectionString) {

        CuratorFramework curatorFramework =
            CuratorFrameworkFactory.builder().connectionTimeoutMs(10 * 1000).retryPolicy(new ExponentialBackoffRetry(
                                                                                             10,
                                                                                             20)).connectString(
                                       zooKeeperConnectionString).build();

        curatorFramework.start();

        try {
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        return curatorFramework;
    }

    public static void registerForChanges(CuratorFramework curatorFramework,
                                          final RegistrationChangeHandler<MetaData> handler,
                                          String... basePaths) {

        if (curatorFramework.getState() != CuratorFrameworkState.STARTED) {
            curatorFramework.start();
        }

        try {
            curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }

        for (String basePath : basePaths) {

            log.debug("Adding watched path: " + basePath);

            try {
                new EnsurePath(basePath).ensure(curatorFramework.getZookeeperClient());
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }

        log.debug("exiting registerForChanges");
    }

}
