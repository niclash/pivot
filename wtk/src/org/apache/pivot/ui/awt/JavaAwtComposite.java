package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.AlphaComposite;

public class JavaAwtComposite
    implements AlphaComposite
{
    private java.awt.Composite delegate;

    JavaAwtComposite( java.awt.Composite awtComposite )
    {
        delegate = awtComposite;
    }

    java.awt.Composite getDelegate()
    {
        return delegate;
    }

    @Override
    public float getAlpha()
    {
        if( delegate instanceof AlphaComposite )
        {
            return ( (AlphaComposite) delegate ).getAlpha();
        }
        return 0.0f;
    }
}
