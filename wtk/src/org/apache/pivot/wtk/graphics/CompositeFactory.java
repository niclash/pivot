package org.apache.pivot.wtk.graphics;

public interface CompositeFactory
{
    AlphaComposite getClear();

    AlphaComposite getSrcOver();

    AlphaComposite getSrcOver( float opacity );

    AlphaComposite getDstIn();
}
