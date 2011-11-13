package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.graphics.geom.Shape;

public interface AffineTransform
{
    int TYPE_MASK_SCALE = 0x01;

    int getType();

    Shape createTransformedShape( Shape shape );

    boolean isIdentity();

    void translate( int translateX, int translateY );
}
