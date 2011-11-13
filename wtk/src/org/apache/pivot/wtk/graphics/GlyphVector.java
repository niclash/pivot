package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.geom.Shape;

public interface GlyphVector
{
    Bounds getLogicalBounds();

    int getNumGlyphs();

    Shape getGlyphLogicalBounds( int index );
}
