package org.apache.pivot.wtk.graphics;

public interface VolatileImage
{
    int validate( GraphicsConfiguration gc );

    Graphics2D createGraphics();

    boolean contentsLost();
}
