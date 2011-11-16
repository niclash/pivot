package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.PathFactory;
import org.apache.pivot.wtk.graphics.font.FontFactory;
import org.apache.pivot.wtk.graphics.geom.Area;
import org.apache.pivot.wtk.graphics.geom.Ellipse;
import org.apache.pivot.wtk.graphics.geom.Rectangle;
import org.apache.pivot.wtk.graphics.geom.RoundRectangle;

public interface GraphicsSystem
{
    GradientFactory getGradientFactory();

    PathFactory getPathFactory();

    StrokeFactory getStrokeFactory();

    boolean isDispatchThread();

    ColorFactoryProvider getColorFactoryProvider();

    BufferedImage newBufferedImage( int width, int height );

    AffineTransformFactory getAffineTransformFactory();

    Ellipse newEllipse( int x, int y, int width, int height );

    FontFactory getFontFactory();

    Area newArea();
    Area newArea( int x, int y, int width, int height );

    Rectangle newRectangle();
    Rectangle newRectangle(int x, int y, int width, int height );

    RoundRectangle newRoundRectangle( double x, double y, double width, double height, double radius1, double radius2 );

    Line newLine( double x1, double y1, double x2, double y2 );

    Paint newGradientPaint( float i, float v, Color buttonBevelColor, float v1, float v2, Color backgroundColor );

    Paint newRadialGradientPaint( float centerX, float centerY, float radius, float[] fractions, Color[] colors );

    Paint newLinearGradientPaint( float startX, float startY, float endX, float endY, float[] fractions, Color[] colors );
}
