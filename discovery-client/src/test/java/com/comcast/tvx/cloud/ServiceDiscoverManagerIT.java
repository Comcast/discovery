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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ServiceDiscoverManagerIT extends AbstractITBase {

    @Test
    public void testCardinality() throws Exception {

        ServiceDiscoveryManagerImpl serviceDiscoveryManager = new ServiceDiscoveryManagerImpl(getCurator());
        serviceDiscoveryManager.getDiscovery("/goo/bax0");
        serviceDiscoveryManager.getDiscovery("/goo/bax1");
        serviceDiscoveryManager.getDiscovery("/goo/bax2");
        serviceDiscoveryManager.getDiscovery("/goo/bax3");
        serviceDiscoveryManager.getDiscovery("/goo/bax4");
        serviceDiscoveryManager.getDiscovery("/goo/bax4");
        serviceDiscoveryManager.getDiscovery("/goo/bax5");
        serviceDiscoveryManager.getDiscovery("/goo/bax5");

        assertEquals(serviceDiscoveryManager.newCache.size(), 6);
        assertEquals(serviceDiscoveryManager.oldCache.size(), 0);
        serviceDiscoveryManager.prune();
        assertEquals(serviceDiscoveryManager.newCache.size(), 0);
        assertEquals(serviceDiscoveryManager.oldCache.size(), 6);
    }

    @Test
    public void testPathDeleted() throws Exception {

        ServiceDiscoveryManagerImpl serviceDiscoveryManager = new ServiceDiscoveryManagerImpl(getCurator());

        serviceDiscoveryManager.getDiscovery("/goo/bax0");
        serviceDiscoveryManager.getDiscovery("/goo/bax1");
        serviceDiscoveryManager.getDiscovery("/goo/bax2");
        serviceDiscoveryManager.getDiscovery("/goo/bax3");
        serviceDiscoveryManager.getDiscovery("/goo/bax4");
        serviceDiscoveryManager.getDiscovery("/goo/bax5");
        /*
         * need to delete a path to check cleanup
         */

        getCurator().delete().inBackground().forPath("/goo/bax1");
        getCurator().sync().forPath("/goo/bax1");

        /*
         * should be same size ... cache has not hit ZK yet
         */
        assertEquals(serviceDiscoveryManager.newCache.size(), 6);

        assertEquals(serviceDiscoveryManager.oldCache.size(), 0);

        serviceDiscoveryManager.prune();
        // we are asking for one less path now, cache should reflect delete
        // above
        serviceDiscoveryManager.getDiscovery("/goo/bax0");
        serviceDiscoveryManager.getDiscovery("/goo/bax2");
        serviceDiscoveryManager.getDiscovery("/goo/bax3");
        serviceDiscoveryManager.getDiscovery("/goo/bax4");
        serviceDiscoveryManager.getDiscovery("/goo/bax5");

        serviceDiscoveryManager.prune();

        assertEquals(serviceDiscoveryManager.newCache.size(), 0);
        assertEquals(serviceDiscoveryManager.oldCache.size(), 5);
    }

    @Test
    public void testReferencesShouldBeSame() throws Exception {

        ServiceDiscoveryManagerImpl serviceDiscoveryManager = new ServiceDiscoveryManagerImpl(getCurator());
        assertEquals(serviceDiscoveryManager.getDiscovery("/goo/bax0"),
                serviceDiscoveryManager.getDiscovery("/goo/bax0"));
        assertNotEquals(serviceDiscoveryManager.getDiscovery("/goo/bax0"),
                serviceDiscoveryManager.getDiscovery("/goo/bax1"));
    }
}
