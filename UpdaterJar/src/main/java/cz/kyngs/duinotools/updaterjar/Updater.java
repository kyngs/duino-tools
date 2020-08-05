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

package cz.kyngs.duinotools.updaterjar;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Updater {

    public Updater() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {


        }
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(new File("update.properties")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        File old = new File(properties.getProperty("old_file_path"));
        int timeout = 100;
        while (!old.delete() && timeout > 0) {
            try {
                //noinspection BusyWait
                Thread.sleep(50);
                timeout--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        File nev = new File(properties.getProperty("new_file_path"));
        File renamed = new File(properties.getProperty("new_file_name"));
        nev.renameTo(renamed);
        Runtime.getRuntime().exec(String.format("java -jar %s --updated", renamed.getName()));
    }

    public static void main(String[] args) {
        try {
            new Updater();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
