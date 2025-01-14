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

import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.graphics.AlphaComposite;
import org.apache.pivot.wtk.graphics.Color;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ImageViewListener;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.graphics.Composite;
import org.apache.pivot.wtk.graphics.Graphics2D;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.Transparency;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.ImageListener;

/**
 * Image view skin.
 * <p>
 * TODO Add a rotation (float) style.
 */
public class ImageViewSkin extends ComponentSkin implements ImageViewListener {
    private Color backgroundColor = null;
    private float opacity = 1.0f;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private boolean fill = false;
    private boolean preserveAspectRatio = true;

    private int imageX = 0;
    private int imageY = 0;
    private float scaleX = 1;
    private float scaleY = 1;

    private ImageListener imageListener = new ImageListener() {
        @Override
        public void sizeChanged(Image image, int previousWidth, int previousHeight) {
            invalidateComponent();
        }

        @Override
        public void baselineChanged(Image image, int previousBaseline) {
            invalidateComponent();
        }

        @Override
        public void regionUpdated(Image image, int x, int y, int width, int height) {
            // TODO A rounding error is causing an off-by-one error; we're
            // accounting for it here by adding 1 to width and height
            Bounds bounds = new Bounds(imageX + (int)Math.floor(x * scaleX),
                imageY + (int)Math.floor(y * scaleY),
                (int)Math.ceil(width * scaleX) + 1,
                (int)Math.ceil(height * scaleY) + 1);
            repaintComponent(bounds);
        }
    };

    @Override
    public void install(Component component) {
        super.install(component);

        ImageView imageView = (ImageView)component;
        imageView.getImageViewListeners().add(this);

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }
    }

    @Override
    public int getPreferredWidth(int height) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getWidth();
    }

    @Override
    public int getPreferredHeight(int width) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getHeight();
    }

    @Override
    public Dimensions getPreferredSize() {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? new Dimensions(0, 0) : new Dimensions(image.getWidth(),
            image.getHeight());
    }

    @Override
    public int getBaseline(int width, int height) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        int baseline = -1;

        if (image != null) {
            baseline = image.getBaseline();

            if (baseline != -1) {
                Dimensions imageSize = image.getSize();

                if (fill) {
                    // Scale to fit
                    if (preserveAspectRatio) {
                        float aspectRatio = (float)width / (float)height;
                        float imageAspectRatio = (float)imageSize.width / (float)imageSize.height;

                        if (aspectRatio > imageAspectRatio) {
                            baseline *= (float)height / (float)imageSize.height;
                        } else {
                            float scaleY = (float)width / (float)imageSize.width;
                            baseline *= scaleY;
                            baseline += (int)(height - imageSize.height * scaleY) / 2;
                        }
                    } else {
                        baseline *= (float)height / (float)imageSize.height;
                    }
                } else {
                    if (verticalAlignment == VerticalAlignment.CENTER) {
                        baseline += (height - imageSize.height) / 2;
                    } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
                        baseline += height - imageSize.height;
                    }
                }
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            int width = getWidth();
            int height = getHeight();

            Dimensions imageSize = image.getSize();

            if (fill) {
                // Scale to fit
                if (preserveAspectRatio) {
                    float aspectRatio = (float)width / (float)height;
                    float imageAspectRatio = (float)imageSize.width / (float)imageSize.height;

                    if (aspectRatio > imageAspectRatio) {
                        imageY = 0;
                        scaleY = (float)height / (float)imageSize.height;

                        imageX = (int)(width - imageSize.width * scaleY) / 2;
                        scaleX = scaleY;
                    } else {
                        imageX = 0;
                        scaleX = (float)width / (float)imageSize.width;

                        imageY = (int)(height - imageSize.height * scaleX) / 2;
                        scaleY = scaleX;
                    }
                } else {
                    imageX = 0;
                    scaleX = (float)width / (float)imageSize.width;

                    imageY = 0;
                    scaleY = (float)height / (float)imageSize.height;
                }
            } else {
                if (horizontalAlignment == HorizontalAlignment.CENTER) {
                    imageX = (width - imageSize.width) / 2;
                } else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
                    imageX = width - imageSize.width;
                } else {
                    imageX = 0;
                }

                scaleX = 1.0f;

                if (verticalAlignment == VerticalAlignment.CENTER) {
                    imageY = (height - imageSize.height) / 2;
                } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
                    imageY = height - imageSize.height;
                } else {
                    imageY = 0;
                }

                scaleY = 1.0f;
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (image != null) {
            Graphics2D imageGraphics = (Graphics2D)graphics.create();
            imageGraphics.translate(imageX, imageY);
            imageGraphics.scale(scaleX, scaleY);

            // Apply an alpha composite if the opacity value is less than
            // the current alpha
            float alpha = 1.0f;

            Composite composite = imageGraphics.getComposite();
            if (composite instanceof AlphaComposite ) {
                AlphaComposite alphaComposite = (AlphaComposite)composite;
                alpha = alphaComposite.getAlpha();
            }

            if (opacity < alpha) {
                GraphicsSystem graphicsFactory = Platform.getInstalled().getGraphicsSystem();
                AlphaComposite alphaComposite = graphicsFactory.getColorFactoryProvider().getCompositeFactory().getSrcOver( opacity );
                imageGraphics.setComposite(alphaComposite);
            }

            image.paint(imageGraphics);
            imageGraphics.dispose();
        }
    }

    /**
     * @return
     * <tt>false</tt>; image views are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity out of range [0,1].");
        }

        this.opacity = opacity;
        repaintComponent();
    }

    public final void setOpacity(Number opacity) {
        if (opacity == null) {
            throw new IllegalArgumentException("opacity is null.");
        }

        setOpacity(opacity.floatValue());
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        layout();
        repaintComponent();
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        layout();
        repaintComponent();
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
        layout();
        repaintComponent();
    }

    public boolean getPreserveAspectRatio() {
        return preserveAspectRatio;
    }

    public void setPreserveAspectRatio(boolean preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
        layout();
        repaintComponent();
    }

    // Image view events
    @Override
    public void imageChanged(ImageView imageView, Image previousImage) {
        if (previousImage != null) {
            previousImage.getImageListeners().remove(imageListener);
        }

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }

        invalidateComponent();
    }

    @Override
    public void asynchronousChanged(ImageView imageView) {
        // No-op
    }
}
