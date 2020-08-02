/*
 * Copyright 2020 kyngs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cz.kyngs.duinowallet.pc.network;

import cz.kyngs.duinowallet.pc.Wallet;
import cz.kyngs.duinowallet.pc.utils.EntryImpl;
import cz.kyngs.duinowallet.pc.utils.FrameLimiter;
import cz.kyngs.duinowallet.pc.utils.concurrent.ConcurrentSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AliveConnectionHandler implements Runnable {

    private final Wallet wallet;
    private final Set<Map.Entry<Thread, Integer>> timeout;

    public AliveConnectionHandler(Wallet wallet){
        this.wallet = wallet;
        timeout = new ConcurrentSet<>();
        new Thread(this, "Connection Watcher").start();
    }

    public void balanceRequestStart(){
        timeout.add(new EntryImpl<>(Thread.currentThread(), 0));
    }

    public void balanceRequestStop(){
        timeout.removeIf(entry -> entry.getKey() == Thread.currentThread());
    }

    @Override
    public void run() {
        FrameLimiter frameLimiter = new FrameLimiter(20);
        while (wallet.keepReconnecting()){
            frameLimiter.limit();
            for (Map.Entry<Thread, Integer> entry : timeout){
                if (entry.getValue() >= 60){
                    wallet.reconnect(wallet.getConfiguration());
                    timeout.clear();
                    break;
                }
                entry.setValue(entry.getValue()+1);
            }
        }
    }
}
