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

import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.BasicStroke;
import org.apache.pivot.wtk.graphics.Color;
import org.apache.pivot.wtk.graphics.ColorFactory;
import java.text.StringCharacterIterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LabelListener;
import org.apache.pivot.wtk.TextDecoration;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.PrintGraphics;
import org.apache.pivot.wtk.graphics.Transparency;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;
import org.apache.pivot.wtk.graphics.font.LineMetrics;
import org.apache.pivot.wtk.graphics.geom.Shape;

/**
 * Label skin.
 * <p>
 * TODO Add a showEllipsis style.
 */
public class LabelSkin extends ComponentSkin implements LabelListener {
    private Font font;
    private Color color;
    private Color backgroundColor;
    private TextDecoration textDecoration;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private Insets padding;
    private boolean wrapText;

    private ArrayList<GlyphVector> glyphVectors = null;
    private float textHeight = 0;

    public LabelSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = ColorFactory.BLACK;
        backgroundColor = null;
        textDecoration = null;
        horizontalAlignment = HorizontalAlignment.LEFT;
        verticalAlignment = VerticalAlignment.TOP;
        padding = Insets.NONE;
        wrapText = false;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Label label = (Label)getComponent();
        label.getLabelListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        Label label = (Label)getComponent();
        String text = label.getText();

        int preferredWidth;
        if (text != null
            && text.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
            Bounds stringBounds = font.getStringBounds(text, fontRenderContext);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth());
        } else {
            preferredWidth = 0;
        }

        preferredWidth += (padding.left + padding.right);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Label label = (Label)getComponent();
        String text = label.getText();

        float preferredHeight;
        if (text != null) {
            FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            float lineHeight = lm.getHeight();

            preferredHeight = lineHeight;

            int n = text.length();
            if (n > 0
                && wrapText
                && width != -1) {
                // Adjust width for padding
                width -= (padding.left + padding.right);

                float lineWidth = 0;
                int lastWhitespaceIndex = -1;

                int i = 0;
                while (i < n) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c)) {
                        lastWhitespaceIndex = i;
                    }

                    Bounds characterBounds = font.getStringBounds(text, i, i + 1, fontRenderContext);
                    lineWidth += characterBounds.width;

                    if (lineWidth > width
                        && lastWhitespaceIndex != -1) {
                        i = lastWhitespaceIndex;

                        lineWidth = 0;
                        lastWhitespaceIndex = -1;

                        preferredHeight += lineHeight;
                    }

                    i++;
                }
            }
        } else {
            preferredHeight = 0;
        }

        preferredHeight += (padding.top + padding.bottom);

        return (int)Math.ceil(preferredHeight);
    }

    @Override
    public Dimensions getPreferredSize() {
        Label label = (Label)getComponent();
        String text = label.getText();

        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();

        int preferredWidth;
        if (text != null
            && text.length() > 0) {
            Bounds stringBounds = font.getStringBounds(text, fontRenderContext);
            preferredWidth = (int)Math.ceil(stringBounds.width);
        } else {
            preferredWidth = 0;
        }

        preferredWidth += (padding.left + padding.right);

        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        int preferredHeight = (int)Math.ceil(lm.getHeight()) + (padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();

        float textHeight;
        if (wrapText) {
            textHeight = Math.max(getPreferredHeight(width) - (padding.top + padding.bottom), 0);
        } else {
            textHeight = (int)Math.ceil(lm.getHeight());
        }

        int baseline = -1;
        switch (verticalAlignment) {
            case TOP: {
                baseline = Math.round(padding.top + ascent);
                break;
            }

            case CENTER: {
                baseline = Math.round((height - textHeight) / 2 + ascent);
                break;
            }

            case BOTTOM: {
                baseline = Math.round(height - (textHeight + padding.bottom) + ascent);
                break;
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        Label label = (Label)getComponent();
        String text = label.getText();

        glyphVectors = new ArrayList<GlyphVector>();
        textHeight = 0;

        if (text != null) {
            int n = text.length();

            if (n > 0) {
                FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();

                if (wrapText) {
                    int width = getWidth() - (padding.left + padding.right);

                    int i = 0;
                    int start = 0;
                    float lineWidth = 0;
                    int lastWhitespaceIndex = -1;

                    // NOTE We use a character iterator here only because it is the most
                    // efficient way to measure the character bounds (as of Java 6, the version
                    // of Font#getStringBounds() that takes a String performs a string copy,
                    // whereas the version that takes a character iterator does not)
                    StringCharacterIterator ci = new StringCharacterIterator(text);
                    while (i < n) {
                        char c = text.charAt(i);
                        if (Character.isWhitespace(c)) {
                            lastWhitespaceIndex = i;
                        }

                        Bounds characterBounds = font.getStringBounds(ci, i, i + 1, fontRenderContext);
                        lineWidth += characterBounds.width;

                        if (lineWidth > width
                            && lastWhitespaceIndex != -1) {
                            appendLine(text, start, lastWhitespaceIndex, fontRenderContext);

                            i = lastWhitespaceIndex;
                            start = i + 1;
                            lineWidth = 0;
                            lastWhitespaceIndex = -1;
                        }

                        i++;
                    }

                    appendLine(text, start, i, fontRenderContext);
                } else {
                    appendLine(text, 0, text.length(), fontRenderContext);
                }
            }
        }
    }

    private void appendLine(String text, int start, int end, FontRenderContext fontRenderContext) {
        StringCharacterIterator line = new StringCharacterIterator(text, start, end, start);
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
        glyphVectors.add(glyphVector);

        Bounds textBounds = glyphVector.getLogicalBounds();
        textHeight += textBounds.getHeight();
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        // Draw the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Draw the text
        if (glyphVectors.getLength() > 0) {
            graphics.setFont(font);
            graphics.setPaint(color);

            FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
            LineMetrics lm = font.getLineMetrics("", fontRenderContext);
            float ascent = lm.getAscent();
            float lineHeight = lm.getHeight();

            float y = 0;
            switch (verticalAlignment) {
                case TOP: {
                    y = padding.top;
                    break;
                }

                case BOTTOM: {
                    y = height - (textHeight + padding.bottom);
                    break;
                }

                case CENTER: {
                    y = (height - textHeight) / 2;
                    break;
                }
            }

            for (int i = 0, n = glyphVectors.getLength(); i < n; i++) {
                GlyphVector glyphVector = glyphVectors.get(i);

                Bounds textBounds = glyphVector.getLogicalBounds();
                float lineWidth = (float)textBounds.width;

                float x = 0;
                switch (horizontalAlignment) {
                    case LEFT: {
                        x = padding.left;
                        break;
                    }

                    case RIGHT: {
                        x = width - (lineWidth + padding.right);
                        break;
                    }

                    case CENTER: {
                        x = (width - lineWidth) / 2;
                        break;
                    }
                }

                if (graphics instanceof PrintGraphics ) {
                    /* Work-around for printing problem in applets */
                    Label label = (Label)getComponent();
                    String text = label.getText();
                    if (text != null && text.length() > 0) {
                        graphics.drawString(text, x, y + ascent);
                    }
                }
                else {
                    graphics.drawGlyphVector(glyphVector, x, y + ascent);
                }

                GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();
                // Draw the text decoration
                if (textDecoration != null) {
                    graphics.setStroke(graphicsFactory.getStrokeFactory().createBasicStroke());

                    float offset = 0;

                    switch (textDecoration) {
                    case UNDERLINE: {
                            offset = y + ascent + 2;
                            break;
                        }

                    case STRIKETHROUGH: {
                            offset = y + lineHeight / 2 + 1;
                            break;
                        }
                    }

                    Shape line = graphicsFactory.newLine( x, offset, x + lineWidth, offset);
                    graphics.draw( line );
                }

                y += textBounds.getHeight();
            }
        }
    }

    /**
     * @return
     * <tt>false</tt>; labels are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
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

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
        repaintComponent();
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
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

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    // Label events
    @Override
    public void textChanged(Label label, String previousText) {
        invalidateComponent();
    }
}
