package org.apache.pivot.ui.awt;

import java.awt.GraphicsEnvironment;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontFactory;

public class JavaAwtFontFactory
    implements FontFactory
{
    JavaAwtFontFactory()
    {
    }

    @Override
    public Font create( String name, int style, int size )
    {
        return new JavaAwtFont( new java.awt.Font( name, style, size ) );
    }

    @Override
    public Font decode( String fontName )
    {
        java.awt.Font awtFont = java.awt.Font.decode( fontName );
        return new JavaAwtFont( awtFont );
    }

    @Override
    public Font[] getAllFonts()
    {
        java.awt.Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        Font[] result = new Font[ allFonts.length ];
        int i = 0;
        for( java.awt.Font font : allFonts )
        {
            result[ i++ ] = new JavaAwtFont( font );
        }
        return result;
    }
}
