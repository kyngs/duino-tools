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

package cz.kyngs.duinowallet.pc.gui.screens.main;

import com.sun.corba.se.impl.protocol.giopmsgheaders.TargetAddress;
import cz.kyngs.duinowallet.pc.DataLoader;
import cz.kyngs.duinowallet.pc.StatisticListener;
import cz.kyngs.duinowallet.pc.Wallet;
import cz.kyngs.duinowallet.pc.gui.GuiScreen;

import javax.swing.*;
import java.awt.*;

public class GuiMain extends GuiScreen implements StatisticListener {
    private Wallet wallet;
    private JLabel balanceName, balanceValue;
    private boolean init;

    public GuiMain(Wallet wallet) {
        init = false;
        this.wallet = wallet;
        setLayout(null);
        balanceName = new JLabel(String.format("%s's balance", wallet.getConfiguration().getUsername()));
        balanceValue = new JLabel();
        wallet.getDataLoader().registerListener(this);
        onBalanceUpdate(wallet.getDataLoader());

        add(balanceName);
        add(balanceValue);
        init = true;

    }

    @Override
    public void close() {
        wallet.getDataLoader().unregisterListener(this);
    }

    @Override
    public void update() {
        Font valueFont = new Font("Tahoma", Font.PLAIN, 20);
        Font nameFont = new Font("Tahoma", Font.PLAIN, 14);

        balanceName.setFont(nameFont);
        balanceName.setSize(calculateWidth(balanceName), calculateHeight(balanceName) + calculateHeight(balanceName)/2);
        balanceName.setLocation(getWidth()/10, getHeight()/10);

        balanceValue.setFont(valueFont);
        balanceValue.setSize(calculateWidth(balanceValue), calculateHeight(balanceValue));
        balanceValue.setLocation(getWidth()/10, balanceName.getY() + getHeight()/10);
    }

    @Override
    public void onStatisticsUpdate(DataLoader dataLoader) {
        if (init) update();
    }

    @Override
    public void onBalanceUpdate(DataLoader dataLoader) {
        balanceValue.setText(String.format("%s DUCO", dataLoader.getBalance()));
        if (init) update();
    }


}
