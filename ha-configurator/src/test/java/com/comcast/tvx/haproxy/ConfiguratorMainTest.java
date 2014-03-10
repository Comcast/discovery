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
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 */
public class ConfiguratorMainTest {

    @Test
    public void testParseFilters() throws IOException {
        String[] filters =
            new String[] {
                "/services/denver/**",
                "/services/chicago/**",
            };

        String testInput = filters[0] + "\n" + filters[1] + "\n" + "# This is a comment" + "\n";
        BufferedReader input = new BufferedReader(new StringReader(testInput));
        Assert.assertEquals(ConfiguratorMain.parseFilters(input), Arrays.asList(filters));
    }

    public void testFrontEndAdd() {
        HashMap<Integer, String> mappings = new HashMap<Integer, String>();
        mappings.put(Integer.valueOf(80), "/services/1/2/http/.*:/healthcheck");
        mappings.put(Integer.valueOf(443), "/services/.*/https/.*:/checkHealth");
        mappings.put(Integer.valueOf(10004), "/services/.*/xre/.*");
    }

    @Test
    public void testCleanLine() {
        String testout = null;
        testout = ConfiguratorMain.cleanLine("");
        Assert.assertNull(testout);
        testout = ConfiguratorMain.cleanLine("# ");
        Assert.assertNull(testout);
        testout = ConfiguratorMain.cleanLine("  # ");
        Assert.assertNull(testout);
        testout = ConfiguratorMain.cleanLine("  xxx");
        Assert.assertEquals(testout, "xxx");
        testout = ConfiguratorMain.cleanLine("xxx # blah");
        Assert.assertEquals(testout, "xxx");
        testout = ConfiguratorMain.cleanLine("xxx#blah");
        Assert.assertEquals(testout, "xxx");
    }
}
