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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

public class HAConditionalTest {
    @Test
    public void testMakeIsCondition() {

        assertEquals(HACondition.makeIsCondition("81"), "is_81");
        assertNotEquals(HACondition.makeIsCondition("82"), "is_81");
        assertEquals(HACondition.makeIsCondition("82"), "is_82");

    }

    @Test
    public void testRender() {

        HACondition haCondition = new HACondition("a_backend", HACondition.makeIsCondition("80"));
        String expected = "use_backend a_backend if is_80";
        assertEquals(haCondition.render(), expected);

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testBadBackend() {
        HACondition haCondition = new HACondition(null, HACondition.makeIsCondition("80"));
        String expected = "use_backend a_backend if is_80";
        assertEquals(haCondition.render(), expected);

    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testBadIs() {
        HACondition haCondition = new HACondition("back", null);
        String expected = "use_backend a_backend if is_80";
        assertEquals(haCondition.render(), expected);

    }

}
