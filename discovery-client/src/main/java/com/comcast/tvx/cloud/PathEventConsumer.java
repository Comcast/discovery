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
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathEventConsumer<T> implements QueueConsumer<T> {

    private static Logger log = LoggerFactory.getLogger(CuratorClient.class);
    private CuratorFramework framework;
    private RegistrationChangeHandler<MetaData> handler;

    public PathEventConsumer(CuratorFramework framework, RegistrationChangeHandler<MetaData> handler) {
        super();
        this.framework = framework;
        this.handler = handler;
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        /* nothing to do here.. YET.. could trigger a full rebuild, but we
         * dont have enough context to do so (no path)
         */
    }

    /*
     * (non-Javadoc)
     * @see org.apache.curator.framework.recipes.queue.QueueConsumer#consumeMessage(java.lang.Object)
     * consume (T) - T is a string with the path of a node of interest
     */
    @Override
    public void consumeMessage(T message) throws Exception {

        if (!(message instanceof String)) {
            log.debug("got message of: " + message.getClass() + ", but I was expecting String.  Will not process:" +
                      message);

            return;
        }

        log.debug("got message to consume, yum! Message is: " + message);

        String event = message.toString();

        /*
         * Add watch to allow trigger of cal to listener
         */
        framework.getZookeeperClient().getZooKeeper().exists(event, true);

        /*
         * Add listener to handle node removal
         */
        framework.getCuratorListenable().addListener(new CuratorEventListener<MetaData>(handler, event));

        /*
         * call handler to do stuff.
         */
        handler.handleChange(event);

    }
}
