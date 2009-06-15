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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.SliderSkin;


/**
 * Terra slider skin.
 *
 * @author gbrown
 */
public class TerraSliderSkin extends SliderSkin {
    /**
     * Slider thumb component.
     *
     * @author gbrown
     */
    protected class Thumb extends Component {
        public Thumb() {
            setSkin(new ThumbSkin());
        }
    }

    /**
     * Slider thumb skin.
     *
     * @author gbrown
     */
    protected class ThumbSkin extends ComponentSkin {
        private boolean highlighted = false;

        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            return 0;
        }

        public int getPreferredHeight(int width) {
            return 0;
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(new GradientPaint(width / 2, 0, buttonBevelColor,
                width / 2, height, buttonBackgroundColor));
            graphics.fillRect(0, 0, width, height);

            float alpha = (highlighted
                || dragOffset != null) ? 0.25f : 0.0f;
            graphics.setPaint(new Color(0, 0, 0, alpha));
            graphics.fillRect(0, 0, width, height);

            graphics.setPaint(buttonBorderColor);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
        }

        @Override
        public void enabledChanged(Component component) {
            super.enabledChanged(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (Mouse.getCapturer() == component) {
                Slider slider = (Slider)TerraSliderSkin.this.getComponent();
                int sliderWidth = slider.getWidth();
                int thumbWidth = thumb.getWidth();

                Point sliderLocation = thumb.mapPointToAncestor(slider, x, y);
                int sliderX = sliderLocation.x;

                int minX = dragOffset.x;
                if (sliderX < minX) {
                    sliderX = minX;
                }

                int maxX = (sliderWidth - thumbWidth) + dragOffset.x;
                if (sliderX > maxX) {
                    sliderX = maxX;
                }

                float ratio = (float)(sliderX - dragOffset.x) / (sliderWidth - thumbWidth);

                int minimum = slider.getMinimum();
                int maximum = slider.getMaximum();

                int value = (int)(minimum + (maximum - minimum) * ratio);
                slider.setValue(value);
            }

            return consumed;
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                dragOffset = new Point(x, y);
                Mouse.capture(component);
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (Mouse.getCapturer() == component) {
                dragOffset = null;
                Mouse.release();
                repaintComponent();
            }

            return consumed;
        }
    }

    private Thumb thumb = new Thumb();
    Point dragOffset = null;

    private Color trackColor;
    private int trackWidth;
    private Color buttonBackgroundColor;
    private Color buttonBorderColor;
    private int thumbWidth;
    private int thumbHeight;

    // Derived colors
    private Color buttonBevelColor;

    public static final int DEFAULT_WIDTH = 120;
    public static final int MINIMUM_THUMB_WIDTH = 4;
    public static final int MINIMUM_THUMB_HEIGHT = 4;

    public TerraSliderSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        trackColor = theme.getColor(6);
        trackWidth = 2;
        buttonBackgroundColor = theme.getColor(10);
        buttonBorderColor = theme.getColor(7);

        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);

        thumbWidth = 8;
        thumbHeight = 16;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Slider slider = (Slider)component;
        slider.add(thumb);
    }

    public void uninstall() {
        Slider slider = (Slider)getComponent();
        slider.remove(thumb);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        return DEFAULT_WIDTH;
    }

    public int getPreferredHeight(int width) {
        return thumbHeight;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        Slider slider = (Slider)getComponent();

        int width = getWidth();
        int height = getHeight();

        int minimum = slider.getMinimum();
        int maximum = slider.getMaximum();
        int value = slider.getValue();

        float ratio = (float)(value - minimum) / (maximum - minimum);

        thumb.setSize(thumbWidth, thumbHeight);
        thumb.setLocation((int)((width - thumbWidth) * ratio),
            (height - thumbHeight) / 2);
    }

    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setColor(trackColor);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        GraphicsUtilities.drawLine(graphics, 0, (height - trackWidth) / 2,
            width, Orientation.HORIZONTAL, trackWidth);
    }

    public Color getTrackColor() {
        return trackColor;
    }

    public void setTrackColor(Color trackColor) {
        if (trackColor == null) {
            throw new IllegalArgumentException("trackColor is null.");
        }

        this.trackColor = trackColor;
        repaintComponent();
    }

    public final void setTrackColor(String trackColor) {
        if (trackColor == null) {
            throw new IllegalArgumentException("trackColor is null");
        }

        setTrackColor(GraphicsUtilities.decodeColor(trackColor));
    }

    public int getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(int trackWidth) {
        this.trackWidth = trackWidth;
        repaintComponent();
    }

    public void setTrackWidth(Number trackWidth) {
        if (trackWidth == null) {
            throw new IllegalArgumentException("trackWidth is null.");
        }

        setTrackWidth(trackWidth.intValue());
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null.");
        }

        this.buttonBackgroundColor = buttonBackgroundColor;
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
        repaintComponent();
    }

    public final void setButtonBackgroundColor(String buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null");
        }

        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColor));
    }

    public Color getButtonBorderColor() {
        return buttonBorderColor;
    }

    public void setButtonBorderColor(Color buttonBorderColor) {
        if (buttonBorderColor == null) {
            throw new IllegalArgumentException("buttonBorderColor is null.");
        }

        this.buttonBorderColor = buttonBorderColor;
        repaintComponent();
    }

    public final void setButtonBorderColor(String buttonBorderColor) {
        if (buttonBorderColor == null) {
            throw new IllegalArgumentException("buttonBorderColor is null.");
        }

        setButtonBorderColor(GraphicsUtilities.decodeColor(buttonBorderColor));
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        if (thumbWidth < MINIMUM_THUMB_WIDTH) {
            throw new IllegalArgumentException("thumbWidth must be greater than or equal to "
                + MINIMUM_THUMB_WIDTH);
        }

        this.thumbWidth = thumbWidth;
        invalidateComponent();
    }

    public void setThumbWidth(Number thumbWidth) {
        if (thumbWidth == null) {
            throw new IllegalArgumentException("thumbWidth is null.");
        }

        setThumbWidth(thumbWidth.intValue());
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        if (thumbHeight < MINIMUM_THUMB_HEIGHT) {
            throw new IllegalArgumentException("thumbHeight must be greater than or equal to "
                + MINIMUM_THUMB_HEIGHT);
        }

        this.thumbHeight = thumbHeight;
        invalidateComponent();
    }

    public void setThumbHeight(Number thumbHeight) {
        if (thumbHeight == null) {
            throw new IllegalArgumentException("thumbHeight is null.");
        }

        setThumbHeight(thumbHeight.intValue());
    }

    public void valueChanged(Slider slider, int previousValue) {
        layout();
    }
}