package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.BasicStroke;

public class JavaAwtBasicStroke
    implements BasicStroke
{
    private java.awt.BasicStroke delegate;

    JavaAwtBasicStroke(java.awt.BasicStroke basicStroke)
    {
        delegate = basicStroke;
    }

    java.awt.BasicStroke getDelegate() {
        return delegate;
    }
}
