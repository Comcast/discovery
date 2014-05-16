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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comcast.tvx.cloud.Constants;
import com.comcast.tvx.cloud.CuratorClient;
import com.comcast.tvx.cloud.DiscoveryClient;
import com.google.common.base.Throwables;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

/**
 * Generate configuration files for HAProxy. Must be executed as an user that can
 * write to the HAProxy configuration files.
 */
public class ConfiguratorMain {

    /** The log. */
    private static Logger logger = LoggerFactory.getLogger(ConfiguratorMain.class);

    @Argument(alias = "z", description = "ZooKeeper connection string", required = true)
    private static String zooKeeperConnectionString = null;

    @Argument(alias = "r", description = "Registration root path", required = false)
    private static String registrationRoot = Constants.DEFAULT_REGISTRATION_ROOT;

    @Argument(alias = "f", description = "File containing discovery filters", required = true)
    private static String filtersFile = null;

    @Argument(alias = "m", description = "File containing port mappings", required = false)
    private static String mappingsFile = null;

    @Argument(alias = "o", description = "Output file for the generate HAProxy configuration", required = true)
    private static String outputFile = null;

    @Argument(alias = "s", description = "Sleep interval for scanning for changes.", required = false)
    private static Integer sleepInterval = 30 * 1000;

    @Argument(alias = "x", description = "Mappings root to scan in ZK for port mappings.", required = false)
    private static String mappingsRoot = null;

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            Args.parse(ConfiguratorMain.class, args);

            if (mappingsRoot == null && mappingsFile == null) {
                throw new IllegalArgumentException(
                    "You must specify one of mappingRoot or mappingFile.");
            }
        } catch (IllegalArgumentException e) {
            Args.usage(ConfiguratorMain.class);
            System.exit(1);
        }

        // Get all IO operations that can throw Exceptions out of the way.
        final CuratorFramework curatorFramework = CuratorClient.getCuratorFramework(zooKeeperConnectionString);
        final DiscoveryClient client = initClient(curatorFramework);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                logger.info("Normal shutdown executing.");
                curatorFramework.close();
                System.exit(0);
            }
        });

        waitForChangesOrDie(curatorFramework, client);
    }

    private static void waitForChangesOrDie(CuratorFramework curatorFramework, DiscoveryClient client) {

        ZkEventHandler eventHandler = null;
        MappingsProvider mappingsProvider = null;

        /*
         * Instantiate Zookeeper mappings configuration.
         */
        if (mappingsRoot != null){
            logger.info("will monitor ZooKeeper for mappings.conf changes at : " + mappingsRoot);
            mappingsProvider = new ZkMappingsProvider(curatorFramework, mappingsRoot);
        } else {
            logger.info("creating static mappings file handler with file: " + mappingsFile);
            mappingsProvider = new FileMappingsProvider(mappingsFile);
        }

        try {
            eventHandler =
                new ZkEventHandler(client, mappingsProvider, outputFile);
        } catch (Exception e) {
            logger.error("An exception occurrent processing the outputFile: " + outputFile + " cannot continue", e);
            Throwables.propagate(e);
        }

        while (true) {

            try {
                new EnsurePath(registrationRoot).ensure(curatorFramework.getZookeeperClient());
            } catch (Exception e) {
                logger.error("Error calling createPath for: " + registrationRoot, e);
            }

            try {
                eventHandler.process();
            } catch (Exception e) {
                logger.error("An exception occurred calling the HaProxy handler. ", e);
            }

            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                logger.error("an exception occurred waiting. ", e);

                return;
            }

        }

    }

    protected static DiscoveryClient initClient(CuratorFramework curatorFramework) {
        DiscoveryClient client = null;

        try {
            client = new DiscoveryClient(curatorFramework)
                .usingBasePath(registrationRoot);
            List<String> filters = parseFilters(validateAndConvertPath(filtersFile));
            for (String filter : filters) {
                client.withCriteria(filter);
            }
        } catch (IOException e) {
            logger.error("An exception occurred processing the filtersFile: " + filtersFile + ", cannot continue", e);
            Throwables.propagate(e);
        }
        return client;
    }

    /**
     * Grab filters configuration and return a list of filters.
     * 
     * @param input BufferedReader from configuration.
     * @return A service classifier.
     * @throws IOException
     */
    protected static List<String> parseFilters(BufferedReader input) throws IOException {
        List<String> lines = new ArrayList<String>();
        String line = null;

        while ((line = input.readLine()) != null) {
            line = cleanLine(line);

            if (line != null && line.length() > 0) {
                lines.add(line);
            }
        }

        return lines;
    }

    /**
     * Executed on every wake loop so new configuration can be picked up.
     * 
     * @param filePath
     * @return
     */
    protected static BufferedReader validateAndConvertPath(String filePath) {
        File file = new File(filePath);
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        return reader;
    }

    protected static BufferedWriter validateAndConvertOutfile(String filePath) {
        File file = new File(filePath);
        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return writer;
    }

    /**
     * Clean up configuration file input line.
     * 
     * @param input
     * @return
     */
    protected static String cleanLine(String input) {

        if (input == null) {
            return null;
        }

        // Strip spaces
        String ret = input.trim();

        if (input.contains("#")) {
            // Strip trailing comments
            ret = Pattern.compile("\\s*?#.*$").matcher(ret).replaceAll("");
        }

        // Is anything left?
        return ret.length() > 0 ? ret : null;
    }
}
