package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.Color;

public class JavaAwtColor
    implements Color
{
    private java.awt.Color delegate;

    JavaAwtColor(java.awt.Color color)
    {
        delegate = color;
    }

    java.awt.Color getDelegate() {
        return delegate;
    }

    @Override
    public int getTransparency()
    {
        return delegate.getTransparency();
    }

    @Override
    public int getRed()
    {
        return delegate.getRed();
    }

    @Override
    public int getBlue()
    {
        return delegate.getBlue();
    }

    @Override
    public int getGreen()
    {
        return delegate.getGreen();
    }

    @Override
    public int getRGB()
    {
        return delegate.getRGB();
    }

    @Override
    public int getAlpha()
    {
        return delegate.getAlpha();
    }
}
