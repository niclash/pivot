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
package org.apache.pivot.ui.awt;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.graphics.GraphicsSystem;
import org.apache.pivot.wtk.graphics.RenderingHints;
import org.apache.pivot.wtk.graphics.SoundSystem;
import org.apache.pivot.wtk.graphics.font.FontRenderContext;

/**
 * Provides platform-specific information.
 */
public class JavaAwtPlatform extends Platform
{
    private FontRenderContext fontRenderContext;

    private final int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private final int DEFAULT_CURSOR_BLINK_RATE = 600;

    private final Modifier COMMAND_MODIFIER;
    private final Modifier WORD_NAVIGATION_MODIFIER;
    private final String KEYSTROKE_MODIFIER_SEPARATOR;

    public JavaAwtPlatform() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (osName.startsWith("mac os x")) {
            COMMAND_MODIFIER = Modifier.META;
            WORD_NAVIGATION_MODIFIER = Modifier.ALT;
            KEYSTROKE_MODIFIER_SEPARATOR = "";
        } else if (osName.startsWith("windows")) {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "+";
        } else {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "-";
        }

        // Initialize the font render context
        initializeFontRenderContext();

        // Listen for changes to the font desktop hints property
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                initializeFontRenderContext();
                ApplicationContext.invalidateDisplays();
            }
        });
    }

    @Override
    public GraphicsSystem getGraphicsSystem()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SoundSystem getSoundSystem()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    private void initializeFontRenderContext() {
        Object aaHint = null;
        Object fmHint = null;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints = (java.util.Map<?, ?>)toolkit.getDesktopProperty("awt.font.desktophints");

        if (fontDesktopHints != null) {
            aaHint = fontDesktopHints.get( RenderingHints.KEY_TEXT_ANTIALIASING);
            fmHint = fontDesktopHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
        }
        if (aaHint == null) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        }
        if (fmHint == null) {
            fmHint = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
        }

        final String antiAliasingHint = (String) aaHint;
        final String fractionalMetricsHint = (String) fmHint;
        fontRenderContext = new FontRenderContext(){

            @Override
            public String getAntiAliasingHint()
            {
                return antiAliasingHint;
            }

            @Override
            public String getFractionalMetricsHint()
            {
                return fractionalMetricsHint;
            }
        };
    }

    /**
     * Returns the system multi-click interval.
     */
    @Override
    public int getMultiClickInterval() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer multiClickInterval = (Integer)toolkit.getDesktopProperty("awt.multiClickInterval");

        if (multiClickInterval == null) {
            multiClickInterval = DEFAULT_MULTI_CLICK_INTERVAL;
        }

        return multiClickInterval;
    }

    /**
     * Returns the system cursor blink rate.
     */
    @Override
    public int getCursorBlinkRate() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer cursorBlinkRate = (Integer)toolkit.getDesktopProperty("awt.cursorBlinkRate");

        if (cursorBlinkRate == null) {
            cursorBlinkRate = DEFAULT_CURSOR_BLINK_RATE;
        }

        return cursorBlinkRate;
    }

    /**
     * Returns the system drag threshold.
     */
    @Override
    public int getDragThreshold() {
        return java.awt.dnd.DragSource.getDragThreshold();
    }

    /**
     * Returns the system command modifier key.
     */
    @Override
    public Modifier getCommandModifier() {
        return COMMAND_MODIFIER;
    }

    /**
     * Returns the word navigation modifier key.
     */
    @Override
    public Modifier getWordNavigationModifier() {
        return WORD_NAVIGATION_MODIFIER;
    }

    /**
     * Returns the keystroke modifier separator text.
     */
    @Override
    public String getKeyStrokeModifierSeparator() {
        return KEYSTROKE_MODIFIER_SEPARATOR;
    }

    @Override
    public int getNumberOfMouseButtons()
    {
        return MouseInfo.getNumberOfButtons();
    }
}
