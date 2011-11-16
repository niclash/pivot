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

import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.graphics.AffineTransform;
import org.apache.pivot.wtk.graphics.AlphaComposite;
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.ColorFactory;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.graphics.GradientPaint;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.Paint;

/**
 * Decorator that paints a reflection of a component.
 * <p>
 * TODO Make gradient properties configurable.
 */
public class ReflectionDecorator implements Decorator {
    private Component component = null;
    private Graphics2D graphics = null;

    private BufferedImage componentImage = null;
    private Graphics2D componentImageGraphics = null;

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.component = component;
        this.graphics = graphics;

        int width = component.getWidth();
        int height = component.getHeight();

        componentImage = Platform.getInstalled().getGraphicsSystem().newBufferedImage(width, height);
        componentImageGraphics = componentImage.createGraphics();

        // Clear the image background
        componentImageGraphics.setComposite(AlphaComposite.Clear);
        componentImageGraphics.fillRect(0, 0, componentImage.getWidth(), componentImage.getHeight());

        componentImageGraphics.setComposite( AlphaComposite.SrcOver);

        return componentImageGraphics;
    }

    @Override
    public void update() {
        // Draw the component
        graphics.drawImage(componentImage, 0, 0, null);

        // Draw the reflection
        int width = componentImage.getWidth();
        int height = componentImage.getHeight();
        GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();
        Paint mask = graphicsFactory.newGradientPaint(0, height / 4f, ColorFactory.create( 1.0f, 1.0f, 1.0f, 0.0f ),
            0, height, ColorFactory.create(1.0f, 1.0f, 1.0f, 0.5f));
        componentImageGraphics.setPaint(mask);
        AlphaComposite dstIn = graphicsFactory.getColorFactoryProvider().getCompositeFactory().getDstIn();
        componentImageGraphics.setComposite(dstIn);
        componentImageGraphics.fillRect(0, 0, width, height);

        componentImageGraphics.dispose();
        componentImageGraphics = null;

        componentImage.flush();

        graphics.transform(getTransform(component));

        graphics.drawImage(componentImage, 0, 0, null);

        componentImage = null;
        component = null;
        graphics = null;
    }

    @Override
    public Bounds getBounds(Component component) {
        return new Bounds(0, 0, component.getWidth(), component.getHeight() * 2);
    }

    @Override
    public AffineTransform getTransform(Component component) {
        AffineTransform transform = Platform.getInstalled().getGraphicsSystem().getAffineTransformFactory().newScaleTransform(1.0,-1.0);
        transform.translate(0, -(component.getHeight() * 2));
        return transform;
    }
}
