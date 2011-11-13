package org.apache.pivot.ui.awt;

import java.awt.Rectangle;
import java.text.CharacterIterator;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;
import org.apache.pivot.wtk.graphics.font.LineMetrics;

public class JavaAwtFont
    implements Font
{
    private java.awt.Font delegate;

    JavaAwtFont( java.awt.Font font )
    {
        delegate = font;
    }

    java.awt.Font getDelegate()
    {
        return delegate;
    }

    @Override
    public LineMetrics getLineMetrics( String text, FontRenderContext fontRenderContext )
    {
        java.awt.font.FontRenderContext renderContext = ( (JavaAwtFontRenderContext) fontRenderContext ).getDelegate();
        java.awt.font.LineMetrics awtLineMetrics = delegate.getLineMetrics( text, renderContext );
        return new JavaAwtLineMetrics( awtLineMetrics );
    }

    @Override
    public Bounds getStringBounds( String text, FontRenderContext fontRenderContext )
    {
        java.awt.font.FontRenderContext renderContext = ( (JavaAwtFontRenderContext) fontRenderContext ).getDelegate();
        Rectangle bounds = delegate.getStringBounds( text, renderContext ).getBounds();
        return new Bounds( bounds.x, bounds.y, bounds.width, bounds.height );
    }

    @Override
    public String getName()
    {
        return delegate.getName();
    }

    @Override
    public int getSize()
    {
        return delegate.getSize();
    }

    @Override
    public int getStyle()
    {
        return delegate.getStyle();
    }

    @Override
    public Bounds getMaxCharBounds( FontRenderContext fontRenderContext )
    {
        java.awt.font.FontRenderContext awtContext = ( (JavaAwtFontRenderContext) fontRenderContext ).getDelegate();
        Rectangle bounds = delegate.getMaxCharBounds( awtContext ).getBounds();
        return new Bounds( bounds.x, bounds.y, bounds.width, bounds.height );
    }

    @Override
    public GlyphVector createGlyphVector( FontRenderContext fontRenderContext, int[] glyphCodes )
    {
        java.awt.font.FontRenderContext awtContext = ( (JavaAwtFontRenderContext) fontRenderContext ).getDelegate();
        java.awt.font.GlyphVector awtGlyphVector = delegate.createGlyphVector( awtContext, glyphCodes );
        return new JavaAwtGlyphVector( awtGlyphVector );
    }

    @Override
    public int getMissingGlyphCode()
    {
        return delegate.getMissingGlyphCode();
    }

    @Override
    public Bounds getStringBounds( CharacterIterator ci, int begin, int limit, FontRenderContext fontRenderContext )
    {
        java.awt.font.FontRenderContext awtContext = ( (JavaAwtFontRenderContext) fontRenderContext ).getDelegate();
        Rectangle rect = delegate.getStringBounds( ci, begin, limit, awtContext ).getBounds();
        return new Bounds( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public int canDisplayUpTo( String sampleResource )
    {
        return delegate.canDisplayUpTo( sampleResource );
    }

    @Override
    public Font deriveFont( int style, int fontSize )
    {
        java.awt.Font derivedFont = delegate.deriveFont( style, fontSize );
        return new JavaAwtFont( derivedFont );
    }

    @Override
    public Font deriveFont( int style )
    {
        java.awt.Font derivedFont = delegate.deriveFont( style );
        return new JavaAwtFont( derivedFont );
    }
}
