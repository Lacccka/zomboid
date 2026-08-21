/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.IOKit;
import com.sun.jna.platform.mac.IOKitUtil;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.Immutable;
import oshi.hardware.Display;
import oshi.hardware.common.AbstractDisplay;

@Immutable
final class MacDisplay
extends AbstractDisplay {
    private static final Logger LOG = LoggerFactory.getLogger(MacDisplay.class);

    MacDisplay(byte[] edid) {
        super(edid);
        LOG.debug("Initialized MacDisplay");
    }

    public static List<Display> getDisplays() {
        ArrayList<Display> displays = new ArrayList<Display>();
        displays.addAll(MacDisplay.getDisplaysFromService("IODisplayConnect", "IODisplayEDID", "IOService"));
        displays.addAll(MacDisplay.getDisplaysFromService("IOPortTransportStateDisplayPort", "EDID", null));
        return displays;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static List<Display> getDisplaysFromService(String serviceName, String edidKeyName, String childEntryName) {
        ArrayList<Display> displays = new ArrayList<Display>();
        IOKit.IOIterator serviceIterator = IOKitUtil.getMatchingServices(serviceName);
        if (serviceIterator != null) {
            CoreFoundation.CFStringRef cfEdid = CoreFoundation.CFStringRef.createCFString(edidKeyName);
            IOKit.IORegistryEntry sdService = serviceIterator.next();
            while (sdService != null) {
                IOKit.IORegistryEntry propertySource = null;
                try {
                    propertySource = childEntryName == null ? sdService : sdService.getChildEntry(childEntryName);
                    if (propertySource == null) continue;
                    CoreFoundation.CFTypeRef edidRaw = propertySource.createCFProperty(cfEdid);
                    if (edidRaw != null) {
                        CoreFoundation.CFDataRef edid = new CoreFoundation.CFDataRef(edidRaw.getPointer());
                        try {
                            int length = edid.getLength();
                            Pointer p = edid.getBytePtr();
                            displays.add(new MacDisplay(p.getByteArray(0L, length)));
                        }
                        finally {
                            edid.release();
                        }
                    }
                    if (childEntryName == null || propertySource == null) continue;
                    propertySource.release();
                }
                finally {
                    sdService.release();
                    sdService = serviceIterator.next();
                }
            }
            serviceIterator.release();
            cfEdid.release();
        }
        return displays;
    }
}

