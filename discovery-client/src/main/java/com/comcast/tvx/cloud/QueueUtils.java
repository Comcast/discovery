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
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Small helper to hide some noise and provide pleasing semantics to queue users
 */
public class QueueUtils {

    private static Logger log = LoggerFactory.getLogger(QueueUtils.class);

    public static DistributedQueue<String> getQueue(CuratorFramework curatorFramework, String queueName,
                                                    PathEventConsumer<String> consumer) throws Exception {
        QueueBuilder<String> builder =
            QueueBuilder.builder(curatorFramework, consumer, new PathQueueSerializer<String>(),
                                 Constants.PATH_QUEUE);
        DistributedQueue<String> queue = builder.buildQueue();
        queue.start();
        log.info("started queue");

        return queue;
    }

    public static DistributedQueue<String> getQueue(CuratorFramework curatorFramework, String queueName)
        throws Exception {
        return getQueue(curatorFramework, queueName, null);
    }

    public static void enqueue(CuratorFramework curatorFramework, String message, String queueName) throws Exception {
        getQueue(curatorFramework, queueName).put(message);
    }

}
