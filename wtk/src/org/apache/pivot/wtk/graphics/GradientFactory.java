package org.apache.pivot.wtk.graphics;

public interface GradientFactory
{
    RadialGradientPaint createRadialGradientPaint( float centerX, float centerY,
                                                   float radius, float[] fractions,
                                                   Color[] colors
    );

    LinearGradientPaint createLinearGradientPaint( float startX,
                                                   float startY,
                                                   float endX,
                                                   float endY,
                                                   float[] fractions,
                                                   Color[] colors
    );

    GradientPaint createGradientPaint( float startX,
                                       float startY,
                                       Color startColor,
                                       float endX,
                                       float endY,
                                       Color endColor
    );
}
