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
 * Decorator that applies a grayscale conversion to a component.
 */
public class GrayscaleDecorator implements Decorator {
    private Graphics2D graphics = null;

    private BufferedImage bufferedImage = null;
    private Graphics2D bufferedImageGraphics = null;

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.graphics = graphics;

        int width = component.getWidth();
        int height = component.getHeight();

        /* To convert to gray, we create a BufferedImage in the grayscale color
         * space into which the decorated component draws, and we output the
         * resulting image. The naive way to create the buffer is new
         * BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY); but that
         * doesn't respect transparency. Hence the following more complicated
         * method.
         */

        if (bufferedImage == null || bufferedImage.getWidth() < width
            || bufferedImage.getHeight() < height) {
            bufferedImage = bufferedImage.toGrayScale( width, height );
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
