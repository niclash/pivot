package org.apache.pivot.ui.awt;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.io.FileList;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.media.Image;

/**
 * Manifest class that serves as data source for a clipboard or drag/drop
 * operation.
 */
public class JavaAwtLocalManifest implements Manifest
{
    private String text = null;
    private Image image = null;
    private FileList fileList = null;
    private HashMap<String, Object> values = new HashMap<String, Object>();

    @Override
    public String getText() {
        return text;
    }

    public void putText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text is null.");
        }

        this.text = text;
    }

    @Override
    public boolean containsText() {
        return (text != null);
    }

    @Override
    public Image getImage() {
        return image;
    }

    public void putImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("image is null.");
        }

        this.image = image;
    }

    @Override
    public boolean containsImage() {
        return image != null;
    }

    @Override
    public FileList getFileList() {
        return fileList;
    }

    public void putFileList(FileList fileList) {
        if (fileList == null) {
            throw new IllegalArgumentException("fileList is null.");
        }

        this.fileList = fileList;
    }

    @Override
    public boolean containsFileList() {
        return fileList != null;
    }

    @Override
    public Object getValue(String key) {
        return values.get(key);
    }

    public Object putValue(String key, Object value) {
        return values.put(key, value);
    }

    @Override
    public boolean containsValue(String key) {
        return values.containsKey(key);
    }
}
