package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.Bounds;

public interface GlyphVector
{
    Bounds getLogicalBounds();

    int getNumGlyphs();

    Shape getGlyphLogicalBounds( int index );
}
