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

package cz.kyngs.duinotools.wallet.auth;

import cz.kyngs.duinotools.wallet.Wallet;
import cz.kyngs.duinotools.wallet.utils.EntryImpl;
import cz.kyngs.duinotools.wallet.utils.thread.ThreadUtil;

import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * Main authenticating logic with it's own thread.
 *
 * @author kyngs
 * @see cz.kyngs.duinotools.wallet.utils.thread.ThreadUtil
 */
public class AuthCore extends ThreadUtil {

    private final Wallet wallet;

    /**
     * @param wallet Main class
     */
    public AuthCore(Wallet wallet) {
        super("Authentication");
        this.wallet = wallet;
    }

    /**
     * @param username Username
     * @param password Password
     * @param consumer Consumer called after login ended.
     */
    public void authorize(String username, String password, Consumer<Entry<LoginResult, String>> consumer) {
        scheduleTask(() -> {
            int timeout = 0;
            while (timeout < 5) {
                synchronized (wallet.getNetwork()) {
                    LoginResult loginResult;
                    String reason = null;
                    try {
                        if (username.isEmpty() || password.isEmpty()) {
                            consumer.accept(new EntryImpl<>(LoginResult.NO, "Empty username or password"));
                            return;
                        }
                        wallet.getNetwork().write(String.format("LOGI,%s,%s", username, password));
                        String result = wallet.getNetwork().read(2);
                        loginResult = LoginResult.valueOf(result);
                        switch (loginResult) {
                            case NO: {
                                String input = wallet.getNetwork().readLine();
                                reason = input == null ? "Undefined" : input.substring(1);
                                if (reason.contentEquals("Undefined")) {
                                    continue;
                                }
                                break;
                            }
                            case OK: {
                                synchronized (wallet.getAuthenticationLock()) {
                                    wallet.getAuthenticationLock().notifyAll();
                                }
                            }

                        }
                    } catch (Exception e) {
                        timeout++;
                        continue;
                    }
                    consumer.accept(new EntryImpl<>(loginResult, reason));
                    break;
                }
            }
        });

    }


}
