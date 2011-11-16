package org.apache.pivot.wtk.graphics.geom;

import org.apache.pivot.wtk.graphics.AffineTransform;

public interface Area extends Shape
{
    void add( Area area );

    void subtract( Area area );

    Area createTransformedArea( AffineTransform transform );
}
