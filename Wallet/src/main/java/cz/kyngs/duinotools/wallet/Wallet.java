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

package cz.kyngs.duinotools.wallet;

import cz.kyngs.duinotools.wallet.auth.AuthCore;
import cz.kyngs.duinotools.wallet.auth.LoginResult;
import cz.kyngs.duinotools.wallet.auth.Reconnector;
import cz.kyngs.duinotools.wallet.configuration.Configuration;
import cz.kyngs.duinotools.wallet.gui.Window;
import cz.kyngs.duinotools.wallet.gui.screens.GuiInfo;
import cz.kyngs.duinotools.wallet.gui.screens.GuiLogin;
import cz.kyngs.duinotools.wallet.gui.screens.GuiWait;
import cz.kyngs.duinotools.wallet.gui.screens.main.GuiMain;
import cz.kyngs.duinotools.wallet.logging.LogManager;
import cz.kyngs.duinotools.wallet.logging.Logger;
import cz.kyngs.duinotools.wallet.network.Network;
import cz.kyngs.duinotools.wallet.updater.Updater;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Main class for Wallet. Does not extend JavaFXApplication.
 *
 * @author kyngs
 */
public class Wallet {

    /**
     * Instance of logger that is used all around the code
     *
     * @see Logger
     * @see LogManager#createLogger
     */
    public static final Logger LOGGER;
    private static Wallet instance; //Using singleton
    public static final String VERSION;
    public static final boolean DEBUG;

    static {
        VERSION = "SNAPSHOT-1.0-1";
        DEBUG = false;
        LOGGER = new LogManager().createLogger(DEBUG);
    }

    private final Object authenticationLock;
    private Configuration configuration;
    private Network network;
    private AuthCore authCore;
    private Window window;
    private DataLoader dataLoader;
    private boolean keepReconnecting;

    private Properties properties;
    private Updater updater;

    /**
     * Only constructor of Wallet, cannot be accessed from anywhere else.
     *
     * @param args Program arguments.
     * @throws Exception Throws Exception for better crash handling.
     */
    private Wallet(String[] args) throws Exception {
        instance = this;
        System.setProperty("sun.awt.noerasebackground", "true");
        Properties props = System.getProperties();
        props.setProperty("javax.accessibility.assistive_technologies", "");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> new Stopper(this), "Stopper"));

        authenticationLock = new Object();

        keepReconnecting = true;
        SwingUtilities.invokeAndWait(() -> window = new Window(this));
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        window.displayGuiScreen(new GuiWait("Looking for updates"));
        parseProperties();

        updater = new Updater(this);

        window.displayGuiScreen(new GuiWait("Connecting to duino server"));
        try {
            network = new Network(this);
        } catch (IOException e) {
            window.displayGuiScreen(new GuiInfo(e.getMessage(), () -> System.exit(1)));
            return;
        }
        Wallet.LOGGER.info(String.format("Server version is: %s", network.getVersion()));

        authCore = new AuthCore(this);

        parseArgs(args);

        window.displayGuiScreen(new GuiWait("Downloading data!"));

        dataLoader = new DataLoader(this);

        window.displayGuiScreen(new GuiMain(this));

    }

    public Properties getProperties() {
        return properties;
    }

    private void parseProperties() throws URISyntaxException, IOException {
        File dir = new File("wallet-resources");
        if (!dir.exists()) dir.mkdir();
        File settingsFile = new File(dir, "settings.properties");
        if (!settingsFile.exists()) {
            Files.copy(ClassLoader.getSystemResource("settings.properties").openStream(), Paths.get(settingsFile.toURI()));
        }
        properties = new Properties();
        properties.load(settingsFile.toURI().toURL().openStream());
    }

    /**
     * Singleton Anti pattern. It is just here if anyone would've wanted to modify this code, through API and was lazy to do dependency Injection.
     *
     * @return Instance of Wallet.
     * @deprecated Because it is singleton.
     */
    @Deprecated
    public static Wallet getInstance() {
        return instance;
    }

    /**
     * Main method, only instantiates new instance of Wallet.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        try {
            new Wallet(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Returns lock for authentication.
     * @see Object#wait()
     * @see Object#notifyAll()
     */
    public Object getAuthenticationLock() {
        return authenticationLock;
    }

    /**
     * @return Returns program's network system.
     * @see Network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Simple system for reconnecting. Usually called from AliveConnectionHandler
     *
     * @see cz.kyngs.duinotools.wallet.network.AliveConnectionHandler
     */
    public void reconnect() {
        LOGGER.debug("Reconnecting");
        try {
            network.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug("RECONNECTED");
    }

    /**
     * Simple system for reconnecting with authentication afterwards. Usually called from AliveConnectionHandler
     *
     * @see cz.kyngs.duinotools.wallet.network.AliveConnectionHandler
     * @see cz.kyngs.duinotools.wallet.configuration.Configuration
     */
    public void reconnect(Configuration configuration) {
        reconnect(configuration, new Reconnector(this));
    }

    public void reconnect(Configuration configuration, Reconnector reconnector) {
        reconnect();
        authCore.authorize(configuration.getUsername(), configuration.getPassword(), reconnector);
    }

    /**
     * @return Authentication system of program.
     * @see cz.kyngs.duinotools.wallet.auth.AuthCore
     */
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

    /**
     * @return Returning window of program
     * @see Window
     */
    public Window getWindow() {
        return window;
    }

    /**
     * @return Returning system DataLoader.
     * @see DataLoader
     */
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    /**
     * @return System configuration.
     * @see Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Changes boolean value.
     */
    public void stopReconnecting() {
        keepReconnecting = false;
    }

    /**
     * @return keepReconnecting
     */
    public boolean keepReconnecting() {
        return keepReconnecting;
    }
}
