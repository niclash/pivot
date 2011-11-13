package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.GraphicsConfiguration;
import org.apache.pivot.wtk.graphics.VolatileImage;

public class JavaAwtGraphicsConfiguration
    implements GraphicsConfiguration
{
    private java.awt.GraphicsConfiguration delegate;

    JavaAwtGraphicsConfiguration( java.awt.GraphicsConfiguration awt )
    {
        delegate = awt;
    }

    java.awt.GraphicsConfiguration getDelegate()
    {
        return delegate;
    }

    @Override
    public BufferedImage createCompatibleImage( int width, int height, int opaque )
    {
        java.awt.image.BufferedImage awt = delegate.createCompatibleImage( width, height, opaque );
        BufferedImage image = new JavaAwtBufferedImage(awt);
        return image;
    }

    @Override
    public VolatileImage createCompatibleVolatileImage( int width, int height, int opaque )
    {
        java.awt.image.VolatileImage awt = delegate.createCompatibleVolatileImage( width, height, opaque );
        return new JavaAwtVolatileImage(awt);
    }
}
