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

/**
 * This bean is a structured representation of a service that would be
 * registered with the RegistrationClient.
 * 
 */
public class Service {

    private String region = "*";
    private String zone = "*";
    private String flavor = "*";
    private String name = "*";

    public String getRegion() {
        return region;
    }

    public Service setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getZone() {
        return zone;
    }

    public Service setZone(String zone) {
        this.zone = zone;
        return this;
    }

    public String getFlavor() {
        return flavor;
    }

    public Service setFlavor(String flavor) {
        this.flavor = flavor;
        return this;
    }

    public String getName() {
        return name;
    }

    public Service setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer()
            .append(zone).append("/")
            .append(region).append("/")
            .append(flavor).append("/")
            .append(name);
        return buff.toString();
    }

}
