package org.apache.pivot.wtk.graphics;

public interface Raster
{
    void getDataElements( int x, int y, int width, int height, int[] buffer);
}
