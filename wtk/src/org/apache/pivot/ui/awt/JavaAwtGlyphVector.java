package org.apache.pivot.ui.awt;

import java.awt.Rectangle;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.geom.Shape;

public class JavaAwtGlyphVector
    implements GlyphVector
{
    private java.awt.font.GlyphVector delegate;

    JavaAwtGlyphVector( java.awt.font.GlyphVector awtGlyphVector )
    {
        delegate = awtGlyphVector;
    }

    java.awt.font.GlyphVector getDelegate()
    {
        return delegate;
    }

    @Override
    public Bounds getLogicalBounds()
    {
        Rectangle rect = delegate.getLogicalBounds().getBounds();
        return new Bounds( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public int getNumGlyphs()
    {
        return delegate.getNumGlyphs();
    }

    @Override
    public Shape getGlyphLogicalBounds( int index )
    {
        java.awt.Shape bounds = delegate.getGlyphLogicalBounds( index );
        return new JavaAwtShape( bounds );
    }
}
