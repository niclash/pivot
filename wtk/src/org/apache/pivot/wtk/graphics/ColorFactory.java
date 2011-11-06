package org.apache.pivot.wtk.graphics;

import org.apache.pivot.ui.awt.JavaAwtGraphicsSystem;

public class ColorFactory
{
    public static Color WHITE;
    public static Color RED;
    public static Color BLACK;
    public static Color GRAY;
    public static Color BLUE;
    public static Color LIGHT_GRAY;


    private static GraphicsSystem system = new JavaAwtGraphicsSystem();

    public static void setSystem( GraphicsSystem system )
    {
        ColorFactory.system = system;
        WHITE = system.colorFactory().white();
        RED = system.colorFactory().red();
        BLACK = system.colorFactory().black();
        GRAY = system.colorFactory().gray();
        BLUE = system.colorFactory().blue();
        LIGHT_GRAY = system.colorFactory().lightGray();
    }

    public static Color decode(String colorValue)
    {
        return system.colorFactory().decode( colorValue );
    }

    public static float[] RGBtoHSB( int r, int g, int b, float[] hsb )
    {
        return system.colorFactory().RGBtoHSB( r, g, b, hsb );
    }

    public static int HSBtoRGB( float h, float s, float b )
    {
        return system.colorFactory().HSBtoRGB( h, s, b );
    }

    public static Color getHSBColor( float h, float s, float b )
    {
        return system.colorFactory().getHSBColor( h, s, b );
    }

    public static Color create( int r, int g, int b, int alpha )
    {
        return system.colorFactory().createColor( r, g, b, alpha );
    }

    public static Color create( float red, float green, float blue, float alpha )
    {
        return system.colorFactory().createColor( red, green, blue, alpha );
    }

    public static Color create( int red, int green, int blue )
    {
        return system.colorFactory().createColor( red, green, blue, 0xff );
    }

    public static Color create( int rgb, boolean hasAlpha )
    {
        return system.colorFactory().createColor( rgb, hasAlpha);
    }

    public interface ColorSpace {

    }
}
