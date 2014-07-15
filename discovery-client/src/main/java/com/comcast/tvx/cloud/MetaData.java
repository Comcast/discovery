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

import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 * The Class WorkerMetadata.
 */
@JsonRootName("Worker")
public final class MetaData {

    /** The worker id. */
    @JsonProperty
    private UUID workerId;

    /** The listen address. */
    @JsonProperty
    private String listenAddress;

    /** The listen port. */
    @JsonProperty
    private int listenPort;

    /** The service namee. */
    @JsonProperty
    private String serviceName;

    /** The parameter map. */
    @JsonProperty
    private Map<String, String> parameters;

    /**
     * Instantiates a new worker metadata.
     */
    public MetaData() {

    }

    /**
     * Instantiates a new worker metadata.
     *
     * @param  workerId       the worker id
     * @param  listenAddress  the listen address
     * @param  listenPort     the listen port
     * @param  serviceName    the service name
     */
    public MetaData(@JsonProperty
                    UUID workerId, @JsonProperty
                    String listenAddress, @JsonProperty
                    int listenPort, @JsonProperty
                    String serviceName) {
        this.workerId = workerId;
        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
        this.serviceName = serviceName;
    }

    /**
     * Gets the worker id.
     *
     * @return  the worker id
     */
    @JsonProperty
    public UUID getWorkerId() {
        return workerId;
    }

    /**
     * Gets the listen address.
     *
     * @return  the listen address
     */
    @JsonProperty
    public String getListenAddress() {
        return listenAddress;
    }

    /**
     * Gets the listen port.
     *
     * @return  the listen port
     */
    public int getListenPort() {
        return listenPort;
    }

    /**
     * Gets the service name.
     *
     * @return  the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the parameter map.
     *
     * @return  the parameter map
     */
    @JsonProperty
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameter map.
     * @param   parameters - The parameter map
     * @return  void
     */
    @JsonProperty
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
