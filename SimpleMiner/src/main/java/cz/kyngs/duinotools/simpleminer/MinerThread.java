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

package cz.kyngs.duinotools.simpleminer;

import cz.kyngs.duinotools.simpleminer.utils.NetUtil;
import cz.kyngs.duinotools.simpleminer.utils.Sha1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MinerThread implements Runnable {

    private final String hostname;
    private final int port;
    private final String JOB_COMMAND;
    private final Thread thread;
    private int hashCount;
    private Socket socket;

    public MinerThread(String username, String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        JOB_COMMAND = String.format("JOB,%s", username);
        thread = new Thread(this, "Miner");
        thread.start();
    }

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public int getHashCount() {
        return hashCount;
    }

    public void resetHashCount() {
        hashCount = 0;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(hostname, port);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            NetUtil.read(in, 3);
            SimpleMiner.LOGGER.info("Mining started!");
            while (true) {
                out.write(JOB_COMMAND.getBytes());
                String jobImpl = NetUtil.read(in, 1024);
                String[] job = jobImpl.split(",");

                int diff = Integer.parseInt(job[2].trim());
                String correct = job[1];
                for (int i = 0; i != 100 * diff + 1; i++) {
                    hashCount++;
                    String hash = Sha1.hash(job[0] + i);
                    if (hash.contentEquals(correct)) {
                        out.write(String.valueOf(i).getBytes());
                        String resp = NetUtil.read(in, 1024).trim();
                        if (resp.contentEquals("GOOD")) {
                            SimpleMiner.LOGGER.info("Yay! Share accepted!");
                        } else {
                            SimpleMiner.LOGGER.warn("OOPS! Share rejected!");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        thread.interrupt();
        if (socket != null) try {
            socket.close();
        } catch (IOException e) {
            return;
        }
    }
}
