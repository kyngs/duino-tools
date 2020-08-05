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
import cz.kyngs.duinotools.wallet.gui.GuiScreen;
import cz.kyngs.duinotools.wallet.gui.Window;
import cz.kyngs.duinotools.wallet.utils.ColorUtil;

import javax.swing.*;
import java.awt.*;

public abstract class GuiMainScreen extends GuiScreen {

    private static final Color SELECTED_COLOR;

    static {
        SELECTED_COLOR = ColorUtil.invert(UIManager.getLookAndFeel().getDefaults().getColor("window"));
    }

    protected JButton overview;
    protected JButton statistics;
    protected JButton settings;
    private JMenuBar jMenuBar;

    public GuiMainScreen(Wallet wallet) {
        jMenuBar = new JMenuBar();

        Window window = wallet.getWindow();

        overview = new JButton("Overview");
        overview.addActionListener(e -> window.displayGuiScreen(new GuiMain(wallet)));

        statistics = new JButton("Statistics");
        statistics.addActionListener(e -> window.displayGuiScreen(new GuiStatistics(wallet)));

        settings = new JButton("Settings");
        settings.addActionListener(e -> window.displayGuiScreen(new GuiSettings(wallet)));

        jMenuBar.add(overview);
        jMenuBar.add(statistics);
        jMenuBar.add(settings);

        add(jMenuBar);

    }

    protected void selectButton(JButton button) {
        button.setBackground(SELECTED_COLOR);
    }

    @Override
    public final void update() {
        jMenuBar.setSize(getWidth(), getHeight() / 10);
        jMenuBar.setLocation(0, 0);
        updateIMPL();
    }

    public abstract void updateIMPL();
}
