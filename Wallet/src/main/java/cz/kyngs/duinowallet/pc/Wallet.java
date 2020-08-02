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

package cz.kyngs.duinowallet.pc;

import cz.kyngs.duinowallet.pc.auth.AuthCore;
import cz.kyngs.duinowallet.pc.auth.LoginResult;
import cz.kyngs.duinowallet.pc.configuration.Configuration;
import cz.kyngs.duinowallet.pc.gui.Window;
import cz.kyngs.duinowallet.pc.gui.screens.GuiInfo;
import cz.kyngs.duinowallet.pc.gui.screens.GuiLogin;
import cz.kyngs.duinowallet.pc.gui.screens.GuiWait;
import cz.kyngs.duinowallet.pc.gui.screens.main.GuiMain;
import cz.kyngs.duinowallet.pc.logging.LogManager;
import cz.kyngs.duinowallet.pc.logging.Logger;
import cz.kyngs.duinowallet.pc.network.Network;
import cz.kyngs.duinowallet.pc.utils.FrameLimiter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.swing.*;
import java.io.IOException;
import java.util.Properties;

public class Wallet {

    public static final Logger LOGGER;
    private static Wallet instance; //Using singleton

    static {
        LOGGER = new LogManager().createLogger(true);
    }

    private final Object authenticationLock;
    private Configuration configuration;
    private Network network;
    private final AuthCore authCore;
    private Window window;
    private DataLoader dataLoader;
    private boolean keepReconnecting;

    public Wallet(String[] args) throws Exception {
        instance = this;
        System.setProperty("sun.awt.noerasebackground", "true");
        Properties props = System.getProperties();
        props.setProperty("javax.accessibility.assistive_technologies", "");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> new Stopper(this), "Stopper"));

        authenticationLock = new Object();

        keepReconnecting = true;
        try {
            network = new Network(this);
        } catch (IOException e) {
            LOGGER.error("CANNOT CONNECT", e);
            System.exit(1);
        }
        Wallet.LOGGER.info(String.format("Server version is: %s", network.getVersion()));

        authCore = new AuthCore(this);

        SwingUtilities.invokeAndWait(() -> window = new Window(this));
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        parseArgs(args);

        window.displayGuiScreen(new GuiWait("Downloading data!"));

        dataLoader = new DataLoader(this);

        window.displayGuiScreen(new GuiMain(this));

    }

    public static Wallet getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        try {
            new Wallet(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getAuthenticationLock() {
        return authenticationLock;
    }

    public Network getNetwork() {
        return network;
    }

    public void reconnect() {
        LOGGER.debug("Reconnecting");
        try {
            network.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug("RECONNECTED");
    }

    public void reconnect(Configuration configuration){
        reconnect();
        authCore.authorize(configuration.getUsername(), configuration.getPassword(), result -> {
            if (result.getKey() == LoginResult.NO) {
                window.displayGuiScreen(new GuiInfo(String.format("Failed to authorize: %s", result.getValue()), () -> System.exit(1)));
            }
        });
    }

    public AuthCore getAuthCore() {
        return authCore;
    }

    /**
     * Should be called after window initialization!
     *
     * @param args Program arguments.
     *
     */
    private void parseArgs(String[] args) {

        OptionParser optionParser = new OptionParser();

        OptionSpec<String> usernameSpec = optionParser.accepts("username").withRequiredArg();
        OptionSpec<String> passwordSpec = optionParser.accepts("password").withRequiredArg();

        OptionSet optionSet = optionParser.parse(args);

        String username = optionSet.valueOf(usernameSpec);
        String password = optionSet.valueOf(passwordSpec);

        configuration = new Configuration(username, password);

        if (username == null || password == null) {
            window.displayGuiScreen(new GuiLogin(this, true));
            synchronized (authenticationLock) {
                try {
                    authenticationLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        } else {
            getWindow().displayGuiScreen(new GuiWait("Authorizing!"));
            authCore.authorize(username, password, result -> {
                if (result.getKey() == LoginResult.NO) {
                    window.displayGuiScreen(new GuiInfo(String.format("Failed to authorize: %s", result.getValue()), () -> System.exit(1)));
                }
            });
        }

    }

    public Window getWindow() {
        return window;
    }

    public DataLoader getDataLoader() {
        return dataLoader;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void stopReconnecting() {
        keepReconnecting = false;
    }

    public boolean keepReconnecting() {
        return keepReconnecting;
    }
}
