/*
 * Copyright 2020 kyngs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cz.kyngs.duinowallet.pc.gui;

import cz.kyngs.duinowallet.pc.Wallet;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Window extends JFrame {

    private final Wallet wallet;
    private GuiScreen guiScreen;

    public Window(Wallet wallet) {
        this.wallet = wallet;
        setLayout(null);
        setVisible(true);
        setResizable(false);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (guiScreen != null) {
                    guiScreen.setSize(getSize());
                    guiScreen.update();
                }
            }
        });
        setSize(500, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Duino Wallet - SNAPSHOT-1.0");
    }

    public void displayGuiScreen(GuiScreen guiScreen) {
        if (this.guiScreen != null)this.guiScreen.close();
        setIgnoreRepaint(false);
        setContentPane(guiScreen);

        guiScreen.setSize(getSize());

        revalidate();
        repaint();
        guiScreen.update();
        this.guiScreen = guiScreen;
    }

}
