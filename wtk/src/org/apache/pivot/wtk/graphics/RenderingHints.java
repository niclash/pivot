package org.apache.pivot.wtk.graphics;

public class RenderingHints
{
    public static final RenderingHintsKey KEY_RENDERING = new RenderingHintsKey( "rendering" );
    public static final RenderingHintsKey KEY_INTERPOLATION = new RenderingHintsKey( "interpolation" );
    public static final RenderingHintsKey KEY_ANTIALIASING = new RenderingHintsKey( "anti-aliasing" );
    public static final RenderingHintsKey KEY_TEXT_ANTIALIASING = new RenderingHintsKey( "text anti-aliasing" );
    public static final RenderingHintsKey KEY_FRACTIONALMETRICS = new RenderingHintsKey( "fractional metrics" );

    public static final String VALUE_RENDER_SPEED = "render speed";
    public static final String VALUE_TEXT_ANTIALIAS_DEFAULT = "anti-alias default";
    public static final String VALUE_ANTIALIAS_ON = "anti-alias on";
    public static final String VALUE_ANTIALIAS_OFF = "anti-alias off";
    public static final String VALUE_FRACTIONALMETRICS_DEFAULT = "fractional-metrics default";

    public static final String VALUE_INTERPOLATION_NEAREST_NEIGHBOR = "interpolation-nearest-neighbor";
    public static final String VALUE_INTERPOLATION_BILINEAR = "interpolation-bilinear";
    public static final String VALUE_INTERPOLATION_BICUBIC = "interpolation-bicubic";
}
