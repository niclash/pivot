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
package org.apache.pivot.wtk.test;

import java.awt.Font;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LocalManifest;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Visual;


public class NativeDragDropTest implements Application {
    private Frame frame = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        final Label label = new Label("http://www.apple.com");
        label.getStyles().put("font", new Font("Arial", Font.PLAIN, 24));
        label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        label.setDragSource(new DragSource() {
            private LocalManifest content = null;

            public boolean beginDrag(Component component, int x, int y) {
                content = new LocalManifest();

                URL url = null;
                try {
                    url = new URL(label.getText());
                } catch(MalformedURLException exception) {
                }

                if (url != null) {
                    content.putURL(url);
                }

                return true;
            }

            public void endDrag(Component component, DropAction dropAction) {
                content = null;
            }

            public boolean isNative() {
                return true;
            }

            public LocalManifest getContent() {
                return content;
            }

            public Visual getRepresentation() {
                return null;
            }

            public Point getOffset() {
                return null;
            }

            public int getSupportedDropActions() {
                return DropAction.COPY.getMask();
            }
        });

        label.setDropTarget(new DropTarget() {
            public DropAction dragEnter(Component component, Manifest dragContent,
                int supportedDropActions, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsURL()) {
                    frame.getStyles().put("backgroundColor", "#ffcccc");
                    dropAction = DropAction.COPY;
                }

                return dropAction;
            }

            public void dragExit(Component component) {
                frame.getStyles().put("backgroundColor", "#ffffff");
            }

            public DropAction dragMove(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsURL() ? DropAction.COPY : null);
            }

            public DropAction userDropActionChange(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                return (dragContent.containsURL() ? DropAction.COPY : null);
            }

            public DropAction drop(Component component, Manifest dragContent,
                int supportedDropActions, int x, int y, DropAction userDropAction) {
                DropAction dropAction = null;

                if (dragContent.containsURL()) {
                    Label label = (Label)component;
                    try {
                        label.setText(dragContent.getURL().toString());
                    } catch(IOException exception) {
                        System.err.println(exception);
                    }
                }

                dragExit(component);

                return dropAction;
            }
        });

        frame = new Frame(label);
        frame.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}