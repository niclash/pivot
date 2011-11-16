/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk;

import org.apache.pivot.collections.EnumSet;
import org.apache.pivot.collections.Set;

/**
 * Class representing the system keyboard.
 */
public final class Keyboard {


    /**
     * Enumeration representing keyboard modifiers.
     */
    public enum Modifier {
        SHIFT,
        CTRL,
        ALT,
        META;
    }


    /**
     * Enumeration representing key locations.
     */
    public enum KeyLocation {
        STANDARD,
        LEFT,
        RIGHT,
        KEYPAD;
    }


    public enum Key {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,  // A-Z
        N0,N1,N2,N3,N4,N5,N6,N7,N8,N9,                        // Standard number keys 0-9
        KEYPAD_0,KEYPAD_1,KEYPAD_2,KEYPAD_3,KEYPAD_4,KEYPAD_5,KEYPAD_6,KEYPAD_7,KEYPAD_8,KEYPAD_9,  // Keypad 0-9
        UP, DOWN, RIGHT, LEFT, PAGE_UP, PAGE_DOWN, HOME, END, // Navigation keys
        KEYPAD_UP, KEYPAD_DOWN, KEYPAD_RIGHT, KEYPAD_LEFT,    // Navigation keys
        TAB, SPACE, ENTER, ESCAPE, BACKSPACE, DELETE, INSERT,
        PLUS, MINUS, EQUALS,
        ADD, SUBTRACT, MULTIPLY, DIVIDE,
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
        UNDEFINED;
    }
    /**
     * Represents a keystroke, a combination of a keycode and modifier flags.
     */
    public static final class KeyStroke {

        private Key keyCode = Key.UNDEFINED;
        private Set<Modifier> modifiers;
        public KeyStroke(Key keyCode, Set<Modifier> modifiers) {
            this.keyCode = keyCode;
            this.modifiers = modifiers;
        }

        public Key getKeyCode() {
            return keyCode;
        }

        public Set<Modifier> getModifiers() {
            return this.modifiers;
        }

        @Override
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            KeyStroke keyStroke = (KeyStroke) o;
            return keyCode == keyStroke.keyCode && modifiers == keyStroke.modifiers;
        }

        @Override
        public int hashCode()
        {
            int result = keyCode.hashCode();
            result = 31 * result + modifiers.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return String.format( "KeyStroke{keyCode=%s, modifiers=%s}", keyCode, modifiers );
        }

        public static KeyStroke decode(String value) {
            return Platform.getInstalled().getInputSystem().decodeKeyStroke( value );
        }

    }
    private static Set<Modifier> modifiers = new EnumSet<Modifier>(Modifier.class);

    /**
     * Returns a bitfield representing the keyboard modifiers that are
     * currently pressed.
     * @return A set of Modifier enums representing the current state of the Keyboard Modifier keys.
     */
    public static Set<Modifier> getModifiers() {
        return modifiers;
    }

    public static boolean isNoModifiers()
    {
        return modifiers.getCount() == 0;
    }

    public static void setModifier( Modifier modifier ){
        modifiers.add( modifier );
    }

    public static void clearModifier( Modifier modifier ){
        modifiers.remove( modifier );
    }

    /**
     * Tests the pressed state of a modifier.
     *
     * @param modifier
     *
     * @return
     * <tt>true</tt> if the modifier is pressed; <tt>false</tt>, otherwise.
     */
    public static boolean isPressed(Modifier modifier) {
        return modifiers.contains( modifier );
    }

    /**
     * Returns the current drop action.
     *
     * @return
     * The drop action corresponding to the currently pressed modifier keys,
     * or <tt>null</tt> if no modifiers are pressed.
     */
    public static DropAction getDropAction() {
        // TODO Return an appropriate action for OS:
        // Windows: no modifier - move; control - copy; control-shift - link
        // Mac OS X: no modifier - move; option - copy; option-command - link

        DropAction dropAction = null;

        if (isPressed(Modifier.CTRL)
            && isPressed(Modifier.SHIFT)) {
            dropAction = DropAction.LINK;
        } else if (isPressed(Modifier.CTRL)) {
            dropAction = DropAction.COPY;
        } else {
            dropAction = DropAction.MOVE;
        }

        return dropAction;
    }
}
