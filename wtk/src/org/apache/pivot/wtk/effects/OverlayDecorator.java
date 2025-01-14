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
package org.apache.pivot.wtk.effects;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.Graphics2D;

/**
 * Decorator that overlays a component on top of another component. The
 * overlay component is strictly visual and does not respond to user input.
 */
public class OverlayDecorator implements Decorator {
    private Component overlay;
    private Graphics2D graphics = null;

    public OverlayDecorator() {
        this(null);
    }

    public OverlayDecorator(Component overlay) {
        this.overlay = overlay;
    }

    public Component getOverlay() {
        return overlay;
    }

    public void setOverlay(Component overlay) {
        this.overlay = overlay;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        overlay.setSize(component.getSize());
        overlay.validate();

        return graphics;
    }

    @Override
    public void update() {
        overlay.paint(graphics);

        graphics = null;
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(0, 0, component.getWidth(), component.getHeight());
    }

    @Override
    public AffineTransform getTransform(Component component) {
        return Platform.getInstalled().getGraphicsSystem().getAffineTransformFactory().newAffineTransform();
    }
}
