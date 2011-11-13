package org.apache.pivot.wtk.graphics.font;

public interface FontFactory
{
    Font create( String name, int style, int size );

    Font decode( String fontName );

    Font[] getAllFonts();
}
