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
