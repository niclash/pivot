package org.apache.pivot.ui.awt;

import java.awt.Image;
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.ImageObserver;

public class JavaAwtImageObserver
    implements ImageObserver
{
    private java.awt.image.ImageObserver delegate;

    JavaAwtImageObserver( java.awt.image.ImageObserver observer )
    {
        delegate = observer;
    }

    java.awt.image.ImageObserver getDelegate()
    {
        return delegate;
    }

    @Override
    public boolean imageUpdate( BufferedImage img, int infoflags, int x, int y, int width, int height )
    {
        Image awtImage = ( (JavaAwtBufferedImage) img ).getDelegate();
        return delegate.imageUpdate( awtImage, infoflags, x, y, width, height );
    }
}
