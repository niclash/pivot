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
        WHITE = system.getColorFactoryProvider().white();
        RED = system.getColorFactoryProvider().red();
        BLACK = system.getColorFactoryProvider().black();
        GRAY = system.getColorFactoryProvider().gray();
        BLUE = system.getColorFactoryProvider().blue();
        LIGHT_GRAY = system.getColorFactoryProvider().lightGray();
    }

    public static Color decode(String colorValue)
    {
        return system.getColorFactoryProvider().decode( colorValue );
    }

    public static float[] RGBtoHSB( int r, int g, int b, float[] hsb )
    {
        return system.getColorFactoryProvider().RGBtoHSB( r, g, b, hsb );
    }

    public static int HSBtoRGB( float h, float s, float b )
    {
        return system.getColorFactoryProvider().HSBtoRGB( h, s, b );
    }

    public static Color getHSBColor( float h, float s, float b )
    {
        return system.getColorFactoryProvider().getHSBColor( h, s, b );
    }

    public static Color create( int r, int g, int b, int alpha )
    {
        return system.getColorFactoryProvider().createColor( r, g, b, alpha );
    }

    public static Color create( float red, float green, float blue, float alpha )
    {
        return system.getColorFactoryProvider().createColor( red, green, blue, alpha );
    }

    public static Color create( int red, int green, int blue )
    {
        return system.getColorFactoryProvider().createColor( red, green, blue, 0xff );
    }

    public static Color create( int rgb, boolean hasAlpha )
    {
        return system.getColorFactoryProvider().createColor( rgb, hasAlpha);
    }

    public static CompositeFactory getCompositeFactory()
    {
        return system.getColorFactoryProvider().getCompositeFactory();
    }
}
