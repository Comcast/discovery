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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests.
 */
public class RegistrationClientTest {

    @Test
    public void testConstructPath() {
        String path = RegistrationClient.constructRegistrationPath("/com/cvs", "chocolate");
        assertEquals(path, "/com/cvs/chocolate");
    }

    @Test
    public void testSpecParser() {

        // No collaborators needed to test method
        Map<String, Integer> result = null;
        result = ServiceUtil.parseServiceSpec("http:80,https:443");
        assertEquals(result.size(), 2);
        assertTrue(result.containsKey("http"));
        assertTrue(result.containsKey("https"));
        assertEquals(result.get("http").intValue(), 80);
        assertEquals(result.get("https").intValue(), 443);
    }

}
