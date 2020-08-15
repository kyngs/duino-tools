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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class Updater {

    private static final Gson GSON;


    static {
        GSON = new Gson();
    }

    private final Wallet wallet;

    public Updater(Wallet wallet, boolean updated) throws IOException {
        this.wallet = wallet;
        if (updated) {
            File updater = new File("Updater.jar");
            if (updater.exists()) updater.delete();
        }
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void compareAndDownload(String expectedVersion) throws IOException, URISyntaxException {
        if (Wallet.VERSION.contentEquals(expectedVersion)) {
            Wallet.LOGGER.info("Version up to date.");
            return;
        }
        wallet.getWindow().displayGuiScreen(new GuiWait(String.format("Updating! \n Downloading new version. \n %s", expectedVersion)));
        File jar = new File(Wallet.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        File newJar = new File(jar.getParent(), String.format("NEW%s", jar.getName()));
        if (newJar.exists()) newJar.delete();
        Files.copy(new URL(String.format("https://raw.githubusercontent.com/kyngs/duino-tools/master/Updater/Wallet/jars/%s.jar", expectedVersion)).openStream(), Paths.get(newJar.toURI()));
        File updater = new File(jar.getParent(), "Updater.jar");
        if (updater.exists()) updater.delete();
        Files.copy(ClassLoader.getSystemResourceAsStream("Updater.jar"), Paths.get(updater.toURI()));
        while (!updater.exists()) try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File propertiesFile = new File(jar.getParent(), "update.properties");
        Properties properties = new Properties();
        properties.setProperty("old_file_path", jar.getAbsolutePath());
        properties.setProperty("new_file_path", newJar.getAbsolutePath());
        properties.setProperty("new_file_name", jar.getName());
        properties.store(new OutputStreamWriter(new FileOutputStream(propertiesFile)), "DO NOT CHANGE THIS UNDER ANY CIRCUMSTANCES");
        Runtime.getRuntime().exec("java -jar Updater.jar", null, jar.getParentFile());
        System.exit(0);
    }

}
