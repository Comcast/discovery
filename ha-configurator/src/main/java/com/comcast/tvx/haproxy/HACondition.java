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

/*
 *     use_backend healthCheck_18181 if is_18181
 */
public class HACondition extends HARenderer {

    private String backend;
    private String condition;

    /*
     * backend is full name of backend, condition is full name of condition
     */
    public HACondition(String backend, String condition) {
        super();
        this.backend = backend;
        this.condition = condition;
    }

    public String toString() {
        return new StringBuilder().append("use_backend ").append(backend).append(" if ").append(condition).toString();
    }

    public void validate() {
        if ((backend == null) || (backend.trim().length() == 0))
            throw new RuntimeException("backend must be specified");
        if ((condition == null) || (condition.trim().length() == 0))
            throw new RuntimeException("condition must be specified");
    }

    public static String makeIsCondition(String subject) {
        return "is_" + subject;
    }

    @Override
    public int compareTo(HAConfigurationSection o) {
        if (!(o instanceof HACondition))
            return 1;
        HACondition that = (HACondition) o;
        if (!that.toString().equalsIgnoreCase(this.toString()))
            return 1;
        return 0;
    }
}
