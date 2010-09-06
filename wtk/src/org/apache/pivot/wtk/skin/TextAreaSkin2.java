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
package org.apache.pivot.wtk.skin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.TextArea2;
import org.apache.pivot.wtk.TextAreaListener2;
import org.apache.pivot.wtk.TextAreaContentListener2;
import org.apache.pivot.wtk.TextAreaSelectionListener2;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Visual;

/**
 * Text area skin.
 */
public class TextAreaSkin2 extends ComponentSkin implements TextArea2.Skin,
    TextAreaListener2, TextAreaContentListener2, TextAreaSelectionListener2 {
    /**
     * Paragraph view.
     */
    public class ParagraphView implements Visual, TextArea2.ParagraphListener {
        private TextArea2.Paragraph paragraph;

        private int width = 0;
        private int height = 0;

        private int breakWidth = Integer.MAX_VALUE;

        private boolean valid = false;
        private ArrayList<GlyphVector> rows = new ArrayList<GlyphVector>();

        public ParagraphView(TextArea2.Paragraph paragraph) {
            this.paragraph = paragraph;
        }

        public TextArea2.Paragraph getParagraph() {
            return paragraph;
        }

        @Override
        public int getWidth() {
            validate();
            return width;
        }

        @Override
        public int getHeight() {
            validate();
            return height;
        }

        public int getBreakWidth() {
            return breakWidth;
        }

        public void setBreakWidth(int breakWidth) {
            int previousBreakWidth = this.breakWidth;
            this.breakWidth = breakWidth;

            if (previousBreakWidth > breakWidth) {
                invalidate();
            }
        }

        @Override
        public int getBaseline() {
            return -1;
        }

        @Override
        public void paint(Graphics2D graphics) {
            // Iterate over rows and paint each
            // TODO Only paint visible glyphs
            for (GlyphVector line : rows) {
                // TODO
                graphics.drawGlyphVector(line, 0, 0);
            }
        }

        public void invalidate() {
            valid = false;
        }

        public void validate() {
            if (!valid) {
                // TODO Re-layout glyphs and recalculate size
                rows = new ArrayList<GlyphVector>();
                width = 0;
                height = 0;
            }

            valid = true;
        }

        @Override
        public void textInserted(TextArea2.Paragraph paragraph, int index, int count) {
            invalidate();
            invalidateComponent();
        }

        @Override
        public void textRemoved(TextArea2.Paragraph paragraph, int index, int count) {
            invalidate();
            invalidateComponent();
        }
    }

    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (selection == null) {
                TextArea2 textArea = (TextArea2)getComponent();
                textArea.repaint(caret.x, caret.y, caret.width, caret.height, true);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextArea2 textArea = (TextArea2)getComponent();
            int selectionStart = textArea.getSelectionStart();
            int selectionLength = textArea.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case UP: {
                    // Get previous offset
                    int offset = getNextInsertionPoint(mouseX, selectionStart, scrollDirection);

                    if (offset != -1) {
                        textArea.setSelection(offset, selectionEnd - offset + 1);
                        scrollCharacterToVisible(offset + 1);
                    }

                    break;
                }

                case DOWN: {
                    // Get next offset
                    int offset = getNextInsertionPoint(mouseX, selectionEnd, scrollDirection);

                    if (offset != -1) {
                        // If the next character is a paragraph terminator, increment the selection
                        if (textArea.getCharacterAt(offset) == '\n') {
                            offset++;
                        }

                        textArea.setSelection(selectionStart, offset - selectionStart);
                        scrollCharacterToVisible(offset - 1);
                    }

                    break;
                }
            }
        }
    }

    private int caretX = 0;
    private Rectangle caret = new Rectangle();
    private Area selection = null;

    private boolean caretOn = false;

    private int anchor = -1;
    private TextArea2.ScrollDirection scrollDirection = null;
    private int mouseX = -1;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color backgroundColor;
    private Color inactiveColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Insets margin = new Insets(4);
    private boolean wrapText = true;

    private ArrayList<ParagraphView> paragraphViews = new ArrayList<ParagraphView>();

    public TextAreaSkin2() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        backgroundColor = Color.WHITE;
        inactiveColor = Color.GRAY;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextArea2 textArea = (TextArea2)component;
        textArea.getTextAreaListeners().add(this);
        textArea.getTextAreaContentListeners().add(this);
        textArea.getTextAreaSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
        }

        preferredWidth += margin.left + margin.right;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        // Include margin in constraint
        int breakWidth = (wrapText
            && width != -1) ? Math.max(width - (margin.left + margin.right), 0) : Integer.MAX_VALUE;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(breakWidth);
            preferredHeight += paragraphView.getHeight();
        }

        preferredHeight += margin.top + margin.bottom;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(Integer.MAX_VALUE);
            preferredWidth = Math.max(preferredWidth, paragraphView.getWidth());
            preferredHeight += paragraphView.getHeight();
        }

        preferredWidth += margin.left + margin.right;
        preferredHeight += margin.top + margin.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        int width = getWidth();

        for (ParagraphView paragraphView : paragraphViews) {
            paragraphView.setBreakWidth(width);

            // TODO Set location
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        // TODO Paint selection state
        // TODO Walk paragraph view list and translate graphics/paint each view (only
        // paint visible views)
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public int getInsertionPoint(int x, int y) {
        // TODO
        return -1;
    }

    public int getNextInsertionPoint(int x, int from, TextArea2.ScrollDirection direction) {
        // TODO
        return -1;
    }

    public int getRowIndex(int index) {
        // TODO
        return -1;
    }

    public int getRowCount() {
        // TODO
        return 0;
    }

    public Bounds getCharacterBounds(int index) {
        // TODO Check for paragraph terminators including implicit final terminator
        return null;
    }

    private void scrollCharacterToVisible(int offset) {
        TextArea2 textArea = (TextArea2)getComponent();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            textArea.scrollAreaToVisible(characterBounds.x, characterBounds.y,
                characterBounds.width, characterBounds.height);
        }
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

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
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

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(Color inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(String inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        setColor(GraphicsUtilities.decodeColor(inactiveColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public Insets getMargin() {
        return margin;
    }

    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    public final void setMargin(String margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(Insets.decode(margin));
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        // TODO

        return consumed;
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        // TODO

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        // TODO

        return consumed;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextArea2 textArea = (TextArea2)getComponent();
        if (textArea.isFocused()
            && textArea.getSelectionLength() == 0) {
            scrollCharacterToVisible(textArea.getSelectionStart());
            showCaret(true);
        } else {
            showCaret(false);
        }

        repaintComponent();
    }

    @Override
    public void maximumLengthChanged(TextArea2 textArea, int previousMaximumLength) {
        // TODO
    }

    @Override
    public void editableChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void paragraphInserted(TextArea2 textArea, int index) {
        // TODO Create paragraph view and add as paragraph listener
    }

    @Override
    public void paragraphsRemoved(TextArea2 textArea, int index, Sequence<TextArea2.Paragraph> removed) {
        // TODO Remove paragraph views and remove as paragraph listeners
    }

    @Override
    public void textChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void selectionChanged(TextArea2 textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // TODO
    }

    private void updateSelection() {
        TextArea2 textArea = (TextArea2)getComponent();

        if (textArea.getCharacterCount() > 0) {
            // Update the caret
            int selectionStart = textArea.getSelectionStart();

            Bounds leadingSelectionBounds = getCharacterBounds(selectionStart);
            caret = leadingSelectionBounds.toRectangle();
            caret.width = 1;

            // Update the selection
            int selectionLength = textArea.getSelectionLength();

            if (selectionLength > 0) {
                int selectionEnd = selectionStart + selectionLength - 1;
                Bounds trailingSelectionBounds = getCharacterBounds(selectionEnd);
                selection = new Area();

                int firstRowIndex = getRowIndex(selectionStart);
                int lastRowIndex = getRowIndex(selectionEnd);

                if (firstRowIndex == lastRowIndex) {
                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x, leadingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - leadingSelectionBounds.x,
                        trailingSelectionBounds.y + trailingSelectionBounds.height - leadingSelectionBounds.y)));
                } else {
                    int width = getWidth();

                    selection.add(new Area(new Rectangle(leadingSelectionBounds.x,
                        leadingSelectionBounds.y,
                        width - margin.right - leadingSelectionBounds.x,
                        leadingSelectionBounds.height)));

                    if (lastRowIndex - firstRowIndex > 0) {
                        selection.add(new Area(new Rectangle(margin.left,
                            leadingSelectionBounds.y + leadingSelectionBounds.height,
                            width - (margin.left + margin.right),
                            trailingSelectionBounds.y - (leadingSelectionBounds.y
                                + leadingSelectionBounds.height))));
                    }

                    selection.add(new Area(new Rectangle(margin.left, trailingSelectionBounds.y,
                        trailingSelectionBounds.x + trailingSelectionBounds.width - margin.left,
                        trailingSelectionBounds.height)));
                }
            } else {
                selection = null;
            }
        } else {
            // Clear the caret and the selection
            caret = new Rectangle();
            selection = null;
        }
    }

    private void showCaret(boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback =
                ApplicationContext.scheduleRecurringCallback(blinkCaretCallback,
                    Platform.getCursorBlinkRate());

            // Run the callback once now to show the cursor immediately
            blinkCaretCallback.run();
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}