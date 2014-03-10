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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation to encapsulate common code.
 */
public abstract class AbstractMappingsProvider implements MappingsProvider {

    private static Logger logger = LoggerFactory.getLogger(AbstractMappingsProvider.class);

    protected Map<Integer, String> parseMappings(BufferedReader input) throws NumberFormatException, IOException {
        Map<Integer, String> mappings = new HashMap<Integer, String>();
        String line = null;

        while ((line = input.readLine()) != null) {
            line = cleanLine(line);

            if (line != null && line.length() > 0) {
                String[] parts = line.split(":", 2);
                logger.info(parts[0] + ":" + parts[1]);
                if ((parts[1] == null) || (parts[1]).trim().length() == 0) {
                    throw new IllegalArgumentException(line + " is missing path definition, cannot continue");
                }
                mappings.put(Integer.parseInt(parts[0]), parts[1]);
            }
        }

        return mappings;
    }

    protected static String cleanLine(String input) {

        if (input == null) {
            return null;
        }

        // Strip spaces
        String ret = input.trim();

        if (input.contains("#")) {
            // Strip trailing comments
            ret = Pattern.compile("\\s*?#.*$").matcher(ret).replaceAll("");
        }

        // Is anything left?
        return ret.length() > 0 ? ret : null;
    }

    abstract public BufferedReader acquire() throws IOException;

}
