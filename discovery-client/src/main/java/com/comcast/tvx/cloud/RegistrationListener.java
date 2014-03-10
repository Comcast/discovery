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

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Event listener.
 */
public class RegistrationListener {

    private CuratorFramework framework;

    private String serviceName;

    private String listenPath;

    public RegistrationListener(CuratorFramework framework, String basePath, String flavor, String serviceName) {
        this.framework = framework;
        this.serviceName = serviceName;
        this.listenPath = basePath + "/" + flavor;
    }

    public void watch(RegistrationChangeHandler<MetaData> handler) throws Exception {
        framework.getData().watched().inBackground().forPath(listenPath);

        Listenable<CuratorListener> listenable = framework.getCuratorListenable();
        listenable.addListener(new CuratorEventListener(handler));
    }

    class CuratorEventListener implements CuratorListener {

        private RegistrationChangeHandler<MetaData> handler;

        public CuratorEventListener(RegistrationChangeHandler<MetaData> handler) {
            this.handler = handler;
        }

        @Override
        public void eventReceived(CuratorFramework curatorFramework, CuratorEvent event) throws Exception {

            switch (event.getType()) {

                case CREATE:
                case DELETE:

                    ServiceDiscovery<MetaData> discovery = ServiceUtil.getDiscovery(listenPath, curatorFramework);
                    Collection<ServiceInstance<MetaData>> found = discovery.queryForInstances(serviceName);
                    handler.handleChange(found);

                case CHILDREN:
                    break;

                case CLOSING:
                    break;

                case EXISTS:
                    break;

                case GET_ACL:
                    break;

                case GET_DATA:
                    break;

                case SET_DATA:
                    break;

                case SYNC:
                    break;

                case WATCHED:
                    break;

                default:
                    break;
            }
        }
    }
}
