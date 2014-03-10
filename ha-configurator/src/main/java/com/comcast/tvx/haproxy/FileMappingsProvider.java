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
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapping provider that reads a configuration file on disk.
 */
class FileMappingsProvider extends AbstractMappingsProvider {

    private static Logger logger = LoggerFactory.getLogger(FileMappingsProvider.class);

    String filename = null;

    /**
     * @param filename Path to the configuration file on disk.
     */
    FileMappingsProvider(String filename) {
        this.filename = filename;
    }

    @Override
    public Map<Integer, String> getMappings() {
        logger.debug("Processing: ");
        try {
            return parseMappings(acquire());
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BufferedReader acquire() throws IOException {
        return new BufferedReader(new FileReader(filename));
    }

}
