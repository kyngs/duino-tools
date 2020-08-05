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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * System Network logic
 *
 * @author kyngs
 * @see Socket
 */
public class Network {

    private BufferedReader bufferedReader;
    private Socket socket;
    private String version;
    private Wallet wallet;
    private AliveConnectionHandler aliveConnectionHandler;

    /**
     * @param wallet Main class
     * @throws IOException if server cannot be contacted.
     */
    public Network(Wallet wallet) throws IOException {
        this.wallet = wallet;

        socket = new Socket(Wallet.SERVER_HOST, Wallet.SERVER_PORT);

        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        version = read(3);
        if (!validVersion(version)) {
            throw new IOException("Cannot contact server!\n Is server off?");
        }

        aliveConnectionHandler = new AliveConnectionHandler(wallet);

    }

    /**
     * @return Alive connection handler
     * @see AliveConnectionHandler
     */
    public AliveConnectionHandler getAliveConnectionHandler() {
        return aliveConnectionHandler;
    }

    /**
     * Checks if version is legal
     * @param version version to be tested
     * @return true if version is OK, false otherwise.
     */
    private boolean validVersion(String version) {
        try {
            Double.parseDouble(version);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * @return Server version
     * @see String
     */
    public String getVersion() {
        return version;
    }

    /**
     * Writes to output stream.
     * @param s String to be written
     * @throws IOException if I/O error occurs.
     * @see Socket#getOutputStream()
     * @see java.io.OutputStream#write(int)
     */
    public void write(String s) throws IOException {
        try {
            socket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                throw e;
            }
        }
    }

    /**
     * Used to read line.
     * @return read line.
     * @throws IOException
     * @see BufferedReader#readLine()
     */
    public String readLine() throws IOException {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            if (e instanceof SocketException) {
                return "";
            }
            throw e;
        }
    }

    /**
     * Used to read String of defined length.
     * @param length expected length.
     * @return read String,
     * @throws IOException
     */
    public String read(int length) throws IOException {
        char[] chars = new char[length];

        try {
            //noinspection ResultOfMethodCallIgnored
            bufferedReader.read(chars);
        } catch (IOException e) {
            if (e instanceof SocketException) {
                return "";
            }
            throw e;
        }
        return new String(chars);
    }

    /**
     * Closes socket
     * @see Socket#close()
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Returns socket.
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Connecting and reconnecting logic.
     * @throws IOException if I/O error occurs.
     */
    public void reconnect() throws IOException {
        synchronized (this) {
            socket.close();
            socket = new Socket(Wallet.SERVER_HOST, Wallet.SERVER_PORT);

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            version = read(3);
        }
    }
}
