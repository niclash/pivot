package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.WritableRaster;

public class JavaAwtWritableRaster
    implements WritableRaster
{
    private java.awt.image.WritableRaster delegate;

    JavaAwtWritableRaster( java.awt.image.WritableRaster awt )
    {
        delegate = awt;
    }

    java.awt.image.WritableRaster getDelegate()
    {
        return delegate;
    }

    @Override
    public void setDataElements( int x, int y, int width, int height, int[] buffer )
    {
        delegate.setDataElements( x, y, width, height, buffer );
    }

    @Override
    public void getDataElements( int x, int y, int width, int height, int[] buffer )
    {
        delegate.getDataElements( x, y, width, height, buffer );
    }
}
