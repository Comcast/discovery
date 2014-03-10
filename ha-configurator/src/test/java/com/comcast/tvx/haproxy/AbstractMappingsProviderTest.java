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
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AbstractMappingsProviderTest {

    AbstractMappingsProvider provider = null;

    @BeforeSuite
    public void createProvider() {
        provider = new AbstractMappingsProvider() {
            
            @Override
            public Map<Integer, String> getMappings() {
                return null;
            }
            
            @Override
            public BufferedReader acquire() throws IOException {
                return null;
            }
        };
    }

    @Test
    public void testParseMappings() throws NumberFormatException, IOException {
        List<String> configContent =
            Arrays.asList(new String[] {
                              "# This is a comment",
                              "80:/a/b/c/.*:/foo",
                              "8080:/x/[a-z]*/z/.*:/"
                          });

        StringBuilder buff = new StringBuilder();

        for (String line : configContent) {
            buff.append(line).append("\n");
        }

        String testInput = buff.toString();
        BufferedReader input = new BufferedReader(new StringReader(testInput));
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(Integer.valueOf(80), "/a/b/c/.*:/foo");
        expected.put(Integer.valueOf(8080), "/x/[a-z]*/z/.*:/");
        Assert.assertEquals(provider.parseMappings(input), expected);
    }

}
