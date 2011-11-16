package org.apache.pivot.ui.awt;

import java.awt.Rectangle;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.geom.Area;

public class JavaAwtArea implements Area
{
    private java.awt.geom.Area delegate;

    JavaAwtArea( java.awt.geom.Area awt )
    {
        delegate = awt;
    }

    java.awt.geom.Area getDelegate()
    {
        return delegate;
    }

    @Override
    public void add( Area area )
    {
        java.awt.geom.Area awtArea = ( (JavaAwtArea) area ).getDelegate();
        delegate.add( awtArea );
    }

    @Override
    public void subtract( Area area )
    {
        java.awt.geom.Area awtArea = ( (JavaAwtArea) area ).getDelegate();
        delegate.subtract( awtArea );
    }

    @Override
    public Area createTransformedArea( AffineTransform transform )
    {
        java.awt.geom.AffineTransform awtTransform = ( (JavaAwtAffineTransform) transform ).getDelegate();
        java.awt.geom.Area transformedArea = delegate.createTransformedArea( awtTransform );
        return new JavaAwtArea( transformedArea );
    }

    @Override
    public Bounds getBounds()
    {
        Rectangle rect = delegate.getBounds();
        return new Bounds( (int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight() );
    }

    @Override
    public boolean contains( int x, int y )
    {
        return delegate.contains( x, y );
    }
}
