package org.apache.pivot.wtk.graphics.font;

import java.text.CharacterIterator;
import org.apache.pivot.text.CharSequenceCharacterIterator;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.GlyphVector;

public interface Font
{
    int BOLD = 1;
    int ITALIC = 2;
    int PLAIN = 0;

    LineMetrics getLineMetrics( String text, FontRenderContext fontRenderContext );

    Bounds getStringBounds( String text, FontRenderContext fontRenderContext );

    String getName();

    int getSize();

    int getStyle();

    Font deriveFont( int style );

    Bounds getMaxCharBounds( FontRenderContext fontRenderContext );

    GlyphVector createGlyphVector( FontRenderContext fontRenderContext, int[] glyphCodes );

    int getMissingGlyphCode();

    Bounds getStringBounds( CharacterIterator ci, int begin, int limit, FontRenderContext fontRenderContext );

    int canDisplayUpTo( String sampleResource );

    Font deriveFont( int style, int fontSize );
}
