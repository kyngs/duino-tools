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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Gui for displaying some info with button.
 *
 * @author kyngs
 * @see cz.kyngs.duinotools.wallet.gui.GuiScreen
 * @see cz.kyngs.duinotools.wallet.gui.screens.GuiWait
 */
public class GuiInfo extends GuiWait {

    private final JButton okayButton;

    /**
     *
     * @param message Message
     * @param onActionPerformed What will happen when button OK is pressed.
     */
    public GuiInfo(String message, Runnable onActionPerformed) {
        super(message);
        remove(jProgressBar);
        okayButton = new JButton("OK");
        okayButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionPerformed.run();
            }
        });
        add(okayButton);
    }

    @Override
    public void update() {
        super.update();
        Font font = new Font("Tahoma", Font.PLAIN, getHeight() / 13);
        okayButton.setFont(font);
        okayButton.setSize(getWidth() / 2, getHeight() / 10);
        okayButton.setLocation(getWidth() / 2 - okayButton.getWidth() / 2, getHeight() - okayButton.getHeight() * 2);
    }

    /**
     * Overridden from GuiWait so ProgressBar is not displayed.
     */
    @Override
    protected void displayLoadingAnimation() {
    }
}
