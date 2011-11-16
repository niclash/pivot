package org.apache.pivot.ui.awt;

import org.apache.pivot.wtk.graphics.AlphaComposite;
import org.apache.pivot.wtk.graphics.CompositeFactory;

public class JavaAwtCompositeFactory
    implements CompositeFactory
{
    private final static AlphaComposite CLEAR = new JavaAwtComposite( java.awt.AlphaComposite.Clear );
    private final static AlphaComposite SRC_OVER = new JavaAwtComposite( java.awt.AlphaComposite.SrcOver );
    private final static AlphaComposite DST_IN = new JavaAwtComposite( java.awt.AlphaComposite.DstIn );

    JavaAwtCompositeFactory()
    {

    }

    @Override
    public AlphaComposite getClear()
    {
        return CLEAR;
    }

    @Override
    public AlphaComposite getSrcOver()
    {
        return SRC_OVER;
    }

    @Override
    public AlphaComposite getSrcOver( float opacity )
    {
        return new JavaAwtComposite( java.awt.AlphaComposite.getInstance( java.awt.AlphaComposite.SRC_OVER, opacity ) );
    }

    @Override
    public AlphaComposite getDstIn()
    {
        return DST_IN;
    }
}
