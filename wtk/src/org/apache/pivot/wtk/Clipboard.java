package org.apache.pivot.wtk;

import org.apache.pivot.ui.awt.JavaAwtLocalManifest;

public interface Clipboard
{
    Manifest getContent();

    void setContent( JavaAwtLocalManifest content );

    void setContent( JavaAwtLocalManifest content,
                     ClipboardContentListener clipboardContentListener
    );
}
