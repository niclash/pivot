package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.geom.Shape;

public class JavaAwtAffineTransform
    implements AffineTransform
{
    private java.awt.geom.AffineTransform delegate;

    JavaAwtAffineTransform( java.awt.geom.AffineTransform awtTransform )
    {
        delegate = awtTransform;
    }

    java.awt.geom.AffineTransform getDelegate()
    {
        return delegate;
    }

    @Override
    public int getType()
    {
        // TODO: Need to work out what all these mapped integers really mean.
        return delegate.getType();
    }

    @Override
    public Shape createTransformedShape( Shape shape )
    {
        java.awt.Shape awtShape = ((JavaAwtShape) shape).getDelegate();
        java.awt.Shape transformedShape = delegate.createTransformedShape( awtShape );
        return new JavaAwtShape(transformedShape);
    }

    @Override
    public boolean isIdentity()
    {
        return delegate.isIdentity();
    }

    @Override
    public void translate( int translateX, int translateY )
    {
        delegate.translate( translateX, translateY );
    }
}
