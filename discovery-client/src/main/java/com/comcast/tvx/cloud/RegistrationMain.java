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

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main driver for registration producer.
 */
public class RegistrationMain {

    private static final Logger log = LoggerFactory.getLogger(RegistrationMain.class);

    @Argument(alias = "z", description = "ZooKeeper connection string", required = true)
    private static String zooKeeperConnectionString = null;

    @Argument(alias = "p", description = "Registration root path", required = false)
    private static String registrationPath = Constants.DEFAULT_REGISTRATION_ROOT;

    @Argument(alias = "i", description = "IP of service to register", required = true)
    private static String ip = null;

    @Argument(alias = "s", description = "Comma seperated list of services to register (e.g. \"http:80,https:443\")", required = true)
    private static String serviceSpec = null;

    @Argument(alias = "f", description = "Flavor of deployed application.", required = false)
    private static String flavor = "default";

    @Argument(alias = "r", description = "Region or data center", required = false)
    private static String region = "region1";

    @Argument(alias = "a", description = "Availability Zone", required = false)
    private static String availabilityZone = "zone1";

    /**
     * @param args Needs: <ul>
     *  <li> -zooKeeperConnectionString connection string (hostname:port) </li>
     *  <li> -ip to register </li>
     *  <li> -serviceSpec to register </li>
     *  <li> [-registrationRoot] Zookeeper directory root to use </li>
     *  <li> [-flavor] Flavor of software (e.g. service group) </li>
     *  <li> [-region] where services are deployed </li>
     *  <li> [-availabilityZone] zones within a region </li>
     *  </ul>
     * 
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        try {
            Args.parse(RegistrationMain.class, args);
        } catch (IllegalArgumentException e) {
            Args.usage(RegistrationMain.class);
            System.exit(1);

            return;
        }

        String basePath = new StringBuilder().append(registrationPath).append("/").append(region).append("/")
                .append(availabilityZone).toString();
        final CuratorFramework curatorFramework = CuratorClient.getCuratorFramework(zooKeeperConnectionString);
        final RegistrationClient registrationClient = new RegistrationClient(curatorFramework, basePath, flavor, ip,
                serviceSpec);

        log.info("created client, advertising");

        registrationClient.advertiseAvailability();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                log.info("Normal shutdown executing.");
                registrationClient.deAdvertiseAvailability();
                curatorFramework.close();
                System.exit(0);
            }
        });

        waitForChanges(registrationClient);
        System.exit(0);

    }

    private static void waitForChanges(RegistrationClient registrationClient) {

        while (true) {
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                /*
                 * Log and carry on
                 */
                log.error(e.getMessage(), e);
            }
            try {
                registrationClient.verifyRegistrations();
            } catch (Exception e) {
                /*
                 * Something bad did happen, but carry on
                 */
                log.error(e.getMessage(), e);
            }
        }
    }

}
