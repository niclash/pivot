package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.Paint;

public class JavaAwtPaint
    implements Paint
{
    private java.awt.Paint delegate;

    JavaAwtPaint(java.awt.Paint awtPaint)
    {
        delegate = awtPaint;
    }

    java.awt.Paint getDelegate()
    {
        return delegate;
    }

    @Override
    public int getTransparency()
    {
        return delegate.getTransparency();
    }
}
