/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.windows;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.VersionHelpers;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.driver.windows.wmi.MSFTStorage;
import oshi.hardware.LogicalVolumeGroup;
import oshi.hardware.common.AbstractLogicalVolumeGroup;
import oshi.util.ParseUtil;
import oshi.util.platform.windows.WmiQueryHandler;
import oshi.util.platform.windows.WmiUtil;
import oshi.util.tuples.Pair;

final class WindowsLogicalVolumeGroup
extends AbstractLogicalVolumeGroup {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsLogicalVolumeGroup.class);
    private static final Pattern SP_OBJECT_ID = Pattern.compile(".*ObjectId=.*SP:(\\{.*\\}).*");
    private static final Pattern PD_OBJECT_ID = Pattern.compile(".*ObjectId=.*PD:(\\{.*\\}).*");
    private static final Pattern VD_OBJECT_ID = Pattern.compile(".*ObjectId=.*VD:(\\{.*\\})(\\{.*\\}).*");
    private static final boolean IS_WINDOWS8_OR_GREATER = VersionHelpers.IsWindows8OrGreater();

    WindowsLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        if (!IS_WINDOWS8_OR_GREATER) {
            return Collections.emptyList();
        }
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        try {
            comInit = h.initCOM();
            WbemcliUtil.WmiResult<MSFTStorage.StoragePoolProperty> sp = MSFTStorage.queryStoragePools(h);
            int count = sp.getResultCount();
            if (count == 0) {
                List<LogicalVolumeGroup> list = Collections.emptyList();
                return list;
            }
            HashMap<String, String> vdMap = new HashMap<String, String>();
            WbemcliUtil.WmiResult<MSFTStorage.VirtualDiskProperty> vds = MSFTStorage.queryVirtualDisks(h);
            count = vds.getResultCount();
            for (int i = 0; i < count; ++i) {
                Object vdObjectId = WmiUtil.getString(vds, MSFTStorage.VirtualDiskProperty.OBJECTID, i);
                Matcher m = VD_OBJECT_ID.matcher((CharSequence)vdObjectId);
                if (m.matches()) {
                    vdObjectId = m.group(2) + " " + m.group(1);
                }
                vdMap.put((String)vdObjectId, WmiUtil.getString(vds, MSFTStorage.VirtualDiskProperty.FRIENDLYNAME, i));
            }
            HashMap<String, Pair<String, String>> pdMap = new HashMap<String, Pair<String, String>>();
            WbemcliUtil.WmiResult<MSFTStorage.PhysicalDiskProperty> pds = MSFTStorage.queryPhysicalDisks(h);
            count = pds.getResultCount();
            for (int i = 0; i < count; ++i) {
                String pdObjectId = WmiUtil.getString(pds, MSFTStorage.PhysicalDiskProperty.OBJECTID, i);
                Matcher m = PD_OBJECT_ID.matcher(pdObjectId);
                if (m.matches()) {
                    pdObjectId = m.group(1);
                }
                pdMap.put(pdObjectId, new Pair<String, String>(WmiUtil.getString(pds, MSFTStorage.PhysicalDiskProperty.FRIENDLYNAME, i), WmiUtil.getString(pds, MSFTStorage.PhysicalDiskProperty.PHYSICALLOCATION, i)));
            }
            HashMap<CallSite, String> sppdMap = new HashMap<CallSite, String>();
            WbemcliUtil.WmiResult<MSFTStorage.StoragePoolToPhysicalDiskProperty> sppd = MSFTStorage.queryStoragePoolPhysicalDisks(h);
            count = sppd.getResultCount();
            for (int i = 0; i < count; ++i) {
                String pdObjectId;
                String spObjectId = WmiUtil.getRefString(sppd, MSFTStorage.StoragePoolToPhysicalDiskProperty.STORAGEPOOL, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                if ((m = PD_OBJECT_ID.matcher(pdObjectId = WmiUtil.getRefString(sppd, MSFTStorage.StoragePoolToPhysicalDiskProperty.PHYSICALDISK, i))).matches()) {
                    pdObjectId = m.group(1);
                }
                sppdMap.put((CallSite)((Object)(spObjectId + " " + pdObjectId)), pdObjectId);
            }
            ArrayList<LogicalVolumeGroup> lvgList = new ArrayList<LogicalVolumeGroup>();
            count = sp.getResultCount();
            for (int i = 0; i < count; ++i) {
                String name = WmiUtil.getString(sp, MSFTStorage.StoragePoolProperty.FRIENDLYNAME, i);
                String spObjectId = WmiUtil.getString(sp, MSFTStorage.StoragePoolProperty.OBJECTID, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                HashSet<String> physicalVolumeSet = new HashSet<String>();
                for (Map.Entry entry : sppdMap.entrySet()) {
                    String pdObjectId;
                    Pair nameLoc;
                    if (!((String)entry.getKey()).contains(spObjectId) || (nameLoc = (Pair)pdMap.get(pdObjectId = (String)entry.getValue())) == null) continue;
                    physicalVolumeSet.add((String)nameLoc.getA() + " @ " + (String)nameLoc.getB());
                }
                HashMap<String, Set<String>> logicalVolumeMap = new HashMap<String, Set<String>>();
                for (Map.Entry entry : vdMap.entrySet()) {
                    if (!((String)entry.getKey()).contains(spObjectId)) continue;
                    String vdObjectId = ParseUtil.whitespaces.split((CharSequence)entry.getKey())[0];
                    logicalVolumeMap.put((String)entry.getValue() + " " + vdObjectId, physicalVolumeSet);
                }
                lvgList.add(new WindowsLogicalVolumeGroup(name, logicalVolumeMap, physicalVolumeSet));
            }
            ArrayList<LogicalVolumeGroup> arrayList = lvgList;
            return arrayList;
        }
        catch (COMException e) {
            LOG.warn("COM exception: {}", (Object)e.getMessage());
            List<LogicalVolumeGroup> list = Collections.emptyList();
            return list;
        }
        finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
    }
}

