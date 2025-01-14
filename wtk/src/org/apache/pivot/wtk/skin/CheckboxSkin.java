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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;

/**
 * Abstract base class for checkbox skins.
 */
public abstract class CheckboxSkin extends ButtonSkin {
    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        Checkbox checkbox = (Checkbox)getComponent();

        checkbox.requestFocus();
        checkbox.press();

        return consumed;
    }

    /**
     * {@link Keyboard.Key#SPACE SPACE} 'presses' the button.
     */
    @Override
    public boolean keyReleased(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Checkbox checkbox = (Checkbox)getComponent();

        if (keyCode == Keyboard.Key.SPACE) {
            checkbox.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }
}
