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

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.*;

public class HABackendTest {

    @Test
    public void test() {
        List<HABackendServer> servers = new ArrayList<HABackendServer>();

        servers.add(new HABackendServer("192.168.0.1", 1111, false, "xre"));

        servers.add(new HABackendServer("192.168.0.2", 1111, false, "xre"));
        HABackend backend1 = new HABackend(1111, "/goo", servers, null);
        HABackend backend2 = new HABackend(1111, "/goo", servers, "http");
        assertFalse(backend1.compareTo(backend2) == 1);
        backend2 = new HABackend(1112, "/goo", servers, null);

        assertFalse(backend1.equals(backend2));

    }

}
