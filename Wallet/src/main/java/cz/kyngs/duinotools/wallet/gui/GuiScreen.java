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

package cz.kyngs.duinotools.wallet.gui;

import javax.swing.*;

/**
 * abstract implementation of JPanel with some nice features
 *
 * @author kyngs
 * @see JPanel
 * @see Window
 */
public abstract class GuiScreen extends JPanel {

    /**
     * Called when firstly displayed and when window resizes.
     */
    public abstract void update();

    /**
     * Simple method to calculate width of label.
     *
     * @param label label
     * @return width
     */
    protected int calculateWidth(JLabel label) {
        return getGraphics().getFontMetrics(label.getFont()).stringWidth(label.getText());
    }

    /**
     * Simple method to calculate height of label.
     *
     * @param label label
     * @return height
     */
    protected int calculateHeight(JLabel label) {
        int height = label.getFont().getSize();
        return label.getFont().getSize();
    }

    /**
     * Called from window when GuiScreen is being replaced by another one.
     */
    public void dispose() {
    }
}
