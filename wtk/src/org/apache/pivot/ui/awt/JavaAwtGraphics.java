package org.apache.pivot.ui.awt;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.AlphaComposite;
import org.apache.pivot.wtk.graphics.BasicStroke;
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.Color;
import org.apache.pivot.wtk.graphics.Composite;
import org.apache.pivot.wtk.graphics.GlyphVector;
import org.apache.pivot.wtk.graphics.GraphicsConfiguration;
import org.apache.pivot.wtk.graphics.ImageObserver;
import org.apache.pivot.wtk.graphics.Paint;
import org.apache.pivot.wtk.graphics.RenderingHintsKey;
import org.apache.pivot.wtk.graphics.geom.Shape;
import org.apache.pivot.wtk.graphics.VolatileImage;
import org.apache.pivot.wtk.graphics.font.Font;

public class JavaAwtGraphics
    implements org.apache.pivot.wtk.graphics.Graphics2D
{
    private Graphics2D delegate;

    JavaAwtGraphics( Graphics2D graphics )
    {
        this.delegate = graphics;
    }

    Graphics2D getDelegate()
    {
        return delegate;
    }

    @Override
    public void setColor( Color color )
    {
        java.awt.Color awt = ( (JavaAwtColor) color ).getDelegate();
        delegate.setColor( awt );
    }

    @Override
    public void setStroke( BasicStroke basicStroke )
    {
        Stroke stroke = ( (JavaAwtBasicStroke) basicStroke ).getDelegate();
        delegate.setStroke( stroke );
    }

    @Override
    public void clipRect( int x, int y, int width, int height )
    {
        delegate.clipRect( x, y, width, height );
    }

    @Override
    public Bounds getClipBounds()
    {
        Rectangle clipBounds = delegate.getClipBounds();
        return new Bounds( clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height );
    }

    @Override
    public void fillRect( int x, int y, int width, int height )
    {
        delegate.fillRect( x, y, width, height );
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration()
    {
        java.awt.GraphicsConfiguration awt = delegate.getDeviceConfiguration();
        GraphicsConfiguration config = new JavaAwtGraphicsConfiguration( awt );
        return config;
    }

    @Override
    public void setClip( int x, int y, int width, int height )
    {
        delegate.setClip( x, y, width, height );
    }

    @Override
    public void translate( int x, int y )
    {
        delegate.translate( x, y );
    }

    @Override
    public void dispose()
    {
        delegate.dispose();
    }

    @Override
    public void drawImage( BufferedImage bufferedImage,
                           int x,
                           int y,
                           ImageObserver observer
    )
    {
        java.awt.image.BufferedImage image = ( (JavaAwtBufferedImage) bufferedImage ).getDelegate();
        java.awt.image.ImageObserver imageObserver = ( (JavaAwtImageObserver) observer ).getDelegate();
        delegate.drawImage( image, x, y, imageObserver );
    }

    @Override
    public void setRenderingHint( RenderingHintsKey keyRendering, Object valueRenderSpeed )
    {
        // TODO !!!
    }

    @Override
    public void scale( double scaleX, double scaleY )
    {
        delegate.scale( scaleX, scaleY );
    }

    @Override
    public void setComposite( AlphaComposite composite )
    {
        java.awt.Composite awt = ( (JavaAwtComposite) composite ).getDelegate();
        delegate.setComposite( awt );
    }

    @Override
    public void drawImage( VolatileImage volatileImage, int x, int y, ImageObserver observer )
    {
        java.awt.image.VolatileImage awtImage = ( (JavaAwtVolatileImage) volatileImage ).getDelegate();
        java.awt.image.ImageObserver awtObserver = ( (JavaAwtImageObserver) observer ).getDelegate();
        delegate.drawImage( awtImage, x, y, awtObserver );
    }

    @Override
    public AffineTransform getTransform()
    {
        java.awt.geom.AffineTransform awtTransform = delegate.getTransform();
        AffineTransform transform = new JavaAwtAffineTransform( awtTransform );
        return transform;
    }

    @Override
    public org.apache.pivot.wtk.graphics.Graphics2D create()
    {
        Graphics2D graphics = (Graphics2D) delegate.create();
        return new JavaAwtGraphics( graphics );
    }

    @Override
    public org.apache.pivot.wtk.graphics.Graphics2D create( int x, int y, int width, int height )
    {
        Graphics2D graphics = (Graphics2D) delegate.create( x, y, width, height );
        return new JavaAwtGraphics( graphics );
    }

    @Override
    public void setPaint( Paint backgroundPaint )
    {
        java.awt.Paint paint = ( (JavaAwtPaint) backgroundPaint ).getDelegate();
        delegate.setPaint( paint );
    }

    @Override
    public void fillRoundRect( int x,
                               int y,
                               int width,
                               int height,
                               int arcWidth,
                               int arcHeight
    )
    {
        delegate.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
    }

    @Override
    public void setFont( Font font )
    {
        java.awt.Font awtFont = ( (JavaAwtFont) font ).getDelegate();
        delegate.setFont( awtFont );
    }

    @Override
    public void drawString( String text, int x, int y )
    {
        delegate.drawString( text, x, y );
    }

    @Override
    public Bounds getClip()
    {
        Rectangle rect = delegate.getClip().getBounds();
        return new Bounds( rect.x, rect.y, rect.width, rect.height );
    }

    @Override
    public void clip( Shape clipShape )
    {
        java.awt.Shape awtShape = ( (JavaAwtShape) clipShape ).getDelegate();
        delegate.clip( awtShape );
    }

    @Override
    public void fill( Shape fillShape )
    {
        java.awt.Shape awtShape = ( (JavaAwtShape) fillShape ).getDelegate();
        delegate.fill( awtShape );
    }

    @Override
    public void drawLine( int x1, int y1, int x2, int y2 )
    {
        delegate.drawLine( x1, y1, x2, y2 );
    }

    @Override
    public void fillPolygon( int[] xPoints, int[] yPoints, int points )
    {
        delegate.fillPolygon( xPoints, yPoints, points );
    }

    @Override
    public void drawPolygon( int[] xPoints, int[] yPoints, int points )
    {
        delegate.drawPolygon( xPoints, yPoints, points );
    }

    @Override
    public void rotate( double theta, double width, double height )
    {
        delegate.rotate( theta, width, height );
    }

    @Override
    public Composite getComposite()
    {
        return new JavaAwtComposite( delegate.getComposite() );
    }

    @Override
    public void drawGlyphVector( GlyphVector glyphVector, float x, float y )
    {
        java.awt.font.GlyphVector awtVector = ( (JavaAwtGlyphVector) glyphVector ).getDelegate();
        delegate.drawGlyphVector( awtVector, x, y );
    }

    @Override
    public void drawString( String text, float x, float y )
    {
        delegate.drawString( text, x, y );
    }

    @Override
    public void copyArea( int blitX, int blitY, int blitWidth, int blitHeight, int deltaX, int deltaY )
    {
        delegate.copyArea( blitX, blitY, blitWidth, blitHeight, deltaX, deltaY );
    }

    @Override
    public void setXORMode( Color color )
    {
        java.awt.Color awtColor = ( (JavaAwtColor) color ).getDelegate();
        delegate.setXORMode( awtColor );
    }

    @Override
    public void drawRect( int x, int y, int width, int height )
    {
        delegate.drawRect( x, y, width, height );
    }

    @Override
    public void fillOval( int x, int y, int width, int height )
    {
        delegate.fillOval( x, y, width, height );
    }

    @Override
    public void rotate( double angle )
    {
        delegate.rotate( angle );
    }

    @Override
    public void setClip( Bounds clip )
    {
        delegate.setClip( clip.x, clip.y, clip.width, clip.height );
    }

    @Override
    public void draw( Shape shapeToDraw )
    {
        java.awt.Shape awtShape = ( (JavaAwtShape) shapeToDraw ).getDelegate();
        delegate.draw( awtShape );
    }

    @Override
    public void transform( AffineTransform transform )
    {
        java.awt.geom.AffineTransform awtTransform = ( (JavaAwtAffineTransform) transform ).getDelegate();
        delegate.transform( awtTransform );
    }
}
