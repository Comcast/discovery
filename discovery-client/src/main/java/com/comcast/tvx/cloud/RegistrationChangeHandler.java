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

import org.apache.curator.x.discovery.ServiceInstance;

/**
 * This interface provides a callback mechanism when change events occur.
 */
public interface RegistrationChangeHandler<T> {

    /**
     * This method is call when a change event is detected. Note that method does not generate a
     * delta, and only returns a list of instances registered after the change.
     *
     * @param  instances  List of registered instances.
     */
    void handleChange(Collection<ServiceInstance<T>> instances);

    /**
     * Called when there is a change to "basePath" or one of it's children (recursive).
     *
     * @param  basePath
     */
    void handleChange(String basePath);
}
