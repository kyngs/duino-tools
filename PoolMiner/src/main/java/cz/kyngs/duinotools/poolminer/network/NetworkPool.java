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

package cz.kyngs.duinotools.poolminer.network;

import cz.kyngs.duinotools.poolminer.utils.EntryImpl;
import cz.kyngs.duinotools.poolminer.utils.NetUtil;
import cz.kyngs.duinotools.poolminer.utils.thread.MultiThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetworkPool extends MultiThreadUtil {

    private final Set<Map.Entry<Socket, String[]>> free;
    private final String JOB_COMMAND;
    private Socket used;

    public NetworkPool(String username, String hostname, int port, int socketCount) {
        super("Network", socketCount);
        free = new HashSet<>();
        JOB_COMMAND = String.format("JOB,%s", username);
        for (int i = 0; i < socketCount; i++) {
            try {
                Socket socket = createSocket(hostname, port);
                String[] job = obtainJob(socket);
                free.add(new EntryImpl<>(socket, job));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Socket createSocket(String hostname, int port) throws IOException {
        Socket socket = new Socket(hostname, port);
        NetUtil.read(socket.getInputStream(), 3);
        return socket;
    }

    private String[] obtainJob(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(JOB_COMMAND.getBytes());
        String jobImpl = NetUtil.read(in, 1024);
        return jobImpl.split(",");
    }

    public String[] getJob() {
        Map.Entry<Socket, String[]> entry;

        if (free.isEmpty()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        synchronized (free) {
            entry = free.iterator().next();
            free.remove(entry);
        }

        used = entry.getKey();
        return entry.getValue();
    }

    public void finishJob(int id) {
        Socket socket = used;
        scheduleTask(() -> {
            try {
                OutputStream out = socket.getOutputStream();
                out.write(String.valueOf(id).getBytes());
                NetUtil.read(socket.getInputStream(), 1024);
                String[] job = obtainJob(socket);
                synchronized (free) {
                    free.add(new EntryImpl<>(socket, job));
                }
                synchronized (this) {
                    notifyAll();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

}
