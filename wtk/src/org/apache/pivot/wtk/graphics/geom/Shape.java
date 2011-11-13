package org.apache.pivot.wtk.graphics.geom;

import org.apache.pivot.wtk.Bounds;

public interface Shape
{
    Bounds getBounds();

    boolean contains( int x, int y );
}
