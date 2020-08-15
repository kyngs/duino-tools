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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MinerThread implements Runnable {

    private final Thread thread;
    private final NetworkPool networkPool;
    private int hashRate;

    public MinerThread(NetworkPool networkPool) {
        this.networkPool = networkPool;
        thread = new Thread(this, "Miner");
        thread.start();
        hashRate = 0;
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

    @Override
    public void run() {
        try {
            while (true) {
                String[] job = networkPool.getJob();
                int diff = Integer.parseInt(job[2].trim());
                String correct = job[1];
                for (int i = 0; i != 100 * diff + 1; i++) {
                    hashRate++;
                    String hash = sha1(job[0] + i);
                    if (hash.contentEquals(correct)) {
                        networkPool.finishJob(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getHashRate() {
        return hashRate;
    }

    public void resetHashRate() {
        hashRate = 0;
    }

}
