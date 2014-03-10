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

import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicHARuleTest {

    @Test
    public void testToStringWithHealthCheck() {
        String httpchk = "/whatsnew/healthcheck.cgi";
        BasicHARule rule = new BasicHARule("http", 80, httpchk);

        rule.servers.add("127.0.0.1:80");
        rule.servers.add("127.0.0.2:80");

        String expected =
            "listen http *:80\n" +
            "  mode tcp\n" +
            "  option httpchk " + httpchk + "\n" +

            // "  option forwardfor\n" +
            "  balance roundrobin\n" +

            // "  cookie SERVERID insert indirect\n" +
            "  server server1 127.0.0.1:80 check\n" +
            "  server server2 127.0.0.2:80 check\n\n";

        Assert.assertEquals(rule.toString(), expected);
    }

    @Test
    public void testToStringWithOutHealthCheck() {

        BasicHARule rule = new BasicHARule("http", 80, null);

        rule.servers.add("127.0.0.1:80");
        rule.servers.add("127.0.0.2:80");

        String expected =
            "listen http *:80\n" +
            "  mode tcp\n" +

            // "  option forwardfor\n" +
            "  balance roundrobin\n" +

            // "  cookie SERVERID insert indirect\n" +
            "  server server1 127.0.0.1:80 check\n" +
            "  server server2 127.0.0.2:80 check\n\n";

        Assert.assertEquals(rule.toString(), expected);
    }
}
