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

package cz.kyngs.duinotools.miner.core;

import cz.kyngs.duinotools.miner.Miner;
import cz.kyngs.duinotools.miner.network.Network;
import cz.kyngs.duinotools.miner.utils.CryptoUtil;

public class MinerThread implements Runnable {

    private final Miner miner;
    private final int id;
    private MinerThreadGroup parent;
    private int hashCount;
    private int acceptedShares;
    private int refusedShares;
    private boolean run;

    public MinerThread(Miner miner, int ID, MinerThreadGroup parent) {
        this.miner = miner;
        id = ID;
        hashCount = 0;
        this.parent = parent;
        run = true;
        new Thread(parent, this, String.format("Miner Thread/%d", ID)).start();
    }

    @Override
    public void run() {
        Network network = miner.getNetwork();
        while (run) {
            try {
                network.write("JOB");
                String jobImpl = network.read(1024);
                String[] job = jobImpl.split(",");
                int difficulty = Integer.parseInt(job[2].substring(1));
                System.out.println(difficulty);
                for (int i = 0; i < difficulty * 100 + 1; i++) {
                    hashCount++;
                    String hash = CryptoUtil.hash(job[0] + i);
                    if (job[1].contentEquals(hash)) {
                        network.write(String.valueOf(i));
                        String feedback = network.read(1024);

                        if (feedback.contentEquals("GOOD")) {
                            acceptedShares++;
                        } else {
                            refusedShares++;
                        }
                        break;
                    } else {
                        System.out.println(hash + " vs " + job[1]);
                    }
                }
            } catch (Exception e) {
                Miner.LOGGER.error("Error while processing cycle: ", e);
            }
        }
    }
}
