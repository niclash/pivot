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

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.geom.Area;
import org.apache.pivot.wtk.graphics.Color;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.TextInputListener;
import org.apache.pivot.wtk.TextInputSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.RenderingHints;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;
import org.apache.pivot.wtk.graphics.font.LineMetrics;
import org.apache.pivot.wtk.graphics.geom.Rectangle;
import org.apache.pivot.wtk.graphics.geom.Shape;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.validation.Validator;

/**
 * Text input skin.
 */
public class TerraTextInputSkin extends ComponentSkin implements TextInput.Skin,
    TextInputListener, TextInputContentListener, TextInputSelectionListener {
    private class BlinkCaretCallback implements Runnable {
        @Override
        public void run() {
            caretOn = !caretOn;

            if (caret != null) {
                TextInput textInput = (TextInput)getComponent();
                Bounds bounds = caret.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private class ScrollSelectionCallback implements Runnable {
        @Override
        public void run() {
            TextInput textInput = (TextInput)getComponent();
            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();
            int selectionEnd = selectionStart + selectionLength - 1;

            switch (scrollDirection) {
                case FORWARD: {
                    if (selectionEnd < textInput.getCharacterCount() - 1) {
                        selectionEnd++;
                        textInput.setSelection(selectionStart, selectionEnd - selectionStart + 1);
                        scrollCharacterToVisible(selectionEnd);
                    }

                    break;
                }

                case BACKWARD: {
                    if (selectionStart > 0) {
                        selectionStart--;
                        textInput.setSelection(selectionStart, selectionEnd - selectionStart + 1);
                        scrollCharacterToVisible(selectionStart);
                    }

                    break;
                }

                default: {
                    throw new RuntimeException();
                }
            }
        }
    }

    private GlyphVector glyphVector = null;

    private int anchor = -1;
    private Rectangle caret = Platform.getInstalled().getGraphicsSystem().newRectangle();
    private Rectangle selection = null;

    private int scrollLeft = 0;

    private boolean caretOn = true;

    private FocusTraversalDirection scrollDirection = null;

    private BlinkCaretCallback blinkCaretCallback = new BlinkCaretCallback();
    private ApplicationContext.ScheduledCallback scheduledBlinkCaretCallback = null;

    private ScrollSelectionCallback scrollSelectionCallback = new ScrollSelectionCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollSelectionCallback = null;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color promptColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color invalidColor;
    private Color invalidBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Color bevelColor;
    private Color disabledBevelColor;
    private Color invalidBevelColor;

    private Insets padding;

    private Dimensions averageCharacterSize;

    private static final int SCROLL_RATE = 50;
    private static final char BULLET = 0x2022;

    public TerraTextInputSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setFont(theme.getFont());

        color = theme.getColor(1);
        promptColor = theme.getColor(7);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(11);
        disabledBackgroundColor = theme.getColor(10);
        invalidColor = theme.getColor(4);
        invalidBackgroundColor = theme.getColor(22);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(2);

        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(14);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);

        bevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;
        invalidBevelColor = TerraTheme.darken(invalidBackgroundColor);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TextInput textInput = (TextInput)component;
        textInput.getTextInputListeners().add(this);
        textInput.getTextInputContentListeners().add(this);
        textInput.getTextInputSelectionListeners().add(this);

        textInput.setCursor(Cursor.TEXT);

        updateSelection();
    }

    @Override
    public int getPreferredWidth(int height) {
        TextInput textInput = (TextInput)getComponent();
        int textSize = textInput.getTextSize();

        return averageCharacterSize.width * textSize + (padding.left + padding.right) + 2;
    }

    @Override
    public int getPreferredHeight(int width) {
        return averageCharacterSize.height + (padding.top + padding.bottom) + 2;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        float textHeight = lm.getHeight();

        int baseline = Math.round((height - textHeight) / 2 + ascent);

        return baseline;
    }

    @Override
    public void layout() {
        TextInput textInput = (TextInput)getComponent();

        glyphVector = null;

        int n = textInput.getCharacterCount();
        if (n > 0) {
            CharSequence characters;
            if (textInput.isPassword()) {
                StringBuilder passwordBuilder = new StringBuilder(n);
                for (int i = 0; i < n; i++) {
                    passwordBuilder.append(BULLET);
                }

                characters = passwordBuilder;
            } else {
                characters = textInput.getCharacters();
            }

            CharSequenceCharacterIterator ci = new CharSequenceCharacterIterator(characters);

            FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
            glyphVector = font.createGlyphVector(fontRenderContext, ci);

            Bounds textBounds = glyphVector.getLogicalBounds();
            int textWidth = (int)textBounds.getWidth();
            int width = getWidth();

            if (textWidth - scrollLeft + padding.left + 1 < width - padding.right - 1) {
                // The right edge of the text is less than the right inset; align
                // the text's right edge with the inset
                scrollLeft = Math.max(textWidth + (padding.left + padding.right + 2) - width, 0);
            } else {
                // Scroll lead selection to visible
                int selectionStart = textInput.getSelectionStart();
                if (selectionStart <= n
                    && textInput.isFocused()) {
                    scrollCharacterToVisible(selectionStart);
                }
            }
        }

        updateSelection();
        showCaret(textInput.isFocused()
            && textInput.getSelectionLength() == 0);
    }

    @Override
    public void paint(Graphics2D graphics) {
        TextInput textInput = (TextInput)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor;
        Color borderColor;
        Color bevelColor;

        if (textInput.isEnabled()) {
            if (textInput.isTextValid()) {
                backgroundColor = this.backgroundColor;
                bevelColor = this.bevelColor;
            } else {
                backgroundColor = invalidBackgroundColor;
                bevelColor = invalidBevelColor;
            }

            borderColor = this.borderColor;
        } else {
            backgroundColor = disabledBackgroundColor;
            borderColor = disabledBorderColor;
            bevelColor = disabledBevelColor;
        }
        GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();
        graphics.setStroke(graphicsFactory.getStrokeFactory().createBasicStroke());

        // Paint the background
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // Paint the bevel
        graphics.setColor(bevelColor);
        GraphicsUtilities.drawLine(graphics, 0, 0, width, Orientation.HORIZONTAL);

        // Paint the content
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        float textHeight = lm.getHeight();

        String prompt = textInput.getPrompt();

        Color caretColor;
        if (glyphVector == null
            && prompt != null) {
            graphics.setFont(font);
            graphics.setColor(promptColor);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                fontRenderContext.getAntiAliasingHint());
            graphics.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS,
                fontRenderContext.getFractionalMetricsHint());
            graphics.drawString(prompt, padding.left - scrollLeft + 1,
                (int) ((height - textHeight) / 2 + ascent));

            caretColor = color;
        } else {
            boolean textValid = textInput.isTextValid();

            Color color;
            if (textInput.isEnabled()) {
                if (!textValid) {
                    color = invalidColor;
                } else {
                    color = this.color;
                }
            } else {
               color = disabledColor;
            }

            caretColor = color;

            if (glyphVector != null) {
                graphics.setFont(font);

                if (selection == null) {
                    // Paint the text
                    graphics.setColor(color);
                    graphics.drawGlyphVector(glyphVector, padding.left - scrollLeft + 1,
                        (height - textHeight) / 2 + ascent);
                } else {
                    // Paint the unselected text
                    Area unselectedArea = graphicsFactory.newArea();
                    unselectedArea.add(graphicsFactory.newArea(0, 0, width, height));
                    Bounds bounds = selection.getBounds();
                    unselectedArea.subtract(graphicsFactory.newArea(bounds.x, bounds.y,bounds.width,bounds.height));

                    Graphics2D textGraphics = graphics.create();
                    textGraphics.setColor(color);
                    textGraphics.clip(unselectedArea);
                    textGraphics.drawGlyphVector(glyphVector, padding.left - scrollLeft + 1, (height - textHeight) / 2 + ascent);
                    textGraphics.dispose();

                    // Paint the selection
                    Color selectionColor;
                    Color selectionBackgroundColor;

                    if (textInput.isFocused() && textInput.isEditable()) {
                        selectionColor = this.selectionColor;
                        selectionBackgroundColor = this.selectionBackgroundColor;
                    } else {
                        selectionColor = inactiveSelectionColor;
                        selectionBackgroundColor = inactiveSelectionBackgroundColor;
                    }

                    graphics.setColor(selectionBackgroundColor);
                    graphics.fill(selection);

                    Graphics2D selectedTextGraphics = graphics.create();
                    selectedTextGraphics.setColor(selectionColor);
                    selectedTextGraphics.clip(selection);
                    selectedTextGraphics.drawGlyphVector(glyphVector, padding.left - scrollLeft + 1,
                        (height - textHeight) / 2 + ascent);
                    selectedTextGraphics.dispose();
                }
            }
        }

        // Paint the caret
        if (selection == null
            && caretOn
            && textInput.isFocused()) {
            graphics.setColor(caretColor);
            graphics.fill(caret);
        }

        // Paint the border
        graphics.setColor(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
    }

    public int getInsertionPoint(int x) {
        int offset = -1;

        if (glyphVector == null) {
            offset = 0;
        } else {
            // Translate to glyph coordinates
            x -= (padding.left - scrollLeft + 1);

            Bounds textBounds = glyphVector.getLogicalBounds();

            if (x < 0) {
                offset = 0;
            } else if (x > textBounds.getWidth()) {
                offset = glyphVector.getNumGlyphs();
            } else {
                int n = glyphVector.getNumGlyphs();
                int i = 0;
                while (i < n) {
                    Shape glyphBounds = glyphVector.getGlyphLogicalBounds(i);
                    Bounds glyphBounds2D = glyphBounds.getBounds();

                    float glyphX = glyphBounds2D.x;
                    float glyphWidth = glyphBounds2D.width;
                    if (x >= glyphX
                        && x < glyphX + glyphWidth) {

                        if (x - glyphX > glyphWidth / 2) {
                            // The user clicked on the right half of the character; select
                            // the next character
                            i++;
                        }

                        offset = i;
                        break;
                    }

                    i++;
                }
            }
        }

        return offset;
    }

    public Bounds getCharacterBounds(int index) {
        Bounds characterBounds = null;

        if (glyphVector != null) {
            int x, width;
            if (index < glyphVector.getNumGlyphs()) {
                Shape glyphBounds = glyphVector.getGlyphLogicalBounds(index);
                Bounds glyphBounds2D = glyphBounds.getBounds();

                x = (int)Math.floor(glyphBounds2D.x);
                width = (int)Math.ceil(glyphBounds2D.width);
            } else {
                // This is the terminator character
                Bounds glyphVectorBounds = glyphVector.getLogicalBounds();
                x = (int)Math.floor(glyphVectorBounds.width);
                width = 0;
            }

            characterBounds = new Bounds(x + padding.left - scrollLeft + 1, padding.top + 1,
                width, getHeight() - (padding.top + padding.bottom + 2));
        }

        return characterBounds;
    }

    private void setScrollLeft(int scrollLeft) {
        this.scrollLeft = scrollLeft;
        updateSelection();
        repaintComponent();
    }

    private void scrollCharacterToVisible(int offset) {
        int width = getWidth();
        Bounds characterBounds = getCharacterBounds(offset);

        if (characterBounds != null) {
            int glyphX = characterBounds.x - (padding.left + 1) + scrollLeft;

            if (characterBounds.x < padding.left + 1) {
                setScrollLeft(glyphX);
            } else if (characterBounds.x + characterBounds.width > width - (padding.right + 1)) {
                setScrollLeft(glyphX + (padding.left + padding.right + 2) + characterBounds.width - width);
            }
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

        int missingGlyphCode = font.getMissingGlyphCode();
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();

        GlyphVector missingGlyphVector = font.createGlyphVector(fontRenderContext,
            new int[] {missingGlyphCode});
        Bounds textBounds = missingGlyphVector.getLogicalBounds();

        Bounds maxCharBounds = font.getMaxCharBounds(fontRenderContext);
        averageCharacterSize = new Dimensions((int)Math.ceil(textBounds.width),
            (int)Math.ceil(maxCharBounds.height));

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

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getPromptColor() {
        return promptColor;
    }

    public void setPromptColor(Color promptColor) {
        if (promptColor == null) {
            throw new IllegalArgumentException("promptColor is null.");
        }

        this.promptColor = promptColor;
        repaintComponent();
    }

    public final void setPromptColor(String promptColor) {
        if (promptColor == null) {
            throw new IllegalArgumentException("promptColor is null.");
        }

        setPromptColor(GraphicsUtilities.decodeColor(promptColor));
    }

    public final void setPromptColor(int promptColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setPromptColor(theme.getColor(promptColor));
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

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        bevelColor = TerraTheme.darken(backgroundColor);
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public Color getInvalidColor() {
        return invalidColor;
    }

    public void setInvalidColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.invalidColor = color;
        repaintComponent();
    }

    public final void setInvalidColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setInvalidColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setInvalidColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInvalidColor(theme.getColor(color));
    }

    public Color getInvalidBackgroundColor() {
        return invalidBackgroundColor;
    }

    public void setInvalidBackgroundColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.invalidBackgroundColor = color;
        invalidBevelColor = TerraTheme.darken(color);
        repaintComponent();
    }

    public final void setInvalidBackgroundColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("invalidBackgroundColor is null.");
        }

        setInvalidBackgroundColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setInvalidBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInvalidBackgroundColor(theme.getColor(color));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        disabledBevelColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(GraphicsUtilities.decodeColor(disabledBackgroundColor));
    }

    public final void setDisabledBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBackgroundColor(theme.getColor(color));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public final void setBorderColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBorderColor(theme.getColor(color));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(GraphicsUtilities.decodeColor(disabledBorderColor));
    }

    public final void setDisabledBorderColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledBorderColor(theme.getColor(color));
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

    public final void setSelectionColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionColor(theme.getColor(color));
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

    public final void setSelectionBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(color));
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

    public final void setInactiveSelectionColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(color));
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

    public final void setInactiveSelectionBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(color));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            TextInput textInput = (TextInput)getComponent();
            int width = getWidth();

            if (x >= 0
                && x < width) {
                // Stop the scroll selection timer
                if (scheduledScrollSelectionCallback != null) {
                    scheduledScrollSelectionCallback.cancel();
                    scheduledScrollSelectionCallback = null;
                }

                scrollDirection = null;

                int offset = getInsertionPoint(x);

                if (offset != -1) {
                    // Select the range
                    if (offset > anchor) {
                        textInput.setSelection(anchor, offset - anchor);
                    } else {
                        textInput.setSelection(offset, anchor - offset);
                    }
                }
            } else {
                if (scheduledScrollSelectionCallback == null) {
                    scrollDirection = (x < 0) ? FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;

                    scheduledScrollSelectionCallback =
                        ApplicationContext.scheduleRecurringCallback(scrollSelectionCallback,
                            SCROLL_RATE);

                    // Run the callback once now to scroll the selection immediately
                    scrollSelectionCallback.run();
                }
            }
        } else {
            if (Mouse.isPressed(Mouse.Button.LEFT)
                && Mouse.getCapturer() == null
                && anchor != -1) {
                // Capture the mouse so we can select text
                Mouse.capture(component);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            TextInput textInput = (TextInput)getComponent();

            anchor = getInsertionPoint(x);

            if (anchor != -1) {
                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    // Select the range
                    int selectionStart = textInput.getSelectionStart();

                    if (anchor > selectionStart) {
                        textInput.setSelection(selectionStart, anchor - selectionStart);
                    } else {
                        textInput.setSelection(anchor, selectionStart - anchor);
                    }
                } else {
                    // Move the caret to the insertion point
                    textInput.setSelection(anchor, 0);
                    consumed = true;
                }
            }


            // Set focus to the text input
            textInput.requestFocus();
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (Mouse.getCapturer() == component) {
            // Stop the scroll selection timer
            if (scheduledScrollSelectionCallback != null) {
                scheduledScrollSelectionCallback.cancel();
                scheduledScrollSelectionCallback = null;
            }

            Mouse.release();
        }

        anchor = -1;

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        if (button == Mouse.Button.LEFT
            && count > 1) {
            TextInput textInput = (TextInput)getComponent();
            textInput.selectAll();
        }

        return super.mouseClick(component, button, x, y, count);
    }

    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);
        TextInput textInput = (TextInput)getComponent();

        if (textInput.isEditable()) {
            // Ignore characters in the control range and the ASCII delete
            // character as well as meta key presses
            if (character > 0x1F
                && character != 0x7F
                && !Keyboard.isPressed(Keyboard.Modifier.META)) {
                int selectionLength = textInput.getSelectionLength();

                if (textInput.getCharacterCount() - selectionLength + 1 > textInput.getMaximumLength()) {
                    Platform.getInstalled().getSoundSystem().beep();
                } else {
                    // NOTE We explicitly call getSelectionStart() twice here in case the remove
                    // event is vetoed
                    textInput.removeText(textInput.getSelectionStart(), selectionLength);
                    textInput.insertText(Character.toString(character), textInput.getSelectionStart());
                }
            }
        }

        return consumed;
    }

    /**
     * {@link Keyboard.Key#DELETE DELETE} Delete the character after the caret or
     * the entire selection if there is one.<br>
     * {@link Keyboard.Key#BACKSPACE BACKSPACE} Delete the character before the
     * caret or the entire selection if there is one.<p>
     * {@link Keyboard.Key#HOME HOME} Move the caret to the beginning of the text.
     * <br>
     * {@link Keyboard.Key#LEFT LEFT} + {@link Modifier#META META} Move the caret
     * to the beginning of the text.<p>
     * {@link Keyboard.Key#HOME HOME} + {@link Modifier#SHIFT SHIFT} Select from
     * the caret to the beginning of the text.<br>
     * {@link Keyboard.Key#LEFT LEFT} + {@link Modifier#META META} +
     * {@link Modifier#SHIFT SHIFT} Select from the caret to the beginning of
     * the text.<p>
     * {@link Keyboard.Key#END END} Move the caret to the end of the text.<br>
     * {@link Keyboard.Key#RIGHT RIGHT} + {@link Modifier#META META} Move the caret
     * to the end of the text.<p>
     * {@link Keyboard.Key#END END} + {@link Modifier#SHIFT SHIFT} Select from the
     * caret to the end of the text.<br>
     * {@link Keyboard.Key#RIGHT RIGHT} + {@link Modifier#META META} +
     * {@link Modifier#SHIFT SHIFT} Select from the caret to the end of the
     * text.<p>
     * {@link Keyboard.Key#LEFT LEFT} Clear the selection and move the caret back
     * by one character.<br>
     * {@link Keyboard.Key#LEFT LEFT} + {@link Modifier#SHIFT SHIFT} Add the
     * previous character to the selection.<br>
     * {@link Keyboard.Key#LEFT LEFT} + {@link Modifier#CTRL CTRL} Clear the
     * selection and move the caret to the beginning of the text.<br>
     * {@link Keyboard.Key#LEFT LEFT} + {@link Modifier#CTRL CTRL} +
     * {@link Modifier#SHIFT SHIFT} Add all preceding text to the selection.
     * <p>
     * {@link Keyboard.Key#RIGHT RIGHT} Clear the selection and move the caret
     * forward by one character.<br>
     * {@link Keyboard.Key#RIGHT RIGHT} + {@link Modifier#SHIFT SHIFT} Add the next
     * character to the selection.<br>
     * {@link Keyboard.Key#RIGHT RIGHT} + {@link Modifier#CTRL CTRL} Clear the
     * selection and move the caret to the end of the text.<br>
     * {@link Keyboard.Key#RIGHT RIGHT} + {@link Modifier#CTRL CTRL} +
     * {@link Modifier#SHIFT SHIFT} Add all subsequent text to the selection.
     * <p>
     * CommandModifier + {@link Keyboard.Key#A A} Select all.<br>
     * CommandModifier + {@link Keyboard.Key#X X} Cut selection to clipboard (if
     * not a password TextInput).<br>
     * CommandModifier + {@link Keyboard.Key#C C} Copy selection to clipboard (if
     * not a password TextInput).<br>
     * CommandModifier + {@link Keyboard.Key#V V} Paste from clipboard.<br>
     * CommandModifier + {@link Keyboard.Key#Z Z} Undo.
     *
     * @see org.apache.pivot.ui.awt.JavaAwtPlatform#getCommandModifier()
     */
    @Override
    public boolean keyPressed(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        TextInput textInput = (TextInput)getComponent();
        Keyboard.Modifier commandModifier = Platform.getInstalled().getCommandModifier();
        Keyboard.Modifier wordNavigationModifier = Platform.getInstalled().getWordNavigationModifier();

        if (keyCode == Keyboard.Key.DELETE && textInput.isEditable()) {
            int index = textInput.getSelectionStart();

            if (index < textInput.getCharacterCount()) {
                int count = Math.max(textInput.getSelectionLength(), 1);
                textInput.removeText(index, count);

                consumed = true;
            }
        } else if (keyCode == Keyboard.Key.BACKSPACE && textInput.isEditable()) {
            int index = textInput.getSelectionStart();
            int count = textInput.getSelectionLength();

            if (count == 0
                && index > 0) {
                textInput.removeText(index - 1, 1);
                consumed = true;
            } else {
                textInput.removeText(index, count);
                consumed = true;
            }
        } else if (keyCode == Keyboard.Key.HOME
            || (keyCode == Keyboard.Key.LEFT
                && Keyboard.isPressed(Keyboard.Modifier.META))) {
            // Move the caret to the beginning of the text
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                textInput.setSelection(0, textInput.getSelectionStart());
            } else {
                textInput.setSelection(0, 0);
            }

            scrollCharacterToVisible(0);

            consumed = true;
        } else if (keyCode == Keyboard.Key.END
            || (keyCode == Keyboard.Key.RIGHT
                && Keyboard.isPressed(Keyboard.Modifier.META))) {
            // Move the caret to the end of the text
            int n = textInput.getCharacterCount();

            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                int selectionStart = textInput.getSelectionStart();
                textInput.setSelection(selectionStart, n - selectionStart);
            } else {
                textInput.setSelection(n, 0);
            }

            scrollCharacterToVisible(n);

            consumed = true;
        } else if (keyCode == Keyboard.Key.LEFT) {
            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (Keyboard.isPressed(wordNavigationModifier)) {
                // Move the caret to the start of the next word to the left
                if (selectionStart > 0) {
                    // Skip over any space immediately to the left
                    int index = selectionStart;
                    while (index > 0
                        && Character.isWhitespace(textInput.getCharacterAt(index - 1))) {
                        index--;
                    }

                    // Skip over any word-letters to the left
                    while (index > 0
                        && !Character.isWhitespace(textInput.getCharacterAt(index - 1))) {
                        index--;
                    }

                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        selectionLength += selectionStart - index;
                    } else {
                        selectionLength = 0;
                    }

                    selectionStart = index;
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Add the previous character to the selection
                if (selectionStart > 0) {
                    selectionStart--;
                    selectionLength++;
                }
            } else {
                // Move the caret back by one character
                if (selectionLength == 0
                    && selectionStart > 0) {
                    selectionStart--;
                }

                // Clear the selection
                selectionLength = 0;
            }

            if (selectionStart >= 0) {
                textInput.setSelection(selectionStart, selectionLength);
                scrollCharacterToVisible(selectionStart);

                consumed = true;
            }
        } else if (keyCode == Keyboard.Key.RIGHT) {
            int selectionStart = textInput.getSelectionStart();
            int selectionLength = textInput.getSelectionLength();

            if (Keyboard.isPressed(wordNavigationModifier)) {
                // Move the caret to the start of the next word to the right
                if (selectionStart < textInput.getCharacterCount()) {
                    int index = selectionStart + selectionLength;

                    // Skip over any space immediately to the right
                    while (index < textInput.getCharacterCount()
                        && Character.isWhitespace(textInput.getCharacterAt(index))) {
                        index++;
                    }

                    // Skip over any word-letters to the right
                    while (index < textInput.getCharacterCount()
                        && !Character.isWhitespace(textInput.getCharacterAt(index))) {
                        index++;
                    }

                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                        selectionLength = index - selectionStart;
                    } else {
                        selectionStart = index;
                        selectionLength = 0;
                    }
                }
            } else if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Add the next character to the selection
                selectionLength++;
            } else {
                // Move the caret forward by one character
                if (selectionLength == 0) {
                    selectionStart++;
                } else {
                    selectionStart += selectionLength;
                }

                // Clear the selection
                selectionLength = 0;
            }

            if (selectionStart + selectionLength <= textInput.getCharacterCount()) {
                textInput.setSelection(selectionStart, selectionLength);
                scrollCharacterToVisible(selectionStart + selectionLength);

                consumed = true;
            }
        } else if (Keyboard.isPressed(commandModifier)) {
            if (keyCode == Keyboard.Key.A) {
                textInput.setSelection(0, textInput.getCharacterCount());
                consumed = true;
            } else if (keyCode == Keyboard.Key.X && textInput.isEditable()) {
                if (textInput.isPassword()) {
                    Platform.getInstalled().getSoundSystem().beep();
                } else {
                    textInput.cut();
                }

                consumed = true;
            } else if (keyCode == Keyboard.Key.C) {
                if (textInput.isPassword()) {
                    Platform.getInstalled().getSoundSystem().beep();
                } else {
                    textInput.copy();
                }

                consumed = true;
            } else if (keyCode == Keyboard.Key.V && textInput.isEditable()) {
                textInput.paste();
                consumed = true;
            } else if (keyCode == Keyboard.Key.Z && textInput.isEditable()) {
                if (!Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                    textInput.undo();
                }

                consumed = true;
            }
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        TextInput textInput = (TextInput)component;
        Window window = textInput.getWindow();

        if (component.isFocused()) {
            // If focus was permanently transferred within this window,
            // select all
            if (obverseComponent == null
                || obverseComponent.getWindow() == window) {
                if (Mouse.getCapturer() != component) {
                    textInput.selectAll();
                }
            }

            if (textInput.getSelectionLength() == 0) {
                int selectionStart = textInput.getSelectionStart();
                if (selectionStart < textInput.getCharacterCount()) {
                    scrollCharacterToVisible(selectionStart);
                }

                showCaret(true);
            } else {
                showCaret(false);
            }
        } else {
            // If focus was permanently transferred within this window,
            // clear the selection
            if (obverseComponent == null
                || obverseComponent.getWindow() == window) {
                textInput.clearSelection();
            }

            showCaret(false);
        }

        repaintComponent();
    }

    // Text input events
    @Override
    public void textSizeChanged(TextInput textInput, int previousTextSize) {
        invalidateComponent();
    }

    @Override
    public void maximumLengthChanged(TextInput textInput, int previousMaximumLength) {
        // No-op
    }

    @Override
    public void passwordChanged(TextInput textInput) {
        layout();
        repaintComponent();
    }

    @Override
    public void promptChanged(TextInput textInput, String previousPrompt) {
        repaintComponent();
    }

    @Override
    public void textValidatorChanged(TextInput textInput, Validator previousValidator) {
        repaintComponent();
    }

    @Override
    public void strictValidationChanged(TextInput textInput) {
        // No-op
    }

    @Override
    public void textValidChanged(TextInput textInput) {
        repaintComponent();
    }

    // Text input character events
    @Override
    public Vote previewInsertText(TextInput textInput, CharSequence text, int index) {
        Vote vote = Vote.APPROVE;

        if (textInput.isStrictValidation()) {
            Validator validator = textInput.getValidator();
            if (validator != null) {
                StringBuilder textBuilder = new StringBuilder();
                textBuilder.append(textInput.getText(0, index));
                textBuilder.append(text);
                textBuilder.append(textInput.getText(index, textInput.getCharacterCount()));

                if (!validator.isValid(textBuilder.toString())) {
                    vote = Vote.DENY;
                    Platform.getInstalled().getSoundSystem().beep();
                }
            }
        }

        return vote;
    }

    @Override
    public void insertTextVetoed(TextInput textInput, Vote reason) {
        // No-op
    }

    @Override
    public void textInserted(TextInput textInput, int index, int count) {
        // No-op
    }

    @Override
    public Vote previewRemoveText(TextInput textInput, int index, int count) {
        Vote vote = Vote.APPROVE;

        if (textInput.isStrictValidation()) {
            Validator validator = textInput.getValidator();
            if (validator != null) {
                StringBuilder textBuilder = new StringBuilder();
                textBuilder.append(textInput.getText(0, index));
                textBuilder.append(textInput.getText(index + count, textInput.getCharacterCount()));

                if (!validator.isValid(textBuilder.toString())) {
                    vote = Vote.DENY;
                    Platform.getInstalled().getSoundSystem().beep();
                }
            }
        }

        return vote;
    }

    @Override
    public void removeTextVetoed(TextInput textInput, Vote reason) {
        // No-op
    }

    @Override
    public void textRemoved(TextInput textInput, int index, int count) {
        // No-op
    }

    @Override
    public void textChanged(TextInput textInput) {
        layout();
        repaintComponent();
    }

    @Override
    public void editableChanged(TextInput textInput) {
        repaintComponent();
    }

    // Text input selection events
    @Override
    public void selectionChanged(TextInput textInput, int previousSelectionStart,
        int previousSelectionLength) {
        // If the text input is valid, repaint the selection state; otherwise,
        // the selection will be updated in layout()
        if (textInput.isValid()) {
            // Repaint any previous caret bounds
            if (caret != null) {
                Bounds bounds = caret.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            // Repaint any previous selection bounds
            if (selection != null) {
                Bounds bounds = selection.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            if (textInput.getSelectionLength() == 0) {
                updateSelection();
                showCaret(textInput.isFocused());
            } else {
                updateSelection();
                showCaret(false);
                Bounds bounds = selection.getBounds();
                textInput.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }

    private void updateSelection() {
        TextInput textInput = (TextInput)getComponent();

        int height = getHeight();

        int selectionStart = textInput.getSelectionStart();
        int selectionLength = textInput.getSelectionLength();

        int n = textInput.getCharacterCount();

        Bounds leadingSelectionBounds;
        if (selectionStart < n) {
            leadingSelectionBounds = getCharacterBounds(selectionStart);
        } else {
            // The insertion point is after the last character
            int x;
            if (n == 0) {
                x = padding.left - scrollLeft + 1;
            } else {
                Bounds textBounds = glyphVector.getLogicalBounds();
                x = (int)Math.ceil(textBounds.width) + (padding.left - scrollLeft + 1);
            }

            int y = padding.top + 1;

            leadingSelectionBounds = new Bounds(x, y, 0, height - (padding.top + padding.bottom + 2));
        }

        GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();
        caret = graphicsFactory.newRectangle( leadingSelectionBounds.x, leadingSelectionBounds.y, 1, leadingSelectionBounds.height);

        if (selectionLength > 0) {
            Bounds trailingSelectionBounds = getCharacterBounds(selectionStart
                + selectionLength - 1);
            selection = graphicsFactory.newRectangle(leadingSelectionBounds.x, leadingSelectionBounds.y,
                trailingSelectionBounds.x + trailingSelectionBounds.width - leadingSelectionBounds.x,
                trailingSelectionBounds.y + trailingSelectionBounds.height - leadingSelectionBounds.y);
        } else {
            selection = null;
        }
    }

    public void showCaret(boolean show) {
        if (scheduledBlinkCaretCallback != null) {
            scheduledBlinkCaretCallback.cancel();
        }

        if (show) {
            caretOn = true;
            scheduledBlinkCaretCallback =
                ApplicationContext.scheduleRecurringCallback( blinkCaretCallback,
                                                              Platform.getInstalled().getCursorBlinkRate() );

            // Run the callback once now to show the cursor immediately
            blinkCaretCallback.run();
        } else {
            scheduledBlinkCaretCallback = null;
        }
    }
}
