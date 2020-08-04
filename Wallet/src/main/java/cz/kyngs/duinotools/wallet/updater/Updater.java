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

package cz.kyngs.duinotools.wallet.updater;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.kyngs.duinotools.wallet.Wallet;
import cz.kyngs.duinotools.wallet.gui.screens.GuiWait;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class Updater {

    private static final Gson GSON;


    static {
        GSON = new Gson();
    }

    private Wallet wallet;

    public Updater(Wallet wallet) throws IOException {
        this.wallet = wallet;
        if (Wallet.DEBUG) return;
        try (InputStream is = new URL("https://raw.githubusercontent.com/kyngs/duino-tools/master/Updater/Wallet/version.json").openStream()) {
            Map<String, String> properties = GSON.fromJson(new BufferedReader(new InputStreamReader(is)), new TypeToken<Map<String, String>>() {
            }.getType());
            String latestStable = properties.get("latest_stable");
            String latestPre = properties.get("latest_pre");
            Properties prop = wallet.getProperties();
            boolean pre = Boolean.parseBoolean(prop.getProperty("download-pre-releases"));
            if (pre) {
                if (latestPre == null) return;
                compareAndDownload(latestPre);
            } else {
                if (latestStable == null) return;
                compareAndDownload(latestStable);
            }
        }
    }

    private void compareAndDownload(String expectedVersion) {
        if (Wallet.VERSION.contentEquals(expectedVersion)) {
            Wallet.LOGGER.info("Version up to date.");
            return;
        }
        wallet.getWindow().displayGuiScreen(new GuiWait(String.format("Updating! \n From version %s to %s!", Wallet.VERSION, expectedVersion)));


    }

}
