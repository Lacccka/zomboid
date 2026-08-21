/*
 * Decompiled with CFR 0.152.
 */
package zombie.core;

import generation.builders.validation.TranslationKeyValidator;
import zombie.UsedFromLua;
import zombie.core.Core;
import zombie.scripting.objects.Registries;
import zombie.scripting.objects.RegistryReset;
import zombie.scripting.objects.ResourceLocation;

@UsedFromLua
public class GameMode {
    public static final GameMode APOCALYPSE = GameMode.registerBase("Apocalypse");
    public static final GameMode OUTBREAK = GameMode.registerBase("Outbreak");
    public static final GameMode EXTINCTION = GameMode.registerBase("Extinction");
    public static final GameMode RISING = GameMode.registerBase("Rising");
    public static final GameMode SANDBOX = GameMode.registerBase("Sandbox");
    public static final GameMode CHALLENGES = GameMode.registerBase("Challenges");
    private final String id;
    private final String title;
    private final String description;
    private final String thumbnail;
    private final String video;

    private GameMode(String id) {
        this.id = id;
        this.title = "UI_NewGame_%s".formatted(id);
        this.description = "%s_desc".formatted(this.title);
        this.thumbnail = "media/ui/playstyleIcons/%s.png".formatted(id);
        this.video = "%s.bik".formatted(id);
    }

    public static GameMode get(ResourceLocation id) {
        return Registries.GAME_MODE.get(id);
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getVideo() {
        return this.video;
    }

    public String toString() {
        return this.id;
    }

    public static GameMode register(String id) {
        return GameMode.register(false, id);
    }

    private static GameMode registerBase(String id) {
        return GameMode.register(true, id);
    }

    private static GameMode register(boolean allowDefaultNamespace, String id) {
        return Registries.GAME_MODE.register(RegistryReset.createLocation(id, allowDefaultNamespace), new GameMode(id));
    }

    static {
        if (Core.IS_DEV) {
            for (GameMode gameMode : Registries.GAME_MODE) {
                TranslationKeyValidator.of(gameMode.title);
                TranslationKeyValidator.of(gameMode.description);
            }
        }
    }
}

