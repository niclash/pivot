package org.apache.pivot.wtk;

import org.apache.pivot.wtk.graphics.geom.GeneralPath;

public interface PathFactory
{
    GeneralPath createGeneralPath();

    GeneralPath createGeneralPath( int type );
}
