/*
 * MIT License
 *
 * Copyright (c) 2020 kyngs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cz.kyngs.duinotools.miner;

import cz.kyngs.duinotools.miner.core.MinerThreadGroup;
import cz.kyngs.duinotools.miner.logging.LogManager;
import cz.kyngs.duinotools.miner.logging.Logger;
import cz.kyngs.duinotools.miner.network.Network;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.util.Scanner;

public class Miner {

    public static final Logger LOGGER;

    static {
        LOGGER = new LogManager().createLogger(true);
    }

    private boolean keepReconnecting;
    private Configuration configuration;
    private Network network;
    private MinerThreadGroup minerThreadGroup;

    public Miner(String[] args) {
        keepReconnecting = true;
        parseArgs(args);
        try {
            network = new Network(this);
        } catch (IOException e) {
            LOGGER.error(String.format("CRITICAL ERRROR! %s", e.getMessage()));
            System.exit(1);
        }
        minerThreadGroup = new MinerThreadGroup(configuration.getThreadCount(), this);
    }

    public static void main(String[] args) {
        new Miner(args);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void parseArgs(String[] args) {
        OptionParser optionParser = new OptionParser();

        OptionSpec<String> usernameSpec = optionParser.accepts("username").withRequiredArg();
        OptionSpec<String> passwordSpec = optionParser.accepts("password").withRequiredArg();
        OptionSpec<Integer> threadCountSpec = optionParser.accepts("threadCount").withRequiredArg().ofType(Integer.class);

        OptionSet optionSet = optionParser.parse(args);

        String username = optionSet.valueOf(usernameSpec);
        String password = optionSet.valueOf(passwordSpec);
        Integer threadCount = optionSet.valueOf(threadCountSpec);

        if (username == null) {
            System.out.println("Enter username: ");
            username = new Scanner(System.in).nextLine();
        }

        if (password == null) {
            System.out.println("Enter password: ");
            password = new Scanner(System.in).nextLine();
        }

        if (threadCount == null) {
            System.out.println("Enter thread count: ");
            threadCount = new Scanner(System.in).nextInt();
        }

        configuration = new Configuration(username, password, threadCount);

    }

    public boolean keepReconnecting() {
        return keepReconnecting;
    }

    public void reconnect(Configuration configuration) {

    }

    public Network getNetwork() {
        return network;
    }
}
