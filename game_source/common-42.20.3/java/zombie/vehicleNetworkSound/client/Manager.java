/*
 * Decompiled with CFR 0.152.
 */
package zombie.vehicleNetworkSound.client;

import gnu.trove.map.hash.TShortObjectHashMap;
import zombie.core.math.PZMath;
import zombie.core.textures.Texture;
import zombie.input.Mouse;
import zombie.scripting.ScriptManager;
import zombie.ui.TextManager;
import zombie.ui.UIFont;
import zombie.vehicleNetworkSound.SharedVehicleState;
import zombie.vehicleNetworkSound.client.VehicleState;
import zombie.worldMap.UIWorldMap;
import zombie.worldMap.symbols.MapSymbolDefinitions;

public final class Manager {
    private static Manager instance;
    private final TShortObjectHashMap<VehicleState> stateMap = new TShortObjectHashMap();
    private static VehicleState vehicleUnderMouse;

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public void addVehicle(short id, String scriptName) {
        VehicleState state = this.createState(id);
        state.scriptName = scriptName;
        state.setScript(ScriptManager.instance.getVehicle(scriptName));
    }

    public void updateVehicle(SharedVehicleState state1) {
        VehicleState state = this.getState(state1.id);
        if (state == null) {
            return;
        }
        state.set(state1);
    }

    public void updateVehicle(SharedVehicleState state1, int changeBits) {
        VehicleState state = this.getState(state1.id);
        if (state == null) {
            return;
        }
        state.set(state1, changeBits);
    }

    public void removeVehicle(short id) {
        VehicleState state = this.stateMap.remove(id);
        if (state == null) {
            return;
        }
        state.remove();
    }

    VehicleState createState(short id) {
        VehicleState state = new VehicleState(id);
        this.stateMap.put(state.id, state);
        return state;
    }

    VehicleState getState(short id) {
        return this.stateMap.get(id);
    }

    public boolean hasStateFor(short id) {
        return this.getState(id) != null;
    }

    public void update() {
        this.stateMap.forEachValue(state -> {
            state.update();
            return true;
        });
    }

    public void renderWorldMap(UIWorldMap ui) {
        MapSymbolDefinitions.MapSymbolDefinition symbol = MapSymbolDefinitions.getInstance().getSymbolById("SteeringWheel");
        if (symbol == null) {
            return;
        }
        Texture tex = Texture.getSharedTexture(symbol.getTexturePath());
        if (tex == null || !tex.isReady()) {
            return;
        }
        int mouseX = Mouse.getXA() - ui.getAbsoluteX().intValue();
        int mouseY = Mouse.getYA() - ui.getAbsoluteY().intValue();
        long ms = System.currentTimeMillis();
        float maxTexWidth = (float)tex.getWidth() / 1.5f;
        float maxTexHeight = (float)tex.getHeight() / 1.5f;
        double width = PZMath.lerp((float)tex.getWidth() / 2.0f, maxTexWidth, (float)Math.sin((double)ms / 300.0) + 1.0f);
        double height = PZMath.lerp((float)tex.getHeight() / 2.0f, maxTexHeight, (float)Math.sin((double)ms / 300.0) + 1.0f);
        double r = 0.0;
        double g = 0.0;
        double b = 0.0;
        double a = 1.0;
        vehicleUnderMouse = null;
        this.stateMap.forEachValue(state -> {
            float uiX = PZMath.floor(ui.getAPI().worldToUIX(state.getX(), state.getY()));
            float uiY = PZMath.floor(ui.getAPI().worldToUIY(state.getX(), state.getY()));
            ui.DrawTextureScaledCol(tex, (double)uiX - width / 2.0, (double)uiY - height / 2.0, width, height, 0.0, 0.0, 0.0, 1.0);
            if ((float)mouseX >= uiX - maxTexWidth / 2.0f && (float)mouseX < uiX + maxTexWidth / 2.0f && (float)mouseY >= uiY - maxTexHeight / 2.0f && (float)mouseY < uiY + maxTexHeight / 2.0f) {
                vehicleUnderMouse = state;
            }
            return true;
        });
        if (vehicleUnderMouse != null) {
            float uiX = PZMath.floor(ui.getAPI().worldToUIX(vehicleUnderMouse.getX(), vehicleUnderMouse.getY()));
            float uiY = PZMath.floor(ui.getAPI().worldToUIY(vehicleUnderMouse.getX(), vehicleUnderMouse.getY()));
            int textY = PZMath.fastfloor(uiY + (float)(tex.getHeight() / 2));
            UIFont font = UIFont.Medium;
            int fontHgt = TextManager.instance.getFontHeight(font);
            int lineNum = 0;
            double padX = 4.0;
            double padY = 0.0;
            double textR = 0.0;
            double textG = 0.0;
            double textB = 0.0;
            double textA = 1.0;
            double bgR = 1.0;
            double bgG = 1.0;
            double bgB = 1.0;
            double bgA = 0.9;
            ui.drawTextWithBackground(font, vehicleUnderMouse.getScriptName(), uiX, textY + lineNum++, 0.0, 0.0, 0.0, 1.0, 4.0, 0.0, 1.0, 1.0, 1.0, 0.9);
            ui.drawTextWithBackground(font, "Engine Running: %s".formatted(vehicleUnderMouse.isEngineRunning() ? "YES" : "NO"), uiX, textY + fontHgt * lineNum++, 0.0, 0.0, 0.0, 1.0, 4.0, 0.0, 1.0, 1.0, 1.0, 0.9);
            ui.drawTextWithBackground(font, "Alarm Active: %s".formatted(vehicleUnderMouse.isAlarmActive() ? "YES" : "NO"), uiX, textY + fontHgt * lineNum++, 0.0, 0.0, 0.0, 1.0, 4.0, 0.0, 1.0, 1.0, 1.0, 0.9);
            ui.drawTextWithBackground(font, "Siren Active: %s".formatted(vehicleUnderMouse.isSirenActive() ? "YES" : "NO"), uiX, textY + fontHgt * lineNum, 0.0, 0.0, 0.0, 1.0, 4.0, 0.0, 1.0, 1.0, 1.0, 0.9);
        }
    }

    public void stop() {
        this.stateMap.forEachValue(state -> {
            state.remove();
            return true;
        });
        this.stateMap.clear();
    }
}

