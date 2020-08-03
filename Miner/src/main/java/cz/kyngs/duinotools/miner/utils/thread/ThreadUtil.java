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

package cz.kyngs.duinotools.miner.utils.thread;

import cz.kyngs.duinotools.miner.Miner;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadUtil implements Runnable {

    protected final List<Runnable> tasksToExecute;
    protected final Thread thisThread;
    private int taskID;
    private boolean running;

    public ThreadUtil(String name, @Nullable Runnable runnable) {
        running = true;
        tasksToExecute = new CopyOnWriteArrayList<>();
        Runnable run = runnable;
        if (run == null) run = this;
        thisThread = new Thread(run);
        thisThread.setName(String.format("%s Thread", name));
        thisThread.start();
        taskID = 0;
    }

    public ThreadUtil(String name) {
        this(name, null);
    }

    public void stop() {

        running = false;
        thisThread.interrupt();

    }

    @Override
    public void run() {
        AccessUtils.checkAccess(thisThread, "Someone tried to run ThreadUtil from wrong thread!");
        while (running) {

            while (tasksToExecute.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }

            for (Runnable task : tasksToExecute) {

                try {
                    task.run();
                } catch (Exception e) {

                    Miner.LOGGER.warn("Unexpected error occurred while executing task with ID " + taskID, e);
                    e.printStackTrace();

                }

                taskID++;
                tasksToExecute.remove(task);
            }

        }
    }

    public void scheduleTask(Runnable runnable) {

        tasksToExecute.add(runnable);

    }

}
