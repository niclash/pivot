package org.apache.pivot.ui.awt;

import java.awt.geom.Ellipse2D;
import org.apache.pivot.wtk.PathFactory;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.AffineTransformFactory;
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.Color;
import org.apache.pivot.wtk.graphics.ColorFactoryProvider;
import org.apache.pivot.wtk.graphics.CompositeFactory;
import org.apache.pivot.wtk.graphics.GradientFactory;
import org.apache.pivot.wtk.graphics.GradientPaint;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.LinearGradientPaint;
import org.apache.pivot.wtk.graphics.RadialGradientPaint;
import org.apache.pivot.wtk.graphics.StrokeFactory;
import org.apache.pivot.wtk.graphics.font.FontFactory;
import org.apache.pivot.wtk.graphics.geom.Ellipse;

public class JavaAwtGraphicsSystem
    implements GraphicsSystem
{
    private static final Color BLACK = new JavaAwtColor( java.awt.Color.BLACK );
    private static final Color BLUE = new JavaAwtColor( java.awt.Color.BLUE );
    private static final Color CYAN = new JavaAwtColor( java.awt.Color.CYAN );
    private static final Color DARK_GRAY = new JavaAwtColor( java.awt.Color.DARK_GRAY );
    private static final Color GRAY = new JavaAwtColor( java.awt.Color.GRAY );
    private static final Color GREEN = new JavaAwtColor( java.awt.Color.GREEN );
    private static final Color LIGHT_GRAY = new JavaAwtColor( java.awt.Color.LIGHT_GRAY );
    private static final Color MAGENTA = new JavaAwtColor( java.awt.Color.MAGENTA );
    private static final Color ORANGE = new JavaAwtColor( java.awt.Color.ORANGE );
    private static final Color PINK = new JavaAwtColor( java.awt.Color.PINK );
    private static final Color RED = new JavaAwtColor( java.awt.Color.RED );
    private static final Color WHITE = new JavaAwtColor( java.awt.Color.WHITE );
    private static final Color YELLOW = new JavaAwtColor( java.awt.Color.YELLOW );
    private GradientFactory gradientFactory;
    private ColorFactoryProvider colorFactory;
    private PathFactory pathFactory;
    private StrokeFactory strokeFactory;
    private FontFactory fontFactory;

    JavaAwtGraphicsSystem()
    {
        gradientFactory = new GradientFactory()
        {
            @Override
            public RadialGradientPaint createRadialGradientPaint( float centerX,
                                                                  float centerY,
                                                                  float radius,
                                                                  float[] fractions,
                                                                  Color[] colors
            )
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public LinearGradientPaint createLinearGradientPaint( float startX,
                                                                  float startY,
                                                                  float endX,
                                                                  float endY,
                                                                  float[] fractions,
                                                                  Color[] colors
            )
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public GradientPaint createGradientPaint( float startX,
                                                      float startY,
                                                      Color startColor,
                                                      float endX,
                                                      float endY,
                                                      Color endColor
            )
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        colorFactory = new ColorFactoryProvider()
        {
            public CompositeFactory compositeFactory = new JavaAwtCompositeFactory();

            @Override
            public Color decode( String selectedColor )
            {
                return new JavaAwtColor( java.awt.Color.decode( selectedColor ) );
            }

            public float[] RGBtoHSB( int r, int g, int b, float[] hsb )
            {
                return java.awt.Color.RGBtoHSB( r, g, b, hsb );
            }

            @Override
            public int HSBtoRGB( float h, float s, float b )
            {
                return java.awt.Color.HSBtoRGB( h, s, b );
            }

            @Override
            public Color createColor( int r, int g, int b, int alpha )
            {
                return new JavaAwtColor( new java.awt.Color( r, g, b, alpha ) );
            }

            @Override
            public Color createColor( float red, float green, float blue, float alpha )
            {
                return new JavaAwtColor( new java.awt.Color( red, green, blue, alpha ) );
            }

            @Override
            public Color createColor( int rgb, boolean hasAlpha )
            {
                return new JavaAwtColor( new java.awt.Color( rgb, hasAlpha ) );
            }

            @Override
            public Color getHSBColor( float h, float s, float b )
            {
                return new JavaAwtColor( java.awt.Color.getHSBColor( h, s, b ) );
            }

            @Override
            public Color white()
            {
                return WHITE;
            }

            @Override
            public Color red()
            {
                return RED;
            }

            @Override
            public Color black()
            {
                return BLACK;
            }

            @Override
            public Color gray()
            {
                return GRAY;
            }

            @Override
            public Color blue()
            {
                return BLUE;
            }

            @Override
            public Color lightGray()
            {
                return LIGHT_GRAY;
            }

            @Override
            public CompositeFactory getCompositeFactory()
            {
                return compositeFactory;
            }
        };
    }

    @Override
    public ColorFactoryProvider getColorFactoryProvider()
    {
        return colorFactory;
    }

    @Override
    public BufferedImage newBufferedImage( int width, int height )
    {
        java.awt.image.BufferedImage awtImage = new java.awt.image.BufferedImage( width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB );
        return new JavaAwtBufferedImage( awtImage );
    }

    @Override
    public AffineTransformFactory getAffineTransformFactory()
    {
        return new JavaAwtAffineTransformFactory();
    }

    @Override
    public Ellipse newEllipse( int x, int y, int width, int height )
    {
        Ellipse2D.Double awtEllipse = new Ellipse2D.Double( x, y, width, height );
        return new JavaAwtEllipse( awtEllipse );
    }

    @Override
    public FontFactory getFontFactory()
    {
        return fontFactory;

    }

    @Override
    public GradientFactory getGradientFactory()
    {
        return gradientFactory;
    }

    @Override
    public PathFactory getPathFactory()
    {
        return pathFactory;
    }

    @Override
    public StrokeFactory getStrokeFactory()
    {
        return strokeFactory;
    }

    @Override
    public boolean isDispatchThread()
    {
        return java.awt.EventQueue.isDispatchThread();
    }
}
