package org.apache.pivot.wtk.graphics;

public abstract class PivotPlatform
{
    private static PivotPlatform installed;

    public static PivotPlatform getInstalled() {
        return installed;
    }

    public static void installPivotSystem( PivotPlatform platform ) {
        installed = platform;
    }

    public abstract MediaSystem getMediaSystem();
}
