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

import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.ComponentMouseWheelListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.ComponentTooltipListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Skin;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.graphics.font.FontFactory;

/**
 * Abstract base class for component skins.
 */
public abstract class ComponentSkin implements Skin, ComponentListener,
    ComponentStateListener, ComponentMouseListener, ComponentMouseButtonListener,
    ComponentMouseWheelListener, ComponentKeyListener, ComponentTooltipListener {
    private Component component = null;

    private int width = 0;
    private int height = 0;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public final int getBaseline() {
        return getBaseline(width, height);
    }

    @Override
    public int getBaseline(int width, int height) {
        return -1;
    }

    @Override
    public void install(Component component) {
        assert(this.component == null) : "Skin is already installed on a component.";

        component.getComponentListeners().add(this);
        component.getComponentStateListeners().add(this);
        component.getComponentMouseListeners().add(this);
        component.getComponentMouseButtonListeners().add(this);
        component.getComponentMouseWheelListeners().add(this);
        component.getComponentKeyListeners().add(this);
        component.getComponentTooltipListeners().add(this);

        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }

    /**
     * By default, skins are focusable.
     */
    @Override
    public boolean isFocusable() {
        return true;
    }

    /**
     * By default, skins are assumed to be opaque.
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    // Component events
    @Override
    public void parentChanged(Component component, Container previousParent) {
        // No-op
    }

    @Override
    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        // No-op
    }

    @Override
    public void preferredSizeChanged(Component component,
        int previousPreferredWidth, int previousPreferredHeight) {
        // No-op
    }

    @Override
    public void widthLimitsChanged(Component component, int previousMinimumWidth,
        int previousMaximumWidth) {
        // No-op
    }

    @Override
    public void heightLimitsChanged(Component component, int previousMinimumHeight,
        int previousMaximumHeight) {
        // No-op
    }

    @Override
    public void locationChanged(Component component, int previousX, int previousY) {
        // No-op
    }

    @Override
    public void visibleChanged(Component component) {
        // No-op
    }

    @Override
    public void cursorChanged(Component component, Cursor previousCursor) {
        // No-op
    }

    @Override
    public void tooltipTextChanged(Component component, String previousTooltipText) {
    }

    @Override
    public void tooltipDelayChanged(Component component, int previousTooltipDelay) {
    }

    @Override
    public void dragSourceChanged(Component component, DragSource previousDragSource) {
        // No-op
    }

    @Override
    public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
        // No-op
    }

    @Override
    public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
        // No-op
    }

    @Override
    public void nameChanged(Component component, String previousName) {
        // No-op
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        // No-op
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        // No-op
    }

    // Component mouse events
    @Override
    public boolean mouseMove(Component component, int x, int y) {
        return false;
    }

    @Override
    public void mouseOver(Component component) {
    }

    @Override
    public void mouseOut(Component component) {
    }

    // Component mouse button events
    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        return false;
    }

    // Component mouse wheel events
    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }

    // Component key events
    @Override
    public boolean keyTyped(Component component, char character) {
        return false;
    }

    /**
     * {@link Keyboard.Key#TAB TAB} Transfers focus forwards<br>
     * {@link Keyboard.Key#TAB TAB} + {@link Modifier#SHIFT SHIFT} Transfers focus
     * backwards
     */
    @Override
    public boolean keyPressed(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.Key.TAB
            && getComponent().isFocused()) {
            FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;

            // Transfer focus to the next component
            Component focusedComponent = component.transferFocus(direction);

            // Ensure that the focused component is visible
            if (component != focusedComponent
                && focusedComponent != null) {
                focusedComponent.scrollAreaToVisible(0, 0, focusedComponent.getWidth(),
                    focusedComponent.getHeight());
            }

            consumed = true;
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }

    @Override
    public void tooltipTriggered(Component component, int x, int y) {
        String tooltipText = component.getTooltipText();

        if (tooltipText != null) {
            Tooltip tooltip = new Tooltip(new Label(tooltipText));

            Display display = component.getDisplay();
            Point location = component.mapPointToAncestor(display, x, y);

            // Ensure that the tooltip stays on screen
            int tooltipX = location.x + 16;
            int tooltipY = location.y;

            int tooltipWidth = tooltip.getPreferredWidth();
            int tooltipHeight = tooltip.getPreferredHeight();
            if (tooltipX + tooltipWidth > display.getWidth()) {
                // Try to just fit it inside the display if
                // there would be room to shift it above the
                // cursor, otherwise move it to the left of
                // the cursor
                if (tooltipY > tooltipHeight) {
                    tooltipX = display.getWidth() - tooltipWidth;
                } else {
                    tooltipX = location.x - tooltipWidth - 16;
                }
                if (tooltipX < 0) {
                    tooltipX = 0;
                }
                // Adjust the y location if the tip ends up
                // being behind the mouse cursor because of
                // these x adjustments
                if (tooltipX < location.x && tooltipX + tooltipWidth > location.x) {
                    tooltipY -= tooltipHeight;
                    if (tooltipY < 0) {
                        tooltipY = 0;
                    }
                }
            }
            if (tooltipY + tooltipHeight > display.getHeight()) {
                tooltipY -= tooltipHeight;
            }

            tooltip.setLocation(tooltipX, tooltipY);
            tooltip.open(component.getWindow());
        }
    }

    // Utility methods
    protected void invalidateComponent() {
        if (component != null) {
            component.invalidate();
            component.repaint();
        }
    }

    protected void repaintComponent() {
        repaintComponent(false);
    }

    protected void repaintComponent(boolean immediate) {
        if (component != null) {
            component.repaint(immediate);
        }
    }

    protected void repaintComponent(Bounds area) {
        assert (area != null) : "area is null.";

        if (component != null) {
            component.repaint(area.x, area.y, area.width, area.height);
        }
    }

    protected void repaintComponent(int x, int y, int width, int height) {
        if (component != null) {
            component.repaint(x, y, width, height);
        }
    }

    protected void repaintComponent(int x, int y, int width, int height, boolean immediate) {
        if (component != null) {
            component.repaint(x, y, width, height, immediate);
        }
    }

    public static Font decodeFont(String value) {
        Font font;
        if (value.startsWith("{")) {
            try {
                font = Theme.deriveFont(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            font = Platform.getInstalled().getGraphicsSystem().getFontFactory().decode( value );
        }
        return font;
    }
}
