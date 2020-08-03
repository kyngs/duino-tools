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

import cz.kyngs.duinotools.wallet.gui.GuiScreen;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gui used as a Waiting screen. Contains ProgressBar
 *
 * @author kyngs
 * @see cz.kyngs.duinotools.wallet.gui.GuiScreen
 * @see JProgressBar
 */
public class GuiWait extends GuiScreen {

    private final List<JLabel> jLabels;
    protected JProgressBar jProgressBar;

    /**
     * @param message Waiting message
     */
    public GuiWait(String message) {

        setLayout(null);
        jProgressBar = new JProgressBar();
        jProgressBar.setIndeterminate(true);
        add(jProgressBar);

        jLabels = new ArrayList<>();
        for (String split : message.split("\n")) {
            JLabel jLabel = new JLabel(split);
            jLabels.add(jLabel);
            add(jLabel);
        }
    }

    @Override
    public void update() {
        Font font = new Font("Tahoma", Font.PLAIN, getWidth() / 20);
        int height = getHeight() / 20;
        for (JLabel label : jLabels) {
            label.setFont(font);
            label.setSize(calculateWidth(label), calculateHeight(label) + calculateHeight(label) / 2);
            label.setLocation(getWidth() / 2 - label.getWidth() / 2, height);
            height += label.getHeight() + getHeight() / 20;
        }
        displayLoadingAnimation();
    }

    /**
     * Called to display JProgressBar
     * Can be overridden so there may be something other than JProgressBar
     */
    protected void displayLoadingAnimation() {
        jProgressBar.setSize(getWidth() / 2, getHeight() / 10);
        jProgressBar.setLocation(getWidth() / 2 - jProgressBar.getWidth() / 2, getHeight() - jProgressBar.getHeight() * 2);
    }
}
