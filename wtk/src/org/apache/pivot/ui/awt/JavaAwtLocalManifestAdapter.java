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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.io.FileList;
import org.apache.pivot.ui.awt.JavaAwtLocalManifest;
import org.apache.pivot.wtk.media.Picture;

class JavaAwtLocalManifestAdapter implements Transferable {
    private JavaAwtLocalManifest localManifest;
    private ArrayList<DataFlavor> transferDataFlavors = new ArrayList<DataFlavor>();

    private static final String URI_LIST_MIME_TYPE = "text/uri-list; class=java.lang.String";

    JavaAwtLocalManifestAdapter( JavaAwtLocalManifest localManifest ) {
        this.localManifest = localManifest;

        if (localManifest.containsText()) {
            transferDataFlavors.add(DataFlavor.stringFlavor);
        }

        if (localManifest.containsImage()) {
            transferDataFlavors.add(DataFlavor.imageFlavor);
        }

        if (localManifest.containsFileList()) {
            transferDataFlavors.add(DataFlavor.javaFileListFlavor);

            try {
                transferDataFlavors.add(new DataFlavor(URI_LIST_MIME_TYPE));
            } catch (ClassNotFoundException exception) {
                // No-op
            }
        }
    }

    @Override
    public Object getTransferData(DataFlavor dataFlavor)
        throws UnsupportedFlavorException {
        Object transferData = null;

        int index = transferDataFlavors.indexOf(dataFlavor);
        if (index == -1) {
            throw new UnsupportedFlavorException(dataFlavor);
        }

        if (dataFlavor.equals(DataFlavor.stringFlavor)) {
            transferData = localManifest.getText();
        } else if (dataFlavor.equals(DataFlavor.imageFlavor)) {
            Picture picture = (Picture)localManifest.getImage();
            transferData = picture.getBufferedImage();
        } else if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
            FileList fileList = localManifest.getFileList();
            transferData = fileList.getList();
        } else if (dataFlavor.getMimeType().equals(URI_LIST_MIME_TYPE)) {
            FileList fileList = localManifest.getFileList();

            StringBuilder buf = new StringBuilder();
            for (File file : fileList) {
                buf.append(file.toURI().toString()).append("\r\n");
            }

            transferData = buf.toString();
        }

        return transferData;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return transferDataFlavors.toArray(DataFlavor[].class);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return (transferDataFlavors.indexOf(dataFlavor) != -1);
    }
}
