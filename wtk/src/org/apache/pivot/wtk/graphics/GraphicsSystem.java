package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.PathFactory;
import org.apache.pivot.wtk.graphics.font.FontFactory;
import org.apache.pivot.wtk.graphics.geom.Area;
import org.apache.pivot.wtk.graphics.geom.Ellipse;
import org.apache.pivot.wtk.graphics.geom.Rectangle;

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
}
