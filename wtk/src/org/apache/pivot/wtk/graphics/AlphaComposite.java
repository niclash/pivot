package org.apache.pivot.wtk.graphics;

public interface AlphaComposite extends Composite
{
    AlphaComposite Clear = new AlphaComposite();
    AlphaComposite SrcOver = new AlphaComposite();

    float getAlpha();
}
