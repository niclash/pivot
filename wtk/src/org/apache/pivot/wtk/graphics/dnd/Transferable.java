package org.apache.pivot.wtk.graphics.dnd;

public interface Transferable
{
    DataFlavor[] getTransferDataFlavors();

    Object getTransferData( DataFlavor textDataFlavor );
}
