/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.Attribute
 */
package org.apache.commons.compress.harmony.pack200;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import org.objectweb.asm.Attribute;

public class PackingOptions {
    private static final Attribute[] EMPTY_ATTRIBUTE_ARRAY = new Attribute[0];
    public static final long SEGMENT_LIMIT = 1000000L;
    public static final String STRIP = "strip";
    public static final String ERROR = "error";
    public static final String PASS = "pass";
    public static final String KEEP = "keep";
    private boolean gzip = true;
    private boolean stripDebug;
    private boolean keepFileOrder = true;
    private long segmentLimit = 1000000L;
    private int effort = 5;
    private String deflateHint = "keep";
    private String modificationTime = "keep";
    private final List<String> passFiles = new ArrayList<String>();
    private String unknownAttributeAction = "pass";
    private final Map<String, String> classAttributeActions = new HashMap<String, String>();
    private final Map<String, String> fieldAttributeActions = new HashMap<String, String>();
    private final Map<String, String> methodAttributeActions = new HashMap<String, String>();
    private final Map<String, String> codeAttributeActions = new HashMap<String, String>();
    private boolean verbose;
    private String logFile;
    private Attribute[] unknownAttributeTypes;

    public void addClassAttributeAction(String attributeName, String action) {
        this.classAttributeActions.put(attributeName, action);
    }

    public void addCodeAttributeAction(String attributeName, String action) {
        this.codeAttributeActions.put(attributeName, action);
    }

    public void addFieldAttributeAction(String attributeName, String action) {
        this.fieldAttributeActions.put(attributeName, action);
    }

    public void addMethodAttributeAction(String attributeName, String action) {
        this.methodAttributeActions.put(attributeName, action);
    }

    private void addOrUpdateAttributeActions(List<Attribute> prototypes, Map<String, String> attributeActions, int tag) {
        if (attributeActions != null && attributeActions.size() > 0) {
            for (Map.Entry<String, String> entry : attributeActions.entrySet()) {
                NewAttribute newAttribute;
                String name = entry.getKey();
                String action = entry.getValue();
                boolean prototypeExists = false;
                for (Attribute prototype : prototypes) {
                    newAttribute = (NewAttribute)prototype;
                    if (!newAttribute.type.equals(name)) continue;
                    newAttribute.addContext(tag);
                    prototypeExists = true;
                    break;
                }
                if (prototypeExists) continue;
                newAttribute = ERROR.equals(action) ? new NewAttribute.ErrorAttribute(name, tag) : (STRIP.equals(action) ? new NewAttribute.StripAttribute(name, tag) : (PASS.equals(action) ? new NewAttribute.PassAttribute(name, tag) : new NewAttribute(name, action, tag)));
                prototypes.add(newAttribute);
            }
        }
    }

    public void addPassFile(String passFileName) {
        String fileSeparator = FileSystems.getDefault().getSeparator();
        if (fileSeparator.equals("\\")) {
            fileSeparator = fileSeparator + "\\";
        }
        this.passFiles.add(passFileName.replaceAll(fileSeparator, "/"));
    }

    public String getDeflateHint() {
        return this.deflateHint;
    }

    public int getEffort() {
        return this.effort;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public String getModificationTime() {
        return this.modificationTime;
    }

    private String getOrDefault(Map<String, String> map, String type, String defaultValue) {
        return map == null ? defaultValue : map.getOrDefault(type, defaultValue);
    }

    public long getSegmentLimit() {
        return this.segmentLimit;
    }

    public String getUnknownAttributeAction() {
        return this.unknownAttributeAction;
    }

    public Attribute[] getUnknownAttributePrototypes() {
        if (this.unknownAttributeTypes == null) {
            ArrayList<Attribute> prototypes = new ArrayList<Attribute>();
            this.addOrUpdateAttributeActions(prototypes, this.classAttributeActions, 0);
            this.addOrUpdateAttributeActions(prototypes, this.methodAttributeActions, 2);
            this.addOrUpdateAttributeActions(prototypes, this.fieldAttributeActions, 1);
            this.addOrUpdateAttributeActions(prototypes, this.codeAttributeActions, 3);
            this.unknownAttributeTypes = prototypes.toArray(EMPTY_ATTRIBUTE_ARRAY);
        }
        return this.unknownAttributeTypes;
    }

    public String getUnknownClassAttributeAction(String type) {
        return this.getOrDefault(this.classAttributeActions, type, this.unknownAttributeAction);
    }

    public String getUnknownCodeAttributeAction(String type) {
        return this.getOrDefault(this.codeAttributeActions, type, this.unknownAttributeAction);
    }

    public String getUnknownFieldAttributeAction(String type) {
        return this.getOrDefault(this.fieldAttributeActions, type, this.unknownAttributeAction);
    }

    public String getUnknownMethodAttributeAction(String type) {
        return this.getOrDefault(this.methodAttributeActions, type, this.unknownAttributeAction);
    }

    public boolean isGzip() {
        return this.gzip;
    }

    public boolean isKeepDeflateHint() {
        return KEEP.equals(this.deflateHint);
    }

    public boolean isKeepFileOrder() {
        return this.keepFileOrder;
    }

    public boolean isPassFile(String passFileName) {
        for (String pass : this.passFiles) {
            if (passFileName.equals(pass)) {
                return true;
            }
            if (pass.endsWith(".class")) continue;
            if (!pass.endsWith("/")) {
                pass = pass + "/";
            }
            return passFileName.startsWith(pass);
        }
        return false;
    }

    public boolean isStripDebug() {
        return this.stripDebug;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public void removePassFile(String passFileName) {
        this.passFiles.remove(passFileName);
    }

    public void setDeflateHint(String deflateHint) {
        if (!(KEEP.equals(deflateHint) || "true".equals(deflateHint) || "false".equals(deflateHint))) {
            throw new IllegalArgumentException("Bad argument: -H " + deflateHint + " ? deflate hint should be either true, false or keep (default)");
        }
        this.deflateHint = deflateHint;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public void setKeepFileOrder(boolean keepFileOrder) {
        this.keepFileOrder = keepFileOrder;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public void setModificationTime(String modificationTime) {
        if (!KEEP.equals(modificationTime) && !"latest".equals(modificationTime)) {
            throw new IllegalArgumentException("Bad argument: -m " + modificationTime + " ? transmit modtimes should be either latest or keep (default)");
        }
        this.modificationTime = modificationTime;
    }

    public void setQuiet(boolean quiet) {
        this.verbose = !quiet;
    }

    public void setSegmentLimit(long segmentLimit) {
        this.segmentLimit = segmentLimit;
    }

    public void setStripDebug(boolean stripDebug) {
        this.stripDebug = stripDebug;
    }

    public void setUnknownAttributeAction(String unknownAttributeAction) {
        this.unknownAttributeAction = unknownAttributeAction;
        if (!(PASS.equals(unknownAttributeAction) || ERROR.equals(unknownAttributeAction) || STRIP.equals(unknownAttributeAction))) {
            throw new IllegalArgumentException("Incorrect option for -U, " + unknownAttributeAction);
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}

