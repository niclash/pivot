package org.apache.pivot.wtk.graphics;

public interface ColorFactoryProvider
{
    Color decode( String selectedColor );

    float[] RGBtoHSB( int r, int g, int b, float[] hsb );

    int HSBtoRGB( float h, float s, float b );

    Color createColor( int r, int g, int b, int alpha );

    Color createColor( float red, float green, float blue, float alpha );

    Color createColor( int rgb, boolean hasAlpha );

    Color getHSBColor( float h, float s, float b );

    Color white();

    Color red();

    Color black();

    Color gray();

    Color blue();

    Color lightGray();
}
