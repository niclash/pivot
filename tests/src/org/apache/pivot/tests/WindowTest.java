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
package org.apache.pivot.tests;

import org.apache.pivot.wtk.ApplicationContext;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Palette;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.effects.ReflectionDecorator;

public class WindowTest implements Application {
    private Frame window1 = new Frame();
    private Frame dialogOwner = new Frame();

    @Override
    public void startup(Display display, Map<String, String> properties) {
        window1.setTitle("Window 1");
        window1.setPreferredSize(320, 240);
        window1.setMinimumWidth(220);
        window1.setMaximumWidth(420);
        window1.setMinimumHeight(140);
        window1.setMaximumHeight(340);

        window1.getComponentListeners().add(new ComponentListener.Adapter() {
            @Override
            public void sizeChanged(Component component, int previousWidth, int previousHeight) {
                window1.align(window1.getDisplay().getBounds(),
                    HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
                window1.getComponentListeners().remove(this);
            }
        });

        display.getStyles().put("backgroundColor", new Color(0, 127, 127));
        window1.setContent(new Label("Hello Bar"));
        window1.open(display);

        ApplicationContext.queueCallback(new Runnable() {
            @Override
            public void run() {
                final Sheet sheet = new Sheet();
                sheet.setPreferredSize(120, 60);
                sheet.open(window1);

                ApplicationContext.queueCallback(new Runnable() {
                    @Override
                    public void run() {
                        Sheet sheet2 = new Sheet();
                        sheet2.setPreferredSize(60, 30);
                        sheet2.open(sheet);
                    }
                });
            }
        });

        Frame window1a = new Frame();
        window1a.setTitle("Window 1 A");
        window1a.setPreferredSize(160, 120);
        window1a.open(window1);

        Frame window1ai = new Frame();
        window1ai.setTitle("Window 1 A I");
        window1ai.setPreferredSize(160, 60);
        window1ai.open(window1a);
        window1ai.getDecorators().update(0, new ReflectionDecorator());

        Frame window1aii = new Frame();
        window1aii.setTitle("Window 1 A II");
        window1aii.setPreferredSize(160, 60);
        window1aii.open(window1a);

        Frame window1b = new Frame();
        window1b.setTitle("Window 1 B");
        window1b.setPreferredSize(160, 120);
        window1b.setLocation(20, 20);
        window1b.open(window1);

        Frame window1bi = new Frame();
        window1bi.setTitle("Window 1 B I");
        window1bi.setPreferredSize(160, 60);
        window1bi.open(window1b);

        Frame window1bii = new Frame();
        window1bii.setTitle("Window 1 B II");
        window1bii.setPreferredSize(160, 60);
        window1bii.open(window1b);

        Palette palette1 = new Palette();
        palette1.setTitle("Palette 1bii 1");
        palette1.setPreferredSize(160, 60);
        palette1.open(window1bii);

        Palette palette2 = new Palette();
        palette2.setTitle("Palette 1bii 2");
        palette2.setPreferredSize(160, 60);
        palette2.open(window1bii);

        dialogOwner.setTitle("Dialog Owner");
        dialogOwner.setPreferredSize(320, 120);
        dialogOwner.open(display);

        ApplicationContext.queueCallback(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog();
                dialog.setTitle("Dialog 1");
                dialog.setPreferredSize(280, 100);
                dialog.open(dialogOwner);

                ApplicationContext.queueCallback( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Dialog dialog2 = new Dialog();
                        dialog2.setTitle( "Dialog 2" );
                        dialog2.setPreferredSize( 220, 80 );
                        dialog2.open( dialog );
                    }
                } );
            }
        });
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window1 != null) {
            window1.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(WindowTest.class, args);
    }
}
