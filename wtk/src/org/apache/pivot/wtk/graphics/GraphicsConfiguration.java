package org.apache.pivot.wtk.graphics;

public interface GraphicsConfiguration
{
    BufferedImage createCompatibleImage( int width, int height, int opaque );

    VolatileImage createCompatibleVolatileImage( int width, int height, int opaque );

}
