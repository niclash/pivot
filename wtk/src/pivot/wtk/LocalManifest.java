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
package pivot.wtk;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.net.URL;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.io.FileList;

import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;

/**
 * Manifest class that serves as data source for a clipboard or drag/drop
 * operation.
 *
 * @author gbrown
 */
public class LocalManifest implements Manifest {
    protected String text = null;
    protected Image image = null;
    protected FileList fileList = null;
    protected URL url = null;
    protected HashMap<String, Object> values = new HashMap<String, Object>();

    public String getText() {
        return text;
    }

    public void putText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        this.text = text;
    }

    public boolean containsText() {
        return (text != null);
    }

    public Image getImage() {
        return image;
    }

    public void putImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        this.image = image;
    }

    public boolean containsImage() {
        return image != null;
    }

    public FileList getFileList() {
        return fileList;
    }

    public void putFileList(FileList fileList) {
        if (fileList == null) {
            throw new IllegalArgumentException("fileList is null.");
        }

        this.fileList = fileList;
    }

    public boolean containsFileList() {
        return fileList != null;
    }

    public URL getURL() {
        return url;
    }

    public void putURL(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url is null.");
        }

        this.url = url;
    }

    public boolean containsURL() {
        return url != null;
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    public boolean containsValue(String key) {
        return values.containsKey(key);
    }
}

class LocalManifestAdapter implements Transferable {
    private LocalManifest localManifest;
    private ArrayList<DataFlavor> transferDataFlavors = new ArrayList<DataFlavor>();

    public LocalManifestAdapter(LocalManifest localManifest) {
        this.localManifest = localManifest;

        if (localManifest.containsText()) {
            transferDataFlavors.add(DataFlavor.stringFlavor);
        }

        if (localManifest.containsImage()) {
            transferDataFlavors.add(DataFlavor.imageFlavor);
        }

        if (localManifest.containsFileList()) {
            transferDataFlavors.add(DataFlavor.javaFileListFlavor);
        }

        if (localManifest.containsURL()) {
            transferDataFlavors.add(new DataFlavor(URL.class, URL.class.getName()));
        }
    }

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
        } else if (dataFlavor.getRepresentationClass() == URL.class) {
            transferData = localManifest.getURL();
        } else if (dataFlavor.isRepresentationClassByteBuffer()) {
            // TODO
            /*
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                Object value = localManifest.getValue(key);

                try {
                    serializer.writeObject(value, byteArrayOutputStream);
                    byteArrayOutputStream.close();
                    ByteBuffer byteBuffer = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

                } catch(Exception exception) {
                    System.err.println(exception);
                }
             */
        }

        return transferData;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return transferDataFlavors.toArray();
    }

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return (transferDataFlavors.indexOf(dataFlavor) != -1);
    }
}
