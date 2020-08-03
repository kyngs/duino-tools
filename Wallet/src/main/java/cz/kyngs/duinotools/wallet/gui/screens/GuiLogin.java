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

package cz.kyngs.duinotools.wallet.gui.screens;

import cz.kyngs.duinotools.wallet.Wallet;
import cz.kyngs.duinotools.wallet.auth.LoginResult;
import cz.kyngs.duinotools.wallet.gui.GuiScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GuiLogin extends GuiScreen {

    private final JTextField username;
    private final JPasswordField password;
    private final JLabel passwordLabel;
    private final JLabel usernameLabel;
    private final JLabel name;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JPanel buttonPanel;
    private final Wallet wallet;
    private final boolean waitingForAuth;

    public GuiLogin(Wallet wallet, boolean waitingForAuth) {
        this.wallet = wallet;
        this.waitingForAuth = waitingForAuth;
        setLayout(null);

        username = new JTextField();
        password = new JPasswordField();
        passwordLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        name = new JLabel();
        buttonPanel = new JPanel();
        registerButton = new JButton();

        passwordLabel.setText("Password: ");
        usernameLabel.setText("Username: ");
        name.setText("Duino Wallet");

        loginButton.setText("Login");
        registerButton.setText("Register");

        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        add(username);
        add(password);
        add(passwordLabel);
        add(usernameLabel);
        add(buttonPanel);
        add(name);

        loginButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wallet.getWindow().displayGuiScreen(new GuiWait("Authorizing!"));
                wallet.getAuthCore().authorize(username.getText(), password.getText(), result -> {
                    if (result.getKey() == LoginResult.NO) {
                        wallet.reconnect();
                        wallet.getWindow().displayGuiScreen(new GuiInfo(String.format("Failed to authorize: \n %s", result.getValue()), () -> wallet.getWindow().displayGuiScreen(new GuiLogin(wallet, waitingForAuth))));
                        username.setText("");
                        password.setText("");
                    }else {
                        wallet.getConfiguration().setPassword(password.getText());
                        wallet.getConfiguration().setUsername(username.getText());
                    }
                });
            }
        });
        registerButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wallet.getWindow().displayGuiScreen(new GuiInfo("Register feature is not implemented yet.", () -> wallet.getWindow().displayGuiScreen(new GuiLogin(wallet, waitingForAuth))));
            }
        });

    }

    @Override
    public void update() {

        Font font = new Font("Tahoma", Font.PLAIN, getHeight() / 13);

        username.setFont(font);
        username.setSize((int) (getWidth() / 1.9), getHeight() / 8);
        username.setLocation(getWidth() / 2 - username.getWidth() / 2, username.getHeight() * 2);

        password.setFont(font);
        password.setSize((int) (getWidth() / 1.9), getHeight() / 8);
        password.setLocation(getWidth() / 2 - password.getWidth() / 2, getHeight() - password.getHeight() * 4);

        passwordLabel.setFont(font);
        passwordLabel.setSize(calculateWidth(passwordLabel), calculateHeight(passwordLabel));
        passwordLabel.setLocation(password.getX() - passwordLabel.getWidth(), password.getY() + password.getHeight() / 2 - passwordLabel.getHeight() / 2);

        usernameLabel.setFont(font);
        usernameLabel.setSize(calculateWidth(usernameLabel), calculateHeight(usernameLabel));
        usernameLabel.setLocation(username.getX() - usernameLabel.getWidth(), username.getY() + username.getHeight() / 2 - usernameLabel.getHeight() / 2);

        buttonPanel.setSize(getWidth() / 2, getHeight() / 10);
        buttonPanel.setLocation(getWidth() / 2 - buttonPanel.getWidth() / 2, getHeight() - buttonPanel.getHeight() * 3);
        buttonPanel.setLayout(new GridLayout());
        buttonPanel.setBackground(new Color(255, 255, 255));

        loginButton.setFont(font);
        registerButton.setFont(font);

        name.setFont(font);
        name.setSize(calculateWidth(name), calculateHeight(name));
        name.setLocation(getWidth() / 2 - name.getWidth() / 2, getHeight() / 10);

    }
}
