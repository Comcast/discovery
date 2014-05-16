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
 * This bean is a structured representation of a service that would be registered
 * with the RegistrationClient.  This is not a concrete instance of something that
 * is registered.  As such, not all fields are set to concrete values.
 * 
 */
public class ServiceClassifier {

    private String region = "*";
    private String zone = "*";
    private String group = "*";
    private String name = "*";

    public String getRegion() {
        return region;
    }

    public ServiceClassifier setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getZone() {
        return zone;
    }

    public ServiceClassifier setZone(String zone) {
        this.zone = zone;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public ServiceClassifier setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getName() {
        return name;
    }

    public ServiceClassifier setName(String name) {
        this.name = name;
        return this;
    }

    public String getRegistrationPath() {
        if (zone.equalsIgnoreCase("*") || region.equalsIgnoreCase("*") || group.equalsIgnoreCase("*")) {
            throw new IllegalArgumentException("Incomplete construction of Classifier.");
        }

        StringBuilder buff = new StringBuilder()
            .append(zone).append("/")
            .append(region).append("/")
            .append(group);
        return buff.toString();
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder(zone);
        if (!zone.equalsIgnoreCase("**")) {
            buff.append("/").append(region);
            if (!region.equalsIgnoreCase("**")) {
                buff.append("/").append(group);
                if (!group.equalsIgnoreCase("**")) {
                    buff.append("/").append(name);
                }
            }
        }

        return buff.toString();
    }

}
