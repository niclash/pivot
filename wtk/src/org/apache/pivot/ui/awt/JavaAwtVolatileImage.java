package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.GraphicsConfiguration;
import org.apache.pivot.wtk.graphics.VolatileImage;

public class JavaAwtVolatileImage
    implements VolatileImage
{
    private java.awt.image.VolatileImage delegate;

    JavaAwtVolatileImage( java.awt.image.VolatileImage awt )
    {
        this.delegate = awt;
    }

    java.awt.image.VolatileImage getDelegate()
    {
        return delegate;
    }

    @Override
    public int validate( GraphicsConfiguration gc )
    {
        return delegate.validate( ( (JavaAwtGraphicsConfiguration) gc ).getDelegate() );
    }

    @Override
    public Graphics2D createGraphics()
    {
        java.awt.Graphics2D awt = delegate.createGraphics();
        return new JavaAwtGraphics( awt );
    }

    @Override
    public boolean contentsLost()
    {
        return delegate.contentsLost();
    }
}
