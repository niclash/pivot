package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.font.FontRenderContext;

public class JavaAwtFontRenderContext
    implements FontRenderContext
{
    private java.awt.font.FontRenderContext delegate;

    JavaAwtFontRenderContext(java.awt.font.FontRenderContext awtContext)
    {
        delegate = awtContext;
    }

    java.awt.font.FontRenderContext getDelegate()
    {
        return delegate;
    }

    @Override
    public String getAntiAliasingHint()
    {
        return (String) delegate.getAntiAliasingHint();
    }

    @Override
    public String getFractionalMetricsHint()
    {
        return (String) delegate.getFractionalMetricsHint();
    }
}
