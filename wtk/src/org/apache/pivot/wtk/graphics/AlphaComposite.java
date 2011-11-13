package org.apache.pivot.wtk.graphics;

public interface AlphaComposite extends Composite
{
    AlphaComposite Clear = ColorFactory.getCompositeFactory().getClear();
    AlphaComposite SrcOver = ColorFactory.getCompositeFactory().getSrcOver();

    float getAlpha();
}
