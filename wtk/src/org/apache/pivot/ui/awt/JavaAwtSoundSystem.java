package org.apache.pivot.ui.awt;

import java.awt.Toolkit;
import org.apache.pivot.wtk.graphics.SoundSystem;

public class JavaAwtSoundSystem
    implements SoundSystem
{
    JavaAwtSoundSystem()
    {
    }

    @Override
    public void beep()
    {
        Toolkit.getDefaultToolkit().beep();
    }
}
