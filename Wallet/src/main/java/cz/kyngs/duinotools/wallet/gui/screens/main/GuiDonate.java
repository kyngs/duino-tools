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

package cz.kyngs.duinotools.wallet.gui.screens.main;

import cz.kyngs.duinotools.wallet.Wallet;
import cz.kyngs.duinotools.wallet.gui.Window;
import cz.kyngs.duinotools.wallet.gui.screens.GuiInfo;
import cz.kyngs.duinotools.wallet.network.protocol.Protocol;
import cz.kyngs.duinotools.wallet.network.protocol.throwables.ProtocolException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GuiDonate extends GuiMainScreen {

    private final JLabel amountText;
    private final JTextField amountField;
    private final JButton donate;
    private final JLabel info;

    public GuiDonate(Wallet wallet) {
        super(wallet);
        selectButton(super.donate);
        setLayout(null);
        amountField = new JTextField();
        donate = new JButton("Donate!");
        amountText = new JLabel("Amount: ");
        info = new JLabel("Buy me a cheeseburger.");

        add(amountField);
        add(donate);
        add(amountText);
        add(info);

        donate.addActionListener(e -> {
            Protocol protocol = wallet.getDataLoader().getProtocol();
            Window window = wallet.getWindow();
            double amount;
            try {
                amount = Double.parseDouble(info.getText());
            } catch (NumberFormatException numberFormatException) {
                window.displayGuiScreen(new GuiInfo("That is not a valid number!", () -> window.displayGuiScreen(new GuiDonate(wallet))));
                return;
            }
            try {
                protocol.donate(amount);
            } catch (IOException ioException) {
                window.displayGuiScreen(new GuiInfo("Internal error occurred while proccessing this action.", () -> window.displayGuiScreen(new GuiDonate(wallet))));
            } catch (IllegalArgumentException illegalArgumentException) {
                window.displayGuiScreen(new GuiInfo("That is not a valid number!", () -> window.displayGuiScreen(new GuiDonate(wallet))));
            } catch (ProtocolException protocolException) {
                window.displayGuiScreen(new GuiInfo(String.format("Duco server rejected transaction.\n%s", protocolException.getMessage()), () -> window.displayGuiScreen(new GuiDonate(wallet))));
            }
        });

    }

    @Override
    public void updateIMPL() {
        Font valueFont = new Font("Tahoma", Font.PLAIN, 20);
        Font infoFont = new Font("Tahoma", Font.PLAIN, 30);

        amountText.setFont(valueFont);
        amountField.setFont(valueFont);
        info.setFont(infoFont);

        donate.setSize(getWidth() / 2, getHeight() / 8);
        center(donate, getHeight() - getHeight() / 9 - donate.getHeight());

        amountField.setSize(getWidth() / 2, getHeight() / 6);
        center(amountField, getHeight() - getHeight() / 3 - amountField.getHeight());

        amountText.setSize(calculateWidth(amountText), calculateHeight(amountText));
        amountText.setLocation(amountField.getX() - amountText.getWidth(), amountField.getY() + amountText.getHeight() / 2);

        info.setSize(calculateWidth(info), (int) (calculateHeight(info) * 1.5));
        center(info, getHeight() / 7);

    }
}
