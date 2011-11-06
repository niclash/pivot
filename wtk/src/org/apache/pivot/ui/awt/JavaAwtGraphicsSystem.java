package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.Color;
import org.apache.pivot.wtk.graphics.ColorFactoryProvider;
import org.apache.pivot.wtk.graphics.GradientFactory;
import org.apache.pivot.wtk.graphics.GradientPaint;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.LinearGradientPaint;
import org.apache.pivot.wtk.graphics.RadialGradientPaint;

public class JavaAwtGraphicsSystem implements GraphicsSystem
{
    private static final Color BLACK = new AwtColor( java.awt.Color.BLACK );
    private static final Color BLUE = new AwtColor( java.awt.Color.BLUE );
    private static final Color CYAN = new AwtColor( java.awt.Color.CYAN );
    private static final Color DARK_GRAY = new AwtColor( java.awt.Color.DARK_GRAY );
    private static final Color GRAY = new AwtColor( java.awt.Color.GRAY );
    private static final Color GREEN = new AwtColor( java.awt.Color.GREEN );
    private static final Color LIGHT_GRAY = new AwtColor( java.awt.Color.LIGHT_GRAY );
    private static final Color MAGENTA = new AwtColor( java.awt.Color.MAGENTA );
    private static final Color ORANGE = new AwtColor( java.awt.Color.ORANGE );
    private static final Color PINK = new AwtColor( java.awt.Color.PINK );
    private static final Color RED = new AwtColor( java.awt.Color.RED );
    private static final Color WHITE = new AwtColor( java.awt.Color.WHITE );
    private static final Color YELLOW = new AwtColor( java.awt.Color.YELLOW );
    private GradientFactory gradientFactory;
    private ColorFactoryProvider colorFactory;

    public JavaAwtGraphicsSystem()
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
            @Override
            public Color decode( String selectedColor )
            {
                return new AwtColor( java.awt.Color.decode( selectedColor ) );
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
                return new AwtColor( new java.awt.Color( r, g, b, alpha ) );
            }

            @Override
            public Color createColor( float red, float green, float blue, float alpha )
            {
                return new AwtColor( new java.awt.Color( red, green, blue, alpha ) );
            }

            @Override
            public Color createColor( int rgb, boolean hasAlpha )
            {
                return new AwtColor( new java.awt.Color( rgb, hasAlpha ) );
            }

            @Override
            public Color getHSBColor( float h, float s, float b )
            {
                return new AwtColor( java.awt.Color.getHSBColor( h, s, b ) );
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
        };
    }

    @Override
    public ColorFactoryProvider colorFactory()
    {
        return colorFactory;
    }

    @Override
    public GradientFactory gradientFactory()
    {
        return gradientFactory;
    }

    public static class AwtColor
        implements Color
    {

        private java.awt.Color delegate;

        public AwtColor( java.awt.Color delegate )
        {
            this.delegate = delegate;
        }

//        @Override
//        public PaintContext createContext( ColorModel cm,
//                                           Rectangle deviceBounds,
//                                           Rectangle2D userBounds,
//                                           AffineTransform xform,
//                                           RenderingHints hints
//        )
//        {
//            return delegate.createContext( cm, deviceBounds, userBounds, xform, hints );
//        }

        @Override
        public int getTransparency()
        {
            return delegate.getTransparency();
        }

        public int getRed()
        {
            return delegate.getRed();
        }

        public int getBlue()
        {
            return delegate.getBlue();
        }

        public int getGreen()
        {
            return delegate.getGreen();
        }

        public int getRGB()
        {
            return delegate.getRGB();
        }

        public int getAlpha()
        {
            return delegate.getAlpha();
        }
    }
}
