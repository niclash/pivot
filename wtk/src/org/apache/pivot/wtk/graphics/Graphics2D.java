package org.apache.pivot.wtk.graphics;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.geom.Shape;

public interface Graphics2D
{
    public void setColor( Color color );

    public void setStroke( BasicStroke basicStroke );

    public void clipRect( int x, int y, int width, int height );

    public Bounds getClipBounds();

    public void fillRect( int x, int y, int width, int height );

    public GraphicsConfiguration getDeviceConfiguration();

    public void setClip( int x, int y, int width, int height );

    public void translate( int x, int y );

    public void dispose();

    public void drawImage( BufferedImage bufferedImage, int x, int y, ImageObserver observer );

    public void setRenderingHint( RenderingHintsKey keyRendering, Object valueRenderSpeed );

    public void scale( double scaleX, double scaleY );

    public void setComposite( AlphaComposite clear );

    public void drawImage( VolatileImage volatileImage, int x, int y, ImageObserver observer );

    public AffineTransform getTransform();

    public Graphics2D create();

    public Graphics2D create(int x, int y, int width, int height);

    public void setPaint( Paint backgroundPaint );

    public void fillRoundRect( int x,
                               int y,
                               int width,
                               int height,
                               int arcWidth,
                               int arcHeight
    );

    public void setFont( Font font );

    public void drawString( String text, int x, int y );

    public Bounds getClip();

    public void clip( Shape clipShape );

    public void fill( Shape shape );

    public void drawLine( int x1, int y1, int x2, int y2 );

    public void fillPolygon( int[] xPoints, int[] yPoints, int points );

    public void drawPolygon( int[] xPoints, int[] yPoints, int points );

    public void rotate( double theta, double width, double height );

    public Composite getComposite();

    public void drawGlyphVector( GlyphVector glyphVector, float x, float y );

    public void drawString( String text, float x, float y );

    public void copyArea( int blitX, int blitY, int blitWidth, int blitHeight, int deltaX, int deltaY );

    public void setXORMode( Color color );

    public void drawRect( int x, int y, int width, int height );

    public void fillOval( int x, int y, int width, int height );

    public void rotate( double angle );

    public void setClip( Bounds clip )  ;

    public void draw( Shape shapeToDraw);

    void transform( AffineTransform transform );

    void clip( Bounds selection );
}
