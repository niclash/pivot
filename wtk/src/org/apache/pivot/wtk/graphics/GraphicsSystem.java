package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.PathFactory;

public interface GraphicsSystem
{
    ColorFactory getColorFactory();

    GradientFactory getGradientFactory();

    PathFactory getPathFactory();

    StrokeFactory getStrokeFactory();

}
