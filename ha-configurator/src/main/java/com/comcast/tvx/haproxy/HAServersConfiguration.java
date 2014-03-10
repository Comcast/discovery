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

import java.util.List;

public class HAServersConfiguration extends HARenderer {

    private List<HAConfigurationSection> sections;

    public HAServersConfiguration(List<HAConfigurationSection> sections) {
        this.sections = sections;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (HAConfigurationSection section : sections) {
            builder.append(wrap(section.render()));
        }
        return builder.toString();
    }

    @Override
    public int compareTo(HAConfigurationSection o) {
        if (!(o instanceof HAServersConfiguration))
            return 1;
        HAServersConfiguration that = (HAServersConfiguration) o;

        if (!that.sections.containsAll(this.sections))
            return 1;
        if (!this.sections.containsAll(that.sections))
            return 1;

        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof HAServersConfiguration))
            return false;

        HAServersConfiguration that = (HAServersConfiguration) other;

        if (this == that)
            return true;

        /*
         * simple, quick dirty.. compare outputs
         */
        if (this.render().equalsIgnoreCase(that.render()))
            return true;
        return false;
    }

    protected List<HAConfigurationSection> getSections() {
        return this.sections;
    }
}
