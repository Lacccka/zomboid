/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.wmi;

import com.sun.jna.platform.win32.COM.WbemcliUtil;
import java.util.Objects;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.util.platform.windows.WmiQueryHandler;

@ThreadSafe
public final class Win32VideoController {
    private static final String WIN32_VIDEO_CONTROLLER = "Win32_VideoController";

    private Win32VideoController() {
    }

    public static WbemcliUtil.WmiResult<VideoControllerProperty> queryVideoController() {
        WbemcliUtil.WmiQuery<VideoControllerProperty> videoControllerQuery = new WbemcliUtil.WmiQuery<VideoControllerProperty>(WIN32_VIDEO_CONTROLLER, VideoControllerProperty.class);
        return Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(videoControllerQuery);
    }

    public static enum VideoControllerProperty {
        ADAPTERCOMPATIBILITY,
        ADAPTERRAM,
        DRIVERVERSION,
        NAME,
        PNPDEVICEID;

    }
}

