package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.AffineTransformFactory;

public class JavaAwtAffineTransformFactory
    implements AffineTransformFactory
{
    JavaAwtAffineTransformFactory()
    {
    }

    @Override
    public AffineTransform newAffineTransform()
    {
        return new JavaAwtAffineTransform( new java.awt.geom.AffineTransform() );
    }

    @Override
    public AffineTransform newTranslateTransform( double translateX, double translateY )
    {
        return new JavaAwtAffineTransform( java.awt.geom.AffineTransform.getTranslateInstance(translateX,translateY) );
    }

    @Override
    public AffineTransform newScaleTransform( double scaleX, double scaleY )
    {
        return new JavaAwtAffineTransform( java.awt.geom.AffineTransform.getScaleInstance( scaleX, scaleY ) );
    }

    @Override
    public AffineTransform newRotateTransform( double theta )
    {
        return new JavaAwtAffineTransform( java.awt.geom.AffineTransform.getRotateInstance( theta ) );
    }

    @Override
    public AffineTransform newRotateTransform( double theta, double x, double y )
    {
        return new JavaAwtAffineTransform( java.awt.geom.AffineTransform.getRotateInstance( theta, x, y ) );
    }
}
