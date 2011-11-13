package org.apache.pivot.ui.awt;

import java.awt.Rectangle;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.geom.Shape;

public class JavaAwtShape
    implements Shape
{
    private java.awt.Shape delegate;

    JavaAwtShape( java.awt.Shape awtShape )
    {
        delegate = awtShape;
    }

    java.awt.Shape getDelegate()
    {
        return delegate;
    }

    @Override
    public Bounds getBounds()
    {
        Rectangle rectangle = delegate.getBounds();
        return new Bounds( rectangle.x, rectangle.y, rectangle.width, rectangle.height );
    }

    @Override
    public boolean contains( int x, int y )
    {
        return delegate.contains( x, y );
    }
}
