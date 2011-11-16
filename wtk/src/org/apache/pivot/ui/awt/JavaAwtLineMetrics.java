package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.font.LineMetrics;

public class JavaAwtLineMetrics
    implements LineMetrics
{
    private java.awt.font.LineMetrics delegate;

    JavaAwtLineMetrics( java.awt.font.LineMetrics awtLineMetrics )
    {
        delegate = awtLineMetrics;
    }

    java.awt.font.LineMetrics getDelegate()
    {
        return delegate;
    }

    @Override
    public float getHeight()
    {
        return delegate.getHeight();
    }

    @Override
    public float getAscent()
    {
        return delegate.getAscent();
    }

    @Override
    public float getDescent()
    {
        return delegate.getDescent();
    }

    @Override
    public float getLeading()
    {
        return delegate.getLeading();
    }

    @Override
    public float getStrikethroughOffset()
    {
        return delegate.getStrikethroughOffset();
    }

    @Override
    public float getUnderlineOffset()
    {
        return delegate.getUnderlineOffset();
    }
}
