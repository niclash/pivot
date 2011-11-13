package org.apache.pivot.wtk.graphics;

public interface WritableRaster extends Raster
{
    void setDataElements( int x, int y, int width, int height, int[] buffer );
}
