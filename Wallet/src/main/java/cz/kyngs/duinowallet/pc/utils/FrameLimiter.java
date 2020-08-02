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

package cz.kyngs.duinowallet.pc.utils;

/**
 * This is a FrameLimiting class. Create an instance of it and then call
 * the limit method on each frame. It will automatically sleep during
 * this in order to try to keep the frame rate close to the rate given
 * in it's constructor.
 *
 * @author Joseph Lenton - JosephLenton@StudioFortress.com
 */
public class FrameLimiter
{
    // the number of milliseconds each frame should take
    private final long frameTime;

    // the timestamp for the last time the action method was called
    private long lastFrameTime;

    /**
     * @param frameRate A positive value stating the maximum frameRate, or 0 to run as fast as possible.
     */
    public FrameLimiter(final int frameRate)
    {
        if (frameRate < 0) {
            throw new IllegalArgumentException("The frameRate must be a positive number or 0 to disable it, given: " + frameRate);
        }

        if (frameRate == 0) {
            this.frameTime = 0;
        } else {
            this.frameTime = Math.round(1000000000.0 / (double) frameRate);
        }
    }

    public void limit()
    {
        if (frameTime > 0) {
            /* sleep for the rest of the frame */
            long time = frameTime - (System.nanoTime() - lastFrameTime);
            if (time > 0) {
                // sleep properly
                try {
                    long threadWait = time / 1000000L;
                    if (threadWait > 1) {
                        Thread.sleep(threadWait);
                    }
                    // or spin for the whole time
                } catch (InterruptedException e) {
                    // the time now minus how long the frame has left to wait for
                    long spinEndTime = System.nanoTime() + (frameTime - (System.nanoTime() - lastFrameTime));
                    while (spinEndTime > System.nanoTime()) {
                        // spin! (do nothing)
                    }
                }
            }

            lastFrameTime = System.nanoTime();
        }
    }
}
