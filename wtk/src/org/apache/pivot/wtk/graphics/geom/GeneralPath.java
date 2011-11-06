package org.apache.pivot.wtk.graphics.geom;

import org.apache.pivot.wtk.Bounds;

public interface GeneralPath
{
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

    void moveTo( int x, int y );

    void lineTo( int x, int y );

    void closePath();

    Bounds getBounds();
}
