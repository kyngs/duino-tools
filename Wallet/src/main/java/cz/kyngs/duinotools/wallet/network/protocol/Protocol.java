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

package cz.kyngs.duinotools.wallet.network.protocol;

import cz.kyngs.duinotools.wallet.network.Network;
import cz.kyngs.duinotools.wallet.network.protocol.throwables.ProtocolException;
import cz.kyngs.duinotools.wallet.network.protocol.throwables.ProtocolExecutionException;

import java.io.IOException;

/**
 * Protocol storing some actions.
 *
 * @author kyngs
 */
public class Protocol {

    private final Network network;
    private double lastBalance;

    /**
     * @param network System network.
     */
    public Protocol(Network network) {
        this.network = network;
    }

    /**
     * Protocol for retrieving balance.
     * @return balance
     * @throws IOException if I/O error occurs.
     */
    public double getBalance() throws IOException {
        network.getAliveConnectionHandler().balanceRequestStart();
        network.write("BALA");
        double balance = lastBalance;
        try {
            balance = Double.parseDouble(network.read(1024));
            lastBalance = balance;
        } catch (NumberFormatException | IOException ignored) {
        }
        network.getAliveConnectionHandler().balanceRequestStop();
        return balance;
    }

    public void sendDuco(double amount, String recipient) throws ProtocolException, IllegalArgumentException, IOException {
        if (amount <= 0) throw new IllegalArgumentException("Amount is negative or zero.");
        network.write(String.format("SEND,-,%s,%s", recipient, amount));
        String feedback = network.read(128);
        if (!(feedback.contentEquals("Successfully transfered funds!") || feedback.contentEquals("Successfully transferred funds!"))) {
            throw new ProtocolExecutionException(feedback);
        }
    }

    public void donate(double amount) throws IOException, ProtocolException, IllegalArgumentException {
        sendDuco(amount, "kyngs");
    }

}
