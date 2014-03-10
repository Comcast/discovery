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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAProxyServiceController implements HAProxyService {

    private static Logger logger = LoggerFactory.getLogger(HAProxyServiceController.class);

    @Override
    public int reload() {
        if (!new File("/etc/init.d/haproxy").exists()) {
            logger.info("HaProxy is not installed");
            throw new IllegalArgumentException("HaProxy is not installed");
        }

        CommandLine cmdLine = new CommandLine("sudo");
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);
        cmdLine.addArgument("service");
        cmdLine.addArgument("haproxy");
        cmdLine.addArgument("reload");

        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValues(new int[] { 0, 1 });

        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(psh);

        int exitValue;
        try {
            exitValue = executor.execute(cmdLine);
        } catch (ExecuteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("output from running process: " + stdout.toString());
        logger.info("Exit value was: " + exitValue);
        return exitValue;

    }

}
