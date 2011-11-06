package org.apache.pivot.wtk.graphics;

public interface DisplayHost
{
    Graphics2D getGraphics2D();

    double getScale();

    void paint( Graphics2D graphics );

    void repaint( int x, int y, int width, int height );

    boolean isFocusOwner();

    void requestFocusInWindow();
}
