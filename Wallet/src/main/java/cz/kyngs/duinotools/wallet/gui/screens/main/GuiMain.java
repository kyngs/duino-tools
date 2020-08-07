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

import cz.kyngs.duinotools.wallet.DataLoader;
import cz.kyngs.duinotools.wallet.StatisticListener;
import cz.kyngs.duinotools.wallet.Wallet;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Main landing screen after authentication and data download
 *
 * @author kyngs
 * @see cz.kyngs.duinotools.wallet.gui.GuiScreen
 * @see cz.kyngs.duinotools.wallet.StatisticListener
 */
public class GuiMain extends GuiMainScreen implements StatisticListener {
    private Wallet wallet;
    private JLabel balanceName, balanceValue, profitName, profitValue, costName, costValue, fiatName, fiatValue;
    private boolean init;
    private DecimalFormat decimalFormat;

    /**
     * @param wallet Main class
     */
    public GuiMain(Wallet wallet) {
        super(wallet);
        decimalFormat = new DecimalFormat("#.##############");
        selectButton(overview);
        init = false;
        this.wallet = wallet;
        setLayout(null);
        balanceName = new JLabel(String.format("%s's balance: ", wallet.getConfiguration().getUsername()));
        balanceValue = new JLabel("N/A");

        profitName = new JLabel("Profit per minute: ");
        profitValue = new JLabel("N/A");

        costName = new JLabel("Duino cost: ");
        costValue = new JLabel("N/A");

        fiatName = new JLabel(String.format("%s's fiat balance: ", wallet.getConfiguration().getUsername()));
        fiatValue = new JLabel("N/A");

        wallet.getDataLoader().registerListener(this);
        onBalanceUpdate(wallet.getDataLoader());
        onStatisticsUpdate(wallet.getDataLoader());
        onProfitUpdate(wallet.getDataLoader());

        add(balanceName);
        add(balanceValue);
        add(profitName);
        add(profitValue);
        add(costName);
        add(costValue);
        add(fiatName);
        add(fiatValue);

        init = true;

    }

    /**
     * Unregistering listener on dispose.
     */
    @Override
    public void dispose() {
        wallet.getDataLoader().unregisterListener(this);
    }

    @Override
    public void updateIMPL() {
        Font valueFont = new Font("Tahoma", Font.PLAIN, 20);
        Font nameFont = new Font("Tahoma", Font.PLAIN, 14);

        balanceName.setFont(nameFont);
        balanceName.setSize(calculateWidth(balanceName), calculateHeight(balanceName) + calculateHeight(balanceName) / 2);
        balanceName.setLocation(getWidth() / 10, getHeight() / 10);

        balanceValue.setFont(valueFont);
        balanceValue.setSize(calculateWidth(balanceValue), calculateHeight(balanceValue));
        balanceValue.setLocation(getWidth() / 10, balanceName.getY() + getHeight() / 10);

        profitName.setFont(nameFont);
        profitName.setSize(calculateWidth(profitName), calculateHeight(profitName));
        profitName.setLocation(getWidth() / 10, balanceValue.getY() + getHeight() / 10);

        profitValue.setFont(valueFont);
        profitValue.setSize(calculateWidth(profitValue), calculateHeight(profitValue));
        profitValue.setLocation(getWidth() / 10, profitName.getY() + getHeight() / 10);

        costName.setFont(nameFont);
        costName.setSize(calculateWidth(costName), calculateHeight(costName));
        costName.setLocation(getWidth() / 10, profitValue.getY() + getHeight() / 10);

        costValue.setFont(valueFont);
        costValue.setSize(calculateWidth(costValue), calculateHeight(costValue));
        costValue.setLocation(getWidth() / 10, costName.getY() + getHeight() / 10);

        fiatName.setFont(nameFont);
        fiatName.setSize(calculateWidth(fiatName), calculateHeight(fiatName) + calculateHeight(fiatName) / 2);
        fiatName.setLocation(getWidth() / 10, costValue.getY() + getHeight() / 10);

        fiatValue.setFont(valueFont);
        fiatValue.setSize(calculateWidth(fiatValue), calculateHeight(fiatValue));
        fiatValue.setLocation(getWidth() / 10, fiatName.getY() + getHeight() / 10);

    }

    @Override
    public void onStatisticsUpdate(DataLoader dataLoader) {
        costValue.setText(String.format("%s$ per DUCO", decimalFormat.format(dataLoader.getPrice())));
        fiatValue.setText(String.format("%s$", decimalFormat.format(dataLoader.getBalance() * dataLoader.getPrice())));
        if (init) update();
    }

    @Override
    public void onBalanceUpdate(DataLoader dataLoader) {
        balanceValue.setText(String.format("%s DUCO", decimalFormat.format(dataLoader.getBalance())));
        fiatValue.setText(String.format("%s$", decimalFormat.format(dataLoader.getBalance() * dataLoader.getPrice())));
        if (init) update();
    }

    @Override
    public void onProfitUpdate(DataLoader dataLoader) {
        if (dataLoader.getProfitPerMinute() == -1) return;
        profitValue.setText(String.format("%s DUCOs per minute", decimalFormat.format(dataLoader.getProfitPerMinute())));
        if (init) update();
    }


}
