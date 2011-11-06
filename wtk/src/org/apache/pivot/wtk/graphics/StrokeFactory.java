package org.apache.pivot.wtk.graphics;

public interface StrokeFactory
{
    BasicStroke createBasicStroke( float pixels);

    BasicStroke createBasicStroke( float width, int cap, int join, float miterlimit, float dash[], float dash_phase);
}
