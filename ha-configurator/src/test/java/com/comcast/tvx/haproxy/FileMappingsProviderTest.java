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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.testng.annotations.Test;

public class FileMappingsProviderTest {

    @Test
    public void testLoadMappingsFile() throws FileNotFoundException, IOException {
        FileMappingsProvider fileHandler = new FileMappingsProvider("src/test/resources/mappings.conf.good");
        Map<Integer, String> mappings = fileHandler.getMappings();
        assertTrue(mappings != null);
        assertNotNull(mappings.get(8080));
        assertNull(mappings.get(8081));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testLoadBadMappingsFilePath() throws FileNotFoundException, IOException {
        new FileMappingsProvider("src/test/resources/mappings.conf.bad.path").getMappings();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLoadBadMappingsFilePort() throws FileNotFoundException, IOException {
        new FileMappingsProvider("src/test/resources/mappings.conf.bad.port").getMappings();
    }

}
