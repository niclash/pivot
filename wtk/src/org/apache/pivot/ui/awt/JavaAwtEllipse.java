package org.apache.pivot.ui.awt;

import java.awt.geom.Ellipse2D;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.graphics.geom.Ellipse;

public class JavaAwtEllipse extends JavaAwtShape
    implements Ellipse
{
    JavaAwtEllipse( Ellipse2D.Double awtEllipse )
    {
        super(awtEllipse);
    }
}
