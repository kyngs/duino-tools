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

package cz.kyngs.duinotools.poolminer.utils.thread;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Joins many ThreadUtils into one powerful scheduler.
 *
 * @author kyngs
 * @see ThreadUtil
 */
public class MultiThreadUtil {

    private final ThreadUtil[] threads;
    private final AtomicInteger splitter;

    /**
     * @param name     name of scheduler.
     * @param count    Count of threads.
     * @param runnable Parameter for ThreadUtil.
     * @see ThreadUtil#ThreadUtil(String, Runnable)
     * @see ThreadUtil#ThreadUtil(String)
     */
    public MultiThreadUtil(String name, int count, @Nullable Runnable runnable) {
        if (count <= 0) throw new IllegalArgumentException("There needs to be at least one thread");
        threads = new ThreadUtil[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ThreadUtil(String.format("%s#%d", name, i), runnable);
        }
        splitter = new AtomicInteger(0);
    }

    /**
     * @param name  Name of scheduler.
     * @param count Count of threads.
     */
    public MultiThreadUtil(String name, int count) {
        this(name, count, null);
    }

    /**
     * @param run Runnable to be scheduled on one of child schedulers.
     */
    public void scheduleTask(Runnable run) {
        if (splitter.intValue() >= threads.length) {
            splitter.set(0);
        }
        threads[splitter.intValue()].scheduleTask(run);
        splitter.set(1);
    }

    /**
     * Simple stopping logic
     */
    public void stop() {
        for (ThreadUtil thread : threads) {
            thread.stop();
        }
    }

}
