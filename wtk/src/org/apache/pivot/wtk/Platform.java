package org.apache.pivot.wtk;

import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.SoundSystem;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;

public abstract class Platform
{
    private static Platform installed;

    public abstract GraphicsSystem getGraphicsSystem();

    public abstract SoundSystem getSoundSystem();

    public abstract FontRenderContext getFontRenderContext();

    public abstract int getMultiClickInterval();

    public abstract int getCursorBlinkRate();

    public abstract int getDragThreshold();

    public abstract Keyboard.Modifier getCommandModifier();

    public abstract Keyboard.Modifier getWordNavigationModifier();

    public abstract String getKeyStrokeModifierSeparator();

    public abstract int getNumberOfMouseButtons();

    public static Platform getInstalled()
    {
        return installed;
    }

    public static void installPlatform( Platform platform )
    {
        installed = platform;
    }

    public InputSystem getInputSystem()
    {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
