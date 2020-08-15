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

package cz.kyngs.duinotools.poolminer;

import cz.kyngs.duinotools.poolminer.network.NetworkPool;
import cz.kyngs.duinotools.poolminer.utils.EntryImpl;
import cz.kyngs.duinotools.poolminer.utils.NetUtil;
import cz.kyngs.logger.LogManager;
import cz.kyngs.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class PoolMiner {

    public static final Logger LOGGER;

    static {
        LOGGER = new LogManager()
                .createLogger(true);
    }

    private final MinerThread[] minerThreads;

    public PoolMiner(String[] args) {
        String username = getInput("Enter your username: ", args, 0);
        int threadCount = Integer.parseInt(getInput("Enter thread count: ", args, 1));

        if (username.contentEquals("revox")) {
            LOGGER.error("You cannot mine for revox, he thinks that java is same as javascript.");
            System.exit(1);
        }

        LOGGER.info("Fetching server IP");

        Map.Entry<String, Integer> IP = getServerIP();

        LOGGER.info("IP fetched!");

        try {
            createAndExitInformationSocket(IP.getKey(), IP.getValue(), username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("Starting network pool!");
        LOGGER.info(String.format("Estimated network pool startup time: %d seconds!", threadCount * 16));
        Instant start = Instant.now();


        NetworkPool[] networks = new NetworkPool[threadCount];
        for (int i = 0; i < networks.length; i++) {
            networks[i] = new NetworkPool(username, IP.getKey(), IP.getValue(), 8);
        }


        LOGGER.info(String.format("Network pool started in %s seconds!", Duration.between(start, Instant.now()).getSeconds()));

        LOGGER.info("Starting miners!");

        minerThreads = new MinerThread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            minerThreads[i] = new MinerThread(networks[i]);
        }

        LOGGER.info("Miners started!");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                int hashRate = 0;
                for (MinerThread minerThread : minerThreads) {
                    hashRate += minerThread.getHashRate();
                    minerThread.resetHashRate();
                }
                LOGGER.info(String.valueOf(hashRate));
            }
        }, 1000, 1000);

    }

    private static Map.Entry<String, Integer> getServerIP() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/revoxhere/duino-coin/gh-pages/serverip.txt").openStream()));
            String IP = reader.readLine();
            int port = Integer.parseInt(reader.readLine());
            reader.close();
            return new EntryImpl<>(IP, port);
        } catch (IOException ignored) {
        }
        return new EntryImpl<>("", 0);
    }

    public static void main(String[] args) {
        new PoolMiner(args);
    }

    private void createAndExitInformationSocket(String hostname, int port, String username) throws IOException {
        Socket socket = new Socket(hostname, port);
        LOGGER.info(String.format("Server version: %s", NetUtil.read(socket.getInputStream(), 3)));
        socket.getOutputStream().write(String.format("JOB,%s", username).getBytes());
        NetUtil.read(socket.getInputStream(), 1024);
        socket.getOutputStream().write("0".getBytes());
        if (NetUtil.read(socket.getInputStream(), 1024).equals("INVU")) {
            LOGGER.error("Invalid username!");
            System.exit(1);
        }
        socket.close();
    }

    private String getInput(String inMessage, String[] args, int index) {
        if (args.length <= index) {
            LOGGER.info(inMessage);
            return new Scanner(System.in).nextLine();
        }
        return args[index];
    }

}
