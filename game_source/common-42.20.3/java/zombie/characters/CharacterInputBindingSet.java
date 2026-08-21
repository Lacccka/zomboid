/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import java.io.File;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import zombie.UsedFromLua;
import zombie.ZomboidFileSystem;
import zombie.characters.CharacterInputBindingSetEntry;
import zombie.characters.CharacterJoypadAxis2dBinding;
import zombie.characters.CharacterJoypadButtonBinding;
import zombie.core.Translator;
import zombie.core.logger.ExceptionLogger;
import zombie.debug.DebugType;
import zombie.input.JoypadAxis1d;
import zombie.input.JoypadAxis2d;
import zombie.input.JoypadButton;
import zombie.util.PZXmlParserException;
import zombie.util.PZXmlUtil;
import zombie.util.StringUtils;
import zombie.util.list.PZArrayUtil;

@UsedFromLua
@XmlRootElement(name="inputBindingSet")
public class CharacterInputBindingSet {
    private static CharacterInputBindingSet[] loadedBindingSets;
    @XmlTransient
    public String name;
    @XmlElement(name="description")
    public String description;
    @XmlElements(value={@XmlElement(name="button", type=ButtonBinding.class), @XmlElement(name="buttonAxis1d", type=ButtonAxis1dBinding.class), @XmlElement(name="buttonAxis2d", type=ButtonAxis2dBinding.class), @XmlElement(name="axis2d", type=Axis2dBinding.class)})
    public CharacterInputBindingSetEntry[] allBindings;

    @UsedFromLua
    public String getName() {
        return this.name;
    }

    @UsedFromLua
    public String getDescription() {
        return this.description;
    }

    @UsedFromLua
    public void apply() {
        CharacterInputBindingSet.resetAllToDefault();
        PZArrayUtil.forEach(this.allBindings, CharacterInputBindingSetEntry::apply);
    }

    @UsedFromLua
    public void setBindingsToCurrent() {
        this.allBindings = null;
        PZArrayUtil.forEach(CharacterJoypadButtonBinding.allBindings(), this::addBinding);
        PZArrayUtil.forEach(CharacterJoypadAxis2dBinding.allBindings(), this::addBinding);
    }

    @UsedFromLua
    public boolean save() {
        String bindingsDirPath = ZomboidFileSystem.instance.getCacheDirSub("InputBindings");
        if (!ZomboidFileSystem.ensureFolderExists(bindingsDirPath)) {
            DebugType.FileIO.error("Failed to create InputBindings folder: %s", bindingsDirPath);
            return false;
        }
        File outputFile = new File(bindingsDirPath, this.name + ".xml");
        boolean success = PZXmlUtil.tryWrite(this, outputFile);
        if (!success) {
            DebugType.FileIO.error("Failed to save InputBindings to: %s", outputFile.getPath());
        }
        return success;
    }

    public void addBinding(CharacterInputBindingSetEntry newBinding) {
        if (this.allBindings == null) {
            this.allBindings = new CharacterInputBindingSetEntry[0];
        }
        this.allBindings = PZArrayUtil.add(this.allBindings, newBinding);
    }

    private void addBinding(CharacterJoypadButtonBinding currentBinding) {
        CharacterJoypadButtonBinding.IsDownBinding isDownBinding;
        CharacterJoypadButtonBinding.IsDownBinding isDownBinding2 = isDownBinding = currentBinding.getBinding();
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{CharacterJoypadButtonBinding.JoypadButtonBinding.class, CharacterJoypadButtonBinding.Axis1dMinMaxBinding.class, CharacterJoypadButtonBinding.Axis2dMinMaxBinding.class}, (CharacterJoypadButtonBinding.IsDownBinding)isDownBinding2, n)) {
            case -1: {
                this.addBinding(new ButtonBinding(currentBinding, null));
                break;
            }
            case 0: {
                CharacterJoypadButtonBinding.JoypadButtonBinding buttonBinding = (CharacterJoypadButtonBinding.JoypadButtonBinding)isDownBinding2;
                this.addBinding(new ButtonBinding(currentBinding, buttonBinding.getButton()));
                break;
            }
            case 1: {
                CharacterJoypadButtonBinding.Axis1dMinMaxBinding axis1dBinding = (CharacterJoypadButtonBinding.Axis1dMinMaxBinding)isDownBinding2;
                this.addBinding(new ButtonAxis1dBinding(currentBinding, axis1dBinding.getAxis1d(), axis1dBinding.getMin(), axis1dBinding.getMax()));
                break;
            }
            case 2: {
                CharacterJoypadButtonBinding.Axis2dMinMaxBinding axis2dBinding = (CharacterJoypadButtonBinding.Axis2dMinMaxBinding)isDownBinding2;
                this.addBinding(new ButtonAxis2dBinding(currentBinding, axis2dBinding.getAxis2d(), axis2dBinding.getMin(), axis2dBinding.getMax()));
                break;
            }
            default: {
                DebugType.Input.warn("Unhandled IsDownBinding type. %s", isDownBinding);
            }
        }
    }

    private void addBinding(CharacterJoypadAxis2dBinding currentBinding) {
        this.addBinding(new Axis2dBinding(currentBinding, currentBinding.getBinding()));
    }

    @UsedFromLua
    public static CharacterInputBindingSet[] getLoadedBindingSets() {
        if (loadedBindingSets == null) {
            CharacterInputBindingSet.reloadAll();
        }
        return loadedBindingSets;
    }

    @UsedFromLua
    public static CharacterInputBindingSet[] reloadAll() {
        File[] bindingsFiles;
        ArrayList<CharacterInputBindingSet> allSets = new ArrayList<CharacterInputBindingSet>();
        String bindingsDirPath = ZomboidFileSystem.instance.getCacheDirSub("InputBindings");
        for (File bindingsFile : bindingsFiles = ZomboidFileSystem.listAllFiles(bindingsDirPath, CharacterInputBindingSet::isInputBindingFile, false)) {
            CharacterInputBindingSet loadedSet = CharacterInputBindingSet.loadFromFile(bindingsFile.getPath());
            if (loadedSet == null) continue;
            if (CharacterInputBindingSet.containsSetName(allSets, loadedSet.name)) {
                String nameBase = loadedSet.name;
                String alternateName = nameBase + "_1";
                int alternateIdx = 1;
                while (CharacterInputBindingSet.containsSetName(allSets, alternateName)) {
                    alternateName = nameBase + "_" + alternateIdx;
                    ++alternateIdx;
                }
                DebugType.General.warn("Duplicate CharacterInputBindingSet: %s. Renamed to %s", loadedSet.name, alternateName);
                loadedSet.name = alternateName;
            }
            DebugType.General.warn("Loaded CharacterInputBindingSet: %s.", loadedSet.name);
            allSets.add(loadedSet);
        }
        loadedBindingSets = (CharacterInputBindingSet[])PZArrayUtil.toArray(allSets);
        if (loadedBindingSets == null) {
            loadedBindingSets = new CharacterInputBindingSet[0];
        }
        return loadedBindingSets;
    }

    private static boolean isInputBindingFile(File file) {
        return file != null && file.isFile() && file.getName().endsWith(".xml");
    }

    @UsedFromLua
    public static boolean saveAll() {
        File[] bindingsFiles;
        String bindingsDirPath = ZomboidFileSystem.instance.getCacheDirSub("InputBindings");
        if (!ZomboidFileSystem.ensureFolderExists(bindingsDirPath)) {
            DebugType.FileIO.error("Failed to create InputBindings folder: %s", bindingsDirPath);
            return false;
        }
        for (File bindingsFile : bindingsFiles = ZomboidFileSystem.listAllFiles(bindingsDirPath, CharacterInputBindingSet::isInputBindingFile, false)) {
            bindingsFile.delete();
        }
        boolean allSuccess = true;
        for (CharacterInputBindingSet set : loadedBindingSets) {
            allSuccess = set.save() && allSuccess;
        }
        return allSuccess;
    }

    private static CharacterInputBindingSet loadFromFile(String bindingsFilePath) {
        try {
            CharacterInputBindingSet parsedSet = PZXmlUtil.parse(CharacterInputBindingSet.class, bindingsFilePath);
            parsedSet.name = ZomboidFileSystem.getFileNameNoExtension(bindingsFilePath);
            parsedSet.allBindings = PZArrayUtil.filtered(parsedSet.allBindings, CharacterInputBindingSetEntry::isValid);
            return parsedSet;
        }
        catch (PZXmlParserException e) {
            System.err.println("CharacterInputBindingSet.Parse threw an exception reading file: " + bindingsFilePath);
            ExceptionLogger.logException(e);
            return null;
        }
    }

    private static boolean containsSetName(List<CharacterInputBindingSet> allSets, String name) {
        return PZArrayUtil.contains(allSets, set -> StringUtils.equalsIgnoreCase(set.name, name));
    }

    private static boolean containsSetName(CharacterInputBindingSet[] allSets, String name) {
        return PZArrayUtil.contains(allSets, set -> StringUtils.equalsIgnoreCase(set.name, name));
    }

    @UsedFromLua
    public static boolean containsSetName(String name) {
        return CharacterInputBindingSet.containsSetName(CharacterInputBindingSet.getLoadedBindingSets(), name);
    }

    @UsedFromLua
    public static String getUniqueSetName(String name) {
        if (StringUtils.isNullOrWhitespace(name)) {
            name = Translator.getText("IGUI_Custom", new Object[0]);
        }
        if (!CharacterInputBindingSet.containsSetName(name = name.trim())) {
            return name;
        }
        String nameBase = CharacterInputBindingSet.trimTrailingNumberFromName(name);
        String alternateName = nameBase + "_1";
        int alternateIdx = 1;
        while (CharacterInputBindingSet.containsSetName(alternateName)) {
            alternateName = nameBase + "_" + alternateIdx;
            ++alternateIdx;
        }
        return alternateName;
    }

    private static String trimTrailingNumberFromName(String name) {
        int lastUnderscoreIdx = name.lastIndexOf(95);
        if (lastUnderscoreIdx == -1) {
            return name;
        }
        String trailingNumberStr = name.substring(lastUnderscoreIdx + 1).trim();
        if (StringUtils.tryParseInt(trailingNumberStr, -1) == -1) {
            return name;
        }
        return name.substring(0, lastUnderscoreIdx);
    }

    @UsedFromLua
    public static CharacterInputBindingSet createNewFromCurrent(String name) {
        CharacterInputBindingSet newSet = new CharacterInputBindingSet();
        newSet.name = CharacterInputBindingSet.getUniqueSetName(name);
        newSet.setBindingsToCurrent();
        loadedBindingSets = PZArrayUtil.add(loadedBindingSets, newSet);
        return newSet;
    }

    @UsedFromLua
    public static void resetAllToDefault() {
        CharacterJoypadButtonBinding.setAllToDefault();
        CharacterJoypadAxis2dBinding.setAllToDefault();
    }

    public static class ButtonBinding
    extends CharacterInputBindingSetEntry {
        @XmlAttribute
        public CharacterJoypadButtonBinding binding;
        @XmlAttribute
        public JoypadButton button;

        public ButtonBinding() {
        }

        public ButtonBinding(CharacterJoypadButtonBinding binding, JoypadButton button) {
            this.binding = binding;
            this.button = button;
        }

        @Override
        public void apply() {
            this.binding.setBinding(this.button);
        }

        @Override
        public boolean isValid() {
            return this.binding != null && this.button != null;
        }
    }

    public static class ButtonAxis1dBinding
    extends CharacterInputBindingSetEntry {
        @XmlAttribute
        public CharacterJoypadButtonBinding binding;
        @XmlAttribute
        public JoypadAxis1d axis;
        @XmlAttribute
        public float min;
        @XmlAttribute
        public float max = Float.MAX_VALUE;

        public ButtonAxis1dBinding() {
        }

        public ButtonAxis1dBinding(CharacterJoypadButtonBinding binding, JoypadAxis1d axis, float min, float max) {
            this.binding = binding;
            this.axis = axis;
            this.min = min;
            this.max = max;
        }

        @Override
        public void apply() {
            this.binding.setBinding(this.axis, this.min, this.max);
        }

        @Override
        public boolean isValid() {
            return this.binding != null && this.axis != null;
        }
    }

    public static class ButtonAxis2dBinding
    extends CharacterInputBindingSetEntry {
        @XmlAttribute
        public CharacterJoypadButtonBinding binding;
        @XmlAttribute
        public JoypadAxis2d axis;
        @XmlAttribute
        public float min;
        @XmlAttribute
        public float max = Float.MAX_VALUE;

        public ButtonAxis2dBinding() {
        }

        public ButtonAxis2dBinding(CharacterJoypadButtonBinding binding, JoypadAxis2d axis, float min, float max) {
            this.binding = binding;
            this.axis = axis;
            this.min = min;
            this.max = max;
        }

        @Override
        public void apply() {
            this.binding.setBinding(this.axis, this.min, this.max);
        }

        @Override
        public boolean isValid() {
            return this.binding != null && this.axis != null;
        }
    }

    public static class Axis2dBinding
    extends CharacterInputBindingSetEntry {
        @XmlAttribute
        public CharacterJoypadAxis2dBinding binding;
        @XmlAttribute
        public JoypadAxis2d axis;

        public Axis2dBinding() {
        }

        public Axis2dBinding(CharacterJoypadAxis2dBinding binding, JoypadAxis2d axis) {
            this.binding = binding;
            this.axis = axis;
        }

        @Override
        public void apply() {
            this.binding.setBinding(this.axis);
        }

        @Override
        public boolean isValid() {
            return this.binding != null && this.axis != null;
        }
    }
}

