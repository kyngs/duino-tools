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

package cz.kyngs.duinotools.wallet.network;

import cz.kyngs.duinotools.wallet.Wallet;
import cz.kyngs.duinotools.wallet.utils.EntryImpl;
import cz.kyngs.duinotools.wallet.utils.FrameLimiter;
import cz.kyngs.duinotools.wallet.utils.concurrent.ConcurrentSet;

import java.util.Map;
import java.util.Set;

/**
 * @author kyngs
 * @see java.lang.Runnable
 * Thread that checks if connection is alive, if not tries to reconnect. This needs to be done because TCP sockets are silly.
 */
public class AliveConnectionHandler implements Runnable {

    private final Wallet wallet;
    private final Set<Map.Entry<Thread, Integer>> timeout;

    /**
     * Only constructor.
     * @param wallet Main class.
     */
    public AliveConnectionHandler(Wallet wallet) {
        this.wallet = wallet;
        timeout = new ConcurrentSet<>();
        new Thread(this, "Connection Watcher").start();
    }

    /**
     * Start
     */
    public void balanceRequestStart() {
        timeout.add(new EntryImpl<>(Thread.currentThread(), 0));
    }

    /**
     * End
     */
    public void balanceRequestStop() {
        timeout.removeIf(entry -> entry.getKey() == Thread.currentThread());
    }

    /**
     * Main logic.
     * @see FrameLimiter
     */
    @Override
    public void run() {
        FrameLimiter frameLimiter = new FrameLimiter(20);
        while (wallet.keepReconnecting()) {
            frameLimiter.limit();
            for (Map.Entry<Thread, Integer> entry : timeout) {
                if (entry.getValue() >= 60) {
                    wallet.reconnect(wallet.getConfiguration());
                    timeout.clear();
                    break;
                }
                entry.setValue(entry.getValue() + 1);
            }
        }
    }
}
