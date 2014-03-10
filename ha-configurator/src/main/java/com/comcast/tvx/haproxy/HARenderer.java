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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HARenderer implements HAConfigurationSection {

    // Shared logger for all renderers.
    protected static Logger logger = LoggerFactory.getLogger(HARenderer.class);

    protected String wrap(String item) {
        return item + " \n";
    }

    @Override
    public String render() {
        // call validate, to fail fast
        validate();
        return this.toString();
    }

    @Override
    public void validate() {
        // nothing here...
    }

    @Override
    public abstract String toString();

}
