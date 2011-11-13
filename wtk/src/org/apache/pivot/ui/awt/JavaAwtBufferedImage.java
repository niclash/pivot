package org.apache.pivot.ui.awt;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.Kernel;
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.WritableRaster;

public class JavaAwtBufferedImage
    implements BufferedImage
{
    private java.awt.image.BufferedImage delegate;

    JavaAwtBufferedImage( java.awt.image.BufferedImage awt )
    {
        delegate = awt;
    }

    java.awt.image.BufferedImage getDelegate()
    {
        return delegate;
    }

    @Override
    public int getType()
    {
        return delegate.getType();
    }

    @Override
    public int getTransparency()
    {
        return delegate.getTransparency();
    }

    @Override
    public int getWidth()
    {
        return delegate.getWidth();
    }

    @Override
    public int getHeight()
    {
        return delegate.getHeight();
    }

    @Override
    public Graphics2D getGraphics()
    {
        java.awt.Graphics2D awt = (java.awt.Graphics2D) delegate.getGraphics();
        return new JavaAwtGraphics( awt );
    }

    @Override
    public Graphics2D createGraphics()
    {
        java.awt.Graphics2D awt = delegate.createGraphics();
        return new JavaAwtGraphics( awt );
    }

    @Override
    public WritableRaster getRaster()
    {
        java.awt.image.WritableRaster awt = delegate.getRaster();
        return new JavaAwtWritableRaster( awt);
    }

    @Override
    public void flush()
    {
        delegate.flush();
    }

    @Override
    public BufferedImage blur( int blurMagnitude, float[] data )
    {
        ConvolveOp blur = new ConvolveOp(new Kernel(blurMagnitude, blurMagnitude, data), ConvolveOp.EDGE_NO_OP, null);
        java.awt.image.BufferedImage awtImage = blur.filter( delegate, null );
        return new JavaAwtBufferedImage( awtImage );
    }

    @Override
    public BufferedImage toGrayScale( int width, int height )
    {
        ColorSpace gsColorSpace = ColorSpace.getInstance( ColorSpace.CS_GRAY );
        ComponentColorModel ccm = new ComponentColorModel(gsColorSpace, true, false,
            Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        java.awt.image.WritableRaster raster = ccm.createCompatibleWritableRaster( width, height );
        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage( ccm, raster, ccm.isAlphaPremultiplied(), null );
        return new JavaAwtBufferedImage( bufferedImage );

    }
}
