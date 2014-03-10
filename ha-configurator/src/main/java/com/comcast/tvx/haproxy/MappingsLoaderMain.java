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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.comcast.tvx.cloud.CuratorClient;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate configuration files for HAProxy. Must be executed as the user that owns HAProxy
 * configuration files.
 */
public class MappingsLoaderMain {

    private static Logger logger = LoggerFactory.getLogger(MappingsLoaderMain.class);

    @Argument(alias = "z", description = "ZooKeeper connection string", required = true)
    private static String zooKeeperConnectionString = null;

    @Argument(alias = "m", description = "Path to file containing port mappings that are to be loaded to ZooKeeper", required = true)
    private static String mappingsFile = null;

    @Argument(alias = "r", description = "Mappings root to load mappings to.", required = true)
    private static String mappingsRoot = null;

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            Args.parse(MappingsLoaderMain.class, args);
        } catch (IllegalArgumentException e) {

            System.out.println(e.getMessage());
            Args.usage(MappingsLoaderMain.class);
            System.exit(1);
        }

        final CuratorFramework curatorFramework = CuratorClient.getCuratorFramework(zooKeeperConnectionString);
        try {
            loadMappings(curatorFramework);
        } catch (Exception e) {
            curatorFramework.close();
            e.printStackTrace();
            System.exit(1);
        }
        curatorFramework.close();
        System.exit(0);

    }

    /*
     * 
     * Load a mappings file into ZK
     */
    private static void loadMappings(CuratorFramework curatorFramework) throws Exception {
        /*
         * die quickly
         */
        parseMappings(validateAndConvertPath(mappingsFile));

        String fileContent = org.apache.commons.io.IOUtils.toString(new FileInputStream(new File(mappingsFile)));

        String mappingLoadPath = mappingsRoot + "/mappings.conf";
        logger.info("loading file: " + mappingsFile + " to: " + mappingLoadPath);
        logger.debug("data is: " + fileContent);
        // make sure mappingLoad path exists
        try {

            new EnsurePath(mappingLoadPath).ensure(curatorFramework.getZookeeperClient());

            curatorFramework.create().forPath(mappingLoadPath);
        } catch (NodeExistsException ne) {
            logger.debug("node existed, which is ok.. carrying on. Exception: " + ne.getMessage());
        }
        // Load data...
        Stat stat = curatorFramework.setData().forPath(mappingLoadPath, fileContent.getBytes());
        if (stat.getDataLength() == 0)
            throw new RuntimeException("no data was written, not good");

    }

    protected static List<String> readLineOrientedFile(BufferedReader input) throws IOException {
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

    protected static List<String> parseFilters(BufferedReader input) throws IOException {
        return readLineOrientedFile(input);
    }

    protected static Map<Integer, String> parseMappings(BufferedReader input) throws NumberFormatException, IOException {
        Map<Integer, String> mappings = new HashMap<Integer, String>();
        String line = null;

        while ((line = input.readLine()) != null) {
            line = cleanLine(line);

            if (line != null && line.length() > 0) {
                String[] parts = line.split(":", 2);
                logger.info(parts[0] + ":" + parts[1]);
                mappings.put(Integer.parseInt(parts[0]), parts[1]);
            }
        }

        return mappings;
    }

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
     * Construct a list of HARules which can be rendered. This can be easily
     * refactored to use a specialized parser with a richer HARule
     * implementation.
     * 
     * @param services
     * @param mappings
     * 
     * @return
     */

    /**
     * Clean up configuration file input line.
     * 
     * @param input
     * 
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
