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

package cz.kyngs.duinotools.miner.network;

import cz.kyngs.duinotools.miner.Configuration;
import cz.kyngs.duinotools.miner.Miner;
import cz.kyngs.duinotools.miner.auth.AuthCore;
import cz.kyngs.duinotools.miner.auth.LoginResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

public class Network {

    private BufferedReader bufferedReader;
    private Socket socket;
    private String version;
    private Miner miner;
    private AuthCore authCore;

    public AliveConnectionHandler getAliveConnectionHandler() {
        return aliveConnectionHandler;
    }

    private AliveConnectionHandler aliveConnectionHandler;

    public Network(Miner miner) throws IOException {
        this.miner = miner;

        reconnect();

        aliveConnectionHandler = new AliveConnectionHandler(miner);
    }

    public String getVersion() {
        return version;
    }

    public void write(String s) throws IOException {
        try {
            socket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            if (!(e instanceof SocketException)) {
                throw e;
            }
        }
    }

    public String readLine() throws IOException {
        try {
            return bufferedReader.readLine();
        }catch (IOException e){
            if (e instanceof SocketException){
                return "";
            }
            throw e;
        }
    }

    public String read(int length) throws IOException {
        char[] chars = new char[length];

        try {
            //noinspection ResultOfMethodCallIgnored
            bufferedReader.read(chars);
        } catch (IOException e) {
            if (e instanceof SocketException){
                return "";
            }
            throw e;
        }
        return new String(chars);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private boolean validVersion(String version) {
        try {
            Double.parseDouble(version);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public void reconnect() throws IOException {
        synchronized (this) {
            if (socket != null) socket.close();
            socket = new Socket("0.tcp.ngrok.io", 12300);

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            version = read(3);

            if (!validVersion(version)) {
                throw new IOException("Cannot contact server! Is server off?");
            }

            authCore = new AuthCore(this);

            Configuration configuration = miner.getConfiguration();
            Map.Entry<LoginResult, String> result = authCore.authorize(configuration.getUsername(), configuration.getPassword());

            if (result.getKey() == LoginResult.NO) {
                Miner.LOGGER.info(String.format("Authentication failed!: %s", result.getValue()));
                System.exit(1);
            } else {
                Miner.LOGGER.info("Logged in!");
            }
        }
    }
}
