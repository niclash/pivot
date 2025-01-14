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
import org.apache.pivot.wtk.graphics.BufferedImage;
import org.apache.pivot.wtk.graphics.Graphics2D;

/**
 * Decorator that applies a blur to a component.
 * <p>
 * Blurs are given an integer magnitude, which represents the intensity of
 * the blur. This value translates to a grid of pixels (<tt>blurMagnitude^2</tt>),
 * where each pixel value is calculated by consulting its neighboring pixels
 * according to the grid. Because of this, note that you will get "prettier"
 * blurring if you choose odd values for the blur magnitude; this allows the
 * pixel in question to reside at the center of the grid, thus preventing any
 * arbitrary shifting of pixels. Also note that the greater the intensity of
 * the blur, the greater the intensity of the calculations necessary to
 * accomplish the blur (and the longer it will take to perform the blur).
 * <p>
 * TODO Increase size of buffered image to account for edge conditions of the
 * blur.
 * <p>
 * TODO Use unequal values in the blur kernel to make pixels that are farther
 * away count less towards the blur.
 */
public class BlurDecorator implements Decorator {
    private int blurMagnitude;

    private Graphics2D graphics = null;

    private BufferedImage bufferedImage = null;
    private Graphics2D bufferedImageGraphics = null;

    /**
     * Creates a <tt>BlurDecorator</tt> with the default blur magnitude.
     *
     * @see #BlurDecorator(int)
     */
    public BlurDecorator() {
        this(9);
    }

    /**
     * Creates a <tt>BlurDecorator</tt> with the specified blur magnitude.
     *
     * @param blurMagnitude
     * The intensity of the blur.
     */
    public BlurDecorator(int blurMagnitude) {
        this.blurMagnitude = blurMagnitude;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        int width = component.getWidth();
        int height = component.getHeight();

        if (bufferedImage == null
            || bufferedImage.getWidth() != width
            || bufferedImage.getHeight() != height) {
            bufferedImage = Platform.getInstalled().getGraphicsSystem().newBufferedImage(width, height);
        }

        bufferedImageGraphics = bufferedImage.createGraphics();
        bufferedImageGraphics.setClip(graphics.getClip());

        return bufferedImageGraphics;
    }

    @Override
    public void update() {
        bufferedImageGraphics.dispose();
        bufferedImageGraphics = null;

        bufferedImage.flush();

        float[] kernel = new float[blurMagnitude * blurMagnitude];
        for (int i = 0, n = kernel.length; i < n; i++) {
            kernel[i] = 1f / n;
        }

        bufferedImage = bufferedImage.blur( 0, kernel );
        graphics.drawImage(bufferedImage, 0, 0, null);

        bufferedImage = null;
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
