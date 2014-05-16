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
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.api.CuratorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Concrete listener for our event types.
 *
 * @param <T>
 */
public class CuratorEventListener<T> implements CuratorListener {

    private static Logger log = LoggerFactory.getLogger(CuratorEventListener.class.getName());

    private RegistrationChangeHandler<T> handler;
    private String basePath;

    public CuratorEventListener(RegistrationChangeHandler<T> handler, String basePath) {
        super();
        this.handler = handler;
        this.basePath = basePath;
    }

    @Override
    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        /*
         * We care about  "WATCHED" events, which are set in @PathEventConsumer
         */
        if (!(event.getType() == CuratorEventType.WATCHED)) {
            log.debug("Not caring about event: " + event.toString());

            return;
        }

        log.debug("caring about event for path: " + event.getPath());
        log.debug(event.toString());

        /*
         * if path does not exist, this is a delete... rebuild...
         */
        if (client.checkExists().forPath(event.getPath()) == null) {
            log.debug("handling a delete event for path: " + event.getPath());
            handler.handleChange(basePath);
        }
    }
}
