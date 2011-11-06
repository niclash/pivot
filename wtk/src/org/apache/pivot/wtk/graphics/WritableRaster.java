package org.apache.pivot.wtk.graphics;

public interface WritableRaster extends Raster
{
    void setDataElements( int i, int i1, int dstWidth, int dstHeight, int[] dstBuffer );
}
