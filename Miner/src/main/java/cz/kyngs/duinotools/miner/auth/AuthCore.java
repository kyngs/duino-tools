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

package cz.kyngs.duinotools.miner.auth;

import cz.kyngs.duinotools.miner.network.Network;
import cz.kyngs.duinotools.miner.utils.EntryImpl;

import java.util.Map;

public class AuthCore {

    private final Network network;

    public AuthCore(Network network) {
        this.network = network;
    }

    public Map.Entry<LoginResult, String> authorize(String username, String password) {
        int timeout = 0;
        Exception exception = null;
        while (timeout < 5) {
            synchronized (network) {
                LoginResult loginResult;
                String reason = null;
                try {
                    if (username.isEmpty() || password.isEmpty()) {
                        return new EntryImpl<>(LoginResult.NO, "Empty username or password");
                    }
                    network.write(String.format("LOGI,%s,%s", username, password));
                    String result = network.read(2);
                    loginResult = LoginResult.valueOf(result);
                    switch (loginResult) {
                        case NO: {
                            String input = network.readLine();
                            reason = input == null ? "Undefined" : input.substring(1);
                            break;
                        }
                        case OK: {
                        }
                    }
                } catch (Exception e) {
                    timeout++;
                    exception = e;
                    ;
                    continue;
                }
                return new EntryImpl<>(loginResult, reason);
            }
        }
        ;
        return new EntryImpl<>(LoginResult.NO, "An Exception occurred while authenticating! Server is probably unreachable!");
    }

}
