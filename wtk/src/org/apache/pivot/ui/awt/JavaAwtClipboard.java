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

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.ClipboardContentListener;
import org.apache.pivot.wtk.Manifest;

/**
 * Singleton class providing a means of sharing data between components and
 * applications.
 */
public final class JavaAwtClipboard
    implements Clipboard
{
    private static JavaAwtLocalManifest content = null;
    private static ClipboardContentListener clipboardContentListener = null;

    /**
     * Retrieves the contents of the clipboard.
     */
    @Override public Manifest getContent() {
        Manifest content = JavaAwtClipboard.content;

        if (content == null) {
            try {
                java.awt.datatransfer.Clipboard awtClipboard =
                    Toolkit.getDefaultToolkit().getSystemClipboard();
                content = new JavaAwtRemoteManifest(awtClipboard.getContents(null));
            } catch(SecurityException exception) {
                // No-op
            }
        }

        return content;
    }

    /**
     * Places content on the clipboard.
     *
     * @param content
     */
    @Override public void setContent(JavaAwtLocalManifest content) {
        setContent(content, null);
    }

    /**
     * Places content on the clipboard.
     *
     * @param content
     */
    @Override public void setContent(JavaAwtLocalManifest content,
        ClipboardContentListener clipboardContentListener) {
        if (content == null) {
            throw new IllegalArgumentException("content is null");
        }

        try {
            java.awt.datatransfer.Clipboard awtClipboard =
                Toolkit.getDefaultToolkit().getSystemClipboard();

            JavaAwtLocalManifestAdapter localManifestAdapter = new JavaAwtLocalManifestAdapter(content);
            awtClipboard.setContents(localManifestAdapter, new ClipboardOwner() {
                @Override
                public void lostOwnership(java.awt.datatransfer.Clipboard clipboard,
                    Transferable contents) {
                    JavaAwtLocalManifest previousContent = JavaAwtClipboard.content;
                    JavaAwtClipboard.content = null;

                    if (JavaAwtClipboard.clipboardContentListener != null) {
                        JavaAwtClipboard.clipboardContentListener.contentChanged(previousContent);
                    }
                }
            });
        } catch(SecurityException exception) {
            // No-op
        }

        JavaAwtClipboard.content = content;
        JavaAwtClipboard.clipboardContentListener = clipboardContentListener;
    }
}
