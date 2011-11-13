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

import org.apache.pivot.ui.awt.DisplayHost;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.skin.DisplaySkin;

/**
 * Container that serves as the root of a component hierarchy.
 */
public final class Display extends Container {
    private DisplayHost displayHost;

    public Display(DisplayHost displayHost) {
        this.displayHost = displayHost;
        super.setSkin(new DisplaySkin());
    }

    public DisplayHost getDisplayHost() {
        return displayHost;
    }

    public java.awt.Window getHostWindow() {
        java.awt.Container parent = displayHost.getParent();
        while (parent != null
            && !(parent instanceof java.awt.Window)) {
            parent = parent.getParent();
        }

        if (parent == null) {
            throw new IllegalArgumentException("Window does not have a native host.");
        }

        return (java.awt.Window)parent;
    }

    @Override
    protected void setSkin(Skin skin) {
        throw new UnsupportedOperationException("Can't replace Display skin.");
    }

    @Override
    protected void setParent(Container parent) {
        throw new UnsupportedOperationException("Display can't have a parent.");
    }

    @Override
    public void setLocation(int x, int y) {
        throw new UnsupportedOperationException("Can't change the location of the display.");
    }

    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException("Can't change the visibility of the display.");
    }

    @Override
    public void setTooltipText(String tooltipText) {
        throw new UnsupportedOperationException("Can't set a tooltip on the display.");
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (immediate) {
            displayHost.immediateRepaint( x,y,width,height);
        } else {
            displayHost.repaint(x, y, width, height);
        }
    }

    @Override
    public void insert(Component component, int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance "
               + "of " + Window.class);
        }

        super.insert(component, index);
    }

    @Override
    protected void descendantAdded(Component descendant) {
        super.descendantAdded(descendant);

        String automationID = descendant.getAutomationID();

        if (automationID != null) {
            Automation.add(automationID, descendant);
        }
    }

    @Override
    protected void descendantRemoved(Component descendant) {
        super.descendantRemoved(descendant);

        String automationID = descendant.getAutomationID();

        if (automationID != null) {
            Automation.remove(automationID);
        }
    }

    @Override
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        return null;
    }
}
