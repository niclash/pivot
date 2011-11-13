package org.apache.pivot.wtk.graphics;

public interface AffineTransformFactory
{

    AffineTransform newAffineTransform();

    AffineTransform newTranslateTransform( double translateX, double translateY );

    AffineTransform newScaleTransform( double scaleX, double scaleY );

    AffineTransform newRotateTransform( double theta );

    AffineTransform newRotateTransform( double theta, double x, double y );
}
