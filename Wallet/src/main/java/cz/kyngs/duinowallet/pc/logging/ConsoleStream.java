package cz.kyngs.duinowallet.pc.logging;

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleStream extends OutputStream {

    private final Level level;
    private final boolean errorStream;
    private final Logger logger;
    private StringBuilder line;


    public ConsoleStream(Level level, boolean errorStream, Logger logger) {
        this.level = level;
        this.errorStream = errorStream;
        this.logger = logger;
        line = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {

        if (b == '\n') {

            printAndClear();
            return;
        }

        line.append((char) b);
    }

    private void printAndClear() {
        logger.write(line.toString(), level, getReflectionLevel());
        line = new StringBuilder();
    }

    private int getReflectionLevel() {
        int reflectionLevel = 11;
        return errorStream ? -1 : reflectionLevel;
    }

}
