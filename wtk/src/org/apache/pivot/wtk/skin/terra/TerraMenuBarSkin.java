/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuBarListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ContainerSkin;


/**
 * Menu bar skin.
 *
 * @author gbrown
 */
public class TerraMenuBarSkin extends ContainerSkin implements MenuBarListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private int spacing;

    public TerraMenuBarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(4));

        font = theme.getFont().deriveFont(Font.BOLD);
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        highlightColor = theme.getColor(4);
        highlightBackgroundColor = theme.getColor(20);
        spacing = 2;
    }

    public void install(Component component) {
        super.install(component);

        MenuBar menuBar = (MenuBar)component;
        menuBar.getMenuBarListeners().add(this);

        menuBar.setFocusTraversalPolicy(new IndexFocusTraversalPolicy(true));
    }

    public void uninstall() {
        MenuBar menuBar = (MenuBar)getComponent();
        menuBar.getMenuBarListeners().remove(this);

        menuBar.setFocusTraversalPolicy(null);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        int j = 0;
        for (int i = 0, n = items.getLength(); i < n; i++) {
            if (j > 0) {
                preferredWidth += spacing;
            }

            MenuBar.Item item = items.get(i);
            if (item.isDisplayable()) {
                preferredWidth += item.getPreferredWidth(height);
                j++;
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        for (int i = 0, n = items.getLength(); i < n; i++) {
            MenuBar.Item item = items.get(i);
            if (item.isDisplayable()) {
                preferredHeight = Math.max(item.getPreferredHeight(width), preferredHeight);
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        int j = 0;
        for (int i = 0, n = items.getLength(); i < n; i++) {
            if (j > 0) {
                preferredWidth += spacing;
            }

            MenuBar.Item item = items.get(i);
            if (item.isDisplayable()) {
                preferredWidth += item.getPreferredWidth(-1);
                preferredHeight = Math.max(item.getPreferredHeight(-1), preferredHeight);
            }
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        MenuBar menuBar = (MenuBar)getComponent();

        int height = getHeight();
        int itemX = 0;

        for (MenuBar.Item item : menuBar.getItems()) {
            if (item.isDisplayable()) {
                item.setVisible(true);
                item.setSize(item.getPreferredWidth(height), height);
                item.setLocation(itemX, 0);

                itemX += item.getWidth() + spacing;
            } else {
                item.setVisible(false);
            }
        }
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(color));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(GraphicsUtilities.decodeColor(highlightColor));
    }

    public final void setHighlightColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHighlightColor(theme.getColor(color));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    public final void setHighlightBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHighlightBackgroundColor(theme.getColor(color));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("Spacing is negative.");
        }

        this.spacing = spacing;
        invalidateComponent();
    }

    public void itemInserted(MenuBar menuBar, int index) {
        invalidateComponent();
    }

    public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed) {
        invalidateComponent();
    }

    public void activeChanged(MenuBar menuBar) {
        // No-op
    }
}