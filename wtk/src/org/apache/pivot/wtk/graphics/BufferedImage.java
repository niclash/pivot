package org.apache.pivot.wtk.graphics;

public interface BufferedImage
{
    Object TYPE_INT_ARGB = new Object();

    int getType();

    int getTransparency();

    int getWidth();

    int getHeight();

    Graphics2D getGraphics();

    Graphics2D createGraphics();

    WritableRaster getRaster();

    void flush();

    BufferedImage blur( int blurMagnitude, float[] data );

    BufferedImage toGrayScale( int width, int height );
}
