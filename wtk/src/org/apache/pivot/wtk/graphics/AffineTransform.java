package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.Bounds;

public interface AffineTransform
{
    int TYPE_MASK_SCALE = 0x01;

    int getType();

    Shape createTransformedShape( Bounds area );

    boolean isIdentity();
}
