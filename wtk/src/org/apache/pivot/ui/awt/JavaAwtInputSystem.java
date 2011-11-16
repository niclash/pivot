package org.apache.pivot.ui.awt;

import java.lang.reflect.Field;
import java.util.Locale;
import org.apache.pivot.collections.EnumSet;
import org.apache.pivot.collections.Set;
import org.apache.pivot.wtk.InputSystem;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Platform;

public class JavaAwtInputSystem implements InputSystem
{
    public static final String COMMAND_ABBREVIATION = "CMD";

    @Override
    public Keyboard.KeyStroke decodeKeyStroke( String value )
    {
        if (value == null) {
            throw new IllegalArgumentException("value is null.");
        }

        int keyCode = JavaAwtKeyCode.UNDEFINED;
        Set<Keyboard.Modifier> modifiers = new EnumSet<Keyboard.Modifier>( Keyboard.Modifier.class );

        String[] keys = value.split("-");
        for (int i = 0, n = keys.length; i < n; i++) {
            if (i < n - 1) {
                // Modifier
                String modifierAbbreviation = keys[i].toUpperCase( Locale.ENGLISH);
                modifiers.add( convert( modifierAbbreviation ) );
            } else {
                // Keycode
                try {
                    Field keyCodeField = JavaAwtKeyCode.class.getField(keys[i].toUpperCase(Locale.ENGLISH));
                    keyCode = (Integer)keyCodeField.get(null);
                } catch(Exception exception) {
                    throw new IllegalArgumentException(exception);
                }
            }
        }

        return new Keyboard.KeyStroke(keyCode, modifiers);
    }

    private Keyboard.Modifier convert( String modifierAbbreviation ) {
        Keyboard.Modifier modifier;
        if (modifierAbbreviation.equals(COMMAND_ABBREVIATION)) {
            modifier = Platform.getInstalled().getCommandModifier();
        } else {
            modifier = Keyboard.Modifier.valueOf( modifierAbbreviation );
        }
        return modifier;
    }
}
