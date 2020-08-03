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

package cz.kyngs.duinotools.wallet.logging;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Console Stream used to wrap PrintStream
 *
 * @author kyngs
 * @see java.io.OutputStream
 * @see System#out
 * @see System#err
 * @see Logger
 */
public class ConsoleStream extends OutputStream {

    private final Level level;
    private final boolean errorStream;
    private final Logger logger;
    private StringBuilder line;

    /**
     * @param level       Level of logging
     * @param errorStream is System.err
     * @param logger      Logger as a backend
     */
    public ConsoleStream(Level level, boolean errorStream, Logger logger) {
        this.level = level;
        this.errorStream = errorStream;
        this.logger = logger;
        line = new StringBuilder();
    }

    /**
     * Method of OutputStream
     *
     * @param b
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {

        if (b == '\n') {

            printAndClear();
            return;
        }

        line.append((char) b);
    }

    /**
     * Method to print to console and clear StringBuilder
     *
     * @see StringBuilder
     */
    private void printAndClear() {
        logger.write(line.toString(), level, getReflectionLevel());
        line = new StringBuilder();
    }

    /**
     * @return StackTrace level
     */
    private int getReflectionLevel() {
        int reflectionLevel = 11;
        return errorStream ? -1 : reflectionLevel;
    }

}
