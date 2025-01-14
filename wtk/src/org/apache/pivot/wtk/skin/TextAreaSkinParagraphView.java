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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.geom.Area;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.geom.Shape;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;
import org.apache.pivot.wtk.graphics.font.LineMetrics;

class TextAreaSkinParagraphView
    implements TextArea.ParagraphListener {
    private static class Row {
        public final GlyphVector glyphVector;
        public final int offset;

        public Row(GlyphVector glyphVector, int offset) {
            this.glyphVector = glyphVector;
            this.offset = offset;
        }
    }

    private TextAreaSkin textAreaSkin;
    private TextArea.Paragraph paragraph;

    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;

    private int breakWidth = Integer.MAX_VALUE;

    private int rowOffset = 0;

    private boolean valid = false;
    private ArrayList<Row> rows = new ArrayList<Row>();

    private static final int PARAGRAPH_TERMINATOR_WIDTH = 2;

    public TextAreaSkinParagraphView(TextAreaSkin textAreaSkin, TextArea.Paragraph paragraph) {
        this.textAreaSkin = textAreaSkin;
        this.paragraph = paragraph;
    }

    public TextArea.Paragraph getParagraph() {
        return paragraph;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public void setRowOffset(int rowOffset) {
        this.rowOffset = rowOffset;
    }

    public int getWidth() {
        validate();
        return (int)Math.ceil(width);
    }

    public int getHeight() {
        validate();
        return (int)Math.ceil(height);
    }

    public int getBreakWidth() {
        return breakWidth;
    }

    public void setBreakWidth(int breakWidth) {
        int previousBreakWidth = this.breakWidth;
        if (previousBreakWidth != breakWidth) {
            this.breakWidth = breakWidth;
            invalidate();
        }
    }

    public void paint(Graphics2D graphics) {
        TextArea textArea = (TextArea)textAreaSkin.getComponent();

        int selectionStart = textArea.getSelectionStart();
        int selectionLength = textArea.getSelectionLength();
        Span selectionRange = new Span(selectionStart, selectionStart + selectionLength - 1);

        int paragraphOffset = paragraph.getOffset();
        Span characterRange = new Span(paragraphOffset, paragraphOffset
            + paragraph.getCharacters().length() - 1);
        GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();

        if (selectionLength > 0
            && characterRange.intersects(selectionRange)) {
            boolean focused = textArea.isFocused();
            boolean editable = textArea.isEditable();

            // Determine the selected and unselected areas
            Area selection = textAreaSkin.getSelection();
            AffineTransform transform = graphicsFactory.getAffineTransformFactory().newTranslateTransform( -x, -y );
            Area selectedArea = selection.createTransformedArea( transform );
            Area unselectedArea = graphicsFactory.newArea();
            unselectedArea.add(graphicsFactory.newArea(0, 0, width, height));
            Bounds selectedAreaBounds = selectedArea.getBounds();
            unselectedArea.subtract(graphicsFactory.newArea( selectedAreaBounds.x, selectedAreaBounds.y,selectedAreaBounds.width, selectedAreaBounds.height ));

            // Paint the unselected text
            Graphics2D unselectedGraphics = (Graphics2D)graphics.create();
            unselectedGraphics.clip(unselectedArea);
            paint(unselectedGraphics, focused, editable, false);
            unselectedGraphics.dispose();

            // Paint the selected text
            Graphics2D selectedGraphics = (Graphics2D)graphics.create();
            selectedGraphics.clip(selectedArea);
            paint(selectedGraphics, focused, editable, true);
            selectedGraphics.dispose();
        } else {
            paint(graphics, textArea.isFocused(), textArea.isEditable(), false);
        }
    }

    private void paint(Graphics2D graphics, boolean focused, boolean editable, boolean selected) {
        Font font = textAreaSkin.getFont();
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float ascent = lm.getAscent();
        int rowHeight = (int) (ascent + lm.getDescent());

        Bounds clipBounds = graphics.getClipBounds();

        int rowY = 0;
        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Row row = rows.get(i);

            Bounds textBounds = row.glyphVector.getLogicalBounds();
            int rowWidth = textBounds.width;
            if (clipBounds.intersects(new Bounds(0, rowY, rowWidth, rowHeight))) {
                if (selected) {
                    graphics.setPaint(focused && editable ?
                        textAreaSkin.getSelectionColor() : textAreaSkin.getInactiveSelectionColor());
                } else {
                    graphics.setPaint(textAreaSkin.getColor());
                }

                graphics.drawGlyphVector(row.glyphVector, 0, rowY + ascent);
            }

            rowY += textBounds.height;
        }
    }

    public void invalidate() {
        valid = false;
    }

    public void validate() {
        // TODO Validate from known invalid offset rather than 0, so we don't need to
        // recalculate all glyph vectors
        if (!valid) {
            rows = new ArrayList<Row>();
            width = 0;
            height = 0;

            // Re-layout glyphs and recalculate size
            Font font = textAreaSkin.getFont();
            FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();

            CharSequence characters = paragraph.getCharacters();
            int n = characters.length();

            int i = 0;
            int start = 0;
            float rowWidth = 0;
            int lastWhitespaceIndex = -1;

            // NOTE We use a character iterator here only because it is the most
            // efficient way to measure the character bounds (as of Java 6, the version
            // of Font#getStringBounds() that takes a String performs a string copy,
            // whereas the version that takes a character iterator does not)
            CharSequenceCharacterIterator ci = new CharSequenceCharacterIterator(characters);
            while (i < n) {
                char c = characters.charAt(i);
                if (Character.isWhitespace(c)) {
                    lastWhitespaceIndex = i;
                }

                Bounds characterBounds = font.getStringBounds(ci, i, i + 1,
                    fontRenderContext);
                rowWidth += characterBounds.width;

                if (rowWidth > breakWidth) {
                    if (lastWhitespaceIndex == -1) {
                        if (start == i) {
                            appendLine(characters, start, start + 1, font, fontRenderContext);
                        } else {
                            appendLine(characters, start, i, font, fontRenderContext);
                            i--;
                        }
                    } else {
                        appendLine(characters, start, lastWhitespaceIndex + 1,
                            font, fontRenderContext);
                        i = lastWhitespaceIndex;
                    }

                    start = i + 1;

                    rowWidth = 0;
                    lastWhitespaceIndex = -1;
                }

                i++;
            }

            appendLine(characters, start, i, font, fontRenderContext);

            width = Math.max(width, PARAGRAPH_TERMINATOR_WIDTH);
        }

        valid = true;
    }

    private void appendLine(CharSequence characters, int start, int end,
        Font font, FontRenderContext fontRenderContext) {
        CharSequenceCharacterIterator line = new CharSequenceCharacterIterator(characters,
            start, end, start);
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
        rows.add(new Row(glyphVector, start));

        Bounds textBounds = glyphVector.getLogicalBounds();
        width = Math.max( width, textBounds.width);
        height += textBounds.height;
    }

    public int getInsertionPoint(int x, int y) {
        Font font = textAreaSkin.getFont();
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float rowHeight = lm.getAscent() + lm.getDescent();

        int i = (int)Math.floor(y / rowHeight);

        int n = rows.getLength();
        return (i < 0
            || i >= n) ? -1 : getRowInsertionPoint(i, x);
    }

    public int getNextInsertionPoint(int x, int from, TextArea.ScrollDirection direction) {
        // Identify the row that contains the from index
        int n = rows.getLength();
        int i;
        if (from == -1) {
            i = (direction == TextArea.ScrollDirection.DOWN) ? -1 : n;
        } else {
            i = getRowAt(from);
        }

        // Move to the next or previous row
        if (direction == TextArea.ScrollDirection.DOWN) {
            i++;
        } else {
            i--;
        }

        return (i < 0
            || i >= n) ? -1 : getRowInsertionPoint(i, x);
    }

    private int getRowInsertionPoint(int rowIndex, int x) {
        Row row = rows.get(rowIndex);

        Bounds glyphVectorBounds = row.glyphVector.getLogicalBounds();
        float rowWidth = (float)glyphVectorBounds.width;

        int index;
        if (x < 0) {
            index = 0;
        } else if (x > rowWidth) {
            index = row.glyphVector.getNumGlyphs();

            // If this is not the last row, decrement the index so the insertion
            // point remains on this line
            if (rowIndex < rows.getLength() - 1) {
                index--;
            }
        } else {
            index = 0;
            int n = row.glyphVector.getNumGlyphs();

            while (index < n) {
                Shape glyphBounds = row.glyphVector.getGlyphLogicalBounds(index);
                Bounds glyphBounds2D = glyphBounds.getBounds();

                if (glyphBounds2D.contains( x, glyphBounds2D.getY() )) {
                    // Determine the bias; if the user clicks on the right half of the
                    // character; select the next character
                    if (x - glyphBounds2D.getX() > glyphBounds2D.getWidth() / 2
                        && index < n - 1) {
                        index++;
                    }

                    break;
                }

                index++;
            }
        }

        return index + row.offset;
    }

    public int getRowAt(int index) {
        int rowIndex = rows.getLength() - 1;
        Row row = rows.get(rowIndex);

        while (row.offset > index) {
            row = rows.get(--rowIndex);
        }

        return rowIndex;
    }

    public int getRowOffset(int index) {
        Row row = rows.get(getRowAt(index));
        return row.offset;
    }

    public int getRowLength(int index) {
        Row row = rows.get(getRowAt(index));
        return row.glyphVector.getNumGlyphs();
    }

    public int getRowCount() {
        return rows.getLength();
    }

    public Bounds getCharacterBounds(int index) {
        Bounds characterBounds = null;

        CharSequence characters = paragraph.getCharacters();
        int characterCount = characters.length();

        int rowIndex, x, width;
        if (index == characterCount) {
            // This is the terminator character
            rowIndex = rows.getLength() - 1;
            Row row = rows.get(rowIndex);

            Bounds glyphVectorBounds = row.glyphVector.getLogicalBounds();
            x = (int)Math.floor(glyphVectorBounds.getWidth());
            width = PARAGRAPH_TERMINATOR_WIDTH;
        } else {
            // This is a visible character
            rowIndex = getRowAt(index);
            Row row = rows.get(rowIndex);

            Shape glyphBounds = row.glyphVector.getGlyphLogicalBounds(index - row.offset);
            Bounds glyphBounds2D = glyphBounds.getBounds();
            x = (int)Math.floor(glyphBounds2D.getX());
            width = (int)Math.ceil(glyphBounds2D.getWidth());
        }

        Font font = textAreaSkin.getFont();
        FontRenderContext fontRenderContext = Platform.getInstalled().getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("", fontRenderContext);
        float rowHeight = lm.getAscent() + lm.getDescent();

        characterBounds = new Bounds(x, (int)Math.floor(rowIndex * rowHeight), width,
            (int)Math.ceil(rowHeight));

        return characterBounds;
    }

    @Override
    public void textInserted(TextArea.Paragraph paragraph, int index, int count) {
        invalidate();
        textAreaSkin.invalidateComponent();
    }

    @Override
    public void textRemoved(TextArea.Paragraph paragraph, int index, int count) {
        invalidate();
        textAreaSkin.invalidateComponent();
    }
}
