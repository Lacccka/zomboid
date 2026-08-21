/*
 * Decompiled with CFR 0.152.
 */
package zombie.characters;

import fmod.fmod.BaseSoundListener;
import fmod.fmod.DummySoundListener;
import fmod.fmod.SoundListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.util.vector.Quaternion;
import zombie.AttackType;
import zombie.CombatManager;
import zombie.DebugFileWatcher;
import zombie.GameSounds;
import zombie.GameTime;
import zombie.GameWindow;
import zombie.Lua.LuaEventManager;
import zombie.PredicatedFileWatcher;
import zombie.SandboxOptions;
import zombie.SoundManager;
import zombie.SystemDisabler;
import zombie.UpdateSchedulerSimulationLevel;
import zombie.UsedFromLua;
import zombie.ZomboidFileSystem;
import zombie.ZomboidGlobals;
import zombie.ai.State;
import zombie.ai.sadisticAIDirector.SleepingEvent;
import zombie.ai.states.BumpedState;
import zombie.ai.states.ClimbDownSheetRopeState;
import zombie.ai.states.ClimbOverFenceState;
import zombie.ai.states.ClimbOverWallState;
import zombie.ai.states.ClimbSheetRopeState;
import zombie.ai.states.ClimbThroughWindowPositioningParams;
import zombie.ai.states.ClimbThroughWindowState;
import zombie.ai.states.CloseWindowState;
import zombie.ai.states.CollideWithWallState;
import zombie.ai.states.FakeDeadZombieState;
import zombie.ai.states.FishingState;
import zombie.ai.states.FitnessState;
import zombie.ai.states.IdleState;
import zombie.ai.states.LungeState;
import zombie.ai.states.OpenWindowState;
import zombie.ai.states.PathFindState;
import zombie.ai.states.PlayerActionsState;
import zombie.ai.states.PlayerAimState;
import zombie.ai.states.PlayerDraggingCorpse;
import zombie.ai.states.PlayerEmoteState;
import zombie.ai.states.PlayerExtState;
import zombie.ai.states.PlayerFallDownState;
import zombie.ai.states.PlayerFallingState;
import zombie.ai.states.PlayerGetUpState;
import zombie.ai.states.PlayerHitReactionPVPState;
import zombie.ai.states.PlayerHitReactionState;
import zombie.ai.states.PlayerKnockedDown;
import zombie.ai.states.PlayerOnBedState;
import zombie.ai.states.PlayerOnGroundState;
import zombie.ai.states.PlayerSitOnFurnitureState;
import zombie.ai.states.PlayerSitOnGroundState;
import zombie.ai.states.PlayerStrafeState;
import zombie.ai.states.SmashWindowState;
import zombie.ai.states.StaggerBackState;
import zombie.ai.states.StateManager;
import zombie.ai.states.SwipeStatePlayer;
import zombie.ai.states.WalkTowardState;
import zombie.ai.states.animals.AnimalAlertedState;
import zombie.ai.states.animals.AnimalEatState;
import zombie.ai.states.player.PlayerMilkAnimalState;
import zombie.ai.states.player.PlayerMovementState;
import zombie.ai.states.player.PlayerPetAnimalState;
import zombie.ai.states.player.PlayerShearAnimalState;
import zombie.audio.FMODParameterList;
import zombie.audio.GameSound;
import zombie.audio.LoopedRangedWeaponSounds;
import zombie.audio.MusicIntensityConfig;
import zombie.audio.MusicIntensityEvents;
import zombie.audio.MusicThreatStatuses;
import zombie.audio.parameters.ParameterCharacterMovementSpeed;
import zombie.audio.parameters.ParameterCharacterMoving;
import zombie.audio.parameters.ParameterCharacterOnFire;
import zombie.audio.parameters.ParameterCharacterVoicePitch;
import zombie.audio.parameters.ParameterCharacterVoiceType;
import zombie.audio.parameters.ParameterDeaf;
import zombie.audio.parameters.ParameterDragMaterial;
import zombie.audio.parameters.ParameterElevation;
import zombie.audio.parameters.ParameterEquippedBaggageContainer;
import zombie.audio.parameters.ParameterExercising;
import zombie.audio.parameters.ParameterFirearmDistance;
import zombie.audio.parameters.ParameterFirearmInside;
import zombie.audio.parameters.ParameterFirearmRoomSize;
import zombie.audio.parameters.ParameterFootstepMaterial;
import zombie.audio.parameters.ParameterFootstepMaterial2;
import zombie.audio.parameters.ParameterIsStashTile;
import zombie.audio.parameters.ParameterLocalPlayer;
import zombie.audio.parameters.ParameterMeleeHitSurface;
import zombie.audio.parameters.ParameterMoodles;
import zombie.audio.parameters.ParameterOverlapFoliageType;
import zombie.audio.parameters.ParameterPlayerHealth;
import zombie.audio.parameters.ParameterRoomTypeEx;
import zombie.audio.parameters.ParameterShoeType;
import zombie.audio.parameters.ParameterVehicleHitLocation;
import zombie.characters.AttachedItems.AttachedItems;
import zombie.characters.BodyDamage.BodyDamage;
import zombie.characters.BodyDamage.BodyPart;
import zombie.characters.BodyDamage.BodyPartType;
import zombie.characters.BodyDamage.Fitness;
import zombie.characters.BodyDamage.Nutrition;
import zombie.characters.Capability;
import zombie.characters.CharacterInputMode;
import zombie.characters.CharacterStat;
import zombie.characters.CharacterTimedActions.BaseAction;
import zombie.characters.CharacterTimedActions.LuaTimedActionNew;
import zombie.characters.CheatType;
import zombie.characters.ClothingWetness;
import zombie.characters.ClothingWetnessSync;
import zombie.characters.ContextualAction;
import zombie.characters.FallDamage;
import zombie.characters.FallingConstants;
import zombie.characters.HitReactionNetworkAI;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoLivingCharacter;
import zombie.characters.IsoSurvivor;
import zombie.characters.IsoZombie;
import zombie.characters.MPDebugAI;
import zombie.characters.Moodles.Moodles;
import zombie.characters.NetworkPlayerAI;
import zombie.characters.PlayerCraftHistory;
import zombie.characters.Role;
import zombie.characters.Roles;
import zombie.characters.SafetySystemManager;
import zombie.characters.SurvivorDesc;
import zombie.characters.TriggerXmlFile;
import zombie.characters.ZombiesZoneDefinition;
import zombie.characters.action.ActionGroup;
import zombie.characters.animals.IsoAnimal;
import zombie.characters.component.AIComponent;
import zombie.characters.component.CharacterInputComponent;
import zombie.characters.component.NetworkComponent;
import zombie.characters.component.NetworkPlayerComponent;
import zombie.characters.ecs.ECSComponent;
import zombie.characters.professions.CharacterProfessionDefinition;
import zombie.characters.skills.PerkFactory;
import zombie.core.BoxedStaticValues;
import zombie.core.Color;
import zombie.core.Core;
import zombie.core.Translator;
import zombie.core.logger.ExceptionLogger;
import zombie.core.logger.LoggerManager;
import zombie.core.math.PZMath;
import zombie.core.network.ByteBufferReader;
import zombie.core.network.ByteBufferWriter;
import zombie.core.opengl.Shader;
import zombie.core.physics.BallisticsController;
import zombie.core.physics.PhysicsDebugRenderer;
import zombie.core.profiling.AbstractPerformanceProfileProbe;
import zombie.core.profiling.PerformanceProfileProbe;
import zombie.core.properties.IsoObjectChange;
import zombie.core.raknet.UdpConnection;
import zombie.core.random.Rand;
import zombie.core.skinnedmodel.HelperFunctions;
import zombie.core.skinnedmodel.IGrappleable;
import zombie.core.skinnedmodel.ModelManager;
import zombie.core.skinnedmodel.advancedanimation.AnimEvent;
import zombie.core.skinnedmodel.advancedanimation.AnimLayer;
import zombie.core.skinnedmodel.advancedanimation.IAnimationVariableLogger;
import zombie.core.skinnedmodel.advancedanimation.IAnimationVariableSource;
import zombie.core.skinnedmodel.animation.AnimationPlayer;
import zombie.core.skinnedmodel.animation.AnimationTrack;
import zombie.core.skinnedmodel.animation.TwistableBoneTransform;
import zombie.core.skinnedmodel.visual.AnimalVisual;
import zombie.core.skinnedmodel.visual.BaseVisual;
import zombie.core.skinnedmodel.visual.HumanVisual;
import zombie.core.skinnedmodel.visual.IAnimalVisual;
import zombie.core.skinnedmodel.visual.IHumanVisual;
import zombie.core.skinnedmodel.visual.ItemVisuals;
import zombie.core.textures.ColorInfo;
import zombie.debug.DebugLog;
import zombie.debug.DebugOptions;
import zombie.debug.DebugType;
import zombie.debug.LogSeverity;
import zombie.entity.ComponentType;
import zombie.entity.components.crafting.BaseCraftingLogic;
import zombie.input.AimingReticle;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.inventory.types.Clothing;
import zombie.inventory.types.DrainableComboItem;
import zombie.inventory.types.HandWeapon;
import zombie.inventory.types.WeaponType;
import zombie.iso.BentFences;
import zombie.iso.IsoButcherHook;
import zombie.iso.IsoCamera;
import zombie.iso.IsoCell;
import zombie.iso.IsoChunk;
import zombie.iso.IsoDirections;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoMovingObject;
import zombie.iso.IsoObject;
import zombie.iso.IsoPhysicsObject;
import zombie.iso.IsoUtils;
import zombie.iso.IsoWorld;
import zombie.iso.SliceY;
import zombie.iso.SpriteDetails.IsoFlagType;
import zombie.iso.SpriteDetails.IsoObjectType;
import zombie.iso.Vector2;
import zombie.iso.Vector3;
import zombie.iso.areas.DesignationZoneAnimal;
import zombie.iso.areas.NonPvpZone;
import zombie.iso.areas.SafeHouse;
import zombie.iso.fboRenderChunk.FBORenderChunkManager;
import zombie.iso.objects.IsoBarricade;
import zombie.iso.objects.IsoCurtain;
import zombie.iso.objects.IsoDeadBody;
import zombie.iso.objects.IsoDoor;
import zombie.iso.objects.IsoHutch;
import zombie.iso.objects.IsoThumpable;
import zombie.iso.objects.IsoWindow;
import zombie.iso.objects.IsoWindowFrame;
import zombie.iso.sprite.IsoReticle;
import zombie.iso.sprite.IsoReticleMode;
import zombie.iso.weather.ClimateManager;
import zombie.iso.zones.Zone;
import zombie.network.BodyDamageSync;
import zombie.network.GameClient;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.PassengerMap;
import zombie.network.ServerLOS;
import zombie.network.ServerMap;
import zombie.network.ServerOptions;
import zombie.network.ServerWorldDatabase;
import zombie.network.fields.IPositional;
import zombie.network.fields.hit.HitInfo;
import zombie.network.packets.INetworkPacket;
import zombie.network.packets.SyncPlayerStatsPacket;
import zombie.network.statistics.data.ConnectionQueueStatistic;
import zombie.pathfind.PathFindBehavior2;
import zombie.pathfind.Point;
import zombie.pathfind.PolygonalMap2;
import zombie.popman.animal.AnimalInstanceManager;
import zombie.savefile.ClientPlayerDB;
import zombie.savefile.PlayerDB;
import zombie.scripting.entity.components.crafting.CraftRecipe;
import zombie.scripting.objects.CharacterProfession;
import zombie.scripting.objects.CharacterTrait;
import zombie.scripting.objects.ItemBodyLocation;
import zombie.scripting.objects.ItemKey;
import zombie.scripting.objects.MoodleType;
import zombie.scripting.objects.Registries;
import zombie.scripting.objects.ResourceLocation;
import zombie.scripting.objects.SoundKey;
import zombie.scripting.objects.VehicleScript;
import zombie.scripting.objects.WeaponCategory;
import zombie.ui.TutorialManager;
import zombie.ui.UIManager;
import zombie.util.StringUtils;
import zombie.util.Type;
import zombie.util.lambda.Invokers;
import zombie.util.lambda.PZOptional;
import zombie.util.list.PZArrayList;
import zombie.util.list.PZArrayUtil;
import zombie.vehicles.BaseVehicle;
import zombie.vehicles.VehiclePart;
import zombie.vehicles.VehicleWindow;
import zombie.vehicles.VehiclesDB2;
import zombie.world.WorldDictionary;
import zombie.worldMap.WorldMapRemotePlayer;
import zombie.worldMap.WorldMapRemotePlayers;

@UsedFromLua
public class IsoPlayer
extends IsoLivingCharacter
implements IAnimalVisual,
IHumanVisual,
IPositional {
    private static final int RAND_INJURY = 7;
    private static final int RAND_DISCOMFORT = 7;
    private static final int RAND_SICK = 7;
    private static final int RAND_ENDURANCE = 10;
    private static final int RAND_IDLE_EMOTE = 10;
    private static final int UPDATES_BETWEEN_RANDOM_IDLE_FIDGETS = 5000;
    private static final float StrongTraitMaxWeightDelta = 1.5f;
    private static final float WeakTraitMaxWeightDelta = 0.75f;
    private static final float FeebleTraitMaxWeightDelta = 0.9f;
    private static final float StoutTraitMaxWeightDelta = 1.25f;
    private static final int REMOTE_PLAYER_PATHFINDER_SUPPRESS_MAX_DIST_TILES = 3;
    private static final float IN_TREES_INJURY_INTERVAL_SECONDS = 1.33f;
    private static final float IN_TREES_SPEED_PARK_RANGER_MULTIPLIER = 1.3f;
    private static final float IN_TREES_SPEED_LUMBERJACK_MULTIPLIER = 1.15f;
    private static final float IN_TREES_SPEED_RUNNING_MULTIPLIER = 1.1f;
    public PhysicsDebugRenderer physicsDebugRenderer;
    private AttackType attackType = AttackType.NONE;
    public static final String DEATH_MUSIC_NAME = "PlayerDied";
    public static boolean isTestAIMode;
    public static final boolean NoSound = false;
    public static int assumedPlayer;
    public static int numPlayers;
    public static final short MAX = 4;
    public static final IsoPlayer[] players;
    private static IsoPlayer instance;
    private static final Object instanceLock;
    private static final Vector2 testHitPosition;
    private static int followDeadCount;
    private boolean ignoreAutoVault;
    public int remoteSneakLvl;
    public int remoteStrLvl;
    public int remoteFitLvl;
    public boolean moodleCantSprint;
    private static final Vector2 tempo;
    private static final Vector2 tempVector2;
    private static boolean coopPvp;
    private boolean ignoreContextKey;
    private long lastRemoteUpdate;
    public List<IsoAnimal> luredAnimals = new ArrayList<IsoAnimal>();
    public boolean isLuringAnimals;
    private boolean invPageDirty;
    private final List<IsoAnimal> attachedAnimals = new ArrayList<IsoAnimal>();
    public boolean spottedByPlayer;
    private final HashMap<Integer, Integer> spottedPlayerTimer = new HashMap();
    private float extUpdateCount;
    private boolean attackStarted;
    private static final PredicatedFileWatcher m_isoPlayerTriggerWatcher;
    private final PredicatedFileWatcher setClothingTriggerWatcher;
    private static final Vector2 tempVector2_1;
    private static final Vector2 tempVector2_2;
    protected BaseVisual baseVisual;
    protected final ItemVisuals remotePlayerItemVisuals = new ItemVisuals();
    public boolean targetedByZombie;
    public float lastTargeted = 1.0E8f;
    public float timeSinceOpenDoor;
    public float timeSinceCloseDoor;
    public boolean remote;
    private int timeSinceLastNetData;
    public Role role = Roles.getDefaultForNewUser();
    public String tagPrefix = "";
    public boolean showTag = true;
    public boolean factionPvp;
    public short onlineId = 1;
    public int onlineChunkGridWidth;
    public boolean joypadIgnoreChargingRt;
    public boolean mpTorchCone;
    public float mpTorchDist;
    public float mpTorchStrength;
    public int playerIndex;
    public int serverPlayerIndex = 1;
    public float useChargeDelta;
    public float contextPanic;
    public float numNearbyBuildingsRooms;
    public boolean isCharging;
    public boolean isChargingLt;
    private boolean lookingWhileInVehicle;
    private boolean climbOverWallSuccess;
    private boolean climbOverWallStruggle;
    private boolean justMoved;
    public float maxWeightDelta = 1.0f;
    public float currentSpeed;
    public boolean deathFinished;
    public boolean isSpeek;
    public boolean isVoiceMute;
    public final Vector2 playerMoveDir = new Vector2(0.0f, 0.0f);
    public BaseSoundListener soundListener;
    public String username = "Bob";
    public boolean dirtyRecalcGridStack = true;
    public float dirtyRecalcGridStackTime = 10.0f;
    public float runningTime;
    public float timePressedContext;
    public float chargeTime;
    private float useChargeTime;
    private boolean pressContext;
    private boolean letGoAfterContextIsReleased;
    public float closestZombie = 1000000.0f;
    public final Vector2 lastAngle = new Vector2();
    public String saveFileName;
    public boolean bannedAttacking;
    public int sqlId = -1;
    protected int clearSpottedTimer = -1;
    protected float timeSinceLastStab;
    protected Stack<IsoMovingObject> lastSpotted = new Stack();
    protected boolean changeCharacterDebounce;
    protected int followId;
    protected final Stack<IsoGameCharacter> followCamStack = new Stack();
    protected boolean seenThisFrame;
    protected boolean couldBeSeenThisFrame;
    protected float asleepTime;
    protected final Stack<IsoMovingObject> spottedList = new Stack();
    protected int ticksSinceSeenZombie = 9999999;
    protected boolean waiting = true;
    protected IsoSurvivor dragCharacter;
    protected float heartDelay = 30.0f;
    protected float heartDelayMax = 30.0f;
    protected boolean aimingWeaponAnimation;
    protected long heartEventInstance;
    protected int dialogMood = 1;
    protected int ping;
    protected IsoMovingObject dragObject;
    private double lastSeenZombieTime = 2.0;
    private int checkSafehouse = 200;
    private boolean attackFromBehind;
    private int hypothermiaCache = -1;
    private int hyperthermiaCache = -1;
    private float ticksSincePressedMovement;
    private boolean flickTorch;
    private float checkNearbyRooms;
    private boolean useVehicle;
    private boolean usedVehicle;
    private static final Vector3f tempVector3f;
    private static final org.lwjgl.util.vector.Vector3f templwjglVector3f;
    private final InputState inputState = new InputState();
    private boolean isWearingNightVisionGoggles;
    private float moveSpeed = 0.06f;
    private int offSetXUi;
    private int offSetYUi;
    private float combatSpeed = 1.0f;
    private double hoursSurvived;
    private boolean isAuthorizedHandToHandAction = true;
    private boolean isAuthorizedHandToHand = true;
    private boolean blockMovement;
    private Nutrition nutrition;
    private Fitness fitness;
    private boolean forceOverrideAnim;
    private boolean initiateAttack;
    private final ColorInfo tagColor = new ColorInfo(1.0f, 1.0f, 1.0f, 1.0f);
    private String displayName;
    private boolean seeNonPvpZone;
    private boolean seeDesignationZone;
    private final ArrayList<Double> selectedZonesForHighlight = new ArrayList();
    private Double selectedZoneForHighlight = 0.0;
    private final HashMap<Long, Long> mechanicsItem = new HashMap();
    private int sleepingPillsTaken;
    private long lastPillsTaken;
    private long heavyBreathInstance;
    private String heavyBreathSoundName;
    private boolean allChatMuted;
    private final boolean multiplayer;
    private String saveFileIp;
    protected BaseVehicle vehicle4testCollision;
    private long steamId;
    private final VehicleContainerData vehicleContainerData = new VehicleContainerData();
    private boolean isWalking;
    private float footInjuryTimer;
    private float inTreesInjuryTimer;
    private float turnDelta;
    protected boolean isPlayerMoving;
    private float walkSpeed;
    private float walkInjury;
    private float runSpeed;
    private float idleSpeed;
    private float deltaX;
    private float deltaY;
    private float windspeed;
    private float windForce;
    private float ipX;
    private float ipY;
    private float drunkDelayCommandTimer;
    private float pressedRunTimer;
    private boolean pressedRun;
    private boolean meleePressed;
    private boolean grapplePressed;
    private boolean canLetGoOfGrappled = true;
    private boolean lastAttackWasHandToHand;
    private boolean isPerformingAnAction;
    private final ArrayList<String> alreadyReadBook = new ArrayList();
    public byte bleedingLevel;
    private final MusicIntensityEvents musicIntensityEvents = new MusicIntensityEvents();
    private final MusicThreatStatuses musicThreatStatuses = new MusicThreatStatuses(this);
    private final boolean musicIntensityInside = true;
    private boolean isFarming;
    private float attackVariationX;
    private float attackVariationY;
    public String accessLevel;
    private boolean hasObstacleOnPath;
    private LuaTimedActionNew timedActionToRetrigger;
    private boolean pathfindRun;
    private static final PZArrayList<HitInfo> s_targetsProne;
    private static final PZArrayList<HitInfo> s_targetsStanding;
    private final ArrayList<ContextualAction> contextualActions = new ArrayList();
    private String weaponT;
    private final ParameterCharacterMoving parameterCharacterMoving = new ParameterCharacterMoving(this);
    private final ParameterCharacterMovementSpeed parameterCharacterMovementSpeed = new ParameterCharacterMovementSpeed(this);
    private final ParameterCharacterOnFire parameterCharacterOnFire = new ParameterCharacterOnFire(this);
    private final ParameterCharacterVoiceType parameterCharacterVoiceType = new ParameterCharacterVoiceType(this);
    private final ParameterCharacterVoicePitch parameterCharacterVoicePitch = new ParameterCharacterVoicePitch(this);
    private final ParameterDeaf parameterDeaf = new ParameterDeaf(this);
    private final ParameterDragMaterial parameterDragMaterial = new ParameterDragMaterial(this);
    private final ParameterElevation parameterElevation = new ParameterElevation(this);
    private final ParameterEquippedBaggageContainer parameterEquippedBaggageContainer = new ParameterEquippedBaggageContainer(this);
    private final ParameterExercising parameterExercising = new ParameterExercising(this);
    private final ParameterFirearmDistance parameterFirearmDistance = new ParameterFirearmDistance(this);
    private final ParameterFirearmInside parameterFirearmInside = new ParameterFirearmInside(this);
    private final ParameterFirearmRoomSize parameterFirearmRoomSize = new ParameterFirearmRoomSize(this);
    private final ParameterFootstepMaterial parameterFootstepMaterial = new ParameterFootstepMaterial(this);
    private final ParameterFootstepMaterial2 parameterFootstepMaterial2 = new ParameterFootstepMaterial2(this);
    private final ParameterIsStashTile parameterIsStashTile = new ParameterIsStashTile(this);
    private final ParameterLocalPlayer parameterLocalPlayer = new ParameterLocalPlayer(this);
    private final ParameterMeleeHitSurface parameterMeleeHitSurface = new ParameterMeleeHitSurface(this);
    private final ParameterOverlapFoliageType parameterOverlapFoliageType = new ParameterOverlapFoliageType(this);
    private final ParameterPlayerHealth parameterPlayerHealth = new ParameterPlayerHealth(this);
    private final ParameterVehicleHitLocation parameterVehicleHitLocation = new ParameterVehicleHitLocation();
    private final ParameterShoeType parameterShoeType = new ParameterShoeType(this);
    private ParameterMoodles parameterMoodles;
    private final GrapplerGruntChance grapplerGruntChance = new GrapplerGruntChance(this);
    private final BallisticsController.AimingVectorParameters updateAimingVectorParams = new BallisticsController.AimingVectorParameters();
    private final PlayerCraftHistory craftHistory = new PlayerCraftHistory(this);
    public boolean autoDrink = true;
    private long lastCheatToggleMillis;
    protected static final float NETWORK_SPEED_MUL_MIN = 0.9f;
    protected static final float NETWORK_SPEED_MUL_MAX = 1.1f;
    protected static final float NETWORK_SPEED_SMOOTH_START = 0.9f;
    protected static final float NETWORK_SPEED_SMOOTH_END = 1.1f;
    private static final ArrayList<IsoPlayer> RecentlyRemoved;
    private static final MoveVars s_moveVars;
    private final MoveVars drunkMoveVars = new MoveVars();
    private long attackAnimThrowTimer = System.currentTimeMillis();

    public IsoPlayer(IsoCell cell) {
        this(cell, null, 0, 0, 0);
    }

    public IsoPlayer(IsoCell cell, SurvivorDesc desc, int x, int y, int z, boolean isAnimal) {
        super(cell, x, y, z);
        this.setIsAnimal(isAnimal);
        this.addOnDiedListener(this::onDied, false);
        if (this.isAnimal()) {
            this.baseVisual = new AnimalVisual(this);
        } else {
            this.baseVisual = new HumanVisual(this);
            this.registerVariableCallbacks();
            this.registerAnimEventCallbacks();
            this.getWrappedGrappleable().setOnGrappledEndCallback(this::onGrappleEnded);
        }
        this.setForwardIsoDirection(IsoDirections.W);
        if (!isAnimal) {
            this.nutrition = new Nutrition(this);
            this.fitness = new Fitness(this);
            this.clothingWetness = new ClothingWetness(this);
            if (GameServer.server) {
                this.clothingWetnessSync = new ClothingWetnessSync(this);
            }
            this.initAttachedItems("Human");
        }
        this.initWornItems("Human");
        this.descriptor = desc != null ? desc : new SurvivorDesc();
        this.setFemale(this.descriptor.isFemale());
        this.setVoiceType(this.descriptor.getVoiceType());
        this.setVoicePitch(this.descriptor.getVoicePitch());
        if (!isAnimal) {
            this.Dressup(this.descriptor);
            this.getHumanVisual().copyFrom(this.descriptor.getHumanVisual());
            this.InitSpriteParts(this.descriptor);
        }
        LuaEventManager.triggerEvent("OnCreateLivingCharacter", this, this.descriptor);
        this.descriptor.setInstance(this);
        if (!isAnimal) {
            this.speakColour = new Color(Rand.Next(135) + 120, Rand.Next(135) + 120, Rand.Next(135) + 120, 255);
        }
        if (GameClient.client) {
            if (Core.getInstance().getMpTextColor() != null) {
                this.speakColour = new Color(Core.getInstance().getMpTextColor().r, Core.getInstance().getMpTextColor().g, Core.getInstance().getMpTextColor().b, 1.0f);
            } else {
                Core.getInstance().setMpTextColor(new ColorInfo(this.speakColour.r, this.speakColour.g, this.speakColour.b, 1.0f));
                try {
                    Core.getInstance().saveOptions();
                }
                catch (IOException e) {
                    DebugType.General.printException(e, LogSeverity.Error);
                }
            }
        }
        if (Core.gameMode.equals("LastStand")) {
            this.characterTraits.set(CharacterTrait.STRONG, true);
        }
        if (this.characterTraits.get(CharacterTrait.STRONG)) {
            this.maxWeightDelta = 1.5f;
        } else if (this.characterTraits.get(CharacterTrait.WEAK)) {
            this.maxWeightDelta = 0.75f;
        } else if (this.characterTraits.get(CharacterTrait.FEEBLE)) {
            this.maxWeightDelta = 0.9f;
        } else if (this.characterTraits.get(CharacterTrait.STOUT)) {
            this.maxWeightDelta = 1.25f;
        }
        this.multiplayer = GameServer.server || GameClient.client;
        this.vehicle4testCollision = null;
        if (!isAnimal && Core.debug && DebugOptions.instance.cheat.player.startInvisible.getValue()) {
            this.setGhostMode(true);
            this.setGodMod(true);
        }
        if (!isAnimal) {
            this.getActionContext().setGroup(ActionGroup.getActionGroup("player"));
            this.initializeStates();
            DebugFileWatcher.instance.add(m_isoPlayerTriggerWatcher);
        }
        this.setClothingTriggerWatcher = new PredicatedFileWatcher(ZomboidFileSystem.instance.getMessagingDirSub("Trigger_SetClothing.xml"), TriggerXmlFile.class, this::onTrigger_setClothingToXmlTriggerFile);
        this.initFMODParameters();
    }

    public IsoPlayer(IsoCell cell, SurvivorDesc desc, int x, int y, int z) {
        super(cell, x, y, z);
        this.addOnDiedListener(this::onDied, false);
        this.baseVisual = new HumanVisual(this);
        this.registerVariableCallbacks();
        this.registerAnimEventCallbacks();
        this.getWrappedGrappleable().setOnGrappledEndCallback(this::onGrappleEnded);
        this.setForwardIsoDirection(IsoDirections.W);
        this.nutrition = new Nutrition(this);
        this.fitness = new Fitness(this);
        this.initWornItems("Human");
        this.initAttachedItems("Human");
        this.clothingWetness = new ClothingWetness(this);
        if (GameServer.server) {
            this.clothingWetnessSync = new ClothingWetnessSync(this);
        }
        this.descriptor = desc != null ? desc : new SurvivorDesc();
        this.setFemale(this.descriptor.isFemale());
        this.Dressup(this.descriptor);
        this.getHumanVisual().copyFrom(this.descriptor.getHumanVisual());
        this.InitSpriteParts(this.descriptor);
        LuaEventManager.triggerEvent("OnCreateLivingCharacter", this, this.descriptor);
        this.descriptor.setInstance(this);
        this.speakColour = new Color(Rand.Next(135) + 120, Rand.Next(135) + 120, Rand.Next(135) + 120, 255);
        if (GameClient.client) {
            if (Core.getInstance().getMpTextColor() != null) {
                this.speakColour = new Color(Core.getInstance().getMpTextColor().r, Core.getInstance().getMpTextColor().g, Core.getInstance().getMpTextColor().b, 1.0f);
            } else {
                Core.getInstance().setMpTextColor(new ColorInfo(this.speakColour.r, this.speakColour.g, this.speakColour.b, 1.0f));
                try {
                    Core.getInstance().saveOptions();
                }
                catch (IOException e) {
                    DebugType.General.printException(e, LogSeverity.Error);
                }
            }
        }
        if (Core.gameMode.equals("LastStand")) {
            this.characterTraits.set(CharacterTrait.STRONG, true);
        }
        if (this.characterTraits.get(CharacterTrait.STRONG)) {
            this.maxWeightDelta = 1.5f;
        } else if (this.characterTraits.get(CharacterTrait.WEAK)) {
            this.maxWeightDelta = 0.75f;
        } else if (this.characterTraits.get(CharacterTrait.FEEBLE)) {
            this.maxWeightDelta = 0.9f;
        } else if (this.characterTraits.get(CharacterTrait.STOUT)) {
            this.maxWeightDelta = 1.25f;
        }
        this.multiplayer = GameServer.server || GameClient.client;
        this.vehicle4testCollision = null;
        if (Core.debug && DebugOptions.instance.cheat.player.startInvisible.getValue()) {
            this.setGhostMode(true);
            this.setGodMod(true);
        }
        this.getActionContext().setGroup(ActionGroup.getActionGroup("player"));
        this.initializeStates();
        DebugFileWatcher.instance.add(m_isoPlayerTriggerWatcher);
        this.setClothingTriggerWatcher = new PredicatedFileWatcher(ZomboidFileSystem.instance.getMessagingDirSub("Trigger_SetClothing.xml"), TriggerXmlFile.class, this::onTrigger_setClothingToXmlTriggerFile);
        this.setVoiceType(this.descriptor.getVoiceType());
        this.setVoicePitch(this.descriptor.getVoicePitch());
        this.initFMODParameters();
    }

    @Override
    public void registerECSComponents() {
        super.registerECSComponents();
        this.setECSComponent(new CharacterInputComponent());
        this.setECSComponent(new NetworkPlayerComponent(this));
    }

    public void setOnlineID(short value) {
        this.onlineId = value;
    }

    private void registerVariableCallbacks() {
        this.setVariable("CombatSpeed", () -> this.combatSpeed, (float val) -> {
            this.combatSpeed = val;
        }, (IAnimationVariableSource owner) -> "The combat speed multiplier. Usually 0.0 - 1.0");
        this.setVariable("TurnDelta", () -> this.turnDelta, (float val) -> {
            this.turnDelta = val;
        }, (IAnimationVariableSource owner) -> "The rate of turn per frame. Multiplier: 0.0 - 1.0 but often can be set much higher.");
        this.setVariable("blockMovement", this::isBlockMovement, this::setBlockMovement, (IAnimationVariableSource owner) -> "Is the character blocking movement commands. Eg while busy crawling through a window, the character cannot accept other movement commands.");
        this.setVariable("sneaking", this::isSneaking, this::setSneaking, (IAnimationVariableSource owner) -> "Is the character sneaking.");
        this.setVariable("initiateAttack", this::isInitiateAttack, this::setInitiateAttack, (IAnimationVariableSource owner) -> "The character wishes to initiate an attack.");
        this.setVariable("isMoving", this::isPlayerMoving, (IAnimationVariableSource owner) -> "The character is moving.");
        this.setVariable("isRunning", this::isRunning, this::setRunning, (IAnimationVariableSource owner) -> "The character is running.");
        this.setVariable("run", this::isRunning, this::setRunning, (IAnimationVariableSource owner) -> "The character is running.");
        this.setVariable("isSprinting", this::isSprinting, this::setSprinting, (IAnimationVariableSource owner) -> "The character is sprinting.");
        this.setVariable("sprint", this::isSprinting, this::setSprinting, (IAnimationVariableSource owner) -> "The character is sprinting.");
        this.setVariable("isStrafing", this::isStrafing, (IAnimationVariableSource owner) -> "The character is strafing.");
        this.setVariable("WalkSpeed", () -> this.walkSpeed, (float val) -> {
            this.walkSpeed = val;
        }, (IAnimationVariableSource owner) -> "Walk speed multiplier. Usually 0.0 - 1.0 but can be higher.");
        this.setVariable("WalkInjury", () -> this.walkInjury, (float val) -> {
            this.walkInjury = val;
        }, (IAnimationVariableSource owner) -> "Walk speed while injured. Multiplier.");
        this.setVariable("RunSpeed", () -> this.runSpeed, (float val) -> {
            this.runSpeed = val;
        }, (IAnimationVariableSource owner) -> "Running speed. Multiplier.");
        this.setVariable("IdleSpeed", () -> this.idleSpeed, (float val) -> {
            this.idleSpeed = val;
        }, (IAnimationVariableSource owner) -> "Idle speed. Multiplier.");
        this.setVariable("DeltaX", () -> this.deltaX, (float val) -> {
            this.deltaX = val;
        }, (IAnimationVariableSource owner) -> "Movement rate per frame, along the X axis.");
        this.setVariable("DeltaY", () -> this.deltaY, (float val) -> {
            this.deltaY = val;
        }, (IAnimationVariableSource owner) -> "Movement rate per frame, along the Y axis.");
        this.setVariable("Windspeed", () -> this.windspeed, (float val) -> {
            this.windspeed = val;
        }, (IAnimationVariableSource owner) -> "The character is experiencing wind moving at this speed.");
        this.setVariable("WindForce", () -> this.windForce, (float val) -> {
            this.windForce = val;
        }, (IAnimationVariableSource owner) -> "The character is experiencing wind at this force.");
        this.setVariable("IPX", () -> this.ipX, (float val) -> {
            this.ipX = val;
        }, (IAnimationVariableSource owner) -> "The character's impulse-movement rate per frame, along the X axis.");
        this.setVariable("IPY", () -> this.ipY, (float val) -> {
            this.ipY = val;
        }, (IAnimationVariableSource owner) -> "The character's impulse-movement rate per frame, along the Y axis.");
        this.setVariable("attacktype", this::getAttackTypeAnimationKey, (IAnimationVariableSource owner) -> "The character is performing an attack of this type.");
        this.setVariable("attackfrombehind", () -> this.attackFromBehind, (IAnimationVariableSource owner) -> "The character is attacking a target from behind.");
        this.setVariable("bundervehicle", this::isUnderVehicle, (IAnimationVariableSource owner) -> "The character is currently underneath a vehicle.");
        this.setVariable("reanimatetimer", this::getReanimateTimer, (IAnimationVariableSource owner) -> "The character's waiting to reanimate. Timer countdown.");
        this.setVariable("isattacking", this::isAttacking, (IAnimationVariableSource owner) -> "The character is currently performing an attack.");
        this.setVariable("beensprintingfor", this::getBeenSprintingFor, (IAnimationVariableSource owner) -> "The character has been sprinting for this amount of time.");
        this.setVariable("bannedAttacking", () -> this.bannedAttacking, (IAnimationVariableSource owner) -> "Can this character perform an attack.");
        this.setVariable("meleePressed", () -> this.meleePressed, (IAnimationVariableSource owner) -> "The character detected an input to perform a melee attack.");
        this.setVariable("grapplePressed", () -> this.grapplePressed, (IAnimationVariableSource owner) -> "The character detected an input to perform a grapple.");
        this.setVariable("canLetGoOfGrappled", () -> this.canLetGoOfGrappled, (IAnimationVariableSource owner) -> "The character can let go of its grappled target.");
        this.setVariable("Weapon", this::getWeaponType, this::setWeaponType, (IAnimationVariableSource owner) -> "The character's equipped weapon type.");
        this.setVariable("BumpFall", false);
        this.setVariable("bClient", () -> GameClient.client, (IAnimationVariableSource owner) -> "Network: The character is in a multiplier game, client-side.");
        this.setVariable("bRemote", () -> this.remote, (IAnimationVariableSource owner) -> "Network: The character is in a multiplier game, and is a remote character.");
        this.setVariable("IsPerformingAnAction", this::isPerformingAnAction, this::setPerformingAnAction, (IAnimationVariableSource owner) -> "The character is currently performing an action.");
        this.setVariable("bShoveAiming", this::isShovingWhileAiming, (IAnimationVariableSource owner) -> "The character is currently aiming, and performing a shove.");
        this.setVariable("bGrappleAiming", this::isGrapplingWhileAiming, (IAnimationVariableSource owner) -> "The character is currently aiming, and performing a grapple.");
        this.setVariable("AttackVariationX", this::getAttackVariationX, (IAnimationVariableSource owner) -> "The character's attack varation, along the x-axis.");
        this.setVariable("AttackVariationY", this::getAttackVariationY, (IAnimationVariableSource owner) -> "The character's attack variation, along the y-axis.");
        this.setVariable("sneakLimpSpeedScale", 1.0f, this::getSneakLimpSpeedScale, this::setSneakLimpSpeedScale, (IAnimationVariableSource owner) -> "Get the character Limp speed scale while sneaking.");
    }

    private void registerAnimEventCallbacks() {
        this.addAnimEventListener("GrapplerRandomGrunt", this::OnAnimEvent_GrapplerPlayRandomGrunt);
    }

    private void onGrappleEnded() {
        this.setDoGrappleLetGoAfterContextKeyIsReleased(false);
    }

    private void OnAnimEvent_GrapplerPlayRandomGrunt(IsoGameCharacter owner, String gruntSoundList) {
        if (Rand.Next(100) < this.grapplerGruntChance.gruntChance) {
            this.grapplerGruntChance.gruntChance = this.grapplerGruntChance.baseChance;
            String[] gruntSounds = gruntSoundList.split(",");
            int grundSoundIdx = Rand.Next(0, gruntSounds.length);
            String gruntSound = gruntSounds[grundSoundIdx];
            DebugType.Zombie.trace("Dragging corpse. Grunting: %s", gruntSound);
            this.stopPlayerVoiceSound(gruntSound);
            this.playerVoiceSound(gruntSound);
        } else {
            this.grapplerGruntChance.gruntChance += this.grapplerGruntChance.chanceIncrement;
        }
    }

    @Override
    protected Vector2 getDeferredMovement(Vector2 result, boolean reset) {
        super.getDeferredMovement(result, reset);
        if (DebugOptions.instance.cheat.player.invisibleSprint.getValue() && this.isGhostMode() && (this.IsRunning() || this.isSprinting()) && !this.isCurrentState(ClimbOverFenceState.instance()) && !this.isCurrentState(ClimbThroughWindowState.instance()) && !this.isCurrentState(PlayerGetUpState.instance())) {
            if (this.getPath2() == null && !this.pressedMovement(false) && !this.isAutoWalk()) {
                return result.set(0.0f, 0.0f);
            }
            if (this.getCurrentBuilding() != null) {
                result.scale(2.5f);
                return result;
            }
            result.scale(7.5f);
        }
        return result;
    }

    @Override
    public float getTurnDelta() {
        if (DebugOptions.instance.cheat.player.invisibleSprint.getValue() && this.isGhostMode() && (this.isRunning() || this.isSprinting()) && !this.isSittingOnFurniture()) {
            return 10.0f;
        }
        return super.getTurnDelta();
    }

    public void setPerformingAnAction(boolean val) {
        this.isPerformingAnAction = val;
    }

    public boolean isPerformingAnAction() {
        return this.isPerformingAnAction;
    }

    @Override
    public boolean isAttacking() {
        return !this.isAttackType(AttackType.NONE);
    }

    @Override
    public boolean shouldBeTurning() {
        if (this.getAnimationPlayer().getMultiTrack().getTrackCount() == 0) {
            return false;
        }
        return super.shouldBeTurning();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void invokeOnPlayerInstance(Runnable callback) {
        Object object = instanceLock;
        synchronized (object) {
            if (instance != null) {
                callback.run();
            }
        }
    }

    public static IsoPlayer getInstance() {
        return instance;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setInstance(IsoPlayer newInstance) {
        Object object = instanceLock;
        synchronized (object) {
            instance = newInstance;
        }
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    private static void onTrigger_ResetIsoPlayerModel(String entryKey) {
        if (instance != null) {
            DebugLog.log(DebugType.General, "DebugFileWatcher Hit. Resetting player model: " + entryKey);
            instance.resetModel();
        } else {
            DebugLog.log(DebugType.General, "DebugFileWatcher Hit. Player instance null : " + entryKey);
        }
    }

    public static int getFollowDeadCount() {
        return followDeadCount;
    }

    public static void setFollowDeadCount(int aFollowDeadCount) {
        followDeadCount = aFollowDeadCount;
    }

    public static ArrayList<String> getAllFileNames() {
        ArrayList<String> names = new ArrayList<String>();
        String saveDir = ZomboidFileSystem.instance.getCurrentSaveDir();
        for (int n = 1; n < 100; ++n) {
            File file = new File(saveDir + File.separator + "map_p" + n + ".bin");
            if (!file.exists()) continue;
            names.add("map_p" + n + ".bin");
        }
        return names;
    }

    public static String getUniqueFileName() {
        int max = 0;
        String saveDir = ZomboidFileSystem.instance.getCurrentSaveDir();
        for (int n = 1; n < 100; ++n) {
            File file = new File(saveDir + File.separator + "map_p" + n + ".bin");
            if (!file.exists()) continue;
            max = n;
        }
        return ZomboidFileSystem.instance.getFileNameInCurrentSave("map_p" + ++max + ".bin");
    }

    public static ArrayList<IsoPlayer> getAllSavedPlayers() {
        ArrayList<IsoPlayer> players = GameClient.client ? ClientPlayerDB.getInstance().getAllNetworkPlayers() : PlayerDB.getInstance().getAllLocalPlayers();
        for (int i = players.size() - 1; i >= 0; --i) {
            if (!players.get(i).isDead()) continue;
            players.remove(i);
        }
        return players;
    }

    public static boolean isServerPlayerIDValid(String id) {
        if (GameClient.client) {
            String serverPlayerId = ServerOptions.instance.serverPlayerId.getValue();
            if (serverPlayerId == null || serverPlayerId.isEmpty()) {
                return true;
            }
            return serverPlayerId.equals(id);
        }
        return true;
    }

    public static int getPlayerIndex() {
        if (instance == null) {
            return assumedPlayer;
        }
        return IsoPlayer.instance.playerIndex;
    }

    public static IsoPlayer getPlayer(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= PZArrayUtil.lengthOf(players)) {
            return null;
        }
        return players[playerIndex];
    }

    public static int getPlayerIndex(IsoGameCharacter chr) {
        if (chr instanceof IsoPlayer) {
            IsoPlayer player = (IsoPlayer)chr;
            return player.getIndex();
        }
        return -1;
    }

    public static void visitAllPlayers(Consumer<IsoPlayer> visitor) {
        for (IsoPlayer player : players) {
            if (player == null) continue;
            visitor.accept(player);
        }
    }

    public static <C> IsoPlayer findPlayer(C compareParam, BiPredicate<IsoPlayer, C> predicate) {
        for (IsoPlayer player : players) {
            if (player == null || !predicate.test(player, compareParam)) continue;
            return player;
        }
        return null;
    }

    public static <C> boolean anyPlayer(C compareParam, BiPredicate<IsoPlayer, C> predicate) {
        return IsoPlayer.findPlayer(compareParam, predicate) != null;
    }

    public static <ComponentType extends ECSComponent> void visitAllPlayersWithComponent(Class<ComponentType> componentClass, BiConsumer<IsoPlayer, ComponentType> visitor) {
        for (IsoPlayer player : players) {
            ComponentType component;
            if (player == null || (component = player.tryGetECSComponent(componentClass)) == null) continue;
            visitor.accept(player, component);
        }
    }

    public final int getIndex() {
        return this.playerIndex;
    }

    @Deprecated
    @UsedFromLua
    public final int getPlayerNum() {
        return this.getIndex();
    }

    public static boolean allPlayersDead() {
        for (int n = 0; n < numPlayers; ++n) {
            if (players[n] == null || players[n].isDead()) continue;
            return false;
        }
        return IsoWorld.instance == null || IsoWorld.instance.addCoopPlayers.isEmpty();
    }

    public static ArrayList<IsoPlayer> getPlayers() {
        return new ArrayList<IsoPlayer>(Arrays.asList(players));
    }

    public static boolean allPlayersAsleep() {
        int numLiving = 0;
        int numSleeping = 0;
        for (int pn = 0; pn < numPlayers; ++pn) {
            if (players[pn] == null || players[pn].isDead()) continue;
            ++numLiving;
            if (players[pn] == null || !players[pn].isAsleep()) continue;
            ++numSleeping;
        }
        return numLiving > 0 && numLiving == numSleeping;
    }

    public static boolean getCoopPVP() {
        return coopPvp;
    }

    public static void setCoopPVP(boolean enabled) {
        coopPvp = enabled;
    }

    public void TestAnimalSpotPlayer(IsoAnimal chr) {
        float dist = IsoUtils.DistanceManhatten(chr.getX(), chr.getY(), this.getX(), this.getY());
        chr.spotted(this, false, dist);
    }

    public void TestZombieSpotPlayer(IsoMovingObject chr) {
        float dist;
        IsoZombie isoZombie;
        if (GameServer.server && chr instanceof IsoZombie && (isoZombie = (IsoZombie)chr).getTarget() != this && isoZombie.isLeadAggro(this)) {
            INetworkPacket.send(isoZombie.getOwner(), PacketTypes.PacketType.ZombieControl, isoZombie, this);
            return;
        }
        chr.spotted(this, false);
        if (chr instanceof IsoZombie && (dist = chr.DistTo(this)) < this.closestZombie && !chr.isOnFloor()) {
            this.closestZombie = dist;
        }
    }

    public float getPathSpeed() {
        float speed = this.getMoveSpeed() * 0.9f;
        switch (this.moodles.getMoodleLevel(MoodleType.ENDURANCE)) {
            case 1: {
                speed *= 0.95f;
                break;
            }
            case 2: {
                speed *= 0.9f;
                break;
            }
            case 3: {
                speed *= 0.8f;
                break;
            }
            case 4: {
                speed *= 0.6f;
            }
        }
        if (this.stats.isEnduranceRecharging()) {
            speed *= 0.85f;
        }
        if (this.getMoodles().getMoodleLevel(MoodleType.HEAVY_LOAD) > 0) {
            float weight = this.getInventory().getCapacityWeight();
            float maxWeight = this.getMaxWeight();
            float ratio = Math.min(2.0f, weight / maxWeight) - 1.0f;
            speed *= 0.65f + 0.35f * (1.0f - ratio);
        }
        return speed;
    }

    public boolean isGhostMode() {
        return this.isInvisible();
    }

    public void setGhostMode(boolean aGhostMode, boolean isForced) {
        this.setInvisible(aGhostMode, isForced);
    }

    public void setGhostMode(boolean aGhostMode) {
        this.setInvisible(aGhostMode);
    }

    public boolean isSeeEveryone() {
        return Core.debug && DebugOptions.instance.cheat.player.seeEveryone.getValue();
    }

    @Override
    public void moveUnmodded(float dirX, float dirY) {
        if (this.getSlowFactor() > 0.0f) {
            dirX *= 1.0f - this.getSlowFactor();
            dirY *= 1.0f - this.getSlowFactor();
        }
        super.moveUnmodded(dirX, dirY);
        this.setCurrentSquareFromPosition();
    }

    public void nullifyAiming() {
        if (this.isForceAim()) {
            this.toggleForceAim();
        }
        this.isCharging = false;
        this.setIsAiming(false);
    }

    private void initializeStates() {
        this.clearAIStateMap();
        if (this.getVehicle() == null) {
            this.registerAIState("actions", PlayerActionsState.instance());
            this.registerAIState("aim", PlayerAimState.instance());
            this.registerAIState("aim-sneak", PlayerAimState.instance());
            this.registerAIState("aim-strafe", PlayerStrafeState.instance());
            this.registerAIState("bumped", BumpedState.instance());
            this.registerAIState("bumped-bump", BumpedState.instance());
            this.registerAIState("climbdownrope", ClimbDownSheetRopeState.instance());
            this.registerAIState("climbfence", ClimbOverFenceState.instance());
            this.registerAIState("climbrope", ClimbSheetRopeState.instance());
            this.registerAIState("climbwall", ClimbOverWallState.instance());
            this.registerAIState("climbwindow", ClimbThroughWindowState.instance());
            this.registerAIState("closewindow", CloseWindowState.instance());
            this.registerAIState("collide", CollideWithWallState.instance());
            this.registerAIState("emote", PlayerEmoteState.instance());
            this.registerAIState("ext", PlayerExtState.instance());
            this.registerAIState("falldown", PlayerFallDownState.instance());
            this.registerAIState("falling", PlayerFallingState.instance());
            this.registerAIState("fishing", FishingState.instance());
            this.registerAIState("fitness", FitnessState.instance());
            this.registerAIState("getup", PlayerGetUpState.instance());
            this.registerAIState("hitreaction", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-bite", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-biteGeneric", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-crawlerBite", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-endDeath", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-shot", PlayerHitReactionState.instance());
            this.registerAIState("hitreaction-hit", PlayerHitReactionPVPState.instance());
            this.registerAIState("hitreactionpvp", PlayerHitReactionPVPState.instance());
            this.registerAIState("idle", IdleState.instance());
            this.registerAIState("knockeddown", PlayerKnockedDown.instance());
            this.registerAIState("melee", SwipeStatePlayer.instance());
            this.registerAIState("milkanimal", PlayerMilkAnimalState.instance());
            this.registerAIState("movement", PlayerMovementState.instance());
            this.registerAIState("onbed", PlayerOnBedState.instance());
            this.registerAIState("onground", PlayerOnGroundState.instance());
            this.registerAIState("openwindow", OpenWindowState.instance());
            this.registerAIState("petanimal", PlayerPetAnimalState.instance());
            this.registerAIState("ranged", SwipeStatePlayer.instance());
            this.registerAIState("run", PlayerMovementState.instance());
            this.registerAIState("shearanimal", PlayerShearAnimalState.instance());
            this.registerAIState("shove", SwipeStatePlayer.instance());
            this.registerAIState("shoveWithFirearm", SwipeStatePlayer.instance());
            this.registerAIState("shoveWithHandgun", SwipeStatePlayer.instance());
            this.registerAIState("shoveAim", SwipeStatePlayer.instance());
            this.registerAIState("stomp", SwipeStatePlayer.instance());
            this.registerAIState("sitext", PlayerExtState.instance());
            this.registerAIState("sitonfurniture", PlayerSitOnFurnitureState.instance());
            this.registerAIState("sitonground", PlayerSitOnGroundState.instance());
            this.registerAIState("sitonground-sitting", PlayerSitOnGroundState.instance());
            this.registerAIState("sitonground-sitting-warmhands", PlayerSitOnGroundState.instance());
            this.registerAIState("smashwindow", SmashWindowState.instance());
            this.registerAIState("sprint", PlayerMovementState.instance());
            this.registerAIState("strafe", PlayerStrafeState.instance());
            this.registerAIState("grappleGrab", SwipeStatePlayer.instance());
            this.registerAIState("pickUpBody", SwipeStatePlayer.instance());
            this.registerAIState("grabCorpseFromContainer", PlayerDraggingCorpse.instance());
            this.registerAIState("grabCorpseFromContainer-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("pickUpBody-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("pickUpBody-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("pickUpBody-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("pickUpBody-LegsEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("draggingBody-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("draggingBody-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("draggingBody-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("draggingBody-LegsEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("layDownBody-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("layDownBody-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("layDownBody-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("layDownBody-LegsEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOutWindow-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOutWindow-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOutWindow-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOutWindow-LegsEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOverFence-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOverFence-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOverFence-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyOverFence-LegsEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyIntoContainer-HeadEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyIntoContainer-HeadEnd-onFront", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyIntoContainer-LegsEnd-onBack", PlayerDraggingCorpse.instance());
            this.registerAIState("throwBodyIntoContainer-LegsEnd-onFront", PlayerDraggingCorpse.instance());
        } else {
            this.registerAIState("aim", PlayerAimState.instance());
            this.registerAIState("idle", IdleState.instance());
            this.registerAIState("melee", SwipeStatePlayer.instance());
            this.registerAIState("shove", SwipeStatePlayer.instance());
            this.registerAIState("shoveWithFirearm", SwipeStatePlayer.instance());
            this.registerAIState("shoveWithHandgun", SwipeStatePlayer.instance());
            this.registerAIState("shoveAim", SwipeStatePlayer.instance());
            this.registerAIState("stomp", SwipeStatePlayer.instance());
            this.registerAIState("ranged", SwipeStatePlayer.instance());
        }
    }

    @Override
    protected void onAnimPlayerCreated(AnimationPlayer animationPlayer) {
        super.onAnimPlayerCreated(animationPlayer);
        if (this.isAnimal()) {
            return;
        }
        animationPlayer.addBoneReparent("Bip01_L_Thigh", "Bip01");
        animationPlayer.addBoneReparent("Bip01_R_Thigh", "Bip01");
        animationPlayer.addBoneReparent("Bip01_L_Clavicle", "Bip01_Spine1");
        animationPlayer.addBoneReparent("Bip01_R_Clavicle", "Bip01_Spine1");
        animationPlayer.addBoneReparent("Bip01_Prop1", "Bip01_R_Hand");
        animationPlayer.addBoneReparent("Bip01_Prop2", "Bip01_L_Hand");
    }

    @Override
    public String GetAnimSetName() {
        return this.getVehicle() == null ? "player" : "player-vehicle";
    }

    public boolean IsInMeleeAttack() {
        return this.isCurrentState(SwipeStatePlayer.instance());
    }

    @Override
    public void load(ByteBuffer input, int worldVersion, boolean isDebugSave) throws IOException {
        byte serialise = input.get();
        byte objectID = input.get();
        super.load(input, worldVersion, isDebugSave);
        this.setHoursSurvived(input.getDouble());
        SurvivorDesc desc = this.descriptor;
        this.setFemale(desc.isFemale());
        this.InitSpriteParts(desc);
        this.speakColour = new Color(Rand.Next(135) + 120, Rand.Next(135) + 120, Rand.Next(135) + 120, 255);
        if (GameClient.client) {
            if (Core.getInstance().getMpTextColor() != null) {
                this.speakColour = new Color(Core.getInstance().getMpTextColor().r, Core.getInstance().getMpTextColor().g, Core.getInstance().getMpTextColor().b, 1.0f);
            } else {
                Core.getInstance().setMpTextColor(new ColorInfo(this.speakColour.r, this.speakColour.g, this.speakColour.b, 1.0f));
                try {
                    Core.getInstance().saveOptions();
                }
                catch (IOException e) {
                    DebugType.General.printException(e, LogSeverity.Error);
                }
            }
        }
        this.setZombieKills(input.getInt());
        ArrayList items = this.savedInventoryItems;
        int wornItemCount = input.get();
        for (int i = 0; i < wornItemCount; ++i) {
            ItemBodyLocation itemBodyLocation = ItemBodyLocation.get(ResourceLocation.of(GameWindow.ReadString(input)));
            short index = input.getShort();
            if (index < 0 || index >= items.size() || this.wornItems.getBodyLocationGroup().getLocation(itemBodyLocation) == null) continue;
            this.wornItems.setItem(itemBodyLocation, (InventoryItem)items.get(index));
        }
        this.onWornItemsChanged();
        short index = input.getShort();
        if (index >= 0 && index < items.size()) {
            this.leftHandItem = (InventoryItem)items.get(index);
        }
        if ((index = input.getShort()) >= 0 && index < items.size()) {
            this.rightHandItem = (InventoryItem)items.get(index);
        }
        this.setVariable("Weapon", WeaponType.getWeaponType(this).getType());
        this.setSurvivorKills(input.getInt());
        this.initSpritePartsEmpty();
        this.nutrition.load(input);
        this.setAllChatMuted(input.get() != 0);
        this.tagPrefix = GameWindow.ReadString(input);
        this.setTagColor(new ColorInfo(input.getFloat(), input.getFloat(), input.getFloat(), 1.0f));
        this.setDisplayName(GameWindow.ReadString(input));
        this.showTag = input.get() != 0;
        boolean bl = this.factionPvp = input.get() != 0;
        if (worldVersion >= 239) {
            this.setAutoDrink(input.get() != 0);
        }
        if (worldVersion >= 198) {
            input.get();
        } else {
            input.get();
        }
        if (input.get() != 0) {
            this.savedVehicleX = input.getFloat();
            this.savedVehicleY = input.getFloat();
            this.savedVehicleSeat = input.get();
            this.savedVehicleRunning = input.get() != 0;
            this.setZ(0.0f);
        }
        int size = input.getInt();
        for (int i = 0; i < size; ++i) {
            this.mechanicsItem.put(input.getLong(), input.getLong());
        }
        this.fitness.load(input, worldVersion);
        int nb = input.getShort();
        for (int i = 0; i < nb; ++i) {
            short registryItemId = input.getShort();
            String fullType = WorldDictionary.getItemTypeFromID(registryItemId);
            if (fullType == null) continue;
            this.alreadyReadBook.add(fullType);
        }
        this.loadKnownMediaLines(input, worldVersion);
        if (worldVersion >= 203) {
            this.setVoiceType(input.get());
        }
        if (worldVersion >= 228) {
            this.craftHistory.load(input);
        }
    }

    public void setExtraInfoFlags(byte flags, boolean isForced) {
        this.setGodMod((flags & 1) != 0, isForced);
        this.setGhostMode((flags & 2) != 0, isForced);
        this.setInvisible((flags & 4) != 0, isForced);
        this.setNoClip((flags & 8) != 0, isForced);
        this.setShowAdminTag((flags & 0x10) != 0);
        this.setCanHearAll((flags & 0x20) != 0);
    }

    public byte getExtraInfoFlags() {
        boolean showAdminTag = this.calculateShowAdminTag();
        return (byte)((this.isGodMod() ? 1 : 0) | (this.isGhostMode() ? 2 : 0) | (this.isInvisible() ? 4 : 0) | (this.isNoClip() ? 8 : 0) | (showAdminTag ? 16 : 0) | (this.canHearAll() ? 32 : 0));
    }

    public boolean calculateShowAdminTag() {
        return this.isInvisible() || this.isGodMod() || this.isGhostMode() || this.isNoClip() || this.isTimedActionInstantCheat() || this.isUnlimitedCarry() || this.isUnlimitedEndurance() || this.isBuildCheat() || this.isFarmingCheat() || this.isFishingCheat() || this.isHealthCheat() || this.isMechanicsCheat() || this.isMovablesCheat() || this.canSeeAll() || this.canHearAll() || this.isZombiesDontAttack();
    }

    @Override
    public String getDescription(String separatorStr) {
        int i;
        String s = this.getClass().getSimpleName() + " [" + separatorStr;
        s = s + super.getDescription(separatorStr + "    ") + " | " + separatorStr;
        s = s + "hoursSurvived=" + this.getHoursSurvived() + " | " + separatorStr;
        s = s + "zombieKills=" + this.getZombieKills() + " | " + separatorStr;
        s = s + "wornItems=";
        for (i = 0; i < this.getWornItems().size() - 1; ++i) {
            s = s + String.valueOf(this.getWornItems().get(i).getItem()) + ", ";
        }
        s = s + " | " + separatorStr;
        s = s + "primaryHandItem=" + String.valueOf(this.getPrimaryHandItem()) + " | " + separatorStr;
        s = s + "secondaryHandType=" + this.getSecondaryHandType() + " | " + separatorStr;
        s = s + "survivorKills=" + this.getSurvivorKills() + " | " + separatorStr;
        if (this.isAllChatMuted()) {
            s = s + "AllChatMuted | " + separatorStr;
        }
        s = s + "tag=" + this.tagPrefix + ", color=(" + this.getTagColor().r + ", " + this.getTagColor().g + ", " + this.getTagColor().b + ") | " + separatorStr;
        s = s + "displayName=" + this.displayName + " | " + separatorStr;
        if (this.showTag) {
            s = s + "showTag | " + separatorStr;
        }
        if (this.factionPvp) {
            s = s + "factionPvp | " + separatorStr;
        }
        if (this.isNoClip()) {
            s = s + "noClip | " + separatorStr;
        }
        if (this.vehicle != null) {
            s = s + "vehicle [ pos=(" + this.vehicle.getX() + ", " + this.vehicle.getY() + ") seat=" + this.vehicle.getSeat(this) + " isEngineRunning=" + this.vehicle.isEngineRunning() + " ] " + separatorStr;
        }
        s = s + "mechanicsItem=";
        for (i = 0; i < this.mechanicsItem.size() - 1; ++i) {
            s = s + String.valueOf(this.mechanicsItem.get(i)) + ", ";
        }
        s = s + " | " + separatorStr;
        s = s + "alreadyReadBook=";
        for (i = 0; i < this.alreadyReadBook.size() - 1; ++i) {
            s = s + this.alreadyReadBook.get(i) + ", ";
        }
        s = s + " ] ";
        return s;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void save(ByteBuffer output, boolean isDebugSave) throws IOException {
        IsoPlayer oldInstance = instance;
        instance = this;
        try {
            super.save(output, isDebugSave);
        }
        finally {
            instance = oldInstance;
        }
        output.putDouble(this.getHoursSurvived());
        output.putInt(this.getZombieKills());
        if (this.wornItems.size() > 127) {
            throw new RuntimeException("too many worn items");
        }
        output.put((byte)this.wornItems.size());
        this.wornItems.forEach(wornItem -> {
            GameWindow.WriteString(output, Registries.ITEM_BODY_LOCATION.getLocation(wornItem.getLocation()).toString());
            output.putShort((short)this.savedInventoryItems.indexOf(wornItem.getItem()));
        });
        output.putShort((short)this.savedInventoryItems.indexOf(this.getPrimaryHandItem()));
        output.putShort((short)this.savedInventoryItems.indexOf(this.getSecondaryHandItem()));
        output.putInt(this.getSurvivorKills());
        this.nutrition.save(output);
        output.put(this.isAllChatMuted() ? (byte)1 : 0);
        GameWindow.WriteString(output, this.tagPrefix);
        output.putFloat(this.getTagColor().r);
        output.putFloat(this.getTagColor().g);
        output.putFloat(this.getTagColor().b);
        GameWindow.WriteString(output, this.displayName);
        output.put(this.showTag ? (byte)1 : 0);
        output.put(this.factionPvp ? (byte)1 : 0);
        output.put(this.getAutoDrink() ? (byte)1 : 0);
        output.put(this.getExtraInfoFlags());
        if (this.vehicle != null) {
            output.put((byte)1);
            output.putFloat(this.vehicle.getX());
            output.putFloat(this.vehicle.getY());
            output.put((byte)this.vehicle.getSeat(this));
            output.put(this.vehicle.isEngineRunning() ? (byte)1 : 0);
        } else {
            output.put((byte)0);
        }
        output.putInt(this.mechanicsItem.size());
        for (Long itemid : this.mechanicsItem.keySet()) {
            output.putLong(itemid);
            output.putLong(this.mechanicsItem.get(itemid));
        }
        this.fitness.save(output);
        output.putShort((short)this.alreadyReadBook.size());
        for (int i = 0; i < this.alreadyReadBook.size(); ++i) {
            output.putShort(WorldDictionary.getItemRegistryID(this.alreadyReadBook.get(i)));
        }
        this.saveKnownMediaLines(output);
        output.put((byte)this.getVoiceType());
        this.craftHistory.save(output);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save() throws IOException {
        Object object = SliceY.SliceBufferLock;
        synchronized (object) {
            ByteBuffer output = SliceY.SliceBuffer;
            output.clear();
            output.put((byte)80);
            output.put((byte)76);
            output.put((byte)89);
            output.put((byte)82);
            output.putInt(249);
            GameWindow.WriteString(output, this.multiplayer ? ServerOptions.instance.serverPlayerId.getValue() : "");
            output.putInt(PZMath.fastfloor(this.getX() / 8.0f));
            output.putInt(PZMath.fastfloor(this.getY() / 8.0f));
            output.putInt(PZMath.fastfloor(this.getX()));
            output.putInt(PZMath.fastfloor(this.getY()));
            output.putInt(PZMath.fastfloor(this.getZ()));
            this.save(output);
            File file = new File(ZomboidFileSystem.instance.getFileNameInCurrentSave("map_p.bin"));
            if (!Core.getInstance().isNoSave()) {
                try (FileOutputStream fos = new FileOutputStream(file);
                     BufferedOutputStream bos = new BufferedOutputStream(fos);){
                    bos.write(output.array(), 0, output.position());
                }
            }
            if (this.getVehicle() != null && !GameClient.client) {
                VehiclesDB2.instance.updateVehicleAndTrailer(this.getVehicle());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void save(String fileName) throws IOException {
        ZomboidFileSystem.instance.validatePrefix(fileName);
        this.saveFileName = fileName;
        Object object = SliceY.SliceBufferLock;
        synchronized (object) {
            SliceY.SliceBuffer.clear();
            SliceY.SliceBuffer.putInt(249);
            GameWindow.WriteString(SliceY.SliceBuffer, this.multiplayer ? ServerOptions.instance.serverPlayerId.getValue() : "");
            this.save(SliceY.SliceBuffer);
            File outFile = new File(fileName).getAbsoluteFile();
            try (FileOutputStream fos = new FileOutputStream(outFile);
                 BufferedOutputStream bos = new BufferedOutputStream(fos);){
                bos.write(SliceY.SliceBuffer.array(), 0, SliceY.SliceBuffer.position());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load(String fileName) throws IOException {
        File inFile = new File(fileName).getAbsoluteFile();
        if (!inFile.exists()) {
            return;
        }
        this.saveFileName = fileName;
        try (FileInputStream inStream = new FileInputStream(inFile);
             BufferedInputStream input = new BufferedInputStream(inStream);){
            Object object = SliceY.SliceBufferLock;
            synchronized (object) {
                SliceY.SliceBuffer.clear();
                int numBytes = input.read(SliceY.SliceBuffer.array());
                SliceY.SliceBuffer.limit(numBytes);
                int worldVersion = SliceY.SliceBuffer.getInt();
                this.saveFileIp = GameWindow.ReadString(SliceY.SliceBuffer);
                this.load(SliceY.SliceBuffer, worldVersion);
            }
        }
    }

    @Override
    public void loadChange(IsoObjectChange change, ByteBufferReader bb) {
        super.loadChange(change, bb);
        if (change == IsoObjectChange.PLAY_GAIN_EXPERIENCE_LEVEL_SOUND) {
            this.playGainExperienceLevelSound();
        } else if (change == IsoObjectChange.COUGH) {
            this.triggerCough();
        }
    }

    @Override
    public void removeFromWorld() {
        this.getEmitter().stopOrTriggerSoundByName("BurningFlesh");
        this.getEmitter().stopSoundLocal(this.vocalEvent);
        this.vocalEvent = 0L;
        this.removedFromWorldMs = System.currentTimeMillis();
        if (!(this instanceof IsoAnimal) && !RecentlyRemoved.contains(this)) {
            RecentlyRemoved.add(this);
        }
        super.removeFromWorld();
    }

    public static void UpdateRemovedEmitters() {
        IsoCell cell = IsoWorld.instance.currentCell;
        long currentMS = System.currentTimeMillis();
        for (int i = RecentlyRemoved.size() - 1; i >= 0; --i) {
            IsoPlayer player = RecentlyRemoved.get(i);
            if ((cell.getObjectList().contains(player) || cell.getAddList().contains(player)) && !cell.getRemoveList().contains(player)) {
                RecentlyRemoved.remove(i);
                continue;
            }
            player.getFMODParameters().update();
            player.getEmitter().tick();
            if (currentMS - player.removedFromWorldMs <= 5000L) continue;
            player.getEmitter().stopAll();
            RecentlyRemoved.remove(i);
        }
    }

    public static void Reset() {
        for (int i = 0; i < 4; ++i) {
            IsoPlayer player = players[i];
            if (player != null) {
                player.getAdvancedAnimator().reset();
                player.releaseAnimationPlayer();
                player.getSpottedList().clear();
                player.lastSpotted.clear();
            }
            IsoPlayer.players[i] = null;
        }
        RecentlyRemoved.clear();
    }

    public void setVehicle4TestCollision(BaseVehicle vehicle) {
        this.vehicle4testCollision = vehicle;
    }

    public boolean isSaveFileInUse() {
        for (int pn = 0; pn < numPlayers; ++pn) {
            IsoPlayer player2 = players[pn];
            if (player2 == null) continue;
            if (this.sqlId != -1 && this.sqlId == player2.sqlId) {
                return true;
            }
            if (this.saveFileName == null || !this.saveFileName.equals(player2.saveFileName)) continue;
            return true;
        }
        return false;
    }

    public void removeSaveFile() {
        try {
            File file;
            if (PlayerDB.isAvailable()) {
                PlayerDB.getInstance().saveLocalPlayersForce();
            }
            if (this.isNpc() && this.saveFileName != null && (file = new File(this.saveFileName).getAbsoluteFile()).exists()) {
                file.delete();
            }
        }
        catch (Exception ex) {
            ExceptionLogger.logException(ex);
        }
    }

    public boolean isSaveFileIPValid() {
        return IsoPlayer.isServerPlayerIDValid(this.saveFileIp);
    }

    @Override
    public String getObjectName() {
        return "Player";
    }

    public float getScreenChestHeight() {
        return 20.0f * this.def.getScaleY() / Core.getInstance().getZoom(this.getIndex());
    }

    public Vector2 getAimVector(Vector2 vec) {
        if (!this.isAnyAimKeyDown()) {
            return vec.set(0.0f, 0.0f);
        }
        return this.calculateAimVector(vec);
    }

    private Vector2 calculateAimVector(Vector2 vec) {
        int playerIndex = this.getIndex();
        int mx = AimingReticle.getX(playerIndex);
        int my = AimingReticle.getY(playerIndex);
        vec.x = IsoUtils.XToIso(playerIndex, mx, my, this.getAimOriginPosZ()) - this.getAimOriginPosX();
        vec.y = IsoUtils.YToIso(playerIndex, mx, my, this.getAimOriginPosZ()) - this.getAimOriginPosY();
        vec.normalize();
        return vec;
    }

    @Override
    public float getGlobalMovementMod(boolean bDoNoises) {
        if (this.isGhostMode() || this.isNoClip()) {
            return 1.0f;
        }
        return super.getGlobalMovementMod(bDoNoises);
    }

    @Override
    protected void doTreeNoises() {
        if (this instanceof IsoAnimal) {
            super.doTreeNoises();
        }
    }

    @Override
    public boolean isInTrees2(boolean ignoreBush) {
        if (this.isGhostMode() || this.isNoClip()) {
            return false;
        }
        return super.isInTrees2(ignoreBush);
    }

    public float getMoveSpeed() {
        float minDelta = 1.0f;
        for (int i = BodyPartType.ToIndex(BodyPartType.UpperLeg_L); i <= BodyPartType.ToIndex(BodyPartType.Foot_R); ++i) {
            BodyPart part = this.getBodyDamage().getBodyPart(BodyPartType.FromIndex(i));
            float delta = 1.0f;
            if (part.getFractureTime() > 20.0f) {
                delta = 0.4f;
                if (part.getFractureTime() > 50.0f) {
                    delta = 0.3f;
                }
                if (part.getSplintFactor() > 0.0f) {
                    delta += part.getSplintFactor() / 10.0f;
                }
            }
            if (part.getFractureTime() < 20.0f && part.getSplintFactor() > 0.0f) {
                delta = 0.8f;
            }
            if (delta > 0.7f && part.getDeepWoundTime() > 0.0f) {
                delta = 0.7f;
                if (part.bandaged()) {
                    delta += 0.2f;
                }
            }
            if (!(delta < minDelta)) continue;
            minDelta = delta;
        }
        if (minDelta != 1.0f) {
            return this.moveSpeed * minDelta;
        }
        if (this.getMoodles().getMoodleLevel(MoodleType.PANIC) >= 4 && this.characterTraits.get(CharacterTrait.ADRENALINE_JUNKIE)) {
            float del = 1.0f;
            int mul = this.getMoodles().getMoodleLevel(MoodleType.PANIC) + 1;
            return this.moveSpeed * (del += (float)mul / 50.0f);
        }
        return this.moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    @Override
    public float getTorchStrength() {
        if (this.isAnimal()) {
            return 0.0f;
        }
        if (this.remote) {
            return this.mpTorchStrength;
        }
        InventoryItem item = this.getActiveLightItem();
        if (item != null) {
            return item.getLightStrength();
        }
        return 0.0f;
    }

    public float getInvAimingMod() {
        int level = this.getPerkLevel(PerkFactory.Perks.Aiming);
        if (level == 1) {
            return 0.9f;
        }
        if (level == 2) {
            return 0.86f;
        }
        if (level == 3) {
            return 0.82f;
        }
        if (level == 4) {
            return 0.74f;
        }
        if (level == 5) {
            return 0.7f;
        }
        if (level == 6) {
            return 0.66f;
        }
        if (level == 7) {
            return 0.62f;
        }
        if (level == 8) {
            return 0.58f;
        }
        if (level == 9) {
            return 0.54f;
        }
        if (level == 10) {
            return 0.5f;
        }
        return 0.9f;
    }

    public float getAimingMod() {
        int level = this.getPerkLevel(PerkFactory.Perks.Aiming);
        if (level == 1) {
            return 1.1f;
        }
        if (level == 2) {
            return 1.14f;
        }
        if (level == 3) {
            return 1.18f;
        }
        if (level == 4) {
            return 1.22f;
        }
        if (level == 5) {
            return 1.26f;
        }
        if (level == 6) {
            return 1.3f;
        }
        if (level == 7) {
            return 1.34f;
        }
        if (level == 8) {
            return 1.36f;
        }
        if (level == 9) {
            return 1.4f;
        }
        if (level == 10) {
            return 1.5f;
        }
        return 1.0f;
    }

    public float getReloadingMod() {
        int level = this.getPerkLevel(PerkFactory.Perks.Reloading);
        return 3.5f - (float)level * 0.25f;
    }

    public float getAimingRangeMod() {
        int level = this.getPerkLevel(PerkFactory.Perks.Aiming);
        if (level == 1) {
            return 1.2f;
        }
        if (level == 2) {
            return 1.28f;
        }
        if (level == 3) {
            return 1.36f;
        }
        if (level == 4) {
            return 1.42f;
        }
        if (level == 5) {
            return 1.5f;
        }
        if (level == 6) {
            return 1.58f;
        }
        if (level == 7) {
            return 1.66f;
        }
        if (level == 8) {
            return 1.72f;
        }
        if (level == 9) {
            return 1.8f;
        }
        if (level == 10) {
            return 2.0f;
        }
        return 1.1f;
    }

    public boolean isPathfindRunning() {
        return this.pathfindRun;
    }

    public void setPathfindRunning(boolean newvalue) {
        this.pathfindRun = newvalue;
    }

    public boolean isBannedAttacking() {
        return this.bannedAttacking;
    }

    public void setBannedAttacking(boolean b) {
        this.bannedAttacking = b;
    }

    public float getInvAimingRangeMod() {
        int level = this.getPerkLevel(PerkFactory.Perks.Aiming);
        if (level == 1) {
            return 0.8f;
        }
        if (level == 2) {
            return 0.7f;
        }
        if (level == 3) {
            return 0.62f;
        }
        if (level == 4) {
            return 0.56f;
        }
        if (level == 5) {
            return 0.45f;
        }
        if (level == 6) {
            return 0.38f;
        }
        if (level == 7) {
            return 0.31f;
        }
        if (level == 8) {
            return 0.24f;
        }
        if (level == 9) {
            return 0.17f;
        }
        if (level == 10) {
            return 0.1f;
        }
        return 0.8f;
    }

    private void updateCursorVisibility() {
        if (this.playerIndex != 0) {
            return;
        }
        if (!this.isAiming()) {
            return;
        }
        if (this.isDead()) {
            return;
        }
        if (Core.getInstance().getOptionShowCursorWhileAiming()) {
            return;
        }
        if (Core.getInstance().getIsoCursorVisibility() == 0) {
            return;
        }
        if (UIManager.isForceCursorVisible()) {
            return;
        }
        Mouse.setCursorVisible(false);
    }

    private void renderAttachedAnimalRopes() {
        if (!this.attachedAnimals.isEmpty()) {
            for (int i = 0; i < this.attachedAnimals.size(); ++i) {
                this.attachedAnimals.get(i).drawRope(this);
            }
        }
    }

    @Override
    public void render(float x, float y, float z, ColorInfo col, boolean bDoChild, boolean bWallLightingPass, Shader shader) {
        if (!Core.getInstance().isDisplayPlayerModel() && !this.isAnimal() && this.isLocal()) {
            return;
        }
        this.renderAttachedAnimalRopes();
        if (DebugOptions.instance.character.debug.render.displayRoomAndZombiesZone.getValue()) {
            Zone zombieZone;
            Object txt = "";
            if (this.getCurrentRoomDef() != null) {
                txt = this.getCurrentRoomDef().name;
            }
            if ((zombieZone = ZombiesZoneDefinition.getDefinitionZoneAt(PZMath.fastfloor(x), PZMath.fastfloor(y), PZMath.fastfloor(z))) != null) {
                txt = (String)txt + " - " + zombieZone.name + " / " + zombieZone.type;
            }
            this.Say((String)txt);
        }
        if (GameClient.client && !(this instanceof IsoAnimal)) {
            if (!IsoPlayer.getInstance().checkCanSeeClient(this)) {
                this.setTargetAlpha(0.0f);
                IsoPlayer.getInstance().spottedPlayerTimer.remove(this.getRemoteID());
            } else {
                this.setTargetAlpha(1.0f);
            }
        }
        super.render(x, y, z, col, bDoChild, bWallLightingPass, shader);
    }

    @Override
    public void renderlast() {
        super.renderlast();
        if (DebugOptions.instance.character.debug.render.fmodRoomType.getValue() && this.isLocalPlayer()) {
            ParameterRoomTypeEx.render(this);
        }
    }

    @Override
    protected void postHitByVehicleUpdateStance(float speed, boolean knockDownAllowed) {
        if (!this.isAlive()) {
            return;
        }
        boolean wasKnockedDown = this.isKnockedDown();
        boolean newKnockDownAllowed = knockDownAllowed && ServerOptions.getInstance().knockedDownAllowed.getValue();
        super.postHitByVehicleUpdateStance(speed, newKnockDownAllowed);
        if (!this.hasHitReaction()) {
            this.setHitReaction("HitReaction");
        }
        if (this.isKnockedDown() && !wasKnockedDown) {
            this.setReanimateTimer(20.0f);
        }
        this.getActionContext().reportEvent("washit");
    }

    @Override
    public void setIgnoreMovement(boolean ignoreMovement) {
        if (ignoreMovement) {
            this.getNetworkCharacterAI().needToUpdate();
        }
        super.setIgnoreMovement(ignoreMovement);
    }

    @Override
    public float onHitByVehicleApplyDamage(BaseVehicle vehicle, float impactSpeed) {
        if (this.isGodMod()) {
            return 0.0f;
        }
        if (!CombatManager.checkPVP(vehicle, this, false)) {
            return 0.0f;
        }
        if (SandboxOptions.instance.damageToPlayerFromHitByACar.getValue() < 1) {
            return 0.0f;
        }
        float damage = super.onHitByVehicleApplyDamage(vehicle, impactSpeed);
        if (damage > 0.0f) {
            LuaEventManager.triggerEvent("OnPlayerGetDamage", this, "CARHITDAMAGE", Float.valueOf(damage));
        }
        return damage;
    }

    @Override
    public void applyDamageFromVehicleHit(BaseVehicle vehicle, float vehicleSpeed, float damage) {
        if (!GameServer.server && !CombatManager.checkPVP(vehicle, this, false)) {
            return;
        }
        float damageToPlayer = damage * SandboxOptions.instance.damageToPlayerFromHitByACar.getEnumValue().multiplier;
        if (damageToPlayer <= 0.0f) {
            return;
        }
        int damagedBodyPartNum = (int)(2.0f + damageToPlayer * 0.07f);
        for (int j = 0; j < damagedBodyPartNum; ++j) {
            int bodyPartIdx = Rand.Next(BodyPartType.ToIndex(BodyPartType.Hand_L), BodyPartType.ToIndex(BodyPartType.MAX));
            BodyPart bodyPart = this.getBodyDamage().getBodyPart(BodyPartType.FromIndex(bodyPartIdx));
            float realDamage = Math.max(Rand.Next(damageToPlayer - 15.0f, damageToPlayer), 5.0f);
            if (this.characterTraits.get(CharacterTrait.FAST_HEALER)) {
                realDamage *= 0.8f;
            } else if (this.characterTraits.get(CharacterTrait.SLOW_HEALER)) {
                realDamage *= 1.2f;
            }
            realDamage *= SandboxOptions.instance.injurySeverity.getEnumValue().multiplier;
            bodyPart.AddDamage(realDamage *= 0.9f);
            if (realDamage > 40.0f && Rand.Next(12) == 0) {
                bodyPart.generateDeepWound();
            }
            if (realDamage > 10.0f && Rand.Next(100) <= 10 && SandboxOptions.instance.boneFracture.getValue()) {
                bodyPart.generateFracture(VehicleHitDamageConstants.generateFractureTime(realDamage));
            }
            if (realDamage > 30.0f && Rand.Next(100) <= 80 && SandboxOptions.instance.boneFracture.getValue() && bodyPartIdx == BodyPartType.ToIndex(BodyPartType.Head)) {
                bodyPart.generateFracture(VehicleHitDamageConstants.generateFractureTime(realDamage));
            }
            if (!(realDamage > 10.0f) || Rand.Next(100) > 60 || !SandboxOptions.instance.boneFracture.getValue() || bodyPartIdx <= BodyPartType.ToIndex(BodyPartType.Groin)) continue;
            bodyPart.generateFracture(VehicleHitDamageConstants.generateFractureTimeGroin(realDamage));
        }
        this.getBodyDamage().Update();
        this.addBloodFromVehicleImpact(vehicleSpeed);
        if (GameServer.server) {
            this.getNetworkCharacterAI().syncDamage();
        }
    }

    @Override
    public void update() {
        try (AbstractPerformanceProfileProbe abstractPerformanceProfileProbe = s_performance.update.profile();){
            this.updateInternal1();
        }
    }

    private void updateInternal1() {
        if (this.isAnimal()) {
            super.update();
            AnimalInstanceManager.getInstance().update((IsoAnimal)this);
            if (GameClient.client) {
                this.remote = true;
                this.updateRemotePlayer();
                this.setVariable("bMoving", this.isPlayerMoving());
            }
            return;
        }
        boolean updateBaseAndSend = this.updateInternal2();
        if (updateBaseAndSend) {
            if (!this.remote) {
                this.updateLOS();
            }
            super.update();
        }
        GameClient.instance.sendPlayer2(this);
    }

    private void setBeenMovingSprinting() {
        if (this.isJustMoved()) {
            this.setBeenMovingFor(this.getBeenMovingFor() + 1.25f * GameTime.getInstance().getMultiplier());
        } else {
            this.setBeenMovingFor(this.getBeenMovingFor() - 0.625f * GameTime.getInstance().getMultiplier());
        }
        if (this.isJustMoved() && this.isSprinting()) {
            this.setBeenSprintingFor(this.getBeenSprintingFor() + 1.25f * GameTime.getInstance().getMultiplier());
        } else {
            this.setBeenSprintingFor(0.0f);
        }
    }

    private boolean updateInternal2() {
        AnimationPlayer animPlayer;
        if (Core.debug && GameKeyboard.isKeyPressed(28)) {
            FBORenderChunkManager.instance.clearCache();
        }
        if (isTestAIMode) {
            this.setNpc(true);
        }
        if (!GameServer.server && !this.isNpc()) {
            if ((this.falling || this.fallTime > 0.0f || this.isOnFire()) && UIManager.speedControls.getCurrentGameSpeed() > 1) {
                UIManager.speedControls.SetCurrentGameSpeed(1);
            }
            if ((this.falling || this.fallTime > FallingConstants.isFallingThreshold) && this.isGrappling()) {
                this.setDoGrapple(false);
                this.LetGoOfGrappled("Aborted");
            }
        }
        if (!this.attackStarted) {
            this.setInitiateAttack(false);
            this.setAttackType(AttackType.NONE);
        }
        this.runningTime = (this.isRunning() || this.isSprinting()) && this.getDeferredMovement(tempo).getLengthSquared() > 0.0f ? (this.runningTime += GameTime.getInstance().getThirtyFPSMultiplier()) : 0.0f;
        if (this.getLastCollideTime() > 0.0f) {
            this.setLastCollideTime(this.getLastCollideTime() - GameTime.getInstance().getThirtyFPSMultiplier());
        }
        this.updateDeathDragDown();
        this.updateGodModeKey();
        PZOptional.ifPresent(this.tryGetECSComponent(NetworkComponent.class), NetworkComponent::updateNetworkAI);
        this.doDeferredMovement();
        if (!this.isLocal()) {
            this.setVehicleCollision(this.vehicle4testCollision != null && this.vehicle4testCollision.updateNetworkHitByVehicle(this));
        } else {
            this.setVehicleCollision(this.vehicle4testCollision != null && this.testCollideWithVehicles(this.vehicle4testCollision, null));
        }
        this.updateEquippedItemSounds();
        if (!GameServer.server) {
            this.updateEmitter();
        }
        if (!this.isAnimal()) {
            this.updateMechanicsItems();
            this.updateHeavyBreathing();
            this.updateTemperatureCheck();
            if (this.isWeaponReady()) {
                this.updateAimingStance();
            }
            if (this.isLocal()) {
                IsoReticle.trySetTargetReticleMode(this.getIndex(), this.isPrecisionAimKeyDown() && this.isAimingFirearmEquipped() ? IsoReticleMode.RETICLE : IsoReticleMode.CURSOR);
            }
            if (SystemDisabler.doCharacterStats) {
                this.nutrition.update();
            }
            this.fitness.update();
            this.updateVisionEffectTargets();
        }
        this.updateSoundListener();
        SafetySystemManager.update(this);
        if (!GameClient.client && !GameServer.server && this.deathFinished) {
            return false;
        }
        if (!GameClient.client && this.getCurrentBuildingDef() != null && !this.isInvisible()) {
            this.getCurrentBuildingDef().setHasBeenVisited(true);
        }
        if (this.checkSafehouse > 0 && GameServer.server) {
            --this.checkSafehouse;
            if (this.checkSafehouse == 0) {
                this.checkSafehouse = 200;
                SafeHouse safe = SafeHouse.isSafeHouse(this.getCurrentSquare(), null, false);
                if (safe != null) {
                    if (safe.playerAllowed(this.getUsername())) {
                        safe.setLastVisited(System.currentTimeMillis());
                    }
                    safe.checkTrespass(this);
                    this.updateDisguisedState();
                }
            }
        }
        if (this.remote && this.getTimeSinceLastNetData() > 600) {
            IsoWorld.instance.currentCell.getObjectList().remove(this);
            if (this.movingSq != null) {
                this.movingSq.getMovingObjects().remove(this);
            }
        }
        this.setTimeSinceLastNetData(this.getTimeSinceLastNetData() + (int)GameTime.instance.getMultiplier());
        this.timeSinceOpenDoor += GameTime.instance.getMultiplier();
        this.timeSinceCloseDoor += GameTime.instance.getMultiplier();
        this.lastTargeted += GameTime.instance.getMultiplier();
        this.targetedByZombie = false;
        this.checkActionGroup();
        if (this.isJustMoved() && !this.isNpc() && !this.hasPath() && !this.isCurrentActionPathfinding()) {
            if (!GameServer.server && UIManager.getSpeedControls().getCurrentGameSpeed() > 1) {
                UIManager.getSpeedControls().SetCurrentGameSpeed(1);
            }
        } else if (!GameClient.client && this.stats.get(CharacterStat.ENDURANCE) < this.stats.getEnduranceDangerWarning() && Rand.Next((int)(300.0f * GameTime.instance.getInvMultiplier())) == 0) {
            if (GameServer.server) {
                GameServer.addXp(this, PerkFactory.Perks.Fitness, 1.0f);
            } else {
                this.xp.AddXP(PerkFactory.Perks.Fitness, 1.0f);
            }
        }
        float delta = 1.0f;
        float d = 0.0f;
        if (this.isJustMoved() && !this.isNpc()) {
            d = this.isRunning() || this.isSprinting() ? 1.5f : 1.0f;
            if (!GameClient.client && !GameServer.server) {
                LuaEventManager.triggerEvent("OnPlayerMove", this);
            }
        }
        if (this.updateRemotePlayer()) {
            if (this.updateWhileDead()) {
                return true;
            }
            this.updateAttackLoopSound();
            this.updateBringToBearSound();
            this.updateHeartSound();
            this.updateDraggingCorpseSounds();
            this.checkIsNearWall();
            this.checkIsNearVehicle();
            this.updateExt();
            this.setBeenMovingSprinting();
            this.updateAimingDelay();
            if (GameServer.server) {
                this.updateMovementRates();
            }
            if (this.getVehicle() != null) {
                this.updateEnduranceWhileInVehicle();
            } else {
                this.updateEndurance();
            }
            return true;
        }
        assert (!GameServer.server);
        assert (!this.remote);
        assert (!GameClient.client || this.isLocalPlayer());
        IsoCamera.setCameraCharacter(this);
        instance = this;
        if (this.isLocalPlayer()) {
            IsoCamera.cameras[this.playerIndex].update();
            if (UIManager.getMoodleUI(this.playerIndex) != null) {
                UIManager.getMoodleUI(this.playerIndex).setCharacter(this);
            }
        }
        if (this.closestZombie > 1.2f) {
            this.slowTimer = -1.0f;
            this.slowFactor = 0.0f;
        }
        this.contextPanic -= 1.5f * GameTime.instance.getTimeDelta();
        if (this.contextPanic < 0.0f) {
            this.contextPanic = 0.0f;
        }
        this.lastSeenZombieTime += (double)(GameTime.instance.getGameWorldSecondsSinceLastUpdate() / 60.0f / 60.0f);
        LuaEventManager.triggerEvent("OnPlayerUpdate", this);
        if (this.pressedMovement(false)) {
            this.contextPanic = 0.0f;
            this.ticksSincePressedMovement = 0.0f;
        } else {
            this.ticksSincePressedMovement += GameTime.getInstance().getThirtyFPSMultiplier();
        }
        this.setVariable("pressedMovement", this.pressedMovement(true));
        if (this.updateWhileDead()) {
            return true;
        }
        if (this.isAnimationRecorderActive()) {
            this.visitAllComponents(IAnimationVariableLogger.class, IAnimationVariableLogger::logVariablesToRecording, this.getAnimationRecorder());
        }
        this.updateAttackLoopSound();
        this.updateBringToBearSound();
        this.updateHeartSound();
        this.updateDraggingCorpseSounds();
        this.updateVocalProperties();
        this.updateEquippedBaggageContainer();
        this.updateSneakKey();
        this.checkIsNearWall();
        this.checkIsNearVehicle();
        this.updateExt();
        this.updateInteractKeyPanic();
        this.updateMusicIntensityEvents();
        this.updateMusicThreatStatuses();
        if (this.isAsleep()) {
            this.isPlayerMoving = false;
        }
        if (this.getIgnoreMovement() || this.isAsleep()) {
            this.clearUseKeyVariables();
            return true;
        }
        if (this.checkActionsBlockingMovement()) {
            if (this.getVehicle() != null && this.getVehicle().getDriver() == this && this.getVehicle().getController() != null) {
                this.getVehicle().getController().clientControls.reset();
                this.getVehicle().updatePhysics();
            }
            this.clearUseKeyVariables();
            return true;
        }
        this.enterExitVehicle();
        this.checkActionGroup();
        this.checkReloading();
        this.checkWalkTo();
        if (this.checkActionsBlockingMovement()) {
            this.clearUseKeyVariables();
            return true;
        }
        if (this.getVehicle() != null) {
            this.updateWhileInVehicle();
            return true;
        }
        this.checkVehicleContainers();
        this.setCollidable(true);
        this.updateCursorVisibility();
        this.seenThisFrame = false;
        this.couldBeSeenThisFrame = false;
        if (IsoCamera.getCameraCharacter() == null && GameClient.client) {
            IsoCamera.setCameraCharacter(instance);
        }
        if (this.updateUseKey()) {
            return true;
        }
        this.updateEnableModelsKey();
        this.updateChangeCharacterKey();
        boolean isAttacking = false;
        boolean bMelee = false;
        boolean bGrapple = false;
        this.setRunning(false);
        this.setSprinting(false);
        this.useChargeTime = this.chargeTime;
        if (!this.isBlockMovement() && !this.isNpc()) {
            this.chargeTime = this.isCharging || this.isChargingLt ? (this.chargeTime += 1.0f * GameTime.instance.getMultiplier()) : 0.0f;
            this.UpdateInputState(this.inputState);
            bMelee = this.inputState.melee;
            bGrapple = this.inputState.grapple;
            isAttacking = this.inputState.isAttacking;
            this.setRunning(this.isAllowRun() && this.inputState.isRunning());
            this.setSprinting(this.inputState.sprinting);
            if (this.isSprinting() && !this.isJustMoved()) {
                this.setSprinting(false);
            }
            if (this.isSprinting()) {
                this.setRunning(false);
            }
            if (this.inputState.sprinting && !this.isSprinting()) {
                this.setRunning(true);
            }
            if (this.getWrappedGrappleable().isPickingUpBody() || this.getWrappedGrappleable().isPuttingDownBody()) {
                this.inputState.isAiming = false;
            }
            this.setIsAiming(this.inputState.isAiming);
            this.isCharging = this.inputState.isCharging;
            this.isChargingLt = this.inputState.isChargingLt;
            this.updateMovementRates();
            if (this.isAiming()) {
                this.StopAllActionQueueAiming();
            }
            if (isAttacking) {
                this.setIsAiming(true);
            }
            this.waiting = false;
            if (this.isAiming()) {
                this.setMoving(false);
                this.setRunning(false);
                this.setSprinting(false);
            }
            if (this.isFarming()) {
                this.setIsAiming(false);
            }
            ++this.ticksSinceSeenZombie;
        }
        if (this.playerMoveDir.x == 0.0f && this.playerMoveDir.y == 0.0f) {
            this.setForceRun(false);
            this.setForceSprint(false);
        }
        this.movementLastFrame.x = this.playerMoveDir.x;
        this.movementLastFrame.y = this.playerMoveDir.y;
        if (this.getStateMachine().getCurrent() == StaggerBackState.instance() || this.getStateMachine().getCurrent() == FakeDeadZombieState.instance() || UIManager.speedControls == null) {
            return true;
        }
        if (this.isF12KeyDown() && Translator.debug) {
            Translator.loadFiles();
        }
        this.setJustMoved(false);
        MoveVars moveVars = s_moveVars;
        this.updateMovementFromInput(moveVars);
        if (!this.justMoved && this.hasPath() && this.getPathFindBehavior2().shouldBeMoving()) {
            this.setJustMoved(true);
        }
        float strafeX = moveVars.strafeX;
        float strafeY = moveVars.strafeY;
        this.updateAimingDelay();
        this.setBeenMovingSprinting();
        delta *= d;
        if (delta > 1.0f) {
            delta *= this.getSprintMod();
        }
        if (delta > 1.0f && this.characterTraits.get(CharacterTrait.ATHLETIC)) {
            delta *= 1.2f;
        }
        if (delta > 1.0f) {
            if (this.characterTraits.get(CharacterTrait.OVERWEIGHT)) {
                delta *= 0.99f;
            }
            if (this.characterTraits.get(CharacterTrait.OBESE)) {
                delta *= 0.85f;
            }
            if (this.getNutrition().getWeight() > 120.0) {
                delta *= 0.97f;
            }
            if (this.characterTraits.get(CharacterTrait.OUT_OF_SHAPE)) {
                delta *= 0.99f;
            }
            if (this.characterTraits.get(CharacterTrait.UNFIT)) {
                delta *= 0.8f;
            }
        }
        this.updateEndurance();
        if (this.isAiming() && this.isJustMoved()) {
            delta *= 0.7f;
        }
        if (this.isAiming()) {
            delta *= this.getNimbleMod();
        }
        boolean bl = this.isWalking = delta > 0.0f && !this.isNpc();
        if (this.isJustMoved()) {
            this.sprite.animate = true;
        }
        if (this.isNpc()) {
            bMelee = this.getECSComponent(AIComponent.class).doUpdatePlayerControls(this);
        }
        this.meleePressed = bMelee;
        this.grapplePressed = bGrapple;
        if (!this.grapplePressed) {
            this.setDoGrapple(false);
            this.canLetGoOfGrappled = true;
        }
        if (!this.pressContext && this.isDoGrappleLetGoAfterContextKeyIsReleased()) {
            this.setDoGrapple(false);
            this.setDoGrappleLetGo();
        }
        if (bMelee) {
            if (!this.lastAttackWasHandToHand) {
                this.setMeleeDelay(Math.min(this.getMeleeDelay(), 2.0f));
            }
            if (!this.bannedAttacking && this.isAuthorizedHandToHand() && !this.isPerformingAttackAnimation() && this.getMeleeDelay() <= 0.0f) {
                this.setDoShove(true);
                this.setDoGrapple(false);
                if (!this.isCharging && !this.isChargingLt) {
                    this.setIsAiming(false);
                }
                this.AttemptAttack(this.useChargeTime);
                this.useChargeTime = 0.0f;
                this.chargeTime = 0.0f;
            }
        } else if (bGrapple) {
            if (!this.lastAttackWasHandToHand) {
                this.setMeleeDelay(Math.min(this.getMeleeDelay(), 2.0f));
            }
            if (!this.bannedAttacking && this.isAuthorizedHandToHand() && this.CanAttack() && this.getMeleeDelay() <= 0.0f) {
                if (!this.isGrappling()) {
                    this.canLetGoOfGrappled = false;
                    this.setDoGrapple(true);
                    if (!this.isCharging && !this.isChargingLt) {
                        this.setIsAiming(false);
                    }
                } else if (this.canLetGoOfGrappled) {
                    if (this.pressContext) {
                        this.setDoGrappleLetGoAfterContextKeyIsReleased(true);
                    } else {
                        this.setDoGrapple(false);
                        this.setDoGrappleLetGo();
                    }
                }
                this.useChargeTime = 0.0f;
                this.chargeTime = 0.0f;
            }
        } else if (this.isAiming() && this.CanAttack()) {
            if (this.dragCharacter != null) {
                this.dragObject = null;
                this.dragCharacter.dragging = false;
                this.dragCharacter = null;
            }
            if (isAttacking && !this.bannedAttacking && !this.isCurrentState(PlayerHitReactionState.instance()) && !this.isCurrentState(PlayerHitReactionPVPState.instance())) {
                this.sprite.animate = true;
                if (this.getRecoilDelay() <= 0.0f && this.getMeleeDelay() <= 0.0f) {
                    this.AttemptAttack(this.useChargeTime);
                }
                this.useChargeTime = 0.0f;
                this.chargeTime = 0.0f;
            }
        } else if (this.isHeadLookAround()) {
            int headIndex = this.getAnimationPlayer().getSkinningData().boneIndices.get("Bip01_Head");
            TwistableBoneTransform headBone = this.getAnimationPlayer().getBone(headIndex);
            float currentHeadLookHorizontal = this.getHeadLookHorizontal();
            float lerpDelta = GameTime.getInstance().getMultiplier() / 2.0f;
            Vector2 desiredForward2f = tempo;
            desiredForward2f.set(0.0f, 0.0f);
            if (this.isAnyAimKeyDown()) {
                float sourceX = this.getX();
                float sourceY = this.getY();
                float sourceZ = this.getZ();
                int mx = AimingReticle.getX(this.getIndex());
                int my = AimingReticle.getY(this.getIndex());
                headBone.getPosition(templwjglVector3f);
                float posZ = IsoPlayer.templwjglVector3f.z;
                float raycastZ = sourceZ + posZ;
                float raycastX = IsoUtils.XToIso(mx, my, raycastZ);
                float raycastY = IsoUtils.YToIso(mx, my, raycastZ);
                desiredForward2f.set(raycastX - sourceX, raycastY - sourceY);
            } else {
                float headAngleNormalizedX = PZMath.lerp(currentHeadLookHorizontal, 0.0f, lerpDelta);
                this.setHeadLookAroundDirection(headAngleNormalizedX, 0.0f);
            }
            if (desiredForward2f.getLengthSquared() > 0.0f) {
                desiredForward2f.normalize();
                Vector2 forwardDirection = this.getForwardDirection(tempVector2);
                float headAngle = PZMath.angleBetween(forwardDirection, desiredForward2f);
                headAngle = PZMath.clamp(headAngle, -this.getHeadLookAngleMax(), this.getHeadLookAngleMax());
                float headAngleNormalizedX = PZMath.lerp(currentHeadLookHorizontal, headAngle / this.getHeadLookAngleMax(), lerpDelta);
                Quaternion rotation = HelperFunctions.allocQuaternion();
                headBone.getRotation(rotation);
                HelperFunctions.ToEulerAngles(rotation, templwjglVector3f);
                this.setHeadLookAroundDirection(headAngleNormalizedX, 0.0f);
                HelperFunctions.releaseQuaternion(rotation);
            }
        }
        if (this.isAiming() && !this.isNpc() && !this.isCurrentState(FishingState.instance()) && !this.isFarming()) {
            Vector2 vec = this.getAimVector(tempVector2);
            if (vec.getLengthSquared() > 0.0f) {
                this.setForwardDirection(vec);
            }
            moveVars.newFacing = this.getForwardIsoDirection();
        }
        if (this.lastAngle.x != this.getForwardDirectionX() || this.lastAngle.y != this.getForwardDirectionY()) {
            this.lastAngle.x = this.getForwardDirectionX();
            this.lastAngle.y = this.getForwardDirectionY();
            this.dirtyRecalcGridStackTime = 2.0f;
        }
        if (!((animPlayer = this.getAnimationPlayer()) != null && animPlayer.isReady() || this.falling || this.isAiming() || isAttacking)) {
            this.setForwardIsoDirection(moveVars.newFacing);
        }
        if (this.isAiming() && (GameWindow.activatedJoyPad == null || this.getJoypadBind() == -1)) {
            this.playerMoveDir.x = moveVars.moveX;
            this.playerMoveDir.y = moveVars.moveY;
        }
        if (!this.isAiming() && this.isJustMoved()) {
            this.playerMoveDir.x = this.getForwardDirectionX();
            this.playerMoveDir.y = this.getForwardDirectionY();
        }
        this.currentSpeed = this.isJustMoved() ? (this.isSprinting() ? 1.5f : (this.isRunning() ? 1.0f : 0.5f)) : 0.0f;
        boolean overrideAnimation = this.IsInMeleeAttack();
        if (!this.characterActions.isEmpty()) {
            BaseAction action = (BaseAction)this.characterActions.get(0);
            if (action.overrideAnimation) {
                overrideAnimation = true;
            }
        }
        if (!overrideAnimation && !this.isForceOverrideAnim()) {
            if (this.getPath2() == null) {
                if (this.currentSpeed > 0.0f && (!this.climbing || this.lastFallSpeed > 0.0f)) {
                    if (this.isRunning() || this.isSprinting()) {
                        this.StopAllActionQueueRunning();
                    } else {
                        this.StopAllActionQueueWalking();
                    }
                }
            } else {
                this.StopAllActionQueueWalking();
            }
        }
        if (this.slowTimer > 0.0f) {
            this.slowTimer -= GameTime.instance.getRealworldSecondsSinceLastUpdate();
            this.currentSpeed *= 1.0f - this.slowFactor;
            this.slowFactor -= GameTime.instance.getMultiplier() / 100.0f;
            if (this.slowFactor < 0.0f) {
                this.slowFactor = 0.0f;
            }
        } else {
            this.slowFactor = 0.0f;
        }
        this.playerMoveDir.setLength(this.currentSpeed);
        if (this.playerMoveDir.x != 0.0f || this.playerMoveDir.y != 0.0f) {
            this.dirtyRecalcGridStackTime = 10.0f;
        }
        if (this.getPath2() != null && this.current != this.last) {
            this.dirtyRecalcGridStackTime = 10.0f;
        }
        this.closestZombie = 1000000.0f;
        this.weight = 0.3f;
        this.separate();
        if (!this.isAnimal()) {
            this.updateSleepingPillsTaken();
            this.updateTorchStrength();
        }
        if (this.isNpc()) {
            AIComponent ai = this.getECSComponent(AIComponent.class);
            ai.postUpdatePlayer(this);
            strafeX = ai.getHumanControlVars().strafeX;
            strafeY = ai.getHumanControlVars().strafeY;
        }
        this.isPlayerMoving = this.isJustMoved() || this.hasPath() && this.getPathFindBehavior2().shouldBeMoving();
        boolean inTrees = this.isInTrees();
        if (inTrees) {
            this.setVariable("WalkSpeedTrees", this.calculateInTreesSpeed());
        }
        IsoObject collapsedFence = BentFences.getInstance().getCollapsedFence(this.square);
        if ((inTrees || collapsedFence != null || this.walkSpeed < 0.4f || this.walkInjury > 0.5f) && this.isSprinting() && !this.isGhostMode()) {
            if (this.runSpeedModifier < 1.0f) {
                this.setMoodleCantSprint(true);
            }
            this.setSprinting(false);
            this.setRunning(true);
            if (this.isInTreesNoBush() || collapsedFence != null) {
                this.setForceSprint(false);
                this.setBumpType("left");
                this.setVariable("BumpDone", false);
                this.setVariable("BumpFall", true);
                this.setVariable("TripObstacleType", "tree");
                this.getActionContext().reportEvent("wasBumped");
            }
        }
        this.deltaX = strafeX;
        this.deltaY = strafeY;
        this.windspeed = ClimateManager.getInstance().getWindSpeedMovement();
        float angle = this.getForwardDirection().getDirectionNeg();
        this.windForce = ClimateManager.getInstance().getWindForceMovement(this, angle);
        return true;
    }

    public void setNpc(boolean isNpc) {
        if (this.isNpc() == isNpc) {
            return;
        }
        if (isNpc) {
            this.setECSComponent(new AIComponent());
        } else {
            this.removeECSComponent(AIComponent.class);
        }
    }

    @Override
    protected void handleLandingImpact(FallDamage fallDamage) {
        boolean isDamagingFall = fallDamage.isDamagingFall();
        boolean isMoreThanLightFall = fallDamage.isMoreThanLightFall();
        if (isDamagingFall) {
            this.stopPlayerVoiceSound("DeathFall");
        }
        if (GameClient.client) {
            return;
        }
        if (isMoreThanLightFall) {
            this.stats.reset(CharacterStat.BOREDOM);
        }
        super.handleLandingImpact(fallDamage);
    }

    @Override
    protected void playPainVoicesFromFallDamage(FallDamage fallDamage) {
        boolean isMoreThanLightFall = fallDamage.isMoreThanLightFall();
        this.playerVoiceSound(isMoreThanLightFall ? "PainFromFallHigh" : " PainFromFallLow");
    }

    private void setDoGrappleLetGoAfterContextKeyIsReleased(boolean letGoAfterContextIsReleased) {
        this.letGoAfterContextIsReleased = letGoAfterContextIsReleased;
    }

    private boolean isDoGrappleLetGoAfterContextKeyIsReleased() {
        return this.letGoAfterContextIsReleased;
    }

    private void updateMovementFromInput(MoveVars moveVars) {
        moveVars.moveX = 0.0f;
        moveVars.moveY = 0.0f;
        moveVars.strafeX = 0.0f;
        moveVars.strafeY = 0.0f;
        moveVars.newFacing = this.getForwardIsoDirection();
        if (TutorialManager.instance.stealControl) {
            return;
        }
        if (this.isBlockMovement()) {
            return;
        }
        if (this.getWrappedGrappleable().isPickingUpBody() || this.getWrappedGrappleable().isPuttingDownBody()) {
            return;
        }
        if (this.isNpc()) {
            return;
        }
        if (MPDebugAI.updateMovementFromInput(this, moveVars)) {
            return;
        }
        if (this.fallTime > 2.0f) {
            return;
        }
        Vector2 aimVec = this.getAimVector(tempVector2);
        float aimX = aimVec.x;
        float aimY = aimVec.y;
        Vector2 moveVec = this.getInputMoveVector(tempVector2);
        float moveX = moveVec.x;
        float moveY = moveVec.y;
        this.playerMoveDir.x = moveY + moveX;
        this.playerMoveDir.y = moveY - moveX;
        if (Math.abs(moveX) > 0.0f || Math.abs(moveY) > 0.0f) {
            this.setJustMoved(true);
        }
        if (aimX != 0.0f || aimY != 0.0f) {
            newFacingVec = tempVector2.set(aimX, aimY);
            newFacingVec.normalize();
            moveVars.newFacing = IsoDirections.fromAngle(newFacingVec);
        } else if ((moveX != 0.0f || moveY != 0.0f) && this.playerMoveDir.getLengthSquared() > 0.0f) {
            newFacingVec = tempVector2.set(this.playerMoveDir);
            newFacingVec.normalize();
            moveVars.newFacing = IsoDirections.fromAngle(newFacingVec);
        }
        moveVars.moveX = this.playerMoveDir.x;
        moveVars.moveY = this.playerMoveDir.y;
        this.resolveStrafeDirectionFromPath(this.playerMoveDir);
        if (this.playerMoveDir.x != 0.0f || this.playerMoveDir.y != 0.0f) {
            if (this.isStrafing()) {
                tempo.set(this.playerMoveDir.x, -this.playerMoveDir.y);
                tempo.normalize();
                float angle = this.legsSprite.modelSlot.model.animPlayer.getRenderedAngle();
                if ((double)angle > Math.PI * 2) {
                    angle -= (float)Math.PI * 2;
                }
                if (angle < 0.0f) {
                    angle += (float)Math.PI * 2;
                }
                tempo.rotate(angle);
                moveVars.strafeX = IsoPlayer.tempo.x;
                moveVars.strafeY = IsoPlayer.tempo.y;
                this.ipX = this.playerMoveDir.x;
                this.ipY = this.playerMoveDir.y;
            } else {
                moveVars.moveX = this.playerMoveDir.x;
                moveVars.moveY = this.playerMoveDir.y;
                tempo.set(this.playerMoveDir);
                this.setForwardDirection(tempo);
            }
        }
        if (this.isJustMoved()) {
            UIManager.speedControls.SetCurrentGameSpeed(1);
        }
    }

    private void resolveStrafeDirectionFromPath(Vector2 playerMoveDir) {
        PathFindBehavior2 pfb2 = this.getPathFindBehavior2();
        if (playerMoveDir.x == 0.0f && playerMoveDir.y == 0.0f && this.getPath2() != null && pfb2.isStrafing() && !pfb2.stopping) {
            playerMoveDir.set(pfb2.getTargetX() - this.getX(), pfb2.getTargetY() - this.getY());
            playerMoveDir.normalize();
        } else if (playerMoveDir.x == 0.0f && playerMoveDir.y == 0.0f && this.getPath2() != null && this.isAiming() && !pfb2.stopping) {
            playerMoveDir.set(this.reqMovement);
        }
    }

    private void randomizeDrunkenMovement(MoveVars moveVars, boolean isController) {
        int diff;
        int mod;
        int n = mod = Rand.NextBool(20) ? 2 : 1;
        if (this.drunkMoveVars.newFacing == null) {
            this.drunkMoveVars.newFacing = moveVars.newFacing;
        }
        diff = (diff = (moveVars.newFacing.ordinal() - this.drunkMoveVars.newFacing.ordinal() + 4) % 8 - 4) < -4 ? diff + 4 : diff;
        boolean clockwise = false;
        if (Math.abs(diff) >= 2) {
            if (diff < 0) {
                clockwise = true;
            }
        } else if (Rand.NextBool(2)) {
            clockwise = true;
        }
        moveVars.newFacing = clockwise ? this.drunkMoveVars.newFacing.RotRight(mod) : this.drunkMoveVars.newFacing.RotLeft(mod);
        if (isController) {
            switch (moveVars.newFacing) {
                case N: {
                    moveVars.moveX = 1.0f;
                    moveVars.moveY = -1.0f;
                    break;
                }
                case NW: {
                    moveVars.moveX = 0.0f;
                    moveVars.moveY = -1.0f;
                    break;
                }
                case W: {
                    moveVars.moveX = -1.0f;
                    moveVars.moveY = -1.0f;
                    break;
                }
                case SW: {
                    moveVars.moveX = -1.0f;
                    moveVars.moveY = 0.0f;
                    break;
                }
                case S: {
                    moveVars.moveX = -1.0f;
                    moveVars.moveY = 1.0f;
                    break;
                }
                case SE: {
                    moveVars.moveX = 0.0f;
                    moveVars.moveY = 1.0f;
                    break;
                }
                case E: {
                    moveVars.moveX = 1.0f;
                    moveVars.moveY = 1.0f;
                    break;
                }
                case NE: {
                    moveVars.moveX = 1.0f;
                    moveVars.moveY = 0.0f;
                }
            }
            return;
        }
        switch (moveVars.newFacing) {
            case N: {
                moveVars.moveX = 0.0f;
                moveVars.moveY = -1.0f;
                break;
            }
            case NW: {
                moveVars.moveX = -1.0f;
                moveVars.moveY = -1.0f;
                break;
            }
            case W: {
                moveVars.moveX = -1.0f;
                moveVars.moveY = 0.0f;
                break;
            }
            case SW: {
                moveVars.moveX = -1.0f;
                moveVars.moveY = 1.0f;
                break;
            }
            case S: {
                moveVars.moveX = 0.0f;
                moveVars.moveY = 1.0f;
                break;
            }
            case SE: {
                moveVars.moveX = 1.0f;
                moveVars.moveY = 1.0f;
                break;
            }
            case E: {
                moveVars.moveX = 1.0f;
                moveVars.moveY = 0.0f;
                break;
            }
            case NE: {
                moveVars.moveX = 1.0f;
                moveVars.moveY = -1.0f;
            }
        }
    }

    private void adjustMovementForDrunks(MoveVars moveVars, boolean isController) {
        if (this.getMoodles().getMoodleLevel(MoodleType.DRUNK) > 1) {
            this.drunkDelayCommandTimer += GameTime.getInstance().getMultiplier();
            int chance = Rand.AdjustForFramerate(4 * this.getMoodles().getMoodleLevel(MoodleType.DRUNK));
            if ((float)chance > this.drunkDelayCommandTimer) {
                moveVars.moveY = this.drunkMoveVars.moveY;
                moveVars.moveX = this.drunkMoveVars.moveX;
                moveVars.newFacing = this.drunkMoveVars.newFacing;
            } else {
                this.drunkDelayCommandTimer = 0.0f;
                if ((moveVars.moveX != 0.0f || moveVars.moveY != 0.0f) && (float)Rand.Next(160) < this.getStats().get(CharacterStat.INTOXICATION)) {
                    this.randomizeDrunkenMovement(moveVars, isController);
                }
                if ((this.isSprinting() || this.isRunning()) && (float)Rand.Next(2000) < this.getStats().get(CharacterStat.INTOXICATION) * (float)this.getMoodles().getMoodleLevel(MoodleType.DRUNK)) {
                    this.setVariable("BumpDone", false);
                    this.clearVariable("BumpFallType");
                    this.setBumpType("trippingFromSprint");
                    this.setBumpFall(true);
                    this.setBumpFallType(Rand.NextBool(5) ? "pushedFront" : "pushedBehind");
                    this.getActionContext().reportEvent("wasBumped");
                }
            }
        } else {
            this.drunkDelayCommandTimer = 0.0f;
        }
        this.drunkMoveVars.moveY = moveVars.moveY;
        this.drunkMoveVars.moveX = moveVars.moveX;
        this.drunkMoveVars.newFacing = moveVars.newFacing;
    }

    private void updateAimingStance() {
        boolean bAttacking;
        HitInfo bestProne;
        if (this.isVariable("LeftHandMask", "RaiseHand")) {
            this.clearVariable("LeftHandMask");
        }
        if (!this.isAiming() || this.isCurrentState(SwipeStatePlayer.instance())) {
            return;
        }
        HandWeapon weapon = Type.tryCastTo(this.getPrimaryHandItem(), HandWeapon.class);
        HandWeapon handWeapon = weapon = weapon == null ? this.bareHands : weapon;
        if (weapon.hasComponent(ComponentType.FluidContainer) && weapon.getFluidContainer().getRainCatcher() > 0.0f && !weapon.getFluidContainer().isEmpty()) {
            weapon.getFluidContainer().Empty();
        }
        CombatManager.getInstance().calcValidTargets(this, weapon, s_targetsProne, s_targetsStanding);
        HitInfo bestStanding = s_targetsStanding.isEmpty() ? null : s_targetsStanding.get(0);
        HitInfo hitInfo = bestProne = s_targetsProne.isEmpty() ? null : s_targetsProne.get(0);
        if (CombatManager.getInstance().isProneTargetBetter(this, bestStanding, bestProne)) {
            bestStanding = null;
        }
        if (!(bAttacking = this.isPerformingHostileAnimation())) {
            this.setAimAtFloor(false);
        }
        if (bestStanding != null) {
            if (!bAttacking) {
                this.setAimAtFloor(false);
            }
        } else if (bestProne != null && !bAttacking) {
            this.setAimAtFloor(true);
        }
        if (bestStanding != null) {
            boolean raiseHand;
            boolean bl = raiseHand = !this.isPerformingAttackAnimation() && weapon.getSwingAnim() != null && weapon.closeKillMove != null && bestStanding.distSq < weapon.getMinRange() * weapon.getMinRange();
            if (raiseHand && (this.getSecondaryHandItem() == null || this.getSecondaryHandItem().getItemReplacementSecondHand() == null)) {
                this.setVariable("LeftHandMask", "RaiseHand");
            }
        }
        CombatManager.getInstance().hitInfoPool.release((List<HitInfo>)s_targetsStanding);
        CombatManager.getInstance().hitInfoPool.release((List<HitInfo>)s_targetsProne);
        s_targetsStanding.clear();
        s_targetsProne.clear();
    }

    @Override
    protected void calculateStats() {
        if (!GameClient.client) {
            super.calculateStats();
        }
    }

    @Override
    protected void updateStats_Sleeping() {
        float endMult = 2.0f;
        if (IsoPlayer.allPlayersAsleep()) {
            endMult *= GameTime.instance.getDeltaMinutesPerDay();
        }
        this.stats.add(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.imobileEnduranceReduce * SandboxOptions.instance.getEnduranceRegenMultiplier() * (double)this.getRecoveryMod() * (double)GameTime.instance.getMultiplier() * (double)endMult));
        if (this.stats.isAboveMinimum(CharacterStat.FATIGUE)) {
            float mul = 1.0f;
            if (this.characterTraits.get(CharacterTrait.INSOMNIAC)) {
                mul *= 0.5f;
            }
            if (this.characterTraits.get(CharacterTrait.NIGHT_OWL)) {
                mul *= 1.4f;
            }
            float bedQualityMultiplier = 1.0f;
            if ("averageBedPillow".equals(this.getBedType())) {
                bedQualityMultiplier = 1.05f;
            }
            if ("goodBed".equals(this.getBedType())) {
                bedQualityMultiplier = 1.1f;
            } else if ("goodBedPillow".equals(this.getBedType())) {
                bedQualityMultiplier = 1.15f;
            } else if ("badBed".equals(this.getBedType())) {
                bedQualityMultiplier = 0.9f;
            } else if ("badBedPillow".equals(this.getBedType())) {
                bedQualityMultiplier = 0.95f;
            } else if ("floor".equals(this.getBedType())) {
                bedQualityMultiplier = 0.6f;
            } else if ("floorPillow".equals(this.getBedType())) {
                bedQualityMultiplier = 0.75f;
            }
            float elapsedHours = 1.0f / GameTime.instance.getMinutesPerDay() / 60.0f * GameTime.instance.getMultiplier() / 2.0f;
            this.timeOfSleep += elapsedHours;
            if (this.timeOfSleep > this.delayToActuallySleep) {
                float traitMul = 1.0f;
                if (this.characterTraits.get(CharacterTrait.NEEDS_LESS_SLEEP)) {
                    traitMul *= 0.75f;
                } else if (this.characterTraits.get(CharacterTrait.NEEDS_MORE_SLEEP)) {
                    traitMul *= 1.18f;
                }
                if (this.stats.get(CharacterStat.FATIGUE) <= 0.3f) {
                    float hoursNeeded = 7.0f * traitMul;
                    this.stats.remove(CharacterStat.FATIGUE, elapsedHours / hoursNeeded * 0.3f * mul * bedQualityMultiplier);
                } else {
                    float hoursNeeded = 5.0f * traitMul;
                    this.stats.remove(CharacterStat.FATIGUE, elapsedHours / hoursNeeded * 0.7f * mul * bedQualityMultiplier);
                }
            }
        }
        if (this.moodles.getMoodleLevel(MoodleType.FOOD_EATEN) == 0) {
            float hungerMult = this.getAppetiteMultiplier();
            this.stats.add(CharacterStat.HUNGER, (float)(ZomboidGlobals.hungerIncreaseWhileAsleep * SandboxOptions.instance.getStatsDecreaseMultiplier() * (double)hungerMult * (double)GameTime.instance.getMultiplier() * (double)GameTime.instance.getDeltaMinutesPerDay() * this.getHungerMultiplier()));
        } else {
            this.stats.add(CharacterStat.HUNGER, (float)(ZomboidGlobals.hungerIncreaseWhenWellFed * SandboxOptions.instance.getStatsDecreaseMultiplier() * ZomboidGlobals.hungerIncreaseWhileAsleep * SandboxOptions.instance.getStatsDecreaseMultiplier() * (double)GameTime.instance.getMultiplier() * this.getHungerMultiplier() * (double)GameTime.instance.getDeltaMinutesPerDay()));
        }
    }

    public void processWakingUp() {
        boolean forceWakeUp;
        if (this.forceWakeUpTime == 0.0f) {
            this.forceWakeUpTime = 9.0f;
        }
        float timeOfDay = GameTime.getInstance().getTimeOfDay();
        float lastTimeOfDay = GameTime.getInstance().getLastTimeOfDay();
        if (lastTimeOfDay > timeOfDay) {
            if (lastTimeOfDay < this.forceWakeUpTime) {
                timeOfDay += 24.0f;
            } else {
                lastTimeOfDay -= 24.0f;
            }
        }
        boolean bl = forceWakeUp = timeOfDay >= this.forceWakeUpTime && lastTimeOfDay < this.forceWakeUpTime;
        if (this.getAsleepTime() > 16.0f) {
            forceWakeUp = true;
        }
        if (GameClient.client || numPlayers > 1) {
            boolean bl2 = forceWakeUp = forceWakeUp || this.pressedAim() || this.pressedMovement(false);
        }
        if (this.forceWakeUp) {
            forceWakeUp = true;
        }
        if (this.asleep && forceWakeUp) {
            this.forceWakeUp = false;
            SoundManager.instance.setMusicWakeState(this, "WakeNormal");
            SleepingEvent.instance.wakeUp(this);
            this.forceWakeUpTime = -1.0f;
            if (GameClient.client) {
                GameClient.instance.sendPlayer(this);
            }
            this.dirtyRecalcGridStackTime = 20.0f;
        }
    }

    public void updateEnduranceWhileSitting() {
        float mul = (float)ZomboidGlobals.sittingEnduranceMultiplier;
        mul *= 1.0f - this.stats.get(CharacterStat.FATIGUE) * 0.8f;
        this.stats.add(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.imobileEnduranceReduce * SandboxOptions.instance.getEnduranceRegenMultiplier() * (double)this.getRecoveryMod() * (double)(mul *= GameTime.instance.getMultiplier())));
    }

    public void updateEnduranceWhileInVehicle() {
        if (this.isAnimal() || GameClient.client) {
            return;
        }
        if (!this.asleep) {
            float mul = (float)ZomboidGlobals.sittingEnduranceMultiplier;
            mul *= 1.0f - this.stats.get(CharacterStat.FATIGUE) * 0.8f;
            this.stats.add(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.imobileEnduranceReduce * SandboxOptions.instance.getEnduranceRegenMultiplier() * (double)this.getRecoveryMod() * (double)(mul *= GameTime.instance.getMultiplier())));
        }
    }

    private void updateEndurance() {
        if (this.isAnimal() || GameClient.client) {
            return;
        }
        if (this.isSitOnGround() || this.isSittingOnFurniture() || this.isResting()) {
            this.updateEnduranceWhileSitting();
            return;
        }
        float sneakMultiplier = 1.0f;
        if (this.isSneaking()) {
            sneakMultiplier = 1.5f;
        }
        if (this.currentSpeed > 0.0f && (this.isRunning() || this.isSprinting() || this.isDraggingCorpse())) {
            double enduranceReduce = ZomboidGlobals.runningEnduranceReduce;
            if (this.isSprinting()) {
                enduranceReduce = ZomboidGlobals.sprintingEnduranceReduce;
            }
            if (this.isDraggingCorpse()) {
                enduranceReduce = ZomboidGlobals.sprintingEnduranceReduce;
            }
            float enddelta = 1.4f;
            if (this.characterTraits.get(CharacterTrait.OVERWEIGHT)) {
                enddelta = 2.9f;
            }
            if (this.characterTraits.get(CharacterTrait.ATHLETIC)) {
                enddelta = 0.8f;
            }
            enddelta *= 2.3f;
            enddelta *= this.getPacingMod();
            enddelta *= this.getHyperthermiaMod();
            float mod = 0.7f;
            if (this.characterTraits.get(CharacterTrait.ASTHMATIC)) {
                mod = 1.0f;
            }
            if (this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD) == 0) {
                this.stats.remove(CharacterStat.ENDURANCE, (float)(enduranceReduce * (double)enddelta * 0.5 * (double)mod * (double)GameTime.instance.getMultiplier() * (double)sneakMultiplier));
            } else {
                float weightDelta = switch (this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD)) {
                    case 1 -> 1.5f;
                    case 2 -> 1.9f;
                    case 3 -> 2.3f;
                    default -> 2.8f;
                };
                this.stats.remove(CharacterStat.ENDURANCE, (float)(enduranceReduce * (double)enddelta * 0.5 * (double)mod * (double)GameTime.instance.getMultiplier() * (double)weightDelta * (double)sneakMultiplier));
            }
        } else if (this.currentSpeed > 0.0f && this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD) > 2) {
            float mod = 0.7f;
            if (this.characterTraits.get(CharacterTrait.ASTHMATIC)) {
                mod = 1.0f;
            }
            float enddelta = 1.4f;
            if (this.characterTraits.get(CharacterTrait.OVERWEIGHT)) {
                enddelta = 2.9f;
            }
            if (this.characterTraits.get(CharacterTrait.ATHLETIC)) {
                enddelta = 0.8f;
            }
            enddelta *= 3.0f;
            enddelta *= this.getPacingMod();
            enddelta *= this.getHyperthermiaMod();
            float weightDelta = 2.8f;
            switch (this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD)) {
                case 2: {
                    weightDelta = 1.5f;
                    break;
                }
                case 3: {
                    weightDelta = 1.9f;
                    break;
                }
                case 4: {
                    weightDelta = 2.3f;
                }
            }
            this.stats.remove(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.runningEnduranceReduce * (double)enddelta * 0.5 * (double)mod * (double)sneakMultiplier * (double)GameTime.instance.getMultiplier() * (double)weightDelta / 2.0));
        }
        if (!this.isPlayerMoving()) {
            float mul = 1.0f;
            mul *= 1.0f - this.stats.get(CharacterStat.FATIGUE) * 0.85f;
            mul *= GameTime.instance.getMultiplier();
            if (this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD) <= 1) {
                this.stats.add(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.imobileEnduranceReduce * SandboxOptions.instance.getEnduranceRegenMultiplier() * (double)this.getRecoveryMod() * (double)mul));
            }
        }
        if (!this.isSprinting() && !this.isRunning() && this.currentSpeed > 0.0f) {
            float mul = 1.0f;
            mul *= 1.0f - this.stats.get(CharacterStat.FATIGUE);
            mul *= GameTime.instance.getMultiplier();
            if (this.getMoodles().getMoodleLevel(MoodleType.ENDURANCE) < 2) {
                if (this.moodles.getMoodleLevel(MoodleType.HEAVY_LOAD) <= 1) {
                    this.stats.add(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.imobileEnduranceReduce / 4.0 * SandboxOptions.instance.getEnduranceRegenMultiplier() * (double)this.getRecoveryMod() * (double)mul));
                }
            } else {
                this.stats.remove(CharacterStat.ENDURANCE, (float)(ZomboidGlobals.runningEnduranceReduce / 7.0 * (double)sneakMultiplier));
            }
        }
    }

    private boolean checkActionsBlockingMovement() {
        if (this.characterActions.isEmpty()) {
            return false;
        }
        BaseAction action = (BaseAction)this.characterActions.get(0);
        return action.blockMovementEtc;
    }

    private void updateInteractKeyPanic() {
        if (this.playerIndex != 0) {
            return;
        }
        if (this.isInteractButtonPressed()) {
            this.contextPanic += 0.6f;
        }
    }

    private void updateSneakKey() {
        if (this.isCrouchButtonPressed()) {
            this.setSneaking(!this.isSneaking());
        }
    }

    private void updateChangeCharacterKey() {
        if (!Core.debug) {
            return;
        }
        if (this.playerIndex != 0 || !this.isChangeCharacterKeyDown()) {
            this.changeCharacterDebounce = false;
            return;
        }
        if (this.changeCharacterDebounce) {
            return;
        }
        this.followCamStack.clear();
        this.changeCharacterDebounce = true;
        for (IsoMovingObject obj : this.getCell().getObjectList()) {
            if (!(obj instanceof IsoSurvivor)) continue;
            IsoSurvivor isoSurvivor = (IsoSurvivor)obj;
            this.followCamStack.add(isoSurvivor);
        }
        if (!this.followCamStack.isEmpty()) {
            if (this.followId >= this.followCamStack.size()) {
                this.followId = 0;
            }
            IsoCamera.SetCharacterToFollow((IsoGameCharacter)this.followCamStack.get(this.followId));
            ++this.followId;
        }
    }

    private void updateEnableModelsKey() {
        if (!Core.debug) {
            return;
        }
        if (this.playerIndex == 0 && GameKeyboard.isKeyPressed("ToggleModelsEnabled")) {
            ModelManager.instance.debugEnableModels = !ModelManager.instance.debugEnableModels;
        }
    }

    private void updateDeathDragDown() {
        if (this.isDead()) {
            return;
        }
        if (!this.isDeathDragDown()) {
            return;
        }
        if (this.isGodMod()) {
            this.setDeathDragDown(false);
            return;
        }
        if ("EndDeath".equals(this.getHitReaction())) {
            return;
        }
        for (int dy = -1; dy <= 1; ++dy) {
            for (int dx = -1; dx <= 1; ++dx) {
                IsoGridSquare square = this.getCell().getGridSquare(PZMath.fastfloor(this.getX()) + dx, PZMath.fastfloor(this.getY()) + dy, PZMath.fastfloor(this.getZ()));
                if (square == null) continue;
                for (int i = 0; i < square.getMovingObjects().size(); ++i) {
                    IsoMovingObject mo = square.getMovingObjects().get(i);
                    IsoZombie zombie = Type.tryCastTo(mo, IsoZombie.class);
                    if (zombie == null || !zombie.isAlive() || zombie.isOnFloor()) continue;
                    this.setAttackedBy(zombie);
                    this.setHitReaction("EndDeath");
                    this.setBlockMovement(true);
                    return;
                }
            }
        }
        this.setDeathDragDown(false);
        if (GameClient.client) {
            DebugType.DetailedInfo.warn("UpdateDeathDragDown: no zombies found around player \"%s\"", this.getUsername());
            this.setHitFromBehind(false);
            this.Kill(null);
        }
    }

    private void updateGodModeKey() {
        if (!Core.debug) {
            return;
        }
        if (!GameKeyboard.isKeyPressed("ToggleGodModeInvisible")) {
            return;
        }
        IsoGameCharacter player = null;
        for (int n = 0; n < numPlayers; ++n) {
            if (players[n] == null || players[n].isDead()) continue;
            player = players[n];
            break;
        }
        if (this == player) {
            boolean godMode = !player.isGodMod();
            DebugType.General.println("Toggle GodMode: %s", godMode ? "ON" : "OFF");
            player.setInvisible(godMode);
            player.setGodMod(godMode);
            ((IsoPlayer)player).setNoClip(godMode);
            player.setFastMoveCheat(godMode);
            if (GameServer.server) {
                for (WorldMapRemotePlayer remotePlayer : WorldMapRemotePlayers.instance.getPlayers()) {
                    if (!remotePlayer.getUsername().equals(((IsoPlayer)player).getUsername())) continue;
                    remotePlayer.setPlayer((IsoPlayer)player);
                }
            }
            for (int n = 0; n < numPlayers; ++n) {
                if (players[n] == null || players[n] == player) continue;
                players[n].setInvisible(godMode);
                players[n].setGodMod(godMode);
                players[n].setNoClip(godMode);
                players[n].setFastMoveCheat(godMode);
            }
            if (GameClient.client) {
                GameClient.sendPlayerExtraInfo((IsoPlayer)player);
            }
        }
    }

    private void checkReloading() {
        HandWeapon weapon = Type.tryCastTo(this.getPrimaryHandItem(), HandWeapon.class);
        if (weapon == null || !weapon.isReloadable(this)) {
            return;
        }
        CharacterInputComponent characterInputComponent = this.getCharacterInputComponent();
        if (characterInputComponent != null) {
            if (characterInputComponent.isReloadWeaponButtonPressed()) {
                this.setVariable("WeaponReloadType", weapon.getWeaponReloadType().toString());
                LuaEventManager.triggerEvent("OnPressReloadButton", this, weapon);
            } else if (characterInputComponent.isRackFirearmButtonPressed()) {
                this.setVariable("WeaponReloadType", weapon.getWeaponReloadType().toString());
                LuaEventManager.triggerEvent("OnPressRackButton", this, weapon, characterInputComponent.isShiftKeyDown());
            }
        }
    }

    @Override
    public void postupdate() {
        try (AbstractPerformanceProfileProbe abstractPerformanceProfileProbe = s_performance.postUpdate.profile();){
            this.postupdateInternal();
        }
    }

    private void postupdateInternal() {
        boolean bHasHitReaction = this.hasHitReaction();
        super.postupdate();
        if (bHasHitReaction && this.hasHitReaction() && !this.isCurrentState(PlayerHitReactionState.instance()) && !this.isCurrentState(PlayerHitReactionPVPState.instance())) {
            this.setHitReaction("");
        }
        if (this.isNpc()) {
            GameTime gameTime = GameTime.getInstance();
            float time = 1.0f / gameTime.getMinutesPerDay() / 60.0f * gameTime.getMultiplier() / 2.0f;
            if (Core.lastStand) {
                time = 1.0f / gameTime.getMinutesPerDay() / 60.0f * gameTime.getUnmoddedMultiplier() / 2.0f;
            }
            this.setHoursSurvived(this.getHoursSurvived() + (double)time);
        }
        if (this.getBodyDamage() != null) {
            this.getBodyDamage().setBodyPartsLastState();
        }
    }

    @Override
    public boolean isSolidForSeparate() {
        if (this.isGhostMode()) {
            return false;
        }
        return super.isSolidForSeparate();
    }

    @Override
    public boolean isPushableForSeparate() {
        if (this.isCurrentState(PlayerHitReactionState.instance())) {
            return false;
        }
        if (this.isCurrentState(SwipeStatePlayer.instance())) {
            return false;
        }
        return super.isPushableForSeparate();
    }

    @Override
    public boolean isPushedByForSeparate(IsoMovingObject other) {
        if (!this.isPlayerMoving() && other.isZombie() && ((IsoZombie)other).isAttacking()) {
            return false;
        }
        if (!(!GameClient.client || this.isLocalPlayer() && this.isJustMoved())) {
            return false;
        }
        return super.isPushedByForSeparate(other);
    }

    private void updateExt() {
        if (this.isSneaking()) {
            return;
        }
        if (this.isGrappling() || this.isBeingGrappled()) {
            return;
        }
        this.extUpdateCount += GameTime.getInstance().getMultiplier() / 0.8f;
        if (!this.getAdvancedAnimator().containsAnyIdleNodes() && !this.isSitOnGround()) {
            this.extUpdateCount = 0.0f;
        }
        if (this.extUpdateCount <= 5000.0f) {
            return;
        }
        this.extUpdateCount = 0.0f;
        if (this.stats.numVisibleZombies != 0 || this.stats.numChasingZombies != 0) {
            return;
        }
        if (!Rand.NextBool(3)) {
            return;
        }
        if (!this.getAdvancedAnimator().containsAnyIdleNodes() && !this.isSitOnGround()) {
            return;
        }
        this.onIdlePerformFidgets();
        this.reportEvent("EventDoExt");
    }

    private void onIdlePerformFidgets() {
        Moodles moodles = this.getMoodles();
        BodyDamage bodyDamage = this.getBodyDamage();
        if (moodles.getMoodleLevel(MoodleType.HYPOTHERMIA) > 0 && Rand.NextBool(7)) {
            this.setVariable("Ext", "Shiver");
            return;
        }
        if (moodles.getMoodleLevel(MoodleType.HYPERTHERMIA) > 0 && Rand.NextBool(7)) {
            this.setVariable("Ext", "WipeBrow");
            return;
        }
        if (moodles.getMoodleLevel(MoodleType.SICK) > 0 && Rand.NextBool(7)) {
            if (Rand.NextBool(4)) {
                this.setVariable("Ext", "Cough");
            } else {
                this.setVariable("Ext", "PainStomach" + (Rand.Next(2) + 1));
            }
            return;
        }
        if (moodles.getMoodleLevel(MoodleType.ENDURANCE) > 2 && Rand.NextBool(10)) {
            if (Rand.NextBool(5) && !this.isSitOnGround()) {
                this.setVariable("Ext", "BentDouble");
            } else {
                this.setVariable("Ext", "WipeBrow");
            }
            return;
        }
        if (moodles.getMoodleLevel(MoodleType.TIRED) > 2 && Rand.NextBool(10)) {
            if (Rand.NextBool(7)) {
                this.setVariable("Ext", "TiredStretch");
            } else if (Rand.NextBool(7)) {
                this.setVariable("Ext", "Sway");
            } else {
                this.setVariable("Ext", "Yawn");
            }
            return;
        }
        if (bodyDamage.doBodyPartsHaveInjuries(BodyPartType.Head, BodyPartType.Neck) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.Head, BodyPartType.Neck) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeHead");
            } else {
                this.setVariable("Ext", "PainHead" + (Rand.Next(2) + 1));
            }
            return;
        }
        if (bodyDamage.doBodyPartsHaveInjuries(BodyPartType.UpperArm_L, BodyPartType.ForeArm_L) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.UpperArm_L, BodyPartType.ForeArm_L) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeArmL");
            } else {
                this.setVariable("Ext", "PainArmL");
            }
            return;
        }
        if (bodyDamage.doBodyPartsHaveInjuries(BodyPartType.UpperArm_R, BodyPartType.ForeArm_R) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.UpperArm_R, BodyPartType.ForeArm_R) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeArmR");
            } else {
                this.setVariable("Ext", "PainArmR");
            }
            return;
        }
        if (bodyDamage.doesBodyPartHaveInjury(BodyPartType.Hand_L) && Rand.NextBool(7)) {
            this.setVariable("Ext", "PainHandL");
            return;
        }
        if (bodyDamage.doesBodyPartHaveInjury(BodyPartType.Hand_R) && Rand.NextBool(7)) {
            this.setVariable("Ext", "PainHandR");
            return;
        }
        if (!this.isSitOnGround() && bodyDamage.doBodyPartsHaveInjuries(BodyPartType.UpperLeg_L, BodyPartType.LowerLeg_L) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.UpperLeg_L, BodyPartType.LowerLeg_L) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeLegL");
            } else {
                this.setVariable("Ext", "PainLegL");
            }
            return;
        }
        if (!this.isSitOnGround() && bodyDamage.doBodyPartsHaveInjuries(BodyPartType.UpperLeg_R, BodyPartType.LowerLeg_R) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.UpperLeg_R, BodyPartType.LowerLeg_R) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeLegR");
            } else {
                this.setVariable("Ext", "PainLegR");
            }
            return;
        }
        if (bodyDamage.doBodyPartsHaveInjuries(BodyPartType.Torso_Upper, BodyPartType.Torso_Lower) && Rand.NextBool(7)) {
            if (bodyDamage.areBodyPartsBleeding(BodyPartType.Torso_Upper, BodyPartType.Torso_Lower) && Rand.NextBool(2)) {
                this.setVariable("Ext", "WipeTorso" + (Rand.Next(2) + 1));
            } else {
                this.setVariable("Ext", "PainTorso");
            }
            return;
        }
        if (WeaponType.getWeaponType(this) != WeaponType.UNARMED) {
            this.setVariable("Ext", "" + (Rand.Next(5) + 1));
            return;
        }
        if (Rand.NextBool(10)) {
            this.setVariable("Ext", "ChewNails");
            return;
        }
        if (Rand.NextBool(10)) {
            this.setVariable("Ext", "ShiftWeight");
            return;
        }
        if (Rand.NextBool(10)) {
            this.setVariable("Ext", "PullAtColar");
            return;
        }
        if (Rand.NextBool(10)) {
            this.setVariable("Ext", "BridgeNose");
            return;
        }
        this.setVariable("Ext", "" + (Rand.Next(5) + 1));
    }

    private boolean updateUseKey() {
        if (GameServer.server) {
            return false;
        }
        if (!this.isLocalPlayer()) {
            return false;
        }
        boolean bInteract = this.isInteractButtonDown();
        this.timePressedContext += GameTime.instance.getRealworldSecondsSinceLastUpdate();
        if (bInteract && this.timePressedContext < 0.5f) {
            this.pressContext = true;
        } else {
            if (this.pressContext && (Core.currentTextEntryBox != null && Core.currentTextEntryBox.isDoingTextEntry() || !GameKeyboard.doLuaKeyPressed)) {
                this.pressContext = false;
            }
            if (this.isGrappleThrowOutWindow()) {
                this.pressContext = false;
            }
            if (this.pressContext && this.doContext()) {
                this.clearUseKeyVariables();
                return true;
            }
            if (!bInteract) {
                this.clearUseKeyVariables();
            }
        }
        return false;
    }

    private void clearUseKeyVariables() {
        this.pressContext = false;
        this.timePressedContext = 0.0f;
    }

    private void updateSoundListener() {
        if (GameServer.server) {
            return;
        }
        if (!this.isLocalPlayer()) {
            return;
        }
        if (this.soundListener == null) {
            this.soundListener = Core.soundDisabled ? new DummySoundListener(this.playerIndex) : new SoundListener(this.playerIndex);
        }
        this.soundListener.setPos(this.getX(), this.getY(), this.getZ());
        this.checkNearbyRooms -= GameTime.getInstance().getThirtyFPSMultiplier();
        if (this.checkNearbyRooms <= 0.0f) {
            this.checkNearbyRooms = 30.0f;
            this.numNearbyBuildingsRooms = IsoWorld.instance.metaGrid.countNearbyBuildingsRooms(this);
        }
        this.soundListener.tick();
    }

    @Override
    public void updateMovementRates() {
        BodyPart footL = this.bodyDamage.getBodyPart(BodyPartType.Foot_L);
        BodyPart footR = this.bodyDamage.getBodyPart(BodyPartType.Foot_R);
        boolean haveGlassL = footL.haveGlass();
        boolean haveGlassR = footR.haveGlass();
        this.calculateWalkSpeed();
        this.idleSpeed = this.calculateIdleSpeed();
        this.updateFootInjuries();
        this.updateInTreesInjuries();
        if (GameClient.client && (!haveGlassL && footL.haveGlass() || !haveGlassR && footR.haveGlass())) {
            GameClient.sendPlayerDamage(this);
        }
    }

    @Override
    protected void calculateWalkSpeed() {
        if (GameClient.client) {
            boolean runOrSprint = this.isRunning() || this.isSprinting();
            NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
            this.setVariable("WalkSpeed", runOrSprint ? networkAi.runSpeed : networkAi.walkSpeed);
            float sneakLimpSpeedScale = 1.0f;
            if (!runOrSprint && Math.abs(this.getVariableFloat("WalkInjury", 0.0f)) > 0.1f) {
                sneakLimpSpeedScale = this.calculateSneakLimpSpeedScale();
            }
            this.setSneakLimpSpeedScale(sneakLimpSpeedScale);
            return;
        }
        if (this.hasAttachedAnimals()) {
            float injurySpeed = this.getFootInjurySpeedModifier();
            this.setVariable("WalkInjury", injurySpeed);
            this.setVariable("WalkSpeed", 0.0f);
            if (GameServer.server) {
                NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
                networkAi.walkSpeed = 0.0f;
                networkAi.runSpeed = 0.0f;
            }
            return;
        }
        super.calculateWalkSpeed();
        if (GameServer.server) {
            NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
            networkAi.walkSpeed = this.walkSpeed;
            networkAi.runSpeed = this.runSpeed;
        }
    }

    public void pressedAttack() {
        CombatManager.getInstance().pressedAttack(this);
    }

    public void setAttackVariationX(float attackVariationX) {
        this.attackVariationX = attackVariationX;
    }

    private float getAttackVariationX() {
        return this.attackVariationX;
    }

    public void setAttackVariationY(float attackVariationY) {
        this.attackVariationY = attackVariationY;
    }

    private float getAttackVariationY() {
        return this.attackVariationY;
    }

    public boolean canPerformHandToHandCombat() {
        if (this.primaryHandModel != null && !StringUtils.isNullOrEmpty(this.primaryHandModel.maskVariableValue) && this.secondaryHandModel != null && !StringUtils.isNullOrEmpty(this.secondaryHandModel.maskVariableValue)) {
            return false;
        }
        return this.getPrimaryHandItem() == null || this.getPrimaryHandItem().getItemReplacementPrimaryHand() == null || this.getSecondaryHandItem() == null || this.getSecondaryHandItem().getItemReplacementSecondHand() == null;
    }

    @Override
    public void clearHandToHandAttack() {
        super.clearHandToHandAttack();
        this.setInitiateAttack(false);
        this.attackStarted = false;
        this.setAttackType(AttackType.NONE);
    }

    public void setAttackAnimThrowTimer(long dt) {
        this.attackAnimThrowTimer = System.currentTimeMillis() + dt;
    }

    public boolean isAttackAnimThrowTimeOut() {
        return this.attackAnimThrowTimer <= System.currentTimeMillis();
    }

    @Override
    public boolean isAiming() {
        if (!this.isAttackAnimThrowTimeOut()) {
            return true;
        }
        if (GameClient.client && this.isLocalPlayer() && DebugOptions.instance.multiplayer.debug.attackPlayer.getValue()) {
            return false;
        }
        return super.isAiming();
    }

    private String getWeaponType() {
        if (!this.isAttackAnimThrowTimeOut()) {
            return "throwing";
        }
        return this.weaponT;
    }

    private void setWeaponType(String val) {
        this.weaponT = val;
    }

    public int calculateCritChance(IsoGameCharacter target) {
        InventoryItem plyr2;
        if (this.isDoShove()) {
            float baseChance = 35.0f;
            if (target instanceof IsoPlayer) {
                IsoPlayer plyr2 = (IsoPlayer)target;
                baseChance = 20.0f;
                if (GameClient.client && !plyr2.isLocalPlayer()) {
                    double d;
                    if (plyr2 instanceof IsoAnimal) {
                        IsoAnimal isoAnimal = (IsoAnimal)plyr2;
                        d = isoAnimal.getData().getWeight();
                    } else {
                        d = plyr2.getNutrition().getWeight();
                    }
                    double weight = d;
                    baseChance -= (float)plyr2.remoteStrLvl * 1.5f;
                    baseChance = weight < 80.0 ? (baseChance += (float)Math.abs((weight - 80.0) / 2.0)) : (baseChance -= (float)((weight - 80.0) / 2.0));
                }
            }
            baseChance -= (float)(this.getMoodles().getMoodleLevel(MoodleType.ENDURANCE) * 5);
            baseChance -= (float)(this.getMoodles().getMoodleLevel(MoodleType.HEAVY_LOAD) * 5);
            baseChance -= (float)this.getMoodles().getMoodleLevel(MoodleType.PANIC) * 1.3f;
            return (int)(baseChance += (float)(this.getPerkLevel(PerkFactory.Perks.Strength) * 2));
        }
        if (this.isDoShove() && target.getStateMachine().getCurrent() == StaggerBackState.instance() && target instanceof IsoZombie) {
            return 100;
        }
        if (this.getPrimaryHandItem() == null || !((plyr2 = this.getPrimaryHandItem()) instanceof HandWeapon)) {
            return 0;
        }
        HandWeapon handWeapon = (HandWeapon)plyr2;
        float baseChance = handWeapon.getCriticalChance();
        if (handWeapon.isAlwaysKnockdown()) {
            return 100;
        }
        WeaponType weaponType = WeaponType.getWeaponType(this);
        if (weaponType.isRanged()) {
            baseChance += (float)(handWeapon.getAimingPerkCritModifier() * this.getPerkLevel(PerkFactory.Perks.Aiming));
            float dist = this.DistToProper(target);
            float max = handWeapon.getMaxSightRange(this);
            float min = handWeapon.getMinSightRange(this);
            baseChance += PZMath.max(CombatManager.getInstance().getDistanceModifierSightless(dist, false) - CombatManager.getInstance().getAimDelayPenaltySightless(PZMath.max(0.0f, this.getAimingDelay()), dist), CombatManager.getInstance().getDistanceModifier(dist, min, max, false) - CombatManager.getInstance().getAimDelayPenalty(PZMath.max(0.0f, this.getAimingDelay()), dist, min, max));
            baseChance -= CombatManager.getInstance().getMoodlesPenalty(this, dist);
            baseChance -= CombatManager.getInstance().getWeatherPenalty(this, handWeapon, target.getCurrentSquare(), dist);
            baseChance -= CombatManager.getMovePenalty(this, dist);
            if (this.characterTraits.get(CharacterTrait.MARKSMAN)) {
                baseChance += 10.0f;
            }
        } else {
            IsoPlayer plyr3;
            if (handWeapon.isTwoHandWeapon() && (this.getPrimaryHandItem() != handWeapon || this.getSecondaryHandItem() != handWeapon)) {
                baseChance -= baseChance / 3.0f;
            }
            if (this.chargeTime < 2.0f) {
                baseChance -= baseChance / 5.0f;
            }
            int skill = this.getPerkLevel(PerkFactory.Perks.Blunt);
            if (handWeapon.isOfWeaponCategory(WeaponCategory.AXE)) {
                skill = this.getPerkLevel(PerkFactory.Perks.Axe);
            }
            if (handWeapon.isOfWeaponCategory(WeaponCategory.LONG_BLADE)) {
                skill = this.getPerkLevel(PerkFactory.Perks.LongBlade);
            }
            if (handWeapon.isOfWeaponCategory(WeaponCategory.SPEAR)) {
                skill = this.getPerkLevel(PerkFactory.Perks.Spear);
            }
            if (handWeapon.isOfWeaponCategory(WeaponCategory.SMALL_BLADE)) {
                skill = this.getPerkLevel(PerkFactory.Perks.SmallBlade);
            }
            if (handWeapon.isOfWeaponCategory(WeaponCategory.SMALL_BLUNT)) {
                skill = this.getPerkLevel(PerkFactory.Perks.SmallBlunt);
            }
            baseChance += (float)skill * 3.0f;
            if (target instanceof IsoPlayer && !(plyr3 = (IsoPlayer)target).isAnimal() && GameClient.client && !plyr3.isLocalPlayer()) {
                baseChance -= (float)plyr3.remoteStrLvl * 1.5f;
                baseChance = plyr3.getNutrition().getWeight() < 80.0 ? (baseChance += (float)Math.abs((plyr3.getNutrition().getWeight() - 80.0) / 2.0)) : (baseChance -= (float)((plyr3.getNutrition().getWeight() - 80.0) / 2.0));
            }
            baseChance -= (float)(this.getMoodles().getMoodleLevel(MoodleType.ENDURANCE) * 5);
            baseChance -= (float)(this.getMoodles().getMoodleLevel(MoodleType.HEAVY_LOAD) * 5);
            baseChance -= (float)this.getMoodles().getMoodleLevel(MoodleType.PANIC) * 1.3f;
        }
        if (target instanceof IsoZombie) {
            if (SandboxOptions.instance.lore.toughness.getValue() == 1) {
                baseChance -= 6.0f;
            }
            if (SandboxOptions.instance.lore.toughness.getValue() == 3) {
                baseChance += 6.0f;
            }
        }
        return (int)PZMath.clamp(baseChance, 10.0f, 90.0f);
    }

    public boolean isAimControlActive() {
        return this.getVehicle() != null ? this.isAimKeyDown() : this.isAnyAimKeyDown();
    }

    @Override
    public boolean isGettingUp() {
        return this.getCurrentState() == PlayerGetUpState.instance();
    }

    @Override
    public UpdateSchedulerSimulationLevel getMinimumSimulationLevel() {
        return UpdateSchedulerSimulationLevel.FULL;
    }

    @Override
    public boolean allowsTwist() {
        if (this.isAttacking() || this.isKnockedDown() || this.isGettingUp() || this.isPerformingAnAction()) {
            return false;
        }
        if (this.isAiming()) {
            return true;
        }
        if (this.isSneaking()) {
            return false;
        }
        return this.isTwisting();
    }

    @Override
    public Vector2 getInputMoveVector(Vector2 out) {
        super.getInputMoveVector(out);
        if (this.isAnimatingBackwards()) {
            out.x = -out.x;
            out.y = -out.y;
        }
        if (this.isAutoWalk() && out.getLengthSquared() == 0.0f) {
            this.getAutoWalkDirection(out);
        }
        this.setAutoWalkDirection(out);
        return out;
    }

    private void UpdateInputState(InputState inputState) {
        inputState.melee = false;
        inputState.grapple = false;
        if (MPDebugAI.updateInputState(this, inputState)) {
            return;
        }
        boolean bl = inputState.isAttacking = this.isCharging && this.isAttackButtonDown();
        if (this.isMeleeButtonDown() && this.isAuthorizedHandToHandAction()) {
            inputState.melee = true;
            inputState.isAttacking = false;
        }
        if ((this.isInteractButtonDown() || this.getGrapplingTarget() != null && ((IsoGameCharacter)this.getGrapplingTarget()).isOnFire()) && this.isAuthorizedHandToHandAction()) {
            inputState.grapple = true;
            inputState.isAttacking = false;
        }
        inputState.isAiming = this.isAnyAimKeyDown() && StringUtils.isNullOrEmpty(this.getVariableString("BumpFallType"));
        inputState.isCharging = this.isAnyAimKeyDown();
        inputState.setRunning(this.isRunButtonDown());
        if (this.getInputMode() == CharacterInputMode.GAMEPAD) {
            inputState.sprinting = inputState.isRunning() && this.isAllowSprint() && this.isSprintButtonDown();
        } else if (this.isAllowSprint()) {
            if (!Core.getInstance().isOptiondblTapJogToSprint()) {
                if (this.isSprintButtonDown()) {
                    inputState.sprinting = true;
                    this.pressedRunTimer = 1.0f;
                } else {
                    inputState.sprinting = false;
                }
            } else {
                if (!this.wasRunButtonDown() && this.isRunButtonDown() && this.pressedRunTimer < 30.0f && this.pressedRun) {
                    inputState.sprinting = true;
                }
                if (this.wasRunButtonDown() && !this.isRunButtonDown()) {
                    inputState.sprinting = false;
                    this.pressedRun = true;
                }
                if (!inputState.isRunning()) {
                    inputState.sprinting = false;
                }
                if (this.pressedRun) {
                    this.pressedRunTimer += 1.0f;
                }
                if (this.pressedRunTimer > 30.0f) {
                    this.pressedRunTimer = 0.0f;
                    this.pressedRun = false;
                }
            }
        } else {
            inputState.sprinting = false;
        }
        if (inputState.isRunning() || inputState.sprinting) {
            this.setForceAim(false);
        }
        if (this.isForceAim()) {
            inputState.isAiming = true;
            inputState.isCharging = true;
        }
        if (this.isForceRun()) {
            inputState.setRunning(true);
        }
        if (this.isForceSprint()) {
            inputState.sprinting = true;
        }
    }

    public IsoGameCharacter getClosestTo(IsoGameCharacter closestTo) {
        IsoZombie result = null;
        ArrayList<IsoZombie> zeds = new ArrayList<IsoZombie>();
        for (IsoMovingObject obj : IsoWorld.instance.currentCell.getObjectList()) {
            if (obj == closestTo || !(obj instanceof IsoZombie)) continue;
            IsoZombie isoZombie = (IsoZombie)obj;
            zeds.add(isoZombie);
        }
        float currentDist = 0.0f;
        for (int n = 0; n < zeds.size(); ++n) {
            IsoZombie testZed = (IsoZombie)zeds.get(n);
            float dist = IsoUtils.DistanceTo(testZed.getX(), testZed.getY(), closestTo.getX(), closestTo.getY());
            if (result != null && !(dist < currentDist)) continue;
            result = testZed;
            currentDist = dist;
        }
        return result;
    }

    @Override
    public void hitConsequences(HandWeapon weapon, IsoGameCharacter wielder, boolean bIgnoreDamage, float damage, boolean bRemote) {
        if (wielder instanceof IsoAnimal) {
            IsoAnimal isoAnimal = (IsoAnimal)wielder;
            this.getBodyDamage().DamageFromAnimal(isoAnimal);
            this.setKnockedDown(isoAnimal.adef.knockdownAttack);
            this.setHitReaction("HitReaction");
            return;
        }
        Object hitReaction = wielder.getVariableString("ZombieHitReaction");
        if ("Shot".equals(hitReaction)) {
            wielder.setCriticalHit(Rand.Next(100) < ((IsoPlayer)wielder).calculateCritChance(this));
        }
        if (wielder instanceof IsoPlayer && (GameServer.server || GameClient.client)) {
            if (ServerOptions.getInstance().knockedDownAllowed.getValue()) {
                this.setKnockedDown(wielder.isCriticalHit());
            }
        } else {
            this.setKnockedDown(wielder.isCriticalHit());
        }
        if (wielder instanceof IsoPlayer) {
            if (!StringUtils.isNullOrEmpty(this.getHitReaction())) {
                this.getActionContext().reportEvent("washitpvpagain");
            }
            this.getActionContext().reportEvent("washitpvp");
            this.setVariable("hitpvp", true);
        } else {
            this.getActionContext().reportEvent("washit");
        }
        if (bIgnoreDamage) {
            if (GameServer.server) {
                GameServer.addXp((IsoPlayer)wielder, PerkFactory.Perks.Strength, 2.0f);
                return;
            }
            if (!GameClient.client) {
                wielder.xp.AddXP(PerkFactory.Perks.Strength, 2.0f);
            }
            this.setHitForce(Math.min(0.5f, this.getHitForce()));
            this.setHitReaction("HitReaction");
            String dotSide = this.testDotSide(wielder);
            this.setHitFromBehind("BEHIND".equals(dotSide));
            return;
        }
        if (GameClient.client && wielder instanceof IsoPlayer) {
            this.getBodyDamage().splatBloodFloorBig();
        } else {
            this.getBodyDamage().DamageFromWeapon(weapon, -1);
        }
        if ("Bite".equals(hitReaction)) {
            String rightStr = "LEFT";
            String leftStr = "RIGHT";
            String dotSide = this.testDotSide(wielder);
            if (dotSide.equals("RIGHT")) {
                hitReaction = (String)hitReaction + "LEFT";
            }
            if (dotSide.equals("LEFT")) {
                hitReaction = (String)hitReaction + "RIGHT";
            }
            if (hitReaction != null && !"".equals(hitReaction)) {
                this.setHitReaction((String)hitReaction);
            }
        } else if (!this.isKnockedDown()) {
            this.setHitReaction("HitReaction");
        }
    }

    private HandWeapon getWeapon() {
        if (this.getPrimaryHandItem() instanceof HandWeapon) {
            return (HandWeapon)this.getPrimaryHandItem();
        }
        if (this.getSecondaryHandItem() instanceof HandWeapon) {
            return (HandWeapon)this.getSecondaryHandItem();
        }
        return (HandWeapon)InventoryItemFactory.CreateItem("BareHands");
    }

    private void updateMechanicsItems() {
        if (this.mechanicsItem.isEmpty()) {
            return;
        }
        Iterator<Long> it = this.mechanicsItem.keySet().iterator();
        ArrayList<Long> toremove = new ArrayList<Long>();
        while (it.hasNext()) {
            Long item = it.next();
            Long milli = this.mechanicsItem.get(item);
            if (GameTime.getInstance().getCalender().getTimeInMillis() <= milli + 86400000L) continue;
            toremove.add(item);
        }
        for (int i = 0; i < toremove.size(); ++i) {
            this.mechanicsItem.remove(toremove.get(i));
        }
    }

    private void enterExitVehicle() {
        boolean interact = this.isInteractButtonClicked();
        if (interact) {
            this.useVehicle = true;
        }
        if (!this.usedVehicle && this.useVehicle && !interact) {
            this.usedVehicle = true;
            BaseVehicle vehicle = this.getVehicle();
            if (vehicle == null) {
                vehicle = this.getUseableVehicle();
            }
            if (vehicle != null) {
                if (!this.getCharacterActions().isEmpty()) {
                    this.setTimedActionToRetrigger((LuaTimedActionNew)this.getCharacterActions().getFirst());
                }
                LuaEventManager.triggerEvent("OnUseVehicle", this, vehicle);
            }
        }
        if (!interact) {
            this.useVehicle = false;
            this.usedVehicle = false;
        }
    }

    public void checkActionGroup() {
        ActionGroup actionGroup = this.getActionContext().getGroup();
        if (this.getVehicle() == null) {
            ActionGroup playerGroup = ActionGroup.getActionGroup("player");
            if (actionGroup != playerGroup) {
                this.getAdvancedAnimator().OnAnimDataChanged(false);
                this.initializeStates();
                this.getActionContext().setGroup(playerGroup);
                this.clearVariable("bEnteringVehicle");
                this.clearVariable("EnterAnimationFinished");
                this.clearVariable("bExitingVehicle");
                this.clearVariable("ExitAnimationFinished");
                this.clearVariable("bSwitchingSeat");
                this.clearVariable("SwitchSeatAnimationFinished");
                this.setHitReaction("");
            }
        } else {
            ActionGroup vehicleGroup = ActionGroup.getActionGroup("player-vehicle");
            if (actionGroup != vehicleGroup) {
                this.getAdvancedAnimator().OnAnimDataChanged(false);
                this.initializeStates();
                this.getActionContext().setGroup(vehicleGroup);
            }
        }
    }

    public BaseVehicle getUseableVehicle() {
        if (this.getVehicle() != null) {
            return null;
        }
        BaseVehicle bestSeat = null;
        int chunkMinX = (PZMath.fastfloor(this.getX()) - 4) / 8 - 1;
        int chunkMinY = (PZMath.fastfloor(this.getY()) - 4) / 8 - 1;
        int chunkMaxX = (int)Math.ceil((this.getX() + 4.0f) / 8.0f) + 1;
        int chunkMaxY = (int)Math.ceil((this.getY() + 4.0f) / 8.0f) + 1;
        for (int cy = chunkMinY; cy < chunkMaxY; ++cy) {
            for (int cx = chunkMinX; cx < chunkMaxX; ++cx) {
                IsoChunk chunk;
                IsoChunk isoChunk = chunk = GameServer.server ? ServerMap.instance.getChunk(cx, cy) : IsoWorld.instance.currentCell.getChunk(cx, cy);
                if (chunk == null) continue;
                for (int i = 0; i < chunk.vehicles.size(); ++i) {
                    BaseVehicle vehicle = chunk.vehicles.get(i);
                    if (vehicle.getUseablePart(this) != null) {
                        return vehicle;
                    }
                    if (vehicle.getBestSeat(this) == -1 || PolygonalMap2.instance.lineClearCollide(this.getX(), this.getY(), vehicle.getX(), vehicle.getY(), PZMath.fastfloor(this.getZ()), vehicle, false, true) || bestSeat != null && !this.isBetterBestSeat(vehicle, bestSeat)) continue;
                    bestSeat = vehicle;
                }
            }
        }
        return bestSeat;
    }

    private boolean isBetterBestSeat(BaseVehicle vehicle1, BaseVehicle vehicle2) {
        Vector2f intersection = BaseVehicle.allocVector2f();
        if (vehicle1.intersectLineWithPoly(this.getX(), this.getY(), this.getX() + this.getLookDirectionX() * 4.0f, this.getY() + this.getLookDirectionY() * 4.0f, intersection)) {
            BaseVehicle.releaseVector2f(intersection);
            return true;
        }
        if (vehicle2.intersectLineWithPoly(this.getX(), this.getY(), this.getX() + this.getLookDirectionX() * 4.0f, this.getY() + this.getLookDirectionY() * 4.0f, intersection)) {
            BaseVehicle.releaseVector2f(intersection);
            return false;
        }
        BaseVehicle.releaseVector2f(intersection);
        int seat1 = vehicle1.getBestSeat(this);
        float dist1 = vehicle1.getEnterSeatDistance(seat1, this.getX(), this.getY());
        int seat2 = vehicle2.getBestSeat(this);
        float dist2 = vehicle2.getEnterSeatDistance(seat2, this.getX(), this.getY());
        return dist1 < dist2;
    }

    public boolean isNearVehicle() {
        if (this.getVehicle() != null) {
            return false;
        }
        int chunkMinX = (PZMath.fastfloor(this.getX()) - 4) / 8 - 1;
        int chunkMinY = (PZMath.fastfloor(this.getY()) - 4) / 8 - 1;
        int chunkMaxX = (int)Math.ceil((this.getX() + 4.0f) / 8.0f) + 1;
        int chunkMaxY = (int)Math.ceil((this.getY() + 4.0f) / 8.0f) + 1;
        for (int cy = chunkMinY; cy < chunkMaxY; ++cy) {
            for (int cx = chunkMinX; cx < chunkMaxX; ++cx) {
                IsoChunk chunk;
                IsoChunk isoChunk = chunk = GameServer.server ? ServerMap.instance.getChunk(cx, cy) : IsoWorld.instance.currentCell.getChunk(cx, cy);
                if (chunk == null) continue;
                for (int i = 0; i < chunk.vehicles.size(); ++i) {
                    BaseVehicle vehicle = chunk.vehicles.get(i);
                    if (vehicle.getScript() == null || !(vehicle.DistTo(this) < 3.5f)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BaseVehicle getNearVehicle() {
        if (this.getVehicle() != null) {
            return null;
        }
        int chunkMinX = (PZMath.fastfloor(this.getX()) - 4) / 8 - 1;
        int chunkMinY = (PZMath.fastfloor(this.getY()) - 4) / 8 - 1;
        int chunkMaxX = (int)Math.ceil((this.getX() + 4.0f) / 8.0f) + 1;
        int chunkMaxY = (int)Math.ceil((this.getY() + 4.0f) / 8.0f) + 1;
        for (int cy = chunkMinY; cy < chunkMaxY; ++cy) {
            for (int cx = chunkMinX; cx < chunkMaxX; ++cx) {
                IsoChunk chunk;
                IsoChunk isoChunk = chunk = GameServer.server ? ServerMap.instance.getChunk(cx, cy) : IsoWorld.instance.currentCell.getChunk(cx, cy);
                if (chunk == null) continue;
                for (int i = 0; i < chunk.vehicles.size(); ++i) {
                    BaseVehicle vehicle = chunk.vehicles.get(i);
                    if (vehicle.getScript() == null || PZMath.fastfloor(this.getZ()) != PZMath.fastfloor(vehicle.getZ()) || this.isLocalPlayer() && vehicle.getTargetAlpha(this.playerIndex) == 0.0f || this.DistToSquared(vehicle) >= 16.0f || !PolygonalMap2.instance.intersectLineWithVehicle(this.getX(), this.getY(), this.getX() + this.getForwardDirectionX() * 4.0f, this.getY() + this.getForwardDirectionY() * 4.0f, vehicle, tempVector2) || PolygonalMap2.instance.lineClearCollide(this.getX(), this.getY(), IsoPlayer.tempVector2.x, IsoPlayer.tempVector2.y, PZMath.fastfloor(this.getZ()), vehicle, false, true)) continue;
                    return vehicle;
                }
            }
        }
        return null;
    }

    private void updateWhileInVehicle() {
        ActionGroup vehicleGroup;
        this.lookingWhileInVehicle = false;
        ActionGroup actionGroup = this.getActionContext().getGroup();
        if (actionGroup != (vehicleGroup = ActionGroup.getActionGroup("player-vehicle"))) {
            this.getAdvancedAnimator().OnAnimDataChanged(false);
            this.initializeStates();
            this.getActionContext().setGroup(vehicleGroup);
        }
        if (GameClient.client && this.getVehicle().getSeat(this) == -1) {
            DebugType.DetailedInfo.trace("forced " + this.getUsername() + " out of vehicle seat -1");
            this.setVehicle(null);
            return;
        }
        this.dirtyRecalcGridStackTime = 10.0f;
        if (this.getVehicle().isDriver(this)) {
            WeaponType wpn;
            this.getVehicle().updatePhysics();
            boolean haveControl = true;
            if (this.isAiming() && (wpn = WeaponType.getWeaponType(this)) == WeaponType.FIREARM) {
                haveControl = false;
            }
            if (this.getVariableBoolean("isLoading")) {
                haveControl = false;
            }
            if (haveControl) {
                this.getVehicle().updateControls();
            }
        } else {
            BaseVehicle vehicleObject = this.getVehicle();
            if (vehicleObject.getDriver() == null && vehicleObject.getEngineSpeed() > (double)vehicleObject.getScript().getEngineIdleSpeed()) {
                vehicleObject.setEngineSpeed(Math.max(vehicleObject.getEngineSpeed() - (double)(50.0f * (GameTime.getInstance().getMultiplier() / 0.8f)), (double)vehicleObject.getScript().getEngineIdleSpeed()));
            }
            if (GameClient.connection != null) {
                PassengerMap.updatePassenger(this);
            }
        }
        this.fallTime = 0.0f;
        this.seenThisFrame = false;
        this.couldBeSeenThisFrame = false;
        this.closestZombie = 1000000.0f;
        this.updateAimingDelay();
        this.setBeenMovingFor(this.getBeenMovingFor() - 0.625f * GameTime.getInstance().getMultiplier());
        this.updateEnduranceWhileInVehicle();
        if (this.vehicle != null) {
            VehicleWindow window;
            AnimationPlayer animPlayer;
            Vector3f vec = this.vehicle.getForwardVector(tempVector3f);
            boolean bAimControlActive = this.isAimControlActive();
            this.setIsAiming(bAimControlActive);
            if (!bAimControlActive && this.isCurrentState(IdleState.instance())) {
                this.setForwardDirection(vec.x, vec.z);
            }
            if (this.lastAngle.x != this.getForwardDirectionX() || this.lastAngle.y != this.getForwardDirectionY()) {
                this.dirtyRecalcGridStackTime = 10.0f;
            }
            if ((animPlayer = this.getAnimationPlayer()) != null && animPlayer.isReady()) {
                animPlayer.setTargetAndCurrentDirection(this.getForwardDirectionX(), this.getForwardDirectionY());
            }
            boolean canAttack = false;
            int seatpos = this.vehicle.getSeat(this);
            VehiclePart door = this.vehicle.getPassengerDoor(seatpos);
            if (door != null && (window = door.findWindow()) != null && !window.isHittable()) {
                canAttack = true;
            }
            if (canAttack) {
                this.attackWhileInVehicle();
            } else if (bAimControlActive) {
                this.lookingWhileInVehicle = true;
                this.setAngleFromAim();
            } else {
                this.setIsAiming(false);
            }
        }
        this.updateCursorVisibility();
    }

    private void attackWhileInVehicle() {
        this.setIsAiming(false);
        boolean isAttacking = false;
        boolean bMelee = false;
        boolean aimKey = this.isAimControlActive();
        this.setIsAiming(aimKey);
        this.isCharging = aimKey;
        if (this.isForceAim()) {
            this.setIsAiming(true);
            this.isCharging = true;
        }
        if (this.isMeleeButtonDown() && this.isAuthorizedHandToHandAction()) {
            bMelee = true;
        } else {
            boolean bl = isAttacking = this.isCharging && this.isAttackButtonDown();
            if (isAttacking) {
                this.setIsAiming(true);
            }
        }
        if (!this.isCharging && !this.isChargingLt) {
            this.chargeTime = 0.0f;
        }
        if (!this.isAiming() || this.bannedAttacking || !this.CanAttack()) {
            return;
        }
        this.chargeTime += GameTime.instance.getMultiplier();
        this.useChargeTime = this.chargeTime;
        this.meleePressed = bMelee;
        this.setAngleFromAim();
        if (bMelee) {
            this.sprite.animate = true;
            this.setDoShove(true);
            this.AttemptAttack(this.useChargeTime);
            this.useChargeTime = 0.0f;
            this.chargeTime = 0.0f;
        } else if (isAttacking) {
            this.sprite.animate = true;
            if (this.getRecoilDelay() <= 0.0f) {
                this.AttemptAttack(this.useChargeTime);
            }
            this.useChargeTime = 0.0f;
            this.chargeTime = 0.0f;
        }
    }

    public void setAngleFromAim() {
        boolean ballisticsSuccess;
        Vector2 desiredForward2f = this.calculateAimVector(this.updateAimingVectorParams.desiredForward2f);
        if (desiredForward2f.getLengthSquared() <= 0.0f) {
            return;
        }
        BallisticsController ballisticsController = this.getBallisticsController();
        boolean bl = ballisticsSuccess = ballisticsController != null && ballisticsController.updateAimingVector(this, this.updateAimingVectorParams);
        if (ballisticsSuccess) {
            this.setTargetVerticalAimAngle(this.updateAimingVectorParams.desiredForwardPitchRads * 57.295776f);
        } else {
            this.setTargetVerticalAimAngle(0.0f);
        }
        this.setForwardDirection(desiredForward2f);
        if (this.lastAngle.x != desiredForward2f.x || this.lastAngle.y != desiredForward2f.y) {
            this.lastAngle.x = desiredForward2f.x;
            this.lastAngle.y = desiredForward2f.y;
            this.dirtyRecalcGridStackTime = 10.0f;
        }
    }

    private void updateTorchStrength() {
        if (this.getTorchStrength() > 0.0f || this.flickTorch) {
            InventoryItem inventoryItem = this.getActiveLightItem();
            if (!(inventoryItem instanceof DrainableComboItem)) {
                return;
            }
            DrainableComboItem torch = (DrainableComboItem)inventoryItem;
            this.flickTorch = false;
            if (this.flickTorch) {
                torch.setActivated(Rand.Next(6) != 0);
                if (Rand.Next(40) == 0) {
                    this.flickTorch = false;
                    torch.setActivated(true);
                }
            }
        }
    }

    public void calculateContext() {
        float cx = this.getX();
        float cy = this.getY();
        float cz = this.getX();
        IsoGridSquare[] sqs = new IsoGridSquare[4];
        if (this.getForwardIsoDirection() == IsoDirections.N) {
            sqs[2] = this.getCell().getGridSquare(cx - 1.0f, cy - 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx, cy - 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx + 1.0f, cy - 1.0f, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.NE) {
            sqs[2] = this.getCell().getGridSquare(cx, cy - 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx + 1.0f, cy - 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx + 1.0f, cy, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.E) {
            sqs[2] = this.getCell().getGridSquare(cx + 1.0f, cy - 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx + 1.0f, cy, cz);
            sqs[3] = this.getCell().getGridSquare(cx + 1.0f, cy + 1.0f, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.SE) {
            sqs[2] = this.getCell().getGridSquare(cx + 1.0f, cy, cz);
            sqs[1] = this.getCell().getGridSquare(cx + 1.0f, cy + 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx, cy + 1.0f, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.S) {
            sqs[2] = this.getCell().getGridSquare(cx + 1.0f, cy + 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx, cy + 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx - 1.0f, cy + 1.0f, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.SW) {
            sqs[2] = this.getCell().getGridSquare(cx, cy + 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx - 1.0f, cy + 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx - 1.0f, cy, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.W) {
            sqs[2] = this.getCell().getGridSquare(cx - 1.0f, cy + 1.0f, cz);
            sqs[1] = this.getCell().getGridSquare(cx - 1.0f, cy, cz);
            sqs[3] = this.getCell().getGridSquare(cx - 1.0f, cy - 1.0f, cz);
        } else if (this.getForwardIsoDirection() == IsoDirections.NW) {
            sqs[2] = this.getCell().getGridSquare(cx - 1.0f, cy, cz);
            sqs[1] = this.getCell().getGridSquare(cx - 1.0f, cy - 1.0f, cz);
            sqs[3] = this.getCell().getGridSquare(cx, cy - 1.0f, cz);
        }
        sqs[0] = this.current;
        for (int n = 0; n < 4; ++n) {
            IsoGridSquare test = sqs[n];
            if (test != null) continue;
        }
    }

    public boolean isSafeToClimbOver(IsoDirections dir) {
        IsoGridSquare sq;
        switch (dir) {
            case N: {
                sq = this.getCell().getGridSquare(this.getXi(), this.getYi() - 1, this.getZi());
                break;
            }
            case S: {
                sq = this.getCell().getGridSquare(this.getXi(), this.getYi() + 1, this.getZi());
                break;
            }
            case W: {
                sq = this.getCell().getGridSquare(this.getXi() - 1, this.getYi(), this.getZi());
                break;
            }
            case E: {
                sq = this.getCell().getGridSquare(this.getXi() + 1, this.getYi(), this.getZi());
                break;
            }
            default: {
                return false;
            }
        }
        if (sq == null) {
            return false;
        }
        if (sq.has(IsoFlagType.water)) {
            return false;
        }
        if (!sq.TreatAsSolidFloor()) {
            return sq.HasStairsBelow();
        }
        return true;
    }

    public boolean canPlaceCorpseOnSquare(IsoGridSquare square) {
        return square != null && square.TreatAsSolidFloor() && !square.has(IsoFlagType.water) && !square.isSolid() && !square.isSolidTrans();
    }

    public boolean canThrowCorpseOver(IsoGridSquare fromSq, IsoDirections dir) {
        IsoGridSquare toSq;
        if (fromSq == null) {
            return false;
        }
        switch (dir) {
            case N: 
            case W: 
            case S: 
            case E: {
                toSq = fromSq.getAdjacentSquare(dir);
                break;
            }
            default: {
                return false;
            }
        }
        if (toSq == null || fromSq.getHoppableTo(toSq) == null || fromSq.getWindowFrameTo(toSq) != null || fromSq.getWindowThumpableTo(toSq) != null) {
            return false;
        }
        return this.canPlaceCorpseOnSquare(toSq) || this.canPlaceCorpseOnSquare(toSq.getFloorSquareBelow());
    }

    public boolean canThrowCorpseOver(IsoDirections dir) {
        return this.canThrowCorpseOver(this.getCurrentSquare(), dir);
    }

    private void addContextualAction(ContextualAction.Action action, IsoDirections dir, IsoGridSquare square, IsoObject object) {
        ContextualAction newAction = ContextualAction.alloc(action, dir, square, object);
        this.contextualActions.add(newAction);
    }

    private void addContextualAction(ContextualAction.Action action, Invokers.Params1.ICallback<ContextualAction> populator) {
        ContextualAction newAction = ContextualAction.alloc(action, populator);
        this.contextualActions.add(newAction);
    }

    private ContextualAction pickBestContextualAction(ArrayList<ContextualAction> contextualActions) {
        ContextualAction best = null;
        ContextualAction bestBehind = null;
        int maxPriority = -1;
        int maxPriorityBehind = -1;
        for (int i = 0; i < contextualActions.size(); ++i) {
            ContextualAction ca = contextualActions.get(i);
            if (ca.behind) {
                if (ca.priority <= maxPriorityBehind) continue;
                bestBehind = ca;
                maxPriorityBehind = ca.priority;
                continue;
            }
            if (ca.priority <= maxPriority) continue;
            best = ca;
            maxPriority = ca.priority;
        }
        if (best == null) {
            return bestBehind;
        }
        if (bestBehind != null && bestBehind.priority > best.priority) {
            return bestBehind;
        }
        return best;
    }

    private void performContextualAction(ContextualAction ca) {
        DebugType.Context.debugln("Action: %s", ca);
        switch (ca.action) {
            case NONE: {
                break;
            }
            case AnimalInteraction: {
                this.triggerContextualAction("AnimalsInteraction", ca.object);
                break;
            }
            case ClimbOverFence: {
                this.triggerContextualAction("ClimbOverFence", ca.object, (Object)ca.dir);
                break;
            }
            case ClimbOverWall: {
                this.climbOverWall(ca.dir);
                break;
            }
            case ClimbSheetRope: {
                this.triggerContextualAction("ClimbSheetRope", ca.square, false);
                break;
            }
            case ClimbThroughWindow: {
                this.triggerContextualAction("ClimbThroughWindow", ca.object);
                break;
            }
            case OpenButcherHook: {
                this.triggerContextualAction("OpenButcherHook", ca.object);
                break;
            }
            case OpenHutch: {
                this.triggerContextualAction("OpenHutch", ca.object);
                break;
            }
            case RestOnFurniture: {
                this.triggerContextualAction("SleepInBed", ca.object);
                break;
            }
            case ThrowGrappledTargetOutWindow: {
                this.throwGrappledTargetOutWindow(ca.object);
                break;
            }
            case ThrowGrappledOverFence: {
                this.throwGrappledOverFence(ca.object, ca.dir);
                break;
            }
            case ThrowGrappledIntoContainer: {
                this.throwGrappledIntoInventory(ca.targetContainer);
                break;
            }
            case ToggleCurtain: {
                IsoObject isoObject = ca.object;
                if (isoObject instanceof IsoCurtain) {
                    IsoCurtain curtain = (IsoCurtain)isoObject;
                    this.triggerContextualAction(curtain.IsOpen() ? "CloseCurtain" : "OpenCurtain", curtain);
                    break;
                }
                isoObject = ca.object;
                if (!(isoObject instanceof IsoDoor)) break;
                IsoDoor door = (IsoDoor)isoObject;
                door.toggleCurtain();
                break;
            }
            case ToggleDoor: {
                IsoObject isoObject = ca.object;
                if (isoObject instanceof IsoDoor) {
                    IsoDoor door = (IsoDoor)isoObject;
                    door.ToggleDoor(this);
                    break;
                }
                isoObject = ca.object;
                if (!(isoObject instanceof IsoThumpable)) break;
                IsoThumpable door = (IsoThumpable)isoObject;
                door.ToggleDoor(this);
                break;
            }
            case ToggleWindow: {
                IsoObject isoObject = ca.object;
                if (!(isoObject instanceof IsoWindow)) break;
                IsoWindow window = (IsoWindow)isoObject;
                this.triggerContextualAction(window.IsOpen() ? "CloseWindow" : "OpenWindow", window);
            }
        }
    }

    public boolean doContext() {
        IsoObject obj;
        IsoDirections oppositeDir1;
        if (this.isIgnoreContextKey()) {
            return false;
        }
        if (this.isBlockMovement()) {
            return false;
        }
        if (this.getWrappedGrappleable().isPickingUpBody() || this.getWrappedGrappleable().isPuttingDownBody()) {
            return false;
        }
        if (!this.getCharacterActions().isEmpty()) {
            this.setTimedActionToRetrigger((LuaTimedActionNew)this.getCharacterActions().get(0));
        }
        if (this.isSittingOnFurniture() || this.isSitOnGround()) {
            LuaEventManager.triggerEvent("OnContextKey", this, BoxedStaticValues.toDouble(PZMath.fastfloor(this.timePressedContext * 1000.0f)));
            return true;
        }
        for (BaseVehicle vehicle : this.getCell().vehicles) {
            if (vehicle.getUseablePart(this) == null) continue;
            return false;
        }
        ContextualAction.releaseAll(this.contextualActions);
        float lx = this.getX() - (float)PZMath.fastfloor(this.getX());
        float ly = this.getY() - (float)PZMath.fastfloor(this.getY());
        IsoDirections oppositeDir2 = null;
        IsoDirections dir = this.getForwardIsoDirection();
        if (this.current == null || this.current.getAdjacentSquare(dir) == null) {
            return false;
        }
        if (dir == IsoDirections.NW) {
            if (ly < lx) {
                this.doContextNSWE(IsoDirections.N);
                this.doContextNSWE(IsoDirections.W);
                oppositeDir1 = IsoDirections.S;
                oppositeDir2 = IsoDirections.E;
            } else {
                this.doContextNSWE(IsoDirections.W);
                this.doContextNSWE(IsoDirections.N);
                oppositeDir1 = IsoDirections.E;
                oppositeDir2 = IsoDirections.S;
            }
        } else if (dir == IsoDirections.NE) {
            if (ly < (lx = 1.0f - lx)) {
                this.doContextNSWE(IsoDirections.N);
                this.doContextNSWE(IsoDirections.E);
                oppositeDir1 = IsoDirections.S;
                oppositeDir2 = IsoDirections.W;
            } else {
                this.doContextNSWE(IsoDirections.E);
                this.doContextNSWE(IsoDirections.N);
                oppositeDir1 = IsoDirections.W;
                oppositeDir2 = IsoDirections.S;
            }
        } else if (dir == IsoDirections.SE) {
            if ((ly = 1.0f - ly) < (lx = 1.0f - lx)) {
                this.doContextNSWE(IsoDirections.S);
                this.doContextNSWE(IsoDirections.E);
                oppositeDir1 = IsoDirections.N;
                oppositeDir2 = IsoDirections.W;
            } else {
                this.doContextNSWE(IsoDirections.E);
                this.doContextNSWE(IsoDirections.S);
                oppositeDir1 = IsoDirections.W;
                oppositeDir2 = IsoDirections.N;
            }
        } else if (dir == IsoDirections.SW) {
            if ((ly = 1.0f - ly) < lx) {
                this.doContextNSWE(IsoDirections.S);
                this.doContextNSWE(IsoDirections.W);
                oppositeDir1 = IsoDirections.N;
                oppositeDir2 = IsoDirections.E;
            } else {
                this.doContextNSWE(IsoDirections.W);
                this.doContextNSWE(IsoDirections.S);
                oppositeDir1 = IsoDirections.E;
                oppositeDir2 = IsoDirections.N;
            }
        } else {
            this.doContextNSWE(dir);
            oppositeDir1 = dir.Rot180();
        }
        if (this.contextualActions.isEmpty() && dir.dx() != 0 && dir.dy() != 0) {
            this.doContextCorners(dir);
        }
        int count = this.contextualActions.size();
        if (oppositeDir1 != null && (obj = this.getContextDoorOrWindowOrWindowFrame(oppositeDir1)) != null) {
            this.doContextDoorOrWindowOrWindowFrame(oppositeDir1, obj);
        }
        if (oppositeDir2 != null && (obj = this.getContextDoorOrWindowOrWindowFrame(oppositeDir2)) != null) {
            this.doContextDoorOrWindowOrWindowFrame(oppositeDir2, obj);
        }
        for (int i = count; i < this.contextualActions.size(); ++i) {
            this.contextualActions.get((int)i).behind = true;
        }
        if (this.contextualActions.isEmpty()) {
            LuaEventManager.triggerEvent("OnContextKey", this, BoxedStaticValues.toDouble(PZMath.fastfloor(this.timePressedContext * 1000.0f)));
            return false;
        }
        ContextualAction ca = this.pickBestContextualAction(this.contextualActions);
        this.performContextualAction(ca);
        return true;
    }

    private boolean doContextNSWE(IsoDirections assumedDir) {
        assert (assumedDir == IsoDirections.N || assumedDir == IsoDirections.S || assumedDir == IsoDirections.W || assumedDir == IsoDirections.E);
        if (this.current == null || this.current.getAdjacentSquare(assumedDir) == null) {
            return false;
        }
        if (this.doContextClimbSheetRope(assumedDir)) {
            return true;
        }
        IsoObject o = this.getContextDoorOrWindowOrWindowFrame(assumedDir);
        if (o != null) {
            this.doContextDoorOrWindowOrWindowFrame(assumedDir, o);
            return true;
        }
        if (!this.isShiftKeyDown()) {
            if (this.doContextHutch(assumedDir)) {
                return true;
            }
            if (this.doContextButcherHook(assumedDir)) {
                return true;
            }
            if (this.doContextAnimalInteraction(assumedDir)) {
                return true;
            }
            if (this.doContextRestOnFurniture(assumedDir)) {
                return true;
            }
        }
        if (this.isShiftKeyDown() && this.current != null && this.ticksSincePressedMovement > 15.0f) {
            if (this.doContextHutch(assumedDir)) {
                return true;
            }
            if (this.doContextToggleCurtain(assumedDir)) {
                return true;
            }
        }
        if (this.doContextThrowGrappledTargetOverFence(assumedDir)) {
            return true;
        }
        if (this.doContextThrowGrappledTargetIntoInventory(assumedDir)) {
            return true;
        }
        if (this.doContextHopOverFence(assumedDir)) {
            return true;
        }
        return this.doContextClimbOverWall(assumedDir);
    }

    private boolean doContextRestOnFurniture(IsoDirections assumedDir) {
        if (this.isGrappling()) {
            return false;
        }
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        IsoObject bed = adjacent.getBed();
        if (bed == null) {
            bed = this.current.getBed();
        }
        if (bed != null) {
            this.addContextualAction(ContextualAction.Action.RestOnFurniture, assumedDir, bed.getSquare(), bed);
            return true;
        }
        return false;
    }

    private boolean doContextAnimalInteraction(IsoDirections assumedDir) {
        if (this.isGrappling()) {
            return false;
        }
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        ArrayList<IsoAnimal> animals = adjacent.getAnimals();
        if (animals.isEmpty()) {
            animals = this.current.getAnimals();
        }
        if (!animals.isEmpty()) {
            this.addContextualAction(ContextualAction.Action.AnimalInteraction, assumedDir, animals.get(0).getCurrentSquare(), animals.get(0));
            return true;
        }
        return false;
    }

    private boolean doContextButcherHook(IsoDirections assumedDir) {
        if (this.isGrappling()) {
            return false;
        }
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        IsoButcherHook hook = adjacent.getButcherHook();
        if (hook == null) {
            hook = this.current.getButcherHook();
        }
        if (hook != null) {
            this.addContextualAction(ContextualAction.Action.OpenButcherHook, assumedDir, hook.getSquare(), hook);
            return true;
        }
        return false;
    }

    private boolean doContextHutch(IsoDirections assumedDir) {
        if (this.isGrappling()) {
            return false;
        }
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        IsoHutch hutch = adjacent.getHutch();
        if (hutch == null) {
            hutch = this.current.getHutch();
        }
        if (hutch != null) {
            this.addContextualAction(ContextualAction.Action.OpenHutch, assumedDir, hutch.getSquare(), hutch);
            return true;
        }
        return false;
    }

    private boolean doContextToggleCurtain(IsoDirections assumedDir) {
        IsoDoor isoDoor;
        IsoGridSquare checkForDoorsAndWindows1;
        IsoDoor door;
        IsoDoor isoDoor2;
        if (this.isGrappling()) {
            return false;
        }
        IsoObject doorN = this.current.getDoor(true);
        if (doorN instanceof IsoDoor && (isoDoor2 = (IsoDoor)doorN).isFacingSheet(this)) {
            this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, doorN.getSquare(), doorN);
            return true;
        }
        IsoObject doorW = this.current.getDoor(false);
        if (doorW instanceof IsoDoor && (door = (IsoDoor)doorW).isFacingSheet(this)) {
            this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, doorW.getSquare(), doorW);
            return true;
        }
        if (assumedDir == IsoDirections.E) {
            IsoObject doorE;
            checkForDoorsAndWindows1 = IsoWorld.instance.currentCell.getGridSquare(this.getX() + 1.0f, this.getY(), this.getZ());
            IsoObject isoObject = doorE = checkForDoorsAndWindows1 != null ? checkForDoorsAndWindows1.getDoor(true) : null;
            if (doorE instanceof IsoDoor && (isoDoor = (IsoDoor)doorE).isFacingSheet(this)) {
                this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, doorE.getSquare(), doorE);
                return true;
            }
        }
        if (assumedDir == IsoDirections.S) {
            IsoObject doorS;
            checkForDoorsAndWindows1 = IsoWorld.instance.currentCell.getGridSquare(this.getX(), this.getY() + 1.0f, this.getZ());
            IsoObject isoObject = doorS = checkForDoorsAndWindows1 != null ? checkForDoorsAndWindows1.getDoor(false) : null;
            if (doorS instanceof IsoDoor && (isoDoor = (IsoDoor)doorS).isFacingSheet(this)) {
                this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, doorS.getSquare(), doorS);
                return true;
            }
        }
        return false;
    }

    private boolean doContextClimbSheetRope(IsoDirections assumedDir) {
        if (this.isGrappling()) {
            return false;
        }
        if (assumedDir == IsoDirections.N && this.current.has(IsoFlagType.climbSheetN) && this.canClimbSheetRope(this.current)) {
            this.addContextualAction(ContextualAction.Action.ClimbSheetRope, assumedDir, this.current, null);
            return true;
        }
        if (assumedDir == IsoDirections.S && this.current.has(IsoFlagType.climbSheetS) && this.canClimbSheetRope(this.current)) {
            this.addContextualAction(ContextualAction.Action.ClimbSheetRope, assumedDir, this.current, null);
            return true;
        }
        if (assumedDir == IsoDirections.W && this.current.has(IsoFlagType.climbSheetW) && this.canClimbSheetRope(this.current)) {
            this.addContextualAction(ContextualAction.Action.ClimbSheetRope, assumedDir, this.current, null);
            return true;
        }
        if (assumedDir == IsoDirections.E && this.current.has(IsoFlagType.climbSheetE) && this.canClimbSheetRope(this.current)) {
            this.addContextualAction(ContextualAction.Action.ClimbSheetRope, assumedDir, this.current, null);
            return true;
        }
        return false;
    }

    private boolean doContextHopOverFence(IsoDirections assumedDir) {
        if (this.ignoreAutoVault) {
            return false;
        }
        if (this.isGrappling()) {
            return false;
        }
        boolean bSafe = this.isSafeToClimbOver(assumedDir);
        if (this.timePressedContext < 0.5f && !bSafe) {
            return false;
        }
        if (assumedDir == IsoDirections.N && this.getCurrentSquare().has(IsoFlagType.HoppableN)) {
            this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, this.getCurrentSquare(), this.getCurrentSquare().getHoppable(true));
            return true;
        }
        if (assumedDir == IsoDirections.W && this.getCurrentSquare().has(IsoFlagType.HoppableW)) {
            this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, this.getCurrentSquare(), this.getCurrentSquare().getHoppable(false));
            return true;
        }
        IsoGridSquare squareS = this.getCurrentSquare().getAdjacentSquare(IsoDirections.S);
        if (assumedDir == IsoDirections.S && squareS != null && squareS.has(IsoFlagType.HoppableN)) {
            this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, squareS, squareS.getHoppable(true));
            return true;
        }
        IsoGridSquare squareE = this.getCurrentSquare().getAdjacentSquare(IsoDirections.E);
        if (assumedDir == IsoDirections.E && squareE != null && squareE.has(IsoFlagType.HoppableW)) {
            this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, squareE, squareE.getHoppable(false));
            return true;
        }
        return false;
    }

    private boolean doContextThrowGrappledTargetOverFence(IsoDirections assumedDir) {
        if (!this.isGrappling()) {
            return false;
        }
        boolean bSafe = this.isSafeToClimbOver(assumedDir);
        if (this.timePressedContext < 0.5f && !bSafe) {
            return false;
        }
        if (assumedDir == IsoDirections.S && this.getCurrentSquare().has(IsoFlagType.HoppableN)) {
            return this.doContextThrowGrappledTargetOverFence(this.getCurrentSquare(), assumedDir, this.getCurrentSquare().getHoppable(true));
        }
        if (assumedDir == IsoDirections.E && this.getCurrentSquare().has(IsoFlagType.HoppableW)) {
            return this.doContextThrowGrappledTargetOverFence(this.getCurrentSquare(), assumedDir, this.getCurrentSquare().getHoppable(false));
        }
        IsoGridSquare squareS = this.getCurrentSquare().getAdjacentSquare(IsoDirections.S);
        if (assumedDir == IsoDirections.N && squareS != null && squareS.has(IsoFlagType.HoppableN)) {
            return this.doContextThrowGrappledTargetOverFence(squareS, assumedDir, squareS.getHoppable(true));
        }
        IsoGridSquare squareE = this.getCurrentSquare().getAdjacentSquare(IsoDirections.E);
        if (assumedDir == IsoDirections.W && squareE != null && squareE.has(IsoFlagType.HoppableW)) {
            return this.doContextThrowGrappledTargetOverFence(squareE, assumedDir, squareE.getHoppable(false));
        }
        return false;
    }

    private boolean doContextCorners(IsoDirections assumedDir) {
        if (!this.isShiftKeyDown()) {
            if (this.doContextHutch(assumedDir)) {
                return true;
            }
            if (this.doContextButcherHook(assumedDir)) {
                return true;
            }
            if (this.doContextAnimalInteraction(assumedDir)) {
                return true;
            }
            if (this.doContextRestOnFurniture(assumedDir)) {
                return true;
            }
        }
        return this.isShiftKeyDown() && this.current != null && this.ticksSincePressedMovement > 15.0f && this.doContextHutch(assumedDir);
    }

    public IsoObject getContextDoorOrWindowOrWindowFrame(IsoDirections assumedDir) {
        if (this.current == null || assumedDir == null) {
            return null;
        }
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        IsoObject obj = null;
        switch (assumedDir) {
            case N: {
                obj = this.current.getOpenDoor(assumedDir);
                if (obj != null) {
                    return obj;
                }
                obj = this.current.getDoorOrWindowOrWindowFrame(assumedDir, true);
                if (obj != null) {
                    return obj;
                }
                obj = this.current.getDoor(true);
                if (obj != null) {
                    return obj;
                }
                if (adjacent == null || this.current.isBlockedTo(adjacent)) break;
                obj = adjacent.getOpenDoor(IsoDirections.S);
                break;
            }
            case S: {
                obj = this.current.getOpenDoor(assumedDir);
                if (obj != null) {
                    return obj;
                }
                if (adjacent == null) break;
                boolean ignoreOpen = this.current.isBlockedTo(adjacent);
                obj = adjacent.getDoorOrWindowOrWindowFrame(IsoDirections.N, ignoreOpen);
                if (obj != null) {
                    return obj;
                }
                obj = adjacent.getDoor(true);
                break;
            }
            case W: {
                obj = this.current.getOpenDoor(assumedDir);
                if (obj != null) {
                    return obj;
                }
                obj = this.current.getDoorOrWindowOrWindowFrame(assumedDir, true);
                if (obj != null) {
                    return obj;
                }
                obj = this.current.getDoor(false);
                if (obj != null) {
                    return obj;
                }
                if (adjacent == null || this.current.isBlockedTo(adjacent)) break;
                obj = adjacent.getOpenDoor(IsoDirections.E);
                break;
            }
            case E: {
                obj = this.current.getOpenDoor(assumedDir);
                if (obj != null) {
                    return obj;
                }
                if (adjacent == null) break;
                boolean ignoreOpen1 = this.current.isBlockedTo(adjacent);
                obj = adjacent.getDoorOrWindowOrWindowFrame(IsoDirections.W, ignoreOpen1);
                if (obj != null) {
                    return obj;
                }
                obj = adjacent.getDoor(false);
            }
        }
        return obj;
    }

    private boolean doContextDoorOrWindowOrWindowFrame(IsoDirections assumedDir, IsoObject o) {
        IsoThumpable thumpable;
        boolean bTopOfSheetRope;
        IsoGridSquare adjacent = this.current.getAdjacentSquare(assumedDir);
        boolean bl = bTopOfSheetRope = IsoWindow.isTopOfSheetRopeHere(adjacent) && this.canClimbDownSheetRope(adjacent);
        if (o instanceof IsoDoor) {
            IsoDoor isoDoor = (IsoDoor)o;
            return this.doContextDoor(assumedDir, isoDoor);
        }
        if (o instanceof IsoThumpable && (thumpable = (IsoThumpable)o).isDoor()) {
            return this.doContextThumpableDoor(assumedDir, thumpable);
        }
        if (o instanceof IsoWindow) {
            IsoWindow isoWindow = (IsoWindow)o;
            if (!o.getSquare().getProperties().has(IsoFlagType.makeWindowInvincible)) {
                return this.doContextWindow(assumedDir, isoWindow, bTopOfSheetRope);
            }
        }
        if (o instanceof IsoThumpable) {
            IsoThumpable isoThumpable = (IsoThumpable)o;
            if (!o.getSquare().getProperties().has(IsoFlagType.makeWindowInvincible)) {
                return this.doContextThumpableWindow(assumedDir, isoThumpable, bTopOfSheetRope);
            }
        }
        if (o instanceof IsoWindowFrame) {
            IsoWindowFrame windowFrame = (IsoWindowFrame)o;
            return this.doContextWindowFrame(assumedDir, windowFrame, bTopOfSheetRope);
        }
        return false;
    }

    private boolean doContextWindowFrame(IsoDirections assumedDir, IsoWindowFrame o, boolean bTopOfSheetRope) {
        if (this.isShiftKeyDown()) {
            IsoCurtain curtain;
            if (!this.isGrappling() && (curtain = o.getCurtain()) != null && this.current != null && !curtain.getSquare().isBlockedTo(this.current)) {
                this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, curtain.getSquare(), curtain);
                return true;
            }
        } else if ((this.timePressedContext >= 0.5f || this.isSafeToClimbOver(assumedDir) || bTopOfSheetRope) && o.canClimbThrough(this)) {
            if (this.doContextThrowGrappledTargetOutWindow(assumedDir, o)) {
                return true;
            }
            if (!this.isGrappling()) {
                this.addContextualAction(ContextualAction.Action.ClimbThroughWindow, assumedDir, o.getSquare(), o);
                return true;
            }
        }
        return false;
    }

    private boolean doContextThumpableWindow(IsoDirections assumedDir, IsoThumpable d, boolean bTopOfSheetRope) {
        if (this.isShiftKeyDown()) {
            IsoCurtain curtain;
            if (!this.isGrappling() && (curtain = d.HasCurtains()) != null && this.current != null && !curtain.getSquare().isBlockedTo(this.current)) {
                this.triggerContextualAction(curtain.IsOpen() ? "CloseCurtain" : "OpenCurtain", curtain);
                return true;
            }
        } else if (this.timePressedContext >= 0.5f) {
            if (d.canClimbThrough(this)) {
                if (this.doContextThrowGrappledTargetOutWindow(assumedDir, d)) {
                    return true;
                }
                if (!this.isGrappling()) {
                    this.addContextualAction(ContextualAction.Action.ClimbThroughWindow, assumedDir, d.getSquare(), d);
                    return true;
                }
            }
        } else if (d.canClimbThrough(this)) {
            if (this.doContextThrowGrappledTargetOutWindow(assumedDir, d)) {
                return true;
            }
            if (!this.isGrappling() && (this.isSafeToClimbOver(assumedDir) || d.getSquare().haveSheetRope || bTopOfSheetRope)) {
                this.addContextualAction(ContextualAction.Action.ClimbThroughWindow, assumedDir, d.getSquare(), d);
                return true;
            }
        }
        return false;
    }

    private boolean doContextWindow(IsoDirections assumedDir, IsoWindow d, boolean bTopOfSheetRope) {
        if (this.isShiftKeyDown()) {
            IsoCurtain curtain = d.HasCurtains();
            if (curtain != null && this.current != null && !curtain.getSquare().isBlockedTo(this.current) && !this.isGrappling()) {
                this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, curtain.getSquare(), curtain);
                return true;
            }
        } else if (this.timePressedContext >= 0.5f) {
            if (d.canClimbThrough(this)) {
                if (this.doContextThrowGrappledTargetOutWindow(assumedDir, d)) {
                    return true;
                }
                if (!this.isGrappling()) {
                    this.addContextualAction(ContextualAction.Action.ClimbThroughWindow, assumedDir, d.getSquare(), d);
                    return true;
                }
            } else if (!(d.isPermaLocked() || d.isBarricaded() || d.IsOpen() || d.isDestroyed() || this.isGrappling())) {
                this.addContextualAction(ContextualAction.Action.ToggleWindow, assumedDir, d.getSquare(), d);
                return true;
            }
        } else if (d.getHealth() > 0 && !d.isDestroyed()) {
            IsoBarricade barricade = d.getBarricadeForCharacter(this);
            if (!d.IsOpen() && barricade == null) {
                if (!this.isGrappling()) {
                    this.addContextualAction(ContextualAction.Action.ToggleWindow, assumedDir, d.getSquare(), d);
                    return true;
                }
            } else if (barricade == null) {
                if (this.doContextThrowGrappledTargetOutWindow(assumedDir, d)) {
                    return true;
                }
                if (!this.isGrappling()) {
                    this.addContextualAction(ContextualAction.Action.ToggleWindow, assumedDir, d.getSquare(), d);
                    return true;
                }
            }
        } else if (d.isGlassRemoved() && !d.isBarricaded()) {
            if (this.doContextThrowGrappledTargetOutWindow(assumedDir, d)) {
                return true;
            }
            if (!this.isGrappling() && (this.isSafeToClimbOver(assumedDir) || d.getSquare().haveSheetRope || bTopOfSheetRope)) {
                this.addContextualAction(ContextualAction.Action.ClimbThroughWindow, assumedDir, d.getSquare(), d);
                return true;
            }
        }
        return false;
    }

    private boolean doContextThrowGrappledTargetOutWindow(IsoDirections dir, IsoObject windowObject) {
        DebugType.Grapple.trace("Attempting to throw out window: %s", windowObject);
        if (!this.isGrappling()) {
            DebugType.Grapple.trace("Not currently grappling anything. Nothing to throw out the window, windowObject:%s", windowObject);
            return false;
        }
        IGrappleable thrownTarget = this.getGrapplingTarget();
        if (!(thrownTarget instanceof IsoGameCharacter)) {
            return false;
        }
        IsoGameCharacter thrownCharacter = (IsoGameCharacter)thrownTarget;
        ClimbThroughWindowPositioningParams params = ClimbThroughWindowPositioningParams.alloc();
        ClimbThroughWindowState.getClimbThroughWindowPositioningParams(this, windowObject, params);
        boolean canClimb = params.canClimb;
        params.release();
        if (!canClimb) {
            DebugType.Grapple.trace("Cannot climb through, cannot throw out through.");
            return false;
        }
        this.addContextualAction(ContextualAction.Action.ThrowGrappledTargetOutWindow, dir, windowObject.getSquare(), windowObject);
        return true;
    }

    private boolean doContextThrowGrappledTargetOverFence(IsoGridSquare square, IsoDirections dir, IsoObject hoppable) {
        DebugType.Grapple.trace("Attempting to throw over fence: %s", hoppable);
        if (!this.isGrappling()) {
            DebugType.Grapple.trace("Not currently grappling anything. Nothing to throw over fence: %s", hoppable);
            return false;
        }
        IGrappleable thrownTarget = this.getGrapplingTarget();
        if (!(thrownTarget instanceof IsoGameCharacter)) {
            return false;
        }
        IsoGameCharacter thrownCharacter = (IsoGameCharacter)thrownTarget;
        this.addContextualAction(ContextualAction.Action.ThrowGrappledOverFence, dir.Rot180(), square, hoppable);
        return true;
    }

    private boolean doContextThrowGrappledTargetIntoInventory(IsoDirections dir) {
        ItemContainer targetContainer = null;
        return this.doContextThrowGrappledTargetIntoInventory(targetContainer);
    }

    private boolean doContextThrowGrappledTargetIntoInventory(ItemContainer targetContainer) {
        DebugType.Grapple.trace("Attempting to throw into inventory: %s", targetContainer);
        if (targetContainer == null) {
            DebugType.Grapple.trace("No inventory.");
            return false;
        }
        if (!this.isGrappling()) {
            DebugType.Grapple.trace("Not currently grappling anything. Nothing to throw into inventory: %s", targetContainer);
            return false;
        }
        IGrappleable thrownTarget = this.getGrapplingTarget();
        if (!(thrownTarget instanceof IsoGameCharacter)) {
            return false;
        }
        IsoGameCharacter thrownCharacter = (IsoGameCharacter)thrownTarget;
        this.addContextualAction(ContextualAction.Action.ThrowGrappledIntoContainer, newAction -> {
            newAction.square = this.square;
            newAction.targetContainer = targetContainer;
        });
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean doContextThumpableDoor(IsoDirections assumedDir, IsoThumpable d) {
        if (this.timePressedContext >= 0.5f) {
            if (d.isHoppable() && !this.isIgnoreAutoVault()) {
                if (this.doContextThrowGrappledTargetOverFence(d.getSquare(), assumedDir, d)) {
                    return true;
                }
                if (this.isGrappling()) return false;
                this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, d.getSquare(), d);
                return true;
            }
            this.addContextualAction(ContextualAction.Action.ToggleDoor, assumedDir, d.getSquare(), d);
            return true;
        }
        this.addContextualAction(ContextualAction.Action.ToggleDoor, assumedDir, d.getSquare(), d);
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean doContextDoor(IsoDirections assumedDir, IsoDoor d) {
        if (this.isShiftKeyDown() && d.HasCurtains() != null && d.isFacingSheet(this) && this.ticksSincePressedMovement > 15.0f) {
            this.addContextualAction(ContextualAction.Action.ToggleCurtain, assumedDir, d.getSquare(), d);
            return true;
        }
        if (this.timePressedContext >= 0.5f) {
            if (d.isHoppable() && !this.isIgnoreAutoVault()) {
                if (this.doContextThrowGrappledTargetOverFence(d.getSquare(), assumedDir, d)) {
                    return true;
                }
                if (this.isGrappling()) return false;
                this.addContextualAction(ContextualAction.Action.ClimbOverFence, assumedDir, d.getSquare(), d);
                return true;
            }
            this.addContextualAction(ContextualAction.Action.ToggleDoor, assumedDir, d.getSquare(), d);
            return true;
        }
        this.addContextualAction(ContextualAction.Action.ToggleDoor, assumedDir, d.getSquare(), d);
        return true;
    }

    public boolean hopFence(IsoDirections dir, boolean bTest) {
        float lx = this.getX() - (float)PZMath.fastfloor(this.getX());
        float ly = this.getY() - (float)PZMath.fastfloor(this.getY());
        if (dir == IsoDirections.NW) {
            if (ly < lx) {
                if (this.hopFence(IsoDirections.N, bTest)) {
                    return true;
                }
                return this.hopFence(IsoDirections.W, bTest);
            }
            if (this.hopFence(IsoDirections.W, bTest)) {
                return true;
            }
            return this.hopFence(IsoDirections.N, bTest);
        }
        if (dir == IsoDirections.NE) {
            if (ly < (lx = 1.0f - lx)) {
                if (this.hopFence(IsoDirections.N, bTest)) {
                    return true;
                }
                return this.hopFence(IsoDirections.E, bTest);
            }
            if (this.hopFence(IsoDirections.E, bTest)) {
                return true;
            }
            return this.hopFence(IsoDirections.N, bTest);
        }
        if (dir == IsoDirections.SE) {
            if ((ly = 1.0f - ly) < (lx = 1.0f - lx)) {
                if (this.hopFence(IsoDirections.S, bTest)) {
                    return true;
                }
                return this.hopFence(IsoDirections.E, bTest);
            }
            if (this.hopFence(IsoDirections.E, bTest)) {
                return true;
            }
            return this.hopFence(IsoDirections.S, bTest);
        }
        if (dir == IsoDirections.SW) {
            if ((ly = 1.0f - ly) < lx) {
                if (this.hopFence(IsoDirections.S, bTest)) {
                    return true;
                }
                return this.hopFence(IsoDirections.W, bTest);
            }
            if (this.hopFence(IsoDirections.W, bTest)) {
                return true;
            }
            return this.hopFence(IsoDirections.S, bTest);
        }
        if (this.current == null) {
            return false;
        }
        IsoGridSquare square1 = this.current.getAdjacentSquare(dir);
        if (square1 == null || square1.has(IsoFlagType.water)) {
            return false;
        }
        if (dir == IsoDirections.N && this.getCurrentSquare().has(IsoFlagType.HoppableN)) {
            if (bTest) {
                return true;
            }
            this.triggerContextualAction("ClimbOverFence", this.getCurrentSquare().getHoppable(true), (Object)dir);
            return true;
        }
        if (dir == IsoDirections.W && this.getCurrentSquare().has(IsoFlagType.HoppableW)) {
            if (bTest) {
                return true;
            }
            this.triggerContextualAction("ClimbOverFence", this.getCurrentSquare().getHoppable(false), (Object)dir);
            return true;
        }
        IsoGridSquare squareS = this.getCurrentSquare().getAdjacentSquare(IsoDirections.S);
        if (dir == IsoDirections.S && squareS != null && squareS.has(IsoFlagType.HoppableN)) {
            if (bTest) {
                return true;
            }
            this.triggerContextualAction("ClimbOverFence", squareS.getHoppable(true), (Object)dir);
            return true;
        }
        IsoGridSquare squareE = this.getCurrentSquare().getAdjacentSquare(IsoDirections.E);
        if (dir == IsoDirections.E && squareE != null && squareE.has(IsoFlagType.HoppableW)) {
            if (bTest) {
                return true;
            }
            this.triggerContextualAction("ClimbOverFence", squareE.getHoppable(false), (Object)dir);
            return true;
        }
        return false;
    }

    public boolean canClimbOverWall(IsoDirections dir) {
        if (this.isSprinting()) {
            return false;
        }
        if (!this.isSafeToClimbOver(dir) || this.current == null) {
            return false;
        }
        if (this.current.haveRoof) {
            return false;
        }
        if (this.current.getBuilding() != null) {
            return false;
        }
        IsoGridSquare above = IsoWorld.instance.currentCell.getGridSquare(this.current.x, this.current.y, this.current.z + 1);
        if (above != null && above.HasSlopedRoof() && !above.HasEave()) {
            return false;
        }
        IsoGridSquare square = this.current.getAdjacentSquare(dir);
        if (square.haveRoof) {
            return false;
        }
        if (square.isSolid() || square.isSolidTrans()) {
            return false;
        }
        if (square.getBuilding() != null) {
            return false;
        }
        IsoGridSquare above2 = IsoWorld.instance.currentCell.getGridSquare(square.x, square.y, square.z + 1);
        if (above2 != null && above2.HasSlopedRoof() && !above2.HasEave()) {
            return false;
        }
        switch (dir) {
            case N: {
                if (this.current.has(IsoFlagType.CantClimb)) {
                    return false;
                }
                if (!this.current.has(IsoObjectType.wall)) {
                    return false;
                }
                if (!this.current.has(IsoFlagType.collideN)) {
                    return false;
                }
                if (this.current.has(IsoFlagType.HoppableN)) {
                    return false;
                }
                if (above == null || !above.has(IsoFlagType.collideN)) break;
                return false;
            }
            case S: {
                if (square.has(IsoFlagType.CantClimb)) {
                    return false;
                }
                if (!square.has(IsoObjectType.wall)) {
                    return false;
                }
                if (!square.has(IsoFlagType.collideN)) {
                    return false;
                }
                if (square.has(IsoFlagType.HoppableN)) {
                    return false;
                }
                if (above2 == null || !above2.has(IsoFlagType.collideN)) break;
                return false;
            }
            case W: {
                if (this.current.has(IsoFlagType.CantClimb)) {
                    return false;
                }
                if (!this.current.has(IsoObjectType.wall)) {
                    return false;
                }
                if (!this.current.has(IsoFlagType.collideW)) {
                    return false;
                }
                if (this.current.has(IsoFlagType.HoppableW)) {
                    return false;
                }
                if (above == null || !above.has(IsoFlagType.collideW)) break;
                return false;
            }
            case E: {
                if (square.has(IsoFlagType.CantClimb)) {
                    return false;
                }
                if (!square.has(IsoObjectType.wall)) {
                    return false;
                }
                if (!square.has(IsoFlagType.collideW)) {
                    return false;
                }
                if (square.has(IsoFlagType.HoppableW)) {
                    return false;
                }
                if (above2 == null || !above2.has(IsoFlagType.collideW)) break;
                return false;
            }
            default: {
                return false;
            }
        }
        return IsoWindow.canClimbThroughHelper(this, this.current, square, dir == IsoDirections.N || dir == IsoDirections.S);
    }

    public boolean doContextClimbOverWall(IsoDirections dir) {
        if (this.ignoreAutoVault) {
            return false;
        }
        if (this.isGrappling()) {
            return false;
        }
        boolean bSafe = this.isSafeToClimbOver(dir);
        if (this.timePressedContext < 0.5f && !bSafe) {
            return false;
        }
        if (this.canClimbOverWall(dir)) {
            this.addContextualAction(ContextualAction.Action.ClimbOverWall, dir, null, null);
            return true;
        }
        return false;
    }

    public boolean climbOverWall(IsoDirections dir) {
        if (!this.canClimbOverWall(dir)) {
            return false;
        }
        this.dropHeavyItems();
        ClimbOverWallState.instance().setParams((IsoGameCharacter)this, dir);
        this.getActionContext().reportEvent("EventClimbWall");
        return true;
    }

    private void updateSleepingPillsTaken() {
        if (this.getSleepingPillsTaken() > 0 && this.lastPillsTaken > 0L && GameTime.instance.calender.getTimeInMillis() - this.lastPillsTaken > 0x6DDD00L) {
            this.setSleepingPillsTaken(this.getSleepingPillsTaken() - 1);
        }
    }

    public boolean AttemptAttack() {
        return this.DoAttack(this.useChargeTime);
    }

    @Override
    public boolean DoAttack(float chargeDelta) {
        return this.DoAttack(chargeDelta, null);
    }

    public boolean DoAttack(float chargeDelta, String clickSound) {
        if (!this.isAuthorizedHandToHandAction()) {
            return false;
        }
        this.setClickSound(clickSound);
        this.pressedAttack();
        return false;
    }

    public void updateLOS() {
        this.spottedList.clear();
        this.stats.numVisibleZombies = 0;
        this.stats.setLastNumberChasingZombies(this.stats.numChasingZombies);
        this.stats.numChasingZombies = 0;
        this.stats.musicZombiesTargetingDistantNotMoving = 0;
        this.stats.musicZombiesTargetingNearbyNotMoving = 0;
        this.stats.musicZombiesTargetingDistantMoving = 0;
        this.stats.musicZombiesTargetingNearbyMoving = 0;
        this.stats.musicZombiesVisible = 0;
        this.numSurvivorsInVicinity = 0;
        if (this.getCurrentSquare() == null) {
            return;
        }
        boolean bServer = GameServer.server;
        boolean bClient = GameClient.client;
        int playerIndex = this.playerIndex;
        float locX = this.getX();
        float locY = this.getY();
        float locZ = this.getZ();
        int close = 0;
        int vclose = 0;
        for (IsoMovingObject movingObject : this.getCell().getObjectList()) {
            boolean couldSee;
            IsoGameCharacter movingCharacter;
            IsoAnimal movingAnimal;
            IsoGridSquare chrCurrentSquare;
            if (movingObject instanceof IsoPhysicsObject || movingObject instanceof BaseVehicle) continue;
            if (movingObject == this) {
                this.spottedList.add(movingObject);
                continue;
            }
            float movingObjectX = movingObject.getX();
            float movingObjectY = movingObject.getY();
            float movingObjectZ = movingObject.getZ();
            float distanceToMovingObject = IsoUtils.DistanceTo(movingObjectX, movingObjectY, locX, locY);
            if (distanceToMovingObject < 20.0f) {
                ++close;
            }
            if ((chrCurrentSquare = movingObject.getCurrentSquare()) == null) continue;
            if (this.isSeeEveryone()) {
                movingObject.setAlphaAndTarget(playerIndex, 1.0f);
            }
            IsoPlayer movingPlayer = (movingAnimal = Type.tryCastTo(movingCharacter = Type.tryCastTo(movingObject, IsoGameCharacter.class), IsoAnimal.class)) == null ? Type.tryCastTo(movingCharacter, IsoPlayer.class) : null;
            IsoZombie movingZombie = Type.tryCastTo(movingCharacter, IsoZombie.class);
            if (movingZombie != null && movingZombie.isReanimatedForGrappleOnly()) {
                IsoMovingObject grappledBy = Type.tryCastTo(movingZombie.getGrappledBy(), IsoMovingObject.class);
                movingObject.setAlphaAndTarget(grappledBy == null ? 1.0f : grappledBy.getTargetAlpha());
                continue;
            }
            if (GameClient.client && movingCharacter != null && movingCharacter.isInvisible() && !this.getRole().hasCapability(Capability.SeesInvisiblePlayers)) {
                movingCharacter.setAlphaAndTarget(playerIndex, 0.0f);
                continue;
            }
            if (bServer) {
                couldSee = ServerLOS.instance.isCouldSee(this, chrCurrentSquare);
            } else {
                couldSee = chrCurrentSquare.isCouldSee(playerIndex);
                if (!couldSee && movingZombie != null && movingZombie.couldSeeHeadSquare(this)) {
                    couldSee = true;
                }
            }
            boolean canSee = couldSee;
            if (movingPlayer == null || ServerOptions.getInstance().hidePlayersBehindYou.getValue()) {
                if (bClient && movingPlayer != null && chrCurrentSquare.getCanSee(playerIndex)) {
                    canSee = true;
                } else if (!bServer && !(canSee = chrCurrentSquare.isCanSee(playerIndex)) && movingZombie != null && movingZombie.canSeeHeadSquare(this)) {
                    canSee = true;
                }
            }
            if (!this.isAsleep() && (canSee || distanceToMovingObject < this.getDetectionRange() && couldSee)) {
                boolean isVisibleToPlayer;
                this.TestZombieSpotPlayer(movingObject);
                if (movingCharacter == null) continue;
                boolean bl = isVisibleToPlayer = GameServer.server ? movingCharacter.TestIfSeen(playerIndex, this) : movingCharacter.isVisibleToPlayer[playerIndex];
                if (isVisibleToPlayer) {
                    if (movingCharacter instanceof IsoSurvivor) {
                        ++this.numSurvivorsInVicinity;
                    }
                    if (movingZombie != null) {
                        this.lastSeenZombieTime = 0.0;
                        if (movingObjectZ >= locZ - 1.0f && distanceToMovingObject < 7.0f && !movingZombie.ghost && !movingZombie.isFakeDead() && chrCurrentSquare.getRoom() == this.getCurrentSquare().getRoom()) {
                            this.ticksSinceSeenZombie = 0;
                            ++this.stats.numVisibleZombies;
                        }
                        if (distanceToMovingObject < 3.0f) {
                            ++vclose;
                        }
                        if (!movingZombie.isSceneCulled()) {
                            ++this.stats.musicZombiesVisible;
                            if (movingZombie.target == this) {
                                if (movingZombie.isCurrentState(WalkTowardState.instance()) || movingZombie.isCurrentState(LungeState.instance()) || movingZombie.isCurrentState(PathFindState.instance())) {
                                    if (this.DistToProper(movingZombie) >= 10.0f) {
                                        ++this.stats.musicZombiesTargetingDistantMoving;
                                    } else {
                                        ++this.stats.musicZombiesTargetingNearbyMoving;
                                    }
                                } else if (this.DistToProper(movingZombie) >= 10.0f) {
                                    ++this.stats.musicZombiesTargetingDistantNotMoving;
                                } else {
                                    ++this.stats.musicZombiesTargetingNearbyNotMoving;
                                }
                            }
                        }
                    }
                    this.spottedList.add(movingCharacter);
                    if (movingPlayer == null && !this.remote) {
                        movingCharacter.setTargetAlpha(playerIndex, 1.0f);
                    }
                    if (!bClient && !bServer && movingPlayer != null) {
                        movingPlayer.setTargetAlpha(playerIndex, 1.0f);
                    }
                    float maxdist = 4.0f;
                    if (this.stats.numVisibleZombies > 4) {
                        maxdist = 7.0f;
                    }
                    if (distanceToMovingObject < maxdist && movingCharacter instanceof IsoZombie && PZMath.fastfloor(movingObjectZ) == PZMath.fastfloor(locZ) && !this.isGhostMode() && !bClient) {
                        GameTime.instance.setMultiplier(1.0f);
                        if (!bServer) {
                            UIManager.getSpeedControls().SetCurrentGameSpeed(1);
                        }
                    }
                    if (distanceToMovingObject < maxdist && movingCharacter instanceof IsoZombie && PZMath.fastfloor(movingObjectZ) == PZMath.fastfloor(locZ) && !this.lastSpotted.contains(movingCharacter)) {
                        this.stats.numVisibleZombies += 2;
                    }
                }
            } else {
                boolean isAttachedAnimal = movingAnimal != null && this.getAttachedAnimals().contains(movingAnimal);
                movingObject.setTargetAlpha(playerIndex, isAttachedAnimal ? 1.0f : 0.0f);
                if (couldSee) {
                    this.TestZombieSpotPlayer(movingObject);
                }
            }
            if (!(distanceToMovingObject < 2.0f) || movingObject.getTargetAlpha(playerIndex) != 1.0f || this.remote) continue;
            movingObject.setAlpha(playerIndex, 1.0f);
        }
        if (!bServer && this.isAlive() && vclose > 0 && this.stats.lastVeryCloseZombies == 0 && this.stats.numVisibleZombies > 0 && this.stats.lastNumVisibleZombies == 0 && this.timeSinceLastStab >= 600.0f) {
            this.timeSinceLastStab = 0.0f;
            long inst = this.getEmitter().playSoundImpl("ZombieSurprisedPlayer", null);
            this.getEmitter().setVolume(inst, (float)Core.getInstance().getOptionJumpScareVolume() / 10.0f);
        }
        if (this.stats.numVisibleZombies > 0) {
            this.timeSinceLastStab = 0.0f;
        }
        if (this.timeSinceLastStab < 600.0f) {
            this.timeSinceLastStab += GameTime.getInstance().getThirtyFPSMultiplier();
        }
        int actualSpotted = 0;
        for (int n = 0; n < this.spottedList.size(); ++n) {
            if (!this.lastSpotted.contains(this.spottedList.get(n))) {
                this.lastSpotted.add((IsoMovingObject)this.spottedList.get(n));
            }
            if (!(this.spottedList.get(n) instanceof IsoZombie)) continue;
            ++actualSpotted;
        }
        if (this.clearSpottedTimer <= 0 && actualSpotted == 0) {
            this.lastSpotted.clear();
            this.clearSpottedTimer = 1000;
        } else {
            --this.clearSpottedTimer;
        }
        this.stats.lastNumVisibleZombies = this.stats.numVisibleZombies;
        this.stats.lastVeryCloseZombies = vclose;
    }

    private boolean checkSpottedPLayerTimer(IsoPlayer remoteChr) {
        if (!remoteChr.spottedByPlayer) {
            return false;
        }
        if (this.spottedPlayerTimer.containsKey(remoteChr.getRemoteID())) {
            this.spottedPlayerTimer.put(remoteChr.getRemoteID(), this.spottedPlayerTimer.get(remoteChr.getRemoteID()) + 1);
        } else {
            this.spottedPlayerTimer.put(remoteChr.getRemoteID(), 1);
        }
        if (this.spottedPlayerTimer.get(remoteChr.getRemoteID()) > 100) {
            remoteChr.spottedByPlayer = false;
            return false;
        }
        return true;
    }

    private float calculateMaxDist() {
        float minScreen = 1920.0f;
        float maxScreen = 7680.0f;
        float minDist = 80.0f;
        float maxDist = 320.0f;
        float t = ((float)IsoCamera.getScreenWidth(0) - 1920.0f) / 5760.0f;
        return PZMath.clamp(80.0f + t * 240.0f, 80.0f, 320.0f);
    }

    public boolean checkCanSeeClient(UdpConnection remoteConnection) {
        if (remoteConnection.getRole().hasCapability(Capability.SeesInvisiblePlayers)) {
            return true;
        }
        return !this.isInvisible();
    }

    public boolean checkCanSeeClient(IsoPlayer remoteChr) {
        boolean canSee;
        Vector2 oPos = tempVector2_1.set(this.getX(), this.getY());
        Vector2 tPos = tempVector2_2.set(remoteChr.getX(), remoteChr.getY());
        tPos.x -= oPos.x;
        tPos.y -= oPos.y;
        Vector2 dir = this.getForwardDirection();
        tPos.normalize();
        dir.normalize();
        float dot = tPos.dot(dir);
        BaseVehicle vehicle = this.getVehicle();
        if (vehicle != null && !this.isAiming() && !this.isLookingWhileInVehicle() && vehicle.isDriver(this) && vehicle.getCurrentSpeedKmHour() < -1.0f) {
            dot = -dot;
        }
        if (!GameClient.client || remoteChr == this || !this.isLocalPlayer()) {
            return true;
        }
        if (!this.isAccessLevel("None") && this.canSeeAll()) {
            remoteChr.spottedByPlayer = true;
            return true;
        }
        if (remoteChr.getCurrentSquare() == null) {
            return false;
        }
        float dist = this.current == null ? 0.0f : remoteChr.getCurrentSquare().DistTo(this.getCurrentSquare());
        if (dist <= 2.0f) {
            remoteChr.spottedByPlayer = true;
            return true;
        }
        if (ServerOptions.getInstance().hidePlayersBehindYou.getValue() && (double)dot < -0.5) {
            return this.checkSpottedPLayerTimer(remoteChr);
        }
        if (remoteChr.isGhostMode() && this.isAccessLevel("None")) {
            remoteChr.spottedByPlayer = false;
            return false;
        }
        IsoGridSquare.ILighting light = remoteChr.getCurrentSquare().lighting[this.getIndex()];
        if (!light.bCouldSee()) {
            return this.checkSpottedPLayerTimer(remoteChr);
        }
        if (dist > this.calculateMaxDist()) {
            remoteChr.spottedByPlayer = false;
            return false;
        }
        if (!remoteChr.isSneaking() || !ServerOptions.getInstance().sneakModeHideFromOtherPlayers.getValue() || remoteChr.isSprinting()) {
            remoteChr.spottedByPlayer = true;
            return true;
        }
        if (remoteChr.spottedByPlayer) {
            return true;
        }
        float expoRangeMod = (float)(Math.pow(Math.max(40.0f - dist, 0.0f), 3.0) / 12000.0);
        float sneakSkillMod = 1.0f - (float)remoteChr.remoteSneakLvl / 10.0f * 0.9f + 0.3f;
        float chance = 1.0f;
        if (dot < 0.8f) {
            chance = 0.3f;
        }
        if (dot < 0.6f) {
            chance = 0.05f;
        }
        float totalLight = (light.lightInfo().getR() + light.lightInfo().getG() + light.lightInfo().getB()) / 3.0f;
        float tirednessMod = (1.0f - (float)this.getMoodles().getMoodleLevel(MoodleType.TIRED) / 5.0f) * 0.7f + 0.3f;
        float drunkennessMod = (1.0f - (float)this.getMoodles().getMoodleLevel(MoodleType.DRUNK) / 5.0f) * 0.7f + 0.3f;
        float movementMod = 0.1f;
        if (remoteChr.isPlayerMoving()) {
            movementMod = 0.35f;
        }
        if (remoteChr.isRunning()) {
            movementMod = 1.0f;
        }
        ArrayList<Point> pts = PolygonalMap2.instance.getPointInLine(remoteChr.getX(), remoteChr.getY(), this.getX(), this.getY(), PZMath.fastfloor(this.getZ()));
        IsoGridSquare sqTestCover = null;
        float sneakDistCover = 0.0f;
        float observerDistCover = 0.0f;
        boolean behindSomething = false;
        boolean behindWindow = false;
        for (int i = 0; i < pts.size(); ++i) {
            Point pt = pts.get(i);
            sqTestCover = IsoCell.getInstance().getGridSquare((double)pt.x, (double)pt.y, this.getZ());
            if (sqTestCover == null) continue;
            float result = sqTestCover.getGridSneakModifier(false);
            if (result > 1.0f) {
                behindSomething = true;
                if (!sqTestCover.getProperties().has(IsoFlagType.windowN) && !sqTestCover.getProperties().has(IsoFlagType.windowW)) break;
                behindWindow = true;
                break;
            }
            for (int j = 0; j < sqTestCover.getObjects().size(); ++j) {
                IsoObject obj = sqTestCover.getObjects().get(j);
                if (obj.getSprite().getProperties().has(IsoFlagType.solidtrans) || obj.getSprite().getProperties().has(IsoFlagType.solid)) {
                    behindSomething = true;
                    break;
                }
                if (!obj.getSprite().getProperties().has(IsoFlagType.windowN) && !obj.getSprite().getProperties().has(IsoFlagType.windowW)) continue;
                behindSomething = true;
                behindWindow = true;
                break;
            }
            if (behindSomething) break;
        }
        float windowMod = 1.0f;
        if (behindWindow && remoteChr.isSneaking()) {
            windowMod = 0.3f;
        }
        if (behindSomething) {
            sneakDistCover = sqTestCover.DistTo(remoteChr.getCurrentSquare());
            observerDistCover = sqTestCover.DistTo(this.getCurrentSquare());
        }
        float coverMod = observerDistCover < 2.0f ? 5.0f : Math.min(sneakDistCover, 5.0f);
        coverMod = Math.max(0.0f, coverMod - 1.0f);
        float fogMod = Math.max(0.1f, 1.0f - ClimateManager.getInstance().getFogIntensity());
        float finalChance = chance * expoRangeMod * totalLight * sneakSkillMod * tirednessMod * drunkennessMod * movementMod * (coverMod = coverMod / 5.0f * 0.9f + 0.1f) * fogMod * windowMod;
        if (finalChance >= 1.0f) {
            remoteChr.spottedByPlayer = true;
            return true;
        }
        finalChance = (float)(1.0 - Math.pow(1.0f - finalChance, GameTime.getInstance().getMultiplier()));
        remoteChr.spottedByPlayer = canSee = Rand.Next(0.0f, 1.0f) < (finalChance *= 0.5f);
        return canSee;
    }

    public String getTimeSurvived() {
        Object total = "";
        int hours = (int)this.getHoursSurvived();
        int days = hours / 24;
        int hoursLeft = hours % 24;
        int months = days / 30;
        days %= 30;
        int years = months / 12;
        months %= 12;
        String dayString = Translator.getText("IGUI_Gametime_day", new Object[0]);
        String yearString = Translator.getText("IGUI_Gametime_year", new Object[0]);
        String hourString = Translator.getText("IGUI_Gametime_hour", new Object[0]);
        String monthString = Translator.getText("IGUI_Gametime_month", new Object[0]);
        if (years != 0) {
            if (years > 1) {
                yearString = Translator.getText("IGUI_Gametime_years", new Object[0]);
            }
            if (!((String)total).isEmpty()) {
                total = (String)total + ", ";
            }
            total = (String)total + years + " " + yearString;
        }
        if (months != 0) {
            if (months > 1) {
                monthString = Translator.getText("IGUI_Gametime_months", new Object[0]);
            }
            if (!((String)total).isEmpty()) {
                total = (String)total + ", ";
            }
            total = (String)total + months + " " + monthString;
        }
        if (days != 0) {
            if (days > 1) {
                dayString = Translator.getText("IGUI_Gametime_days", new Object[0]);
            }
            if (!((String)total).isEmpty()) {
                total = (String)total + ", ";
            }
            total = (String)total + days + " " + dayString;
        }
        if (hoursLeft != 0) {
            if (hoursLeft > 1) {
                hourString = Translator.getText("IGUI_Gametime_hours", new Object[0]);
            }
            if (!((String)total).isEmpty()) {
                total = (String)total + ", ";
            }
            total = (String)total + hoursLeft + " " + hourString;
        }
        if (((String)total).isEmpty()) {
            int minutes = (int)(this.hoursSurvived * 60.0);
            total = minutes + " " + Translator.getText("IGUI_Gametime_minutes", new Object[0]);
        }
        return total;
    }

    public boolean IsUsingAimWeapon() {
        if (this.leftHandItem == null) {
            return false;
        }
        InventoryItem inventoryItem = this.leftHandItem;
        if (!(inventoryItem instanceof HandWeapon)) {
            return false;
        }
        HandWeapon handWeapon = (HandWeapon)inventoryItem;
        if (!this.isAiming()) {
            return false;
        }
        return handWeapon.isAimedFirearm;
    }

    private boolean IsUsingAimHandWeapon() {
        if (this.leftHandItem == null) {
            return false;
        }
        InventoryItem inventoryItem = this.leftHandItem;
        if (!(inventoryItem instanceof HandWeapon)) {
            return false;
        }
        HandWeapon handWeapon = (HandWeapon)inventoryItem;
        if (!this.isAiming()) {
            return false;
        }
        return handWeapon.isAimedHandWeapon;
    }

    private boolean DoAimAnimOnAiming() {
        return this.IsUsingAimWeapon();
    }

    public int getSleepingPillsTaken() {
        return this.sleepingPillsTaken;
    }

    public void setSleepingPillsTaken(int sleepingPillsTaken) {
        if (this.isGodMod()) {
            this.resetSleepingPillsTaken();
            return;
        }
        this.sleepingPillsTaken = sleepingPillsTaken;
        if (this.getStats().get(CharacterStat.INTOXICATION) > 10.0f) {
            ++this.sleepingPillsTaken;
        }
        this.lastPillsTaken = GameTime.instance.calender.getTimeInMillis();
    }

    public void resetSleepingPillsTaken() {
        this.sleepingPillsTaken = 0;
    }

    @Override
    public boolean isOutside() {
        return this.getCurrentSquare() != null && this.getCurrentSquare().getRoom() == null && !this.isInARoom();
    }

    public double getLastSeenZomboidTime() {
        return this.lastSeenZombieTime;
    }

    public float getPlayerClothingTemperature() {
        float result = 0.0f;
        if (this.getClothingItem_Feet() != null) {
            result += ((Clothing)this.getClothingItem_Feet()).getTemperature();
        }
        if (this.getClothingItem_Hands() != null) {
            result += ((Clothing)this.getClothingItem_Hands()).getTemperature();
        }
        if (this.getClothingItem_Head() != null) {
            result += ((Clothing)this.getClothingItem_Head()).getTemperature();
        }
        if (this.getClothingItem_Legs() != null) {
            result += ((Clothing)this.getClothingItem_Legs()).getTemperature();
        }
        if (this.getClothingItem_Torso() != null) {
            result += ((Clothing)this.getClothingItem_Torso()).getTemperature();
        }
        return result;
    }

    public float getPlayerClothingInsulation() {
        float result = 0.0f;
        if (this.getClothingItem_Feet() != null) {
            result += ((Clothing)this.getClothingItem_Feet()).getInsulation() * 0.1f;
        }
        if (this.getClothingItem_Hands() != null) {
            result += ((Clothing)this.getClothingItem_Hands()).getInsulation() * 0.0f;
        }
        if (this.getClothingItem_Head() != null) {
            result += ((Clothing)this.getClothingItem_Head()).getInsulation() * 0.0f;
        }
        if (this.getClothingItem_Legs() != null) {
            result += ((Clothing)this.getClothingItem_Legs()).getInsulation() * 0.3f;
        }
        if (this.getClothingItem_Torso() != null) {
            result += ((Clothing)this.getClothingItem_Torso()).getInsulation() * 0.6f;
        }
        return result;
    }

    public InventoryItem getActiveLightItem() {
        if (this.rightHandItem != null && this.rightHandItem.isEmittingLight()) {
            return this.rightHandItem;
        }
        if (this.leftHandItem != null && this.leftHandItem.isEmittingLight()) {
            return this.leftHandItem;
        }
        AttachedItems attachedItems = this.getAttachedItems();
        for (int i = 0; i < attachedItems.size(); ++i) {
            InventoryItem item = attachedItems.getItemByIndex(i);
            if (!item.isEmittingLight()) continue;
            return item;
        }
        return null;
    }

    public boolean isTorchCone() {
        if (this.remote) {
            return this.mpTorchCone;
        }
        InventoryItem item = this.getActiveLightItem();
        return item != null && item.isTorchCone();
    }

    public float getTorchDot() {
        InventoryItem item = this.getActiveLightItem();
        if (item != null) {
            return item.getTorchDot();
        }
        return 0.0f;
    }

    public float getLightDistance() {
        if (this.remote) {
            return this.mpTorchDist;
        }
        InventoryItem item = this.getActiveLightItem();
        if (item != null) {
            return item.getLightDistance();
        }
        return 0.0f;
    }

    public boolean pressedMovement(boolean ignoreBlock) {
        if (this.isNpc()) {
            return false;
        }
        NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
        if (GameClient.client && !this.isLocal()) {
            return networkAi != null && networkAi.isPressedMovement();
        }
        boolean bPressedRun = false;
        bPressedRun = this.isRunButtonDown();
        this.setVariable("pressedRunButton", bPressedRun);
        if (!ignoreBlock && (this.isBlockMovement() || this.isIgnoreInputsForDirection())) {
            if (GameClient.client && this.isLocal() && networkAi != null) {
                networkAi.setPressedMovement(false);
            }
            return false;
        }
        if (this.isInputMoveAxisApplied()) {
            if (GameClient.client && this.isLocal() && networkAi != null) {
                networkAi.setPressedMovement(true);
            }
            return true;
        }
        if (GameClient.client && this.isLocal()) {
            networkAi.setPressedMovement(false);
        }
        return false;
    }

    public boolean pressedCancelAction() {
        if (this.isNpc()) {
            return false;
        }
        NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
        if (GameClient.client && !this.isLocal()) {
            return networkAi != null && networkAi.isPressedCancelAction();
        }
        boolean cancelActionButtonDown = PZOptional.ifPresent(this.getCharacterInputComponent(), CharacterInputComponent::isCancelActionButtonDown);
        if (GameClient.client && this.isLocal() && networkAi != null) {
            networkAi.setPressedCancelAction(cancelActionButtonDown);
        }
        return cancelActionButtonDown;
    }

    public boolean checkWalkTo() {
        if (this.isNpc()) {
            return false;
        }
        if (this.playerIndex == 0 && this.isWalkToButtonDown()) {
            LuaEventManager.triggerEvent("OnPressWalkTo", 0, 0, 0);
            return true;
        }
        return false;
    }

    public boolean pressedAim() {
        return !this.isNpc() && this.isAnyAimKeyDown();
    }

    @Override
    public boolean isDoingActionThatCanBeCancelled() {
        if (this.isDead()) {
            return false;
        }
        if (!this.getCharacterActions().isEmpty()) {
            return true;
        }
        State state = this.getCurrentState();
        if (state != null && state.isDoingActionThatCanBeCancelled()) {
            return true;
        }
        for (int i = 0; i < this.getStateMachine().getSubStateCount(); ++i) {
            state = this.getStateMachine().getSubStateAt(i);
            if (state == null || !state.isDoingActionThatCanBeCancelled()) continue;
            return true;
        }
        return false;
    }

    public long getSteamID() {
        return this.steamId;
    }

    public void setSteamID(long steamId) {
        this.steamId = steamId;
    }

    public boolean isTargetedByZombie() {
        return this.targetedByZombie;
    }

    @Override
    public boolean isMaskClicked(int x, int y, boolean flip) {
        if (this.sprite == null) {
            return false;
        }
        return this.sprite.isMaskClicked(this.getForwardIsoDirection(), x, y, flip);
    }

    public int getOffSetXUI() {
        return this.offSetXUi;
    }

    public void setOffSetXUI(int offSetXUi) {
        this.offSetXUi = offSetXUi;
    }

    public int getOffSetYUI() {
        return this.offSetYUi;
    }

    public void setOffSetYUI(int offSetYUi) {
        this.offSetYUi = offSetYUi;
    }

    public String getUsername() {
        return this.getUsername(false, false);
    }

    public String getUsername(Boolean canShowFirstname) {
        return this.getUsername(canShowFirstname, false);
    }

    /*
     * Unable to fully structure code
     */
    public String getUsername(Boolean canShowFirstname, Boolean canShowDisguisedName) {
        block9: {
            block8: {
                nameStr = this.username;
                if (!canShowDisguisedName.booleanValue()) break block8;
                this.updateDisguisedState();
                isoGameCharacter = IsoCamera.getCameraCharacter();
                if (!GameClient.client || !(isoGameCharacter instanceof IsoPlayer)) ** GOTO lbl-1000
                player = (IsoPlayer)isoGameCharacter;
                if (player.role.hasCapability(Capability.CanSeePlayersStats)) {
                    v0 = true;
                } else lbl-1000:
                // 2 sources

                {
                    v0 = bViewerIsAdmin = false;
                }
                if (this.isDisguised() && !bViewerIsAdmin) {
                    nameStr = ServerOptions.getInstance().hideDisguisedUserName.getValue() != false ? "" : Translator.getText("IGUI_Disguised_Player_Name", new Object[0]);
                } else if (canShowFirstname.booleanValue() && GameClient.client && ServerOptions.instance.showFirstAndLastName.getValue()) {
                    nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
                    if (ServerOptions.instance.displayUserName.getValue()) {
                        nameStr = (String)nameStr + " (" + this.username + ")";
                    }
                }
                break block9;
            }
            if (canShowFirstname.booleanValue() && GameClient.client && ServerOptions.instance.showFirstAndLastName.getValue()) {
                nameStr = this.getDescriptor().getForename() + " " + this.getDescriptor().getSurname();
                if (ServerOptions.instance.displayUserName.getValue()) {
                    nameStr = (String)nameStr + " (" + this.username + ")";
                }
            }
        }
        return nameStr;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public void updateUsername() {
        if (GameClient.client || GameServer.server) {
            return;
        }
        this.username = this.getDescriptor().getForename() + this.getDescriptor().getSurname();
    }

    @Override
    public short getOnlineID() {
        return this.onlineId;
    }

    public boolean isLocalPlayer() {
        if (GameServer.server) {
            return false;
        }
        for (int pn = 0; pn < numPlayers; ++pn) {
            if (players[pn] != this) continue;
            return true;
        }
        return false;
    }

    public static boolean isLocalPlayer(IsoGameCharacter character) {
        IsoPlayer player;
        return character instanceof IsoPlayer && (player = (IsoPlayer)character).isLocalPlayer();
    }

    public static boolean isLocalPlayer(Object characterObject) {
        IsoPlayer player;
        return characterObject instanceof IsoPlayer && (player = (IsoPlayer)characterObject).isLocalPlayer();
    }

    public static void setLocalPlayer(int index, IsoPlayer newPlayerObj) {
        IsoPlayer.players[index] = newPlayerObj;
    }

    public static IsoPlayer getLocalPlayerByOnlineID(short id) {
        for (int i = 0; i < numPlayers; ++i) {
            IsoPlayer player = players[i];
            if (player == null || player.onlineId != id) continue;
            return player;
        }
        return null;
    }

    public boolean isOnlyPlayerAsleep() {
        if (!this.isAsleep()) {
            return false;
        }
        for (int pn = 0; pn < numPlayers; ++pn) {
            if (players[pn] == null || players[pn].isDead() || players[pn] == this || !players[pn].isAsleep()) continue;
            return false;
        }
        return true;
    }

    public void setHasObstacleOnPath(boolean value) {
        this.hasObstacleOnPath = value;
    }

    public boolean isRemoteAndHasObstacleOnPath() {
        return !this.isLocalPlayer() && this.hasObstacleOnPath;
    }

    @Override
    public void OnDeath() {
        super.OnDeath();
        if (GameServer.server) {
            return;
        }
        this.StopAllActionQueue();
        if (this.isAsleep()) {
            UIManager.FadeIn(this.getIndex(), 0.5);
            this.setAsleep(false);
        }
        this.dropHandItems();
        if (IsoPlayer.allPlayersDead()) {
            SoundManager.instance.playMusic(DEATH_MUSIC_NAME);
        }
        if (this.isLocalPlayer()) {
            LuaEventManager.triggerEvent("OnPlayerDeath", this);
        }
        if (this.isLocalPlayer() && this.getVehicle() != null) {
            this.getVehicle().exit(this);
        }
        this.removeSaveFile();
        if (this.shouldBecomeZombieAfterDeath()) {
            this.forceAwake();
        }
        if (this.getMoodles() != null) {
            this.getMoodles().Update();
        }
        this.getCell().setDrag(null, this.getIndex());
    }

    public boolean isNoClip() {
        return this.getCheats().isSet(CheatType.NO_CLIP);
    }

    public void setNoClip(boolean noClip, boolean isForced) {
        if (!isForced) {
            this.setNoClip(noClip);
            return;
        }
        this.getCheats().set(CheatType.NO_CLIP, noClip);
    }

    public void setNoClip(boolean noClip) {
        if (!Role.hasCapability(this, Capability.ToggleNoclipHimself)) {
            this.getCheats().set(CheatType.NO_CLIP, false);
            return;
        }
        this.getCheats().set(CheatType.NO_CLIP, noClip);
    }

    @Deprecated
    public void setAuthorizeMeleeAction(boolean enabled) {
        this.setAuthorizedHandToHandAction(enabled);
    }

    @Deprecated
    public boolean isAuthorizeMeleeAction() {
        return this.isAuthorizedHandToHandAction();
    }

    @Deprecated
    public void setAuthorizeShoveStomp(boolean enabled) {
        this.setAuthorizedHandToHand(enabled);
    }

    @Deprecated
    public boolean isAuthorizeShoveStomp() {
        return this.isAuthorizedHandToHand();
    }

    public void setAuthorizedHandToHandAction(boolean enabled) {
        this.isAuthorizedHandToHandAction = enabled;
    }

    public boolean isAuthorizedHandToHandAction() {
        return this.isAuthorizedHandToHandAction;
    }

    public void setAuthorizedHandToHand(boolean enabled) {
        this.isAuthorizedHandToHand = enabled;
    }

    public boolean isAuthorizedHandToHand() {
        return this.isAuthorizedHandToHand;
    }

    public boolean isBlockMovement() {
        return this.blockMovement;
    }

    public void setBlockMovement(boolean blockMovement) {
        this.blockMovement = blockMovement;
    }

    public void startReceivingBodyDamageUpdates(IsoPlayer other) {
        if (GameClient.client && other != null && other != this && this.isLocalPlayer() && !other.isLocalPlayer()) {
            other.resetBodyDamageRemote();
            BodyDamageSync.instance.startReceivingUpdates(other);
        }
    }

    public void stopReceivingBodyDamageUpdates(IsoPlayer other) {
        if (GameClient.client && other != null && other != this && !other.isLocalPlayer()) {
            BodyDamageSync.instance.stopReceivingUpdates(other);
        }
    }

    public Nutrition getNutrition() {
        return this.nutrition;
    }

    public Fitness getFitness() {
        return this.fitness;
    }

    public void updateRemotePlayerInVehicle() {
        UdpConnection passengerConnection;
        float newX = this.getVehicle().getX();
        float newY = this.getVehicle().getY();
        float newZ = this.getVehicle().getZ();
        this.realx = newX;
        this.realy = newY;
        this.realz = (byte)PZMath.fastfloor(newZ);
        NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
        networkAi.targetX = newX;
        networkAi.targetY = newY;
        networkAi.targetZ = PZMath.fastfloor(newZ);
        this.setForceX(newX);
        this.setForceY(newY);
        this.setZ(newZ);
        this.setTimeSinceLastNetData(0);
        if (GameServer.server && (passengerConnection = GameServer.getConnectionFromPlayer(this)) != null) {
            passengerConnection.releventPos[this.playerIndex].set(newX, newY, newZ);
        }
    }

    protected float getNetworkSpeedMul() {
        NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
        float scale = IsoUtils.DistanceTo(this.getX(), this.getY(), networkAi.targetX, networkAi.targetY) / IsoUtils.DistanceTo(this.realx, this.realy, networkAi.targetX, networkAi.targetY);
        return 0.9f + 0.20000005f * IsoUtils.smoothstep(0.9f, 1.1f, scale);
    }

    private boolean checkTile(int x, int y, int z, boolean isVerticalWall) {
        IsoGridSquare square = this.getCell().getGridSquare(x, y, z);
        return square != null && square.getWall(isVerticalWall) == null;
    }

    private boolean canWalkAxialPath(int x, int y, int z, int targetX, int targetY) {
        boolean isVerticalMove;
        boolean isHorizontalMove = y == targetY;
        boolean bl = isVerticalMove = x == targetX;
        if (isHorizontalMove) {
            int startX = Math.min(x, targetX);
            int endX = Math.max(x, targetX);
            for (int tileX = startX + 1; tileX <= endX; ++tileX) {
                if (this.checkTile(tileX, y, z, false)) continue;
                return false;
            }
            return true;
        }
        if (isVerticalMove) {
            int startY = Math.min(y, targetY);
            int endY = Math.max(y, targetY);
            for (int tileY = startY + 1; tileY <= endY; ++tileY) {
                if (this.checkTile(x, tileY, z, true)) continue;
                return false;
            }
            return true;
        }
        return false;
    }

    private void trySuppressPathFinder(Vector3 target) {
        float x = this.getX();
        float y = this.getY();
        int tileX = this.getXi();
        int tileY = this.getYi();
        int tileZ = this.getZi();
        int targetTileX = PZMath.fastfloor(target.x);
        int targetTileY = PZMath.fastfloor(target.y);
        if (Math.abs(tileX - targetTileX) + Math.abs(tileY - targetTileY) > 3) {
            return;
        }
        if (!PolygonalMap2.instance.lineClearCollide(x, y, target.x, target.y, tileZ, this.vehicle, false, true)) {
            this.getNetworkCharacterAI().forcePathFinder = false;
            return;
        }
        if (tileX == targetTileX || tileY == targetTileY) {
            return;
        }
        if (this.canWalkAxialPath(tileX, tileY, tileZ, targetTileX, tileY) && this.canWalkAxialPath(targetTileX, tileY, tileZ, targetTileX, targetTileY)) {
            target.y = y;
            this.getNetworkCharacterAI().forcePathFinder = false;
            return;
        }
        if (this.canWalkAxialPath(tileX, tileY, tileZ, tileX, targetTileY) && this.canWalkAxialPath(tileX, targetTileY, tileZ, targetTileX, targetTileY)) {
            target.x = x;
            this.getNetworkCharacterAI().forcePathFinder = false;
        }
    }

    protected boolean updateRemotePlayer() {
        NetworkPlayerAI networkAi;
        if (!this.remote) {
            return false;
        }
        if (this.isSeatedInVehicle()) {
            this.updateRemotePlayerInVehicle();
        }
        if (GameServer.server) {
            ServerLOS.instance.doServerZombieLOS(this);
            ServerLOS.instance.updateLOS(this);
            if (this.isDead()) {
                return true;
            }
            networkAi = this.getNetworkCharacterAI();
            this.isPlayerMoving = networkAi.moving;
            if (this.isCurrentState(PlayerSitOnFurnitureState.instance()) || this.isSeatedInVehicle()) {
                this.isPlayerMoving = false;
            }
            this.currentSpeed = this.isPlayerMoving ? (this.isSprinting() ? 1.5f : (this.isRunning() ? 1.0f : 0.5f)) : 0.0f;
            if (this.isJustMoved() && !this.isNpc()) {
                LuaEventManager.triggerEvent("OnPlayerMove", this);
            }
            this.removeFromSquare();
            this.setForceX(this.realx);
            this.setForceY(this.realy);
            this.setZ(this.realz);
            this.setLastZ(this.realz);
            this.ensureOnTile();
            this.setMovingSquareNow();
            if (this.slowTimer > 0.0f) {
                this.slowTimer -= GameTime.instance.getRealworldSecondsSinceLastUpdate();
                this.slowFactor -= GameTime.instance.getMultiplier() / 100.0f;
                if (this.slowFactor < 0.0f) {
                    this.slowFactor = 0.0f;
                }
            } else {
                this.slowFactor = 0.0f;
            }
        }
        if (GameClient.client) {
            float distance;
            boolean isAnimalStuck;
            if (this.isCurrentState(BumpedState.instance())) {
                return true;
            }
            networkAi = this.getNetworkCharacterAI();
            Vector3 target = networkAi.tempTarget;
            if (networkAi.predictionType == 1) {
                if (this.isAnimal()) {
                    if (networkAi.prediction.distance > 0 && networkAi.prediction.distance <= 10) {
                        networkAi.prediction.updateLerp(this);
                    }
                } else {
                    networkAi.prediction.update();
                }
                networkAi.targetX = networkAi.prediction.position.x;
                networkAi.targetY = networkAi.prediction.position.y;
                networkAi.targetZ = (byte)networkAi.prediction.position.z;
            }
            boolean bl = isAnimalStuck = this.isAnimal() && this.isCollidedThisFrame() && PolygonalMap2.instance.lineClearCollide(this.getX(), this.getY(), networkAi.targetX, networkAi.targetY, PZMath.fastfloor(this.getZ()), null, false, true);
            if (!isAnimalStuck && (networkAi.isCollisionEnabled() || networkAi.isNoCollisionTimeout())) {
                this.setCollidable(true);
                target.set(networkAi.targetX, networkAi.targetY, networkAi.targetZ);
            } else {
                this.setCollidable(false);
                target.set(this.realx, this.realy, this.realz);
            }
            PathFindBehavior2 pfb2 = this.getPathFindBehavior2();
            networkAi.getState().processExitSubState();
            networkAi.getState().processExitState();
            boolean moveToEvent = networkAi.getState().processEnterState(target);
            if (!moveToEvent) {
                moveToEvent = networkAi.getState().processEnterSubState(target);
            }
            networkAi.targetX = target.x;
            networkAi.targetY = target.y;
            if (!networkAi.forcePathFinder && this.isRemoteAndHasObstacleOnPath()) {
                networkAi.forcePathFinder = true;
            }
            if (this.getCurrentState() == ClimbOverFenceState.instance() || this.getCurrentState() == ClimbThroughWindowState.instance() || this.getCurrentState() == ClimbOverWallState.instance()) {
                networkAi.forcePathFinder = false;
            }
            if (networkAi.forcePathFinder) {
                this.trySuppressPathFinder(target);
            }
            float distToMoving = 0.2f;
            if (!networkAi.needToMovingUsingPathFinder && !networkAi.forcePathFinder) {
                if (networkAi.usePathFind) {
                    pfb2.reset();
                    this.setPath2(null);
                    networkAi.usePathFind = false;
                }
                pfb2.walkingOnTheSpot.reset(this.getX(), this.getY());
                if (this.getCurrentState() == ClimbOverWallState.instance() || this.getCurrentState() == ClimbOverFenceState.instance() || this.getCurrentState() == PlayerSitOnFurnitureState.instance() || this.getCurrentState() == ClimbSheetRopeState.instance() || this.getCurrentState() == ClimbDownSheetRopeState.instance()) {
                    this.moveUnmoddedRemotePlayer();
                } else {
                    pfb2.moveToPoint(target.x, target.y, this.getNetworkSpeedMul());
                }
                boolean inaccurateX = PZMath.fastfloor(target.x) != PZMath.fastfloor(this.getX());
                boolean inaccurateY = PZMath.fastfloor(target.y) != PZMath.fastfloor(this.getY());
                boolean inaccurateZ = PZMath.fastfloor(target.z) != PZMath.fastfloor(this.getZ());
                float distance2 = IsoUtils.DistanceManhatten(target.x, target.y, this.getX(), this.getY());
                boolean bl2 = this.isPlayerMoving = moveToEvent || inaccurateX || inaccurateY || inaccurateZ || distance2 > 0.2f || networkAi.moving;
                if (this.isCurrentState(PlayerSitOnGroundState.instance()) || this.isCurrentState(PlayerSitOnFurnitureState.instance()) || this.isCurrentState(AnimalAlertedState.instance()) || this.isCurrentState(AnimalEatState.instance())) {
                    this.isPlayerMoving = false;
                }
                if (!this.isPlayerMoving) {
                    this.DirectionFromVector(networkAi.direction);
                    this.setForwardDirection(networkAi.direction);
                    networkAi.forcePathFinder = false;
                    if (networkAi.usePathFind) {
                        pfb2.reset();
                        this.setPath2(null);
                        networkAi.usePathFind = false;
                    }
                }
                this.setJustMoved(this.isPlayerMoving);
                this.deltaX = 0.0f;
                this.deltaY = 0.0f;
            } else {
                PathFindBehavior2.BehaviorResult result;
                if ((!networkAi.usePathFind || target.x != pfb2.getTargetX() || target.y != pfb2.getTargetY()) && (Math.abs(pfb2.getTargetX() - target.x) > 0.3f || Math.abs(pfb2.getTargetY() - target.y) > 0.3f || Math.abs(pfb2.getTargetZ() - target.z) > 0.3f || !pfb2.isGoalLocation() || pfb2.getIsCancelled())) {
                    pfb2.pathToLocationF(target.x, target.y, target.z);
                    pfb2.walkingOnTheSpot.reset(this.getX(), this.getY());
                    networkAi.usePathFind = true;
                }
                if ((result = pfb2.update()) == PathFindBehavior2.BehaviorResult.Failed) {
                    this.setPathFindIndex(-1);
                    if (networkAi.forcePathFinder) {
                        networkAi.forcePathFinder = false;
                    } else if (this.getVehicle() == null) {
                        this.teleportTo(this.realx, this.realy, (int)this.realz);
                        if (GameServer.server) {
                            DebugType.Multiplayer.warn(String.format("Player %d teleport from (%.2f, %.2f, %.2f) to (%.2f, %.2f, %.2f)", this.getOnlineID(), Float.valueOf(this.getX()), Float.valueOf(this.getY()), Float.valueOf(this.getZ()), Float.valueOf(this.realx), Float.valueOf(this.realy), this.realz));
                        }
                    }
                } else if (result == PathFindBehavior2.BehaviorResult.Succeeded) {
                    int tx = PZMath.fastfloor(pfb2.getTargetX());
                    int ty = PZMath.fastfloor(pfb2.getTargetY());
                    IsoChunk chunk = GameServer.server ? ServerMap.instance.getChunk(tx / 8, ty / 8) : IsoWorld.instance.currentCell.getChunkForGridSquare(tx, ty, 0);
                    this.isPlayerMoving = true;
                    this.setJustMoved(true);
                }
                this.deltaX = 0.0f;
                this.deltaY = 0.0f;
            }
            if (!this.isPlayerMoving || this.isAiming() || this.isCurrentState(SwipeStatePlayer.instance()) || this.isCurrentState(PlayerStrafeState.instance()) || this.isCurrentState(PlayerDraggingCorpse.instance())) {
                this.DirectionFromVector(networkAi.direction);
                this.setForwardDirection(networkAi.direction);
                tempo.set(target.x - this.getNextX(), -(target.y - this.getNextY()));
                tempo.normalize();
                float angle = this.getAnimationPlayer().getRenderedAngle();
                if ((double)angle > Math.PI * 2) {
                    angle = (float)((double)angle - Math.PI * 2);
                }
                if (angle < 0.0f) {
                    angle = (float)((double)angle + Math.PI * 2);
                }
                tempo.rotate(angle);
                tempo.setLength(Math.min(IsoUtils.DistanceTo(target.x, target.y, this.getX(), this.getY()), 1.0f));
                this.deltaX = IsoPlayer.tempo.x;
                this.deltaY = IsoPlayer.tempo.y;
            }
            if (this.isAnimal() && networkAi.animalPacket.isDead() && (distance = IsoUtils.DistanceManhatten(target.x, target.y, this.getX(), this.getY())) <= 0.2f) {
                this.setHealth(0.0f);
                this.setDirectionAngle(networkAi.animalPacket.prediction.direction * 57.295776f);
            }
        }
        return true;
    }

    private void moveUnmoddedRemotePlayer() {
        if (this.getAlpha() > 0.0f) {
            this.getDeferredMovement(tempVector2_2);
            this.moveUnmodded(IsoPlayer.tempVector2_2.x, IsoPlayer.tempVector2_2.y);
        } else {
            this.setX(this.realx);
            this.setY(this.realy);
            this.setZ(this.realz);
            this.setLastX(this.realx);
            this.setLastY(this.realy);
            this.setLastZ(this.realz);
        }
    }

    private boolean updateWhileDead() {
        if (GameServer.server) {
            return false;
        }
        if (!this.isLocalPlayer()) {
            return false;
        }
        if (!this.isDead()) {
            return false;
        }
        this.setVariable("bPathfind", false);
        this.setMoving(false);
        this.isPlayerMoving = false;
        if (this.getVehicle() != null) {
            this.getVehicle().exit(this);
        }
        if (this.heartEventInstance != 0L) {
            this.getEmitter().stopSound(this.heartEventInstance);
            this.heartEventInstance = 0L;
        }
        this.stopAttackLoopSound(false);
        return true;
    }

    private void initFMODParameters() {
        FMODParameterList fmodParameters = this.getFMODParameters();
        if (this instanceof IsoAnimal) {
            fmodParameters.add(this.parameterFootstepMaterial);
            fmodParameters.add(this.parameterFootstepMaterial2);
            return;
        }
        fmodParameters.add(this.parameterCharacterMoving);
        fmodParameters.add(this.parameterCharacterMovementSpeed);
        fmodParameters.add(this.parameterCharacterOnFire);
        fmodParameters.add(this.parameterCharacterVoiceType);
        fmodParameters.add(this.parameterCharacterVoicePitch);
        fmodParameters.add(this.parameterDeaf);
        fmodParameters.add(this.parameterDragMaterial);
        fmodParameters.add(this.parameterElevation);
        fmodParameters.add(this.parameterEquippedBaggageContainer);
        fmodParameters.add(this.parameterExercising);
        fmodParameters.add(this.parameterFirearmDistance);
        fmodParameters.add(this.parameterFirearmInside);
        fmodParameters.add(this.parameterFirearmRoomSize);
        fmodParameters.add(this.parameterFootstepMaterial);
        fmodParameters.add(this.parameterFootstepMaterial2);
        fmodParameters.add(this.parameterIsStashTile);
        fmodParameters.add(this.parameterLocalPlayer);
        fmodParameters.add(this.parameterMeleeHitSurface);
        fmodParameters.add(this.parameterOverlapFoliageType);
        fmodParameters.add(this.parameterPlayerHealth);
        fmodParameters.add(this.parameterShoeType);
        fmodParameters.add(this.parameterVehicleHitLocation);
    }

    public ParameterCharacterMovementSpeed getParameterCharacterMovementSpeed() {
        return this.parameterCharacterMovementSpeed;
    }

    public void setMeleeHitSurface(ParameterMeleeHitSurface.Material material) {
        this.parameterMeleeHitSurface.setMaterial(material);
    }

    public void setMeleeHitSurface(String material) {
        this.parameterMeleeHitSurface.setMaterial(ParameterMeleeHitSurface.getMaterialFromString(material));
    }

    @Override
    public void setVehicleHitLocation(BaseVehicle vehicle) {
        ParameterVehicleHitLocation.HitLocation location = ParameterVehicleHitLocation.calculateLocation(vehicle, this.getX(), this.getY(), this.getZ());
        this.parameterVehicleHitLocation.setLocation(location);
    }

    private void updateHeartSound() {
        boolean playHeartbeats;
        if (GameServer.server) {
            return;
        }
        if (!this.isLocalPlayer()) {
            return;
        }
        GameSound gameSound = GameSounds.getSound("HeartBeat");
        boolean bl = playHeartbeats = gameSound != null && gameSound.getUserVolume() > 0.0f && this.stats.get(CharacterStat.PANIC) > 0.0f;
        if (!this.asleep && playHeartbeats && GameTime.getInstance().getTrueMultiplier() == 1.0f) {
            this.heartDelay -= GameTime.getInstance().getThirtyFPSMultiplier();
            if (this.heartEventInstance == 0L || !this.getEmitter().isPlaying(this.heartEventInstance)) {
                this.heartEventInstance = this.getEmitter().playSoundImpl("HeartBeat", null);
                this.getEmitter().setVolume(this.heartEventInstance, 0.0f);
            }
            if (this.heartDelay <= 0.0f) {
                this.heartDelay = this.heartDelayMax = (float)((int)((1.0f - this.stats.get(CharacterStat.PANIC) / 100.0f * 0.7f) * 25.0f)) * 2.0f;
                if (this.heartEventInstance != 0L) {
                    this.getEmitter().setVolume(this.heartEventInstance, this.stats.get(CharacterStat.PANIC) / 100.0f);
                }
            }
        } else if (this.heartEventInstance != 0L) {
            this.getEmitter().setVolume(this.heartEventInstance, 0.0f);
        }
    }

    private void updateEquippedBaggageContainer() {
        if (GameServer.server) {
            return;
        }
        if (!this.isLocalPlayer()) {
            return;
        }
        InventoryItem item = this.getClothingItem_Back();
        if (item != null && item.IsInventoryContainer()) {
            String paramStr = item.getSoundParameter("EquippedBaggageContainer");
            this.parameterEquippedBaggageContainer.setContainerType(paramStr);
            return;
        }
        item = this.getSecondaryHandItem();
        if (item != null && item.IsInventoryContainer()) {
            String paramStr = item.getSoundParameter("EquippedBaggageContainer");
            this.parameterEquippedBaggageContainer.setContainerType(paramStr);
            return;
        }
        item = this.getPrimaryHandItem();
        if (item != null && item.IsInventoryContainer()) {
            String paramStr = item.getSoundParameter("EquippedBaggageContainer");
            this.parameterEquippedBaggageContainer.setContainerType(paramStr);
            return;
        }
        this.parameterEquippedBaggageContainer.setContainerType(ParameterEquippedBaggageContainer.ContainerType.None);
    }

    @Override
    public void DoFootstepSound(String type) {
        ParameterCharacterMovementSpeed.MovementType movementType = ParameterCharacterMovementSpeed.MovementType.Walk;
        float speed = 0.5f;
        switch (type) {
            case "sneak_walk": {
                speed = 0.25f;
                movementType = ParameterCharacterMovementSpeed.MovementType.SneakWalk;
                break;
            }
            case "sneak_run": {
                speed = 0.25f;
                movementType = ParameterCharacterMovementSpeed.MovementType.SneakRun;
                break;
            }
            case "strafe": {
                speed = 0.5f;
                movementType = ParameterCharacterMovementSpeed.MovementType.Strafe;
                break;
            }
            case "walk": {
                speed = 0.5f;
                movementType = ParameterCharacterMovementSpeed.MovementType.Walk;
                break;
            }
            case "run": {
                speed = 0.75f;
                movementType = ParameterCharacterMovementSpeed.MovementType.Run;
                break;
            }
            case "sprint": {
                speed = 1.0f;
                movementType = ParameterCharacterMovementSpeed.MovementType.Sprint;
            }
        }
        this.parameterCharacterMovementSpeed.setMovementType(movementType);
        this.DoFootstepSound(speed);
    }

    private void updateHeavyBreathing() {
    }

    public long playGainExperienceLevelSound() {
        if (!this.isLocalPlayer()) {
            return 0L;
        }
        String soundName = SoundKey.GAIN_EXPERIENCE_LEVEL.getSoundName();
        if (!this.getEmitter().isPlaying(soundName)) {
            return this.getEmitter().playSoundImpl(soundName, null);
        }
        return 0L;
    }

    public long playerVoiceSound(String suffix) {
        String prefix = this.descriptor.getVoicePrefix();
        if (this.getEmitter().isPlaying(prefix + suffix)) {
            return 0L;
        }
        return this.getEmitter().playVocals(prefix + suffix);
    }

    public long transmitPlayerVoiceSound(String suffix) {
        String prefix = this.descriptor.getVoicePrefix();
        if (this.getEmitter().isPlaying(prefix + suffix)) {
            return 0L;
        }
        if (GameClient.client && (!this.isInvisible() || DebugOptions.instance.character.debug.playSoundWhenInvisible.getValue())) {
            INetworkPacket.send(PacketTypes.PacketType.PlaySound, suffix, (byte)2, this);
        }
        return this.getEmitter().playVocals(prefix + suffix);
    }

    public long stopPlayerVoiceSound(String suffix) {
        String prefix = this.descriptor.getVoicePrefix();
        return this.getEmitter().stopSoundByName(prefix + suffix);
    }

    public void updateVocalProperties() {
        if (GameServer.server) {
            return;
        }
        if (!this.isLocal()) {
            return;
        }
        if (this.vocalEvent != 0L && !this.getEmitter().isPlaying(this.vocalEvent)) {
            this.vocalEvent = 0L;
            if (this.parameterMoodles != null) {
                this.parameterMoodles.reset();
            }
        }
        if (this.vocalEvent == 0L && this.isAlive()) {
            this.vocalEvent = this.playSoundLocal(this.descriptor.getVoicePrefix());
            if (this.parameterMoodles != null) {
                this.parameterMoodles.reset();
            }
        }
        if (this.vocalEvent != 0L) {
            if (this.parameterMoodles == null) {
                this.parameterMoodles = new ParameterMoodles(this);
            }
            this.parameterMoodles.update(this.vocalEvent);
        }
    }

    private void updateDraggingCorpseSounds() {
        if (this.isAnimal()) {
            return;
        }
        boolean bPickingUp = this.getWrappedGrappleable().isPickingUpBody();
        boolean bPuttingDown = this.getWrappedGrappleable().isPuttingDownBody();
        boolean bPlayDrag = false;
        boolean bPlayTurn = false;
        if (this.isDraggingCorpse() && !bPickingUp && !bPuttingDown) {
            if (this.isPlayerMoving() && this.getBeenMovingFor() > 20.0f) {
                bPlayDrag = true;
            }
            if (this.isTurning()) {
                bPlayTurn = true;
            }
        }
        if (bPlayDrag) {
            if (!this.getEmitter().isPlaying("CorpseDrag")) {
                this.getEmitter().playSoundImpl("CorpseDrag", null);
            }
        } else if (this.getEmitter().isPlaying("CorpseDrag")) {
            this.getEmitter().stopOrTriggerSoundByName("CorpseDrag");
        }
        if (bPlayTurn) {
            if (!this.getEmitter().isPlaying("CorpseDragTurn")) {
                this.playSoundLocal("CorpseDragTurn");
            }
        } else if (this.getEmitter().isPlaying("CorpseDragTurn")) {
            this.getEmitter().stopOrTriggerSoundByName("CorpseDragTurn");
        }
    }

    private void updateAttackLoopSound() {
        int ammoCount;
        if (!LoopedRangedWeaponSounds.INSTANCE.isPlaying(this)) {
            return;
        }
        InventoryItem inventoryItem = this.getPrimaryHandItem();
        if (!(inventoryItem instanceof HandWeapon)) {
            this.stopAttackLoopSound(false);
            return;
        }
        HandWeapon weapon = (HandWeapon)inventoryItem;
        int n = ammoCount = this.isUnlimitedAmmo() ? weapon.getMaxAmmo() : weapon.getCurrentAmmoCount();
        if (!weapon.isAimedFirearm() || ammoCount == 0 || weapon.isJammed()) {
            this.stopAttackLoopSound(false);
            return;
        }
        if (!this.isAiming() || !this.isAnyAimKeyDown() || !this.isAttackButtonDown() || this.isDoShove()) {
            this.stopAttackLoopSound(false);
        }
        LoopedRangedWeaponSounds.INSTANCE.setPosition(this, this.getX(), this.getY(), this.getZ());
        LoopedRangedWeaponSounds.INSTANCE.setParameters(this, this.parameterFirearmInside.calculateCurrentValue(), this.parameterFirearmRoomSize.calculateCurrentValue());
    }

    private void updateBringToBearSound() {
        boolean bAiming;
        boolean bl = bAiming = this.isWeaponReady() && this.isAiming();
        if (bAiming && !this.aimingWeaponAnimation) {
            HandWeapon weapon;
            this.aimingWeaponAnimation = true;
            InventoryItem inventoryItem = this.getPrimaryHandItem();
            if (inventoryItem instanceof HandWeapon && (weapon = (HandWeapon)inventoryItem).getBringToBearSound() != null) {
                this.getEmitter().playSoundImpl(weapon.getBringToBearSound(), null);
            }
        } else if (!bAiming && this.aimingWeaponAnimation) {
            HandWeapon weapon;
            this.aimingWeaponAnimation = false;
            InventoryItem inventoryItem = this.getPrimaryHandItem();
            if (inventoryItem instanceof HandWeapon && (weapon = (HandWeapon)inventoryItem).getAimReleaseSound() != null) {
                this.getEmitter().playSoundImpl(weapon.getAimReleaseSound(), null);
            }
        }
    }

    @UsedFromLua
    public boolean isPlayingAttackLoopSound(String soundName) {
        return LoopedRangedWeaponSounds.INSTANCE.isPlaying(this, soundName);
    }

    @UsedFromLua
    public void startAttackLoopSound(String soundName) {
        int ammoCount;
        this.stopAttackLoopSound(true);
        InventoryItem inventoryItem = this.getPrimaryHandItem();
        if (!(inventoryItem instanceof HandWeapon)) {
            return;
        }
        HandWeapon weapon = (HandWeapon)inventoryItem;
        int n = ammoCount = this.isUnlimitedAmmo() ? weapon.getMaxAmmo() : weapon.getCurrentAmmoCount();
        if (!weapon.isAimedFirearm() || ammoCount == 0 || weapon.isJammed()) {
            return;
        }
        if (GameClient.client) {
            INetworkPacket.send(GameClient.connection, PacketTypes.PacketType.LoopedRangedWeaponSound, false, this.getOnlineID(), soundName, Float.valueOf(this.getX()), Float.valueOf(this.getY()), Float.valueOf(this.getZ()), Float.valueOf(this.parameterFirearmInside.calculateCurrentValue()), Float.valueOf(this.parameterFirearmRoomSize.calculateCurrentValue()));
        }
        LoopedRangedWeaponSounds.INSTANCE.setPosition(this, this.getX(), this.getY(), this.getZ());
        LoopedRangedWeaponSounds.INSTANCE.setParameters(this, this.parameterFirearmInside.calculateCurrentValue(), this.parameterFirearmRoomSize.calculateCurrentValue());
        LoopedRangedWeaponSounds.INSTANCE.startAttackLoopSound(this, soundName);
    }

    private void stopAttackLoopSound(boolean cancelPrevious) {
        if (!LoopedRangedWeaponSounds.INSTANCE.isPlaying(this)) {
            return;
        }
        if (GameClient.client) {
            INetworkPacket.send(PacketTypes.PacketType.LoopedRangedWeaponSound, true, this.getOnlineID(), cancelPrevious);
        }
        LoopedRangedWeaponSounds.INSTANCE.stopAttackLoopSound(this, cancelPrevious);
    }

    @UsedFromLua
    public long playRangedWeaponShootSound(String soundName) {
        if (GameClient.client) {
            INetworkPacket.send(GameClient.connection, PacketTypes.PacketType.RangedWeaponSound, soundName, Float.valueOf(this.getX()), Float.valueOf(this.getY()), Float.valueOf(this.getZ()), this.getOnlineID(), Float.valueOf(this.parameterFirearmInside.calculateCurrentValue()), Float.valueOf(this.parameterFirearmRoomSize.calculateCurrentValue()));
        }
        return this.getEmitter().playSoundImpl(soundName, null);
    }

    @Override
    public void playBloodSplatterSound() {
        if (this.isDead()) {
            return;
        }
        super.playBloodSplatterSound();
    }

    private void checkVehicleContainers() {
        ArrayList<VehicleContainer> containers = this.vehicleContainerData.tempContainers;
        containers.clear();
        int x1 = PZMath.fastfloor(this.getX()) - 4;
        int y1 = PZMath.fastfloor(this.getY()) - 4;
        int x2 = PZMath.fastfloor(this.getX()) + 4;
        int y2 = PZMath.fastfloor(this.getY()) + 4;
        int chunkX1 = x1 / 8;
        int chunkY1 = y1 / 8;
        int chunkX2 = (int)Math.ceil((float)x2 / 8.0f);
        int chunkY2 = (int)Math.ceil((float)y2 / 8.0f);
        for (int y = chunkY1; y < chunkY2; ++y) {
            for (int x = chunkX1; x < chunkX2; ++x) {
                IsoChunk chunk;
                IsoChunk isoChunk = chunk = GameServer.server ? ServerMap.instance.getChunk(x, y) : IsoWorld.instance.currentCell.getChunkForGridSquare(x * 8, y * 8, 0);
                if (chunk == null) continue;
                for (int i = 0; i < chunk.vehicles.size(); ++i) {
                    BaseVehicle vehicle = chunk.vehicles.get(i);
                    VehicleScript script = vehicle.getScript();
                    if (script == null) continue;
                    for (int partIndex = 0; partIndex < script.getPartCount(); ++partIndex) {
                        VehicleScript.Part scriptPart = script.getPart(partIndex);
                        if (scriptPart.container == null || scriptPart.area == null || !vehicle.isInArea(scriptPart.area, this)) continue;
                        VehicleContainer container = this.vehicleContainerData.freeContainers.isEmpty() ? new VehicleContainer() : this.vehicleContainerData.freeContainers.pop();
                        containers.add(container.set(vehicle, partIndex));
                    }
                }
            }
        }
        if (containers.size() != this.vehicleContainerData.containers.size()) {
            this.vehicleContainerData.freeContainers.addAll(this.vehicleContainerData.containers);
            this.vehicleContainerData.containers.clear();
            this.vehicleContainerData.containers.addAll(containers);
            LuaEventManager.triggerEvent("OnContainerUpdate");
        } else {
            for (int i = 0; i < containers.size(); ++i) {
                VehicleContainer container2;
                VehicleContainer container1 = containers.get(i);
                if (container1.equals(container2 = this.vehicleContainerData.containers.get(i))) continue;
                this.vehicleContainerData.freeContainers.addAll(this.vehicleContainerData.containers);
                this.vehicleContainerData.containers.clear();
                this.vehicleContainerData.containers.addAll(containers);
                LuaEventManager.triggerEvent("OnContainerUpdate");
                break;
            }
        }
    }

    public ByteBufferWriter createPlayerStats(ByteBufferWriter b, String adminUsername) {
        b.putShort(this.getOnlineID());
        b.putUTF(adminUsername);
        b.putUTF(this.getDisplayName());
        b.putUTF(this.getDescriptor().getForename());
        b.putUTF(this.getDescriptor().getSurname());
        b.putUTF(this.getDescriptor().getCharacterProfession().toString());
        if (b.putBoolean(!StringUtils.isNullOrEmpty(this.getTagPrefix()))) {
            b.putUTF(this.getTagPrefix());
        }
        b.putBoolean(this.isAllChatMuted());
        b.putFloat(this.getTagColor().r);
        b.putFloat(this.getTagColor().g);
        b.putFloat(this.getTagColor().b);
        b.putBoolean(this.showTag);
        b.putBoolean(this.factionPvp);
        return b;
    }

    public String setPlayerStats(ByteBufferReader bb, String adminUsername) {
        String displayName = bb.getUTF();
        String forname = bb.getUTF();
        String surname = bb.getUTF();
        CharacterProfession characterProfession = CharacterProfession.get(ResourceLocation.of(bb.getUTF()));
        CharacterProfessionDefinition characterProfessionDefinition = CharacterProfessionDefinition.getCharacterProfessionDefinition(characterProfession);
        String tag = "";
        if (bb.getBoolean()) {
            tag = bb.getUTF();
        }
        boolean chatMuted = bb.getBoolean();
        float r = bb.getFloat();
        float g = bb.getFloat();
        float b = bb.getFloat();
        Object txt = "";
        this.setTagColor(new ColorInfo(r, g, b, 1.0f));
        this.setTagPrefix(tag);
        this.showTag = bb.getBoolean();
        this.factionPvp = bb.getBoolean();
        if (!forname.equals(this.getDescriptor().getForename())) {
            txt = GameServer.server ? adminUsername + " Changed " + displayName + " forname in " + forname : "Changed your forname in " + forname;
        }
        this.getDescriptor().setForename(forname);
        if (!surname.equals(this.getDescriptor().getSurname())) {
            txt = GameServer.server ? adminUsername + " Changed " + displayName + " surname in " + surname : "Changed your surname in " + surname;
        }
        this.getDescriptor().setSurname(surname);
        if (!characterProfession.equals(this.getDescriptor().getCharacterProfession())) {
            txt = GameServer.server ? adminUsername + " Changed " + displayName + " profession to " + characterProfessionDefinition.getUIName() : "Changed your profession in " + characterProfessionDefinition.getUIName();
        }
        this.getDescriptor().setCharacterProfession(characterProfession);
        if (!this.getDisplayName().equals(displayName)) {
            if (GameServer.server) {
                txt = adminUsername + " Changed display name \"" + this.getDisplayName() + "\" to \"" + displayName + "\"";
                ServerWorldDatabase.instance.updateDisplayName(this.username, displayName);
            } else {
                txt = "Changed your display name to " + displayName;
            }
            this.setDisplayName(displayName);
        }
        if (chatMuted != this.isAllChatMuted()) {
            txt = chatMuted ? (GameServer.server ? adminUsername + " Banned " + displayName + " from using /all chat" : "Banned you from using /all chat") : (GameServer.server ? adminUsername + " Allowed " + displayName + " to use /all chat" : "Now allowed you to use /all chat");
        }
        this.setAllChatMuted(chatMuted);
        if (GameServer.server && !((String)txt).isEmpty()) {
            LoggerManager.getLogger("admin").write((String)txt);
        }
        if (GameClient.client) {
            LuaEventManager.triggerEvent("OnMiniScoreboardUpdate");
        }
        return txt;
    }

    public boolean isAllChatMuted() {
        return this.allChatMuted;
    }

    public void setAllChatMuted(boolean allChatMuted) {
        this.allChatMuted = allChatMuted;
    }

    @Deprecated
    public String getAccessLevel() {
        return this.role == null ? "none" : this.role.getName();
    }

    public Role getRole() {
        return this.role;
    }

    public boolean isAccessLevel(String level) {
        return this.getAccessLevel().equalsIgnoreCase(level);
    }

    public void setRole(String newLvl) {
        if (GameClient.client) {
            Role newRole = Roles.getRole(newLvl.trim().toLowerCase());
            GameClient.SendCommandToServer("/setaccesslevel \"" + this.username + "\" \"" + newRole.getName() + "\"");
        }
    }

    public void addMechanicsItem(String itemid, VehiclePart part, Long milli) {
        int minExp = 1;
        int maxExp = 1;
        if (this.mechanicsItem.get(Long.parseLong(itemid)) == null && !GameClient.client) {
            if (part.getTable("uninstall") != null && part.getTable("uninstall").rawget("skills") != null) {
                String[] skills;
                for (String fSkill : skills = ((String)part.getTable("uninstall").rawget("skills")).split(";")) {
                    if (!fSkill.contains("Mechanics")) continue;
                    int number = Integer.parseInt(fSkill.split(":")[1]);
                    if (number >= 6) {
                        minExp = 3;
                        maxExp = 7;
                    } else if (number >= 4) {
                        minExp = 3;
                        maxExp = 5;
                    } else if (number >= 2) {
                        minExp = 2;
                        maxExp = 4;
                    } else if (Rand.Next(3) == 0) {
                        minExp = 2;
                        maxExp = 2;
                    }
                    minExp *= 2;
                    maxExp *= 2;
                }
            }
            if (GameServer.server) {
                GameServer.addXp(this, PerkFactory.Perks.Mechanics, Rand.Next(minExp, maxExp));
            } else {
                this.getXp().AddXP(PerkFactory.Perks.Mechanics, (float)Rand.Next(minExp, maxExp));
            }
        }
        this.mechanicsItem.put(Long.parseLong(itemid), milli);
    }

    private void updateTemperatureCheck() {
        int hypo = this.moodles.getMoodleLevel(MoodleType.HYPOTHERMIA);
        if (this.hypothermiaCache == -1 || this.hypothermiaCache != hypo) {
            if (hypo >= 3 && hypo > this.hypothermiaCache && this.isAsleep() && !this.forceWakeUp) {
                this.forceAwake();
            }
            this.hypothermiaCache = hypo;
        }
        int hyper = this.moodles.getMoodleLevel(MoodleType.HYPERTHERMIA);
        if (this.hyperthermiaCache == -1 || this.hyperthermiaCache != hyper) {
            if (hyper >= 3 && hyper > this.hyperthermiaCache && this.isAsleep() && !this.forceWakeUp) {
                this.forceAwake();
            }
            this.hyperthermiaCache = hyper;
        }
    }

    public float getZombieRelevenceScore(IsoZombie z) {
        if (z.getCurrentSquare() == null) {
            return -10000.0f;
        }
        float score = 0.0f;
        if (z.getCurrentSquare().getCanSee(this.playerIndex)) {
            score += 100.0f;
        } else if (z.getCurrentSquare().isCouldSee(this.playerIndex)) {
            score += 10.0f;
        }
        if (z.getCurrentSquare().getRoom() != null && this.current.getRoom() == null) {
            score -= 20.0f;
        }
        if (z.getCurrentSquare().getRoom() == null && this.current.getRoom() != null) {
            score -= 20.0f;
        }
        if (z.getCurrentSquare().getRoom() != this.current.getRoom()) {
            score -= 20.0f;
        }
        float d = z.DistTo(this);
        score -= d;
        if (d < 20.0f) {
            score += 300.0f;
        }
        if (d < 15.0f) {
            score += 300.0f;
        }
        if (d < 10.0f) {
            score += 1000.0f;
        }
        if (z.getTargetAlpha() < 1.0f && score > 0.0f) {
            score *= z.getTargetAlpha();
        }
        return score;
    }

    @Override
    public BaseVisual getVisual() {
        return this.baseVisual;
    }

    @Override
    public HumanVisual getHumanVisual() {
        return Type.tryCastTo(this.baseVisual, HumanVisual.class);
    }

    @Override
    public AnimalVisual getAnimalVisual() {
        return Type.tryCastTo(this.baseVisual, AnimalVisual.class);
    }

    @Override
    public String getAnimalType() {
        return null;
    }

    @Override
    public float getAnimalSize() {
        return 1.0f;
    }

    @Override
    public ItemVisuals getItemVisuals() {
        return this.remotePlayerItemVisuals;
    }

    @Override
    public void getItemVisuals(ItemVisuals itemVisuals) {
        if (!this.remote || GameServer.server) {
            this.getWornItems().getItemVisuals(itemVisuals);
        } else {
            itemVisuals.clear();
            itemVisuals.addAll(this.remotePlayerItemVisuals);
        }
    }

    @Override
    public void dressInNamedOutfit(String outfitName) {
        if (this.getHumanVisual() == null) {
            return;
        }
        this.getHumanVisual().dressInNamedOutfit(outfitName, this.remotePlayerItemVisuals);
        this.onClothingOutfitPreviewChanged();
    }

    @Override
    public void dressInClothingItem(String itemGUID) {
        this.getHumanVisual().dressInClothingItem(itemGUID, this.remotePlayerItemVisuals);
        this.onClothingOutfitPreviewChanged();
    }

    private void onClothingOutfitPreviewChanged() {
        if (!this.isLocalPlayer()) {
            return;
        }
        this.getInventory().clear();
        this.wornItems.setFromItemVisuals(this.remotePlayerItemVisuals);
        this.wornItems.addItemsToItemContainer(this.getInventory());
        this.remotePlayerItemVisuals.clear();
        this.onWornItemsChanged();
    }

    @Override
    public void onWornItemsChanged() {
        this.resetModelNextFrame();
        super.onWornItemsChanged();
        this.parameterShoeType.setShoeType(null);
    }

    public Vector2 getLastAngle() {
        return this.lastAngle;
    }

    public void setLastAngle(Vector2 lastAngle) {
        this.lastAngle.set(lastAngle);
    }

    public int getDialogMood() {
        return this.dialogMood;
    }

    public void setDialogMood(int dialogMood) {
        this.dialogMood = dialogMood;
    }

    public int getPing() {
        return this.ping;
    }

    public void setPing(int ping) {
        this.ping = ping;
    }

    public IsoMovingObject getDragObject() {
        return this.dragObject;
    }

    public void setDragObject(IsoMovingObject dragObject) {
        this.dragObject = dragObject;
    }

    public float getAsleepTime() {
        return this.asleepTime;
    }

    public void setAsleepTime(float asleepTime) {
        this.asleepTime = asleepTime;
    }

    public Stack<IsoMovingObject> getSpottedList() {
        return this.spottedList;
    }

    public int getTicksSinceSeenZombie() {
        return this.ticksSinceSeenZombie;
    }

    public void setTicksSinceSeenZombie(int ticksSinceSeenZombie) {
        this.ticksSinceSeenZombie = ticksSinceSeenZombie;
    }

    public boolean isWaiting() {
        return this.waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public IsoSurvivor getDragCharacter() {
        return this.dragCharacter;
    }

    public void setDragCharacter(IsoSurvivor dragCharacter) {
        this.dragCharacter = dragCharacter;
    }

    public float getHeartDelay() {
        return this.heartDelay;
    }

    public void setHeartDelay(float heartDelay) {
        this.heartDelay = heartDelay;
    }

    public float getHeartDelayMax() {
        return this.heartDelayMax;
    }

    public void setHeartDelayMax(int heartDelayMax) {
        this.heartDelayMax = heartDelayMax;
    }

    @Override
    public double getHoursSurvived() {
        return this.hoursSurvived;
    }

    public void setHoursSurvived(double hrs) {
        this.hoursSurvived = hrs;
    }

    public float getMaxWeightDelta() {
        return this.maxWeightDelta;
    }

    public void setMaxWeightDelta(float maxWeightDelta) {
        this.maxWeightDelta = maxWeightDelta;
    }

    public boolean isbChangeCharacterDebounce() {
        return this.changeCharacterDebounce;
    }

    public void setbChangeCharacterDebounce(boolean changeCharacterDebounce) {
        this.changeCharacterDebounce = changeCharacterDebounce;
    }

    public int getFollowID() {
        return this.followId;
    }

    public void setFollowID(int followId) {
        this.followId = followId;
    }

    public boolean isbSeenThisFrame() {
        return this.seenThisFrame;
    }

    public void setbSeenThisFrame(boolean seenThisFrame) {
        this.seenThisFrame = seenThisFrame;
    }

    public boolean isbCouldBeSeenThisFrame() {
        return this.couldBeSeenThisFrame;
    }

    public void setbCouldBeSeenThisFrame(boolean couldBeSeenThisFrame) {
        this.couldBeSeenThisFrame = couldBeSeenThisFrame;
    }

    public float getTimeSinceLastStab() {
        return this.timeSinceLastStab;
    }

    public void setTimeSinceLastStab(float timeSinceLastStab) {
        this.timeSinceLastStab = timeSinceLastStab;
    }

    public Stack<IsoMovingObject> getLastSpotted() {
        return this.lastSpotted;
    }

    public void setLastSpotted(Stack<IsoMovingObject> lastSpotted) {
        this.lastSpotted = lastSpotted;
    }

    public int getClearSpottedTimer() {
        return this.clearSpottedTimer;
    }

    public void setClearSpottedTimer(int clearSpottedTimer) {
        this.clearSpottedTimer = clearSpottedTimer;
    }

    public boolean IsRunning() {
        return this.isRunning();
    }

    public void InitSpriteParts() {
    }

    public String getTagPrefix() {
        return this.tagPrefix;
    }

    public void setTagPrefix(String newTag) {
        this.tagPrefix = newTag;
    }

    public ColorInfo getTagColor() {
        return this.tagColor;
    }

    public void setTagColor(ColorInfo tagColor) {
        this.tagColor.set(tagColor);
    }

    public String getDisplayName() {
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = this.getUsername(ServerOptions.instance.showFirstAndLastName.getValue(), ServerOptions.getInstance().hideDisguisedUserName.getValue() || ServerOptions.getInstance().usernameDisguises.getValue());
        }
        return this.displayName;
    }

    public String getDisguisedDisplayName() {
        if (this.displayName == null || this.displayName.isEmpty()) {
            this.displayName = this.getUsername(ServerOptions.instance.showFirstAndLastName.getValue(), ServerOptions.getInstance().hideDisguisedUserName.getValue() || ServerOptions.getInstance().usernameDisguises.getValue());
            if (this.displayName.isEmpty() && ServerOptions.getInstance().hideDisguisedUserName.getValue()) {
                this.displayName = Translator.getText("IGUI_Disguised_Player_Name", new Object[0]);
            }
        }
        return this.displayName;
    }

    public void resetDisplayName() {
        this.setDisplayName(null);
        if (GameClient.client) {
            this.updateDisguisedState();
            WorldMapRemotePlayer remotePlayer = WorldMapRemotePlayers.instance.getPlayerByID(this.getOnlineID());
            if (remotePlayer != null) {
                remotePlayer.setPlayer(this);
            }
        }
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSeeNonPvpZone() {
        return this.seeNonPvpZone || DebugOptions.instance.multiplayer.debug.seeNonPvpZones.getValue();
    }

    public boolean isSeeDesignationZone() {
        return this.seeDesignationZone;
    }

    public void setSeeDesignationZone(boolean seeMetaAnimalZone) {
        this.seeDesignationZone = seeMetaAnimalZone;
    }

    public void addSelectedZoneForHighlight(Double id) {
        if (!this.selectedZonesForHighlight.contains(id)) {
            this.selectedZonesForHighlight.add(id);
        }
    }

    public void setSelectedZoneForHighlight(Double id) {
        this.selectedZoneForHighlight = id;
    }

    public Double getSelectedZoneForHighlight() {
        return this.selectedZoneForHighlight;
    }

    public ArrayList<Double> getSelectedZonesForHighlight() {
        return this.selectedZonesForHighlight;
    }

    public void resetSelectedZonesForHighlight() {
        this.selectedZonesForHighlight.clear();
    }

    public void setSeeNonPvpZone(boolean seeNonPvpZone) {
        this.seeNonPvpZone = seeNonPvpZone;
    }

    public boolean checkZonesInterception(int x1, int x2, int y1, int y2) {
        ArrayList<NonPvpZone> nonPvpZones = NonPvpZone.getAllZones();
        for (int i = 0; i < nonPvpZones.size(); ++i) {
            NonPvpZone npz = nonPvpZones.get(i);
            if (x1 >= npz.getX2() || x2 <= npz.getX() || y1 >= npz.getY2() || y2 <= npz.getY()) continue;
            return true;
        }
        return false;
    }

    public boolean isShowTag() {
        return this.showTag;
    }

    public void setShowTag(boolean show) {
        this.showTag = show;
    }

    public boolean isFactionPvp() {
        return this.factionPvp;
    }

    public void setFactionPvp(boolean pvp) {
        this.factionPvp = pvp;
    }

    public boolean isForceOverrideAnim() {
        return this.forceOverrideAnim;
    }

    public void setForceOverrideAnim(boolean forceOverride) {
        this.forceOverrideAnim = forceOverride;
    }

    public Long getMechanicsItem(String itemId) {
        return this.mechanicsItem.get(Long.parseLong(itemId));
    }

    public boolean isWearingNightVisionGoggles() {
        return this.isWearingNightVisionGoggles;
    }

    public void setWearingNightVisionGoggles(boolean b) {
        this.isWearingNightVisionGoggles = b;
    }

    @Override
    public void OnAnimEvent(AnimLayer sender, AnimationTrack track, AnimEvent event) {
        super.OnAnimEvent(sender, track, event);
        if (this.characterActions.isEmpty()) {
            return;
        }
        BaseAction action = (BaseAction)this.characterActions.get(0);
        action.OnAnimEvent(event);
    }

    @Override
    public void setAddedToModelManager(ModelManager modelManager, boolean isAdded) {
        super.setAddedToModelManager(modelManager, isAdded);
        if (isAdded) {
            DebugFileWatcher.instance.add(this.setClothingTriggerWatcher);
        } else {
            DebugFileWatcher.instance.remove(this.setClothingTriggerWatcher);
        }
    }

    @Override
    public boolean isTimedActionInstant() {
        if ((GameClient.client || GameServer.server) && this.isAccessLevel("None")) {
            return false;
        }
        return super.isTimedActionInstant();
    }

    @Override
    public boolean isSkeleton() {
        return false;
    }

    @Override
    public void addWorldSoundUnlessInvisible(int radius, int volume, boolean bStressHumans) {
        if (this.isGhostMode()) {
            return;
        }
        super.addWorldSoundUnlessInvisible(radius, volume, bStressHumans);
    }

    private void updateFootInjuries() {
        if (this.isInvulnerable()) {
            return;
        }
        if (this.isSeatedInVehicle()) {
            this.footInjuryTimer = 0.0f;
            return;
        }
        if (GameClient.client) {
            return;
        }
        InventoryItem shoes = this.getWornItems().getItem(ItemBodyLocation.SHOES);
        if (shoes != null && shoes.getCondition() > 0) {
            return;
        }
        if (this.getCurrentSquare() == null) {
            return;
        }
        boolean sync = false;
        String soundSuffix = null;
        if (this.getCurrentSquare().getBrokenGlass() != null) {
            BodyPartType partType = BodyPartType.FromIndex(Rand.Next(BodyPartType.ToIndex(BodyPartType.Foot_L), BodyPartType.ToIndex(BodyPartType.Foot_R) + 1));
            BodyPart part = this.getBodyDamage().getBodyPart(partType);
            if (!part.isDeepWounded() || !part.haveGlass()) {
                part.generateDeepShardWound();
                soundSuffix = "PainFromGlassCut";
                sync = true;
            }
        }
        int modifier = 0;
        String zoneType = this.getCurrentSquare().getZone() == null ? null : this.getCurrentSquare().getZone().getType();
        boolean inForest = zoneType != null && zoneType.endsWith("Forest");
        IsoObject floor = this.getCurrentSquare().getFloor();
        if (floor != null && floor.getSprite() != null && floor.getSprite().getName() != null) {
            String floorName = floor.getSprite().getName();
            if (floorName.contains("blends_natural_01") && inForest) {
                modifier = 2;
            } else if (!floorName.contains("blends_natural_01") && this.getCurrentSquare().getBuilding() == null) {
                modifier = 1;
            }
        }
        if (modifier == 0) {
            this.possiblyPlayVoiceSound(soundSuffix);
            return;
        }
        float multiplier1 = GameTime.getInstance().getThirtyFPSMultiplier() * 2.0f;
        boolean isWalking1 = this.isWalking;
        if (GameServer.server) {
            boolean bl = isWalking1 = this.isPlayerMoving() && !this.isRunning() && !this.isSprinting();
        }
        if (isWalking1 && !this.isRunning() && !this.isSprinting()) {
            this.footInjuryTimer += (float)modifier * multiplier1;
        } else if (this.isRunning() && !this.isSprinting()) {
            this.footInjuryTimer += (float)modifier + 2.0f * multiplier1;
        } else if (this.isSprinting()) {
            this.footInjuryTimer += (float)modifier + 5.0f * multiplier1;
        } else {
            if (this.footInjuryTimer > 0.0f) {
                this.footInjuryTimer -= multiplier1 / 3.0f;
            }
            this.possiblyPlayVoiceSound(soundSuffix);
            return;
        }
        if (Rand.Next(Rand.AdjustForFramerate(8500 - PZMath.fastfloor(this.footInjuryTimer))) <= 0) {
            this.footInjuryTimer = 0.0f;
            BodyPartType partType = BodyPartType.FromIndex(Rand.Next(BodyPartType.ToIndex(BodyPartType.Foot_L), BodyPartType.ToIndex(BodyPartType.Foot_R) + 1));
            BodyPart part = this.getBodyDamage().getBodyPart(partType);
            if (part.getScratchTime() > 30.0f) {
                if (!part.isCut()) {
                    part.setCut(true);
                    part.setCutTime(Rand.Next(1.0f, 3.0f));
                } else {
                    part.setCutTime(part.getCutTime() + Rand.Next(1.0f, 3.0f));
                }
                soundSuffix = "PainFromGlassCut";
            } else {
                if (!part.scratched()) {
                    part.setScratched(true, true);
                    part.setScratchTime(Rand.Next(1.0f, 3.0f));
                } else {
                    part.setScratchTime(part.getScratchTime() + Rand.Next(1.0f, 3.0f));
                }
                if (part.getScratchTime() > 20.0f && part.getBleedingTime() == 0.0f) {
                    part.setBleedingTime(Rand.Next(3.0f, 10.0f));
                }
                soundSuffix = "PainFromScratch";
            }
            sync = true;
        }
        this.possiblyPlayVoiceSound(soundSuffix);
        if (GameServer.server && sync) {
            INetworkPacket.send(this, PacketTypes.PacketType.PlayerDamage, this);
        }
    }

    private float calculateInTreesSpeed() {
        float speed = this.getDescriptor().isCharacterProfession(CharacterProfession.PARK_RANGER) ? 1.3f : 1.0f;
        float f = speed = this.getDescriptor().isCharacterProfession(CharacterProfession.LUMBERJACK) ? 1.15f : speed;
        if (this.isRunning()) {
            speed *= 1.1f;
        }
        return speed;
    }

    private void updateInTreesInjuries() {
        if (!GameServer.server) {
            return;
        }
        if (this.isInvulnerable() || this.isSeatedInVehicle() || !this.isInTreesNoBush() || !this.isPlayerMoving()) {
            this.inTreesInjuryTimer = 0.0f;
            return;
        }
        float walkSpeed = this.calculateInTreesSpeed();
        this.inTreesInjuryTimer += walkSpeed * GameTime.getInstance().getThirtyFPSMultiplier() * 0.033333335f;
        if (this.inTreesInjuryTimer >= 1.33f) {
            this.damageWhileInTrees();
            this.inTreesInjuryTimer = 0.0f;
        }
    }

    private void possiblyPlayVoiceSound(String suffix) {
        if (suffix == null || this.isDead()) {
            return;
        }
        if (GameServer.server) {
            GameServer.sendCharacterSound(this, suffix, (byte)2);
        } else if (!GameClient.client) {
            this.playerVoiceSound(suffix);
        }
    }

    public int getMoodleLevel(MoodleType type) {
        return this.getMoodles().getMoodleLevel(type);
    }

    public boolean isAttackStarted() {
        return this.attackStarted;
    }

    public void setAttackStarted(boolean attackStarted) {
        this.attackStarted = attackStarted;
    }

    @Override
    public boolean isBehaviourMoving() {
        return this.hasPath() || super.isBehaviourMoving();
    }

    public boolean isJustMoved() {
        if (GameServer.server) {
            NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
            return Math.abs(this.getX() - networkAi.targetX) > 0.2f || Math.abs(this.getY() - networkAi.targetY) > 0.2f;
        }
        return this.justMoved;
    }

    public void setJustMoved(boolean val) {
        this.justMoved = val;
        if (GameClient.client) {
            NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
            if (!networkAi.moved && this.justMoved) {
                networkAi.moved = true;
                GameClient.connection.setReady(true);
            }
        }
    }

    @Override
    public boolean isPlayerMoving() {
        return this.isPlayerMoving;
    }

    @Override
    public float getTimedActionTimeModifier() {
        if (this.getBodyDamage().getThermoregulator() != null) {
            return this.getBodyDamage().getThermoregulator().getTimedActionTimeModifier();
        }
        return 1.0f;
    }

    public boolean isLookingWhileInVehicle() {
        return this.getVehicle() != null && this.lookingWhileInVehicle;
    }

    public void setInitiateAttack(boolean initiate) {
        this.initiateAttack = initiate;
    }

    public boolean isInitiateAttack() {
        return this.initiateAttack;
    }

    public boolean isIgnoreContextKey() {
        return this.ignoreContextKey;
    }

    public void setIgnoreContextKey(boolean ignoreContextKey) {
        this.ignoreContextKey = ignoreContextKey;
    }

    public boolean isIgnoreAutoVault() {
        return this.ignoreAutoVault;
    }

    public void setIgnoreAutoVault(boolean ignoreAutoVault) {
        this.ignoreAutoVault = ignoreAutoVault;
    }

    public boolean isAttackType(AttackType attackType) {
        return this.attackType == attackType;
    }

    public AttackType getAttackType() {
        return this.attackType;
    }

    private String getAttackTypeAnimationKey() {
        return this.attackType.toString();
    }

    public void setAttackType(AttackType attackType) {
        this.attackType = attackType;
    }

    public boolean canSeeAll() {
        return this.getCheats().isSet(CheatType.CAN_SEE_EVERYONE);
    }

    public void setCanSeeAll(boolean b) {
        if (!Role.hasCapability(this, Capability.CanSeeAll)) {
            this.getCheats().set(CheatType.CAN_SEE_EVERYONE, false);
            return;
        }
        this.getCheats().set(CheatType.CAN_SEE_EVERYONE, b);
    }

    public boolean isCheatPlayerSeeEveryone() {
        return DebugOptions.instance.cheat.player.seeEveryone.getValue();
    }

    public float getRelevantAndDistance(float x, float y, float relevantRange) {
        if (Math.abs(this.getX() - x) <= relevantRange * 8.0f && Math.abs(this.getY() - y) <= relevantRange * 8.0f) {
            return IsoUtils.DistanceTo(this.getX(), this.getY(), x, y);
        }
        return Float.POSITIVE_INFINITY;
    }

    public boolean canHearAll() {
        return this.getCheats().isSet(CheatType.CAN_HEAR_EVERYONE);
    }

    public void setCanHearAll(boolean b) {
        if (!Role.hasCapability(this, Capability.CanHearAll)) {
            this.getCheats().set(CheatType.CAN_HEAR_EVERYONE, false);
            return;
        }
        this.getCheats().set(CheatType.CAN_HEAR_EVERYONE, b);
    }

    public ArrayList<String> getAlreadyReadBook() {
        return this.alreadyReadBook;
    }

    public void setMoodleCantSprint(boolean b) {
        this.moodleCantSprint = b;
    }

    public void setAttackFromBehind(boolean attackFromBehind) {
        this.attackFromBehind = attackFromBehind;
    }

    public boolean isAttackFromBehind() {
        return this.attackFromBehind;
    }

    @Override
    public void onKilled(IsoGameCharacter killer, HandWeapon attackingWeapon, boolean isGory) {
        super.onKilled(killer, attackingWeapon, isGory);
        if (GameServer.server) {
            if (this.isOnFire()) {
                ConnectionQueueStatistic.getInstance().playersKilledByFireToday.increase();
            }
            if (killer instanceof IsoZombie) {
                ConnectionQueueStatistic.getInstance().playersKilledByZombieToday.increase();
            }
            if (killer instanceof IsoPlayer) {
                ConnectionQueueStatistic.getInstance().playersKilledByPlayerToday.increase();
            }
        }
        if (killer == null) {
            this.DoDeath(null, null, isGory);
        } else {
            this.DoDeath(killer.getUseHandWeapon(), killer, isGory);
        }
    }

    private void onDied(IsoGameCharacter sender, IsoDeadBody body) {
        if (!GameClient.client && this.shouldBecomeZombieAfterDeath()) {
            body.reanimateLater();
        }
        if (GameServer.server) {
            this.getNetworkCharacterAI().syncDamage();
            GameServer.sendCharacterDeath(body);
        }
    }

    @Override
    public NetworkPlayerAI getNetworkCharacterAI() {
        return PZOptional.ifPresent(this.tryGetECSComponent(NetworkPlayerComponent.class), (NetworkPlayerAI)null, NetworkPlayerComponent::getNetworkAI);
    }

    @Override
    public void preupdate() {
        if (GameClient.client) {
            HitReactionNetworkAI hitReaction;
            NetworkPlayerAI networkAi = this.getNetworkCharacterAI();
            if (networkAi.isVehicleHitTimeout()) {
                networkAi.hitByVehicle();
            }
            if (networkAi.isDeadBodyTimeout()) {
                networkAi.onDied();
            }
            if (!this.isLocal() && this.isKnockedDown() && !this.isOnFloor() && (hitReaction = this.getHitReactionNetworkAI()).isSetup() && !hitReaction.isStarted()) {
                hitReaction.start();
            }
        }
        super.preupdate();
    }

    @Override
    public boolean allowsInvisibleAnimationSkips() {
        return false;
    }

    public void setFishingStage(String stage) {
        if (!this.isVariable("FishingStage", stage)) {
            this.setVariable("FishingStage", stage);
            if (GameClient.client) {
                StateManager.enterState(this, FishingState.instance());
            }
        }
    }

    public void setFitnessSpeed() {
        this.clearVariable("FitnessStruggle");
        float speed = (float)this.getPerkLevel(PerkFactory.Perks.Fitness) / 5.0f / 1.1f - (float)this.getMoodleLevel(MoodleType.ENDURANCE) / 20.0f;
        if (speed > 1.5f) {
            speed = 1.5f;
        }
        if (speed < 0.85f) {
            speed = 1.0f;
            this.setVariable("FitnessStruggle", true);
        }
        this.setVariable("FitnessSpeed", speed);
    }

    public boolean isClimbOverWallSuccess() {
        return this.climbOverWallSuccess;
    }

    public void setClimbOverWallSuccess(boolean climbOverWallSuccess) {
        this.climbOverWallSuccess = climbOverWallSuccess;
    }

    public boolean isClimbOverWallStruggle() {
        return this.climbOverWallStruggle;
    }

    public void setClimbOverWallStruggle(boolean climbOverWallStruggle) {
        this.climbOverWallStruggle = climbOverWallStruggle;
    }

    @Override
    public boolean isSkipResolveCollision() {
        if (super.isSkipResolveCollision()) {
            return true;
        }
        if (!this.isLocal()) {
            return this.isCurrentState(PlayerFallDownState.instance()) || this.isCurrentState(BumpedState.instance()) || this.isCurrentState(PlayerKnockedDown.instance()) || this.isCurrentState(PlayerHitReactionState.instance()) || this.isCurrentState(PlayerHitReactionPVPState.instance()) || this.isCurrentState(PlayerOnGroundState.instance());
        }
        return false;
    }

    public MusicIntensityEvents getMusicIntensityEvents() {
        return this.musicIntensityEvents;
    }

    private void updateMusicIntensityEvents() {
        boolean inside;
        boolean bl = inside = this.getCurrentSquare() != null && this.getCurrentSquare().isInARoom();
        if (inside) {
            this.triggerMusicIntensityEvent("InsideBuilding");
        }
        this.musicIntensityEvents.update();
    }

    public void triggerMusicIntensityEvent(String id) {
        MusicIntensityConfig.getInstance().triggerEvent(id, this.getMusicIntensityEvents());
    }

    public MusicThreatStatuses getMusicThreatStatuses() {
        return this.musicThreatStatuses;
    }

    private void updateMusicThreatStatuses() {
        this.musicThreatStatuses.update();
    }

    public void addAttachedAnimal(IsoAnimal anim) {
        this.attachedAnimals.add(anim);
    }

    public List<IsoAnimal> getAttachedAnimals() {
        return this.attachedAnimals;
    }

    public void removeAttachedAnimal(IsoAnimal animal) {
        this.attachedAnimals.remove(animal);
    }

    public void removeAllAttachedAnimals() {
        for (int i = 0; i < this.attachedAnimals.size(); ++i) {
            this.attachedAnimals.get(i).getData().setAttachedPlayer(null);
        }
        this.attachedAnimals.clear();
    }

    public boolean hasAttachedAnimals() {
        return !this.attachedAnimals.isEmpty();
    }

    public void checkAnimalAttachedToRope(InventoryItem newPrimaryItem) {
        if (!this.hasAttachedAnimals()) {
            return;
        }
        if (this.isRopeItem(newPrimaryItem)) {
            return;
        }
        if (this.isRopeItem(this.getPrimaryHandItem())) {
            this.removeAllAttachedAnimals();
        }
    }

    private boolean isRopeItem(InventoryItem item) {
        return item != null && item.is(ItemKey.Normal.ROPE);
    }

    public void lureAnimal(InventoryItem item) {
        DesignationZoneAnimal dZone = DesignationZoneAnimal.getZoneF(this.getX(), this.getY(), this.getZ());
        if (dZone != null) {
            ArrayList<IsoAnimal> animals = dZone.getAnimals();
            for (int i = 0; i < animals.size(); ++i) {
                animals.get(i).tryLure(this, item);
            }
        }
        int radius = 10;
        for (int x = this.getSquare().x - 10; x < this.getSquare().x + 10; ++x) {
            for (int y = this.getSquare().y - 10; y < this.getSquare().y + 10; ++y) {
                IsoGridSquare sq = this.getSquare().getCell().getGridSquare(x, y, this.getSquare().z);
                if (sq == null) continue;
                ArrayList<IsoAnimal> animals = sq.getAnimals();
                for (int i = 0; i < animals.size(); ++i) {
                    animals.get(i).tryLure(this, item);
                }
            }
        }
    }

    public List<IsoAnimal> getLuredAnimals() {
        return this.luredAnimals;
    }

    public void stopLuringAnimals(boolean eatFood) {
        if (this.getPrimaryHandItem() == null) {
            return;
        }
        this.setIsLuringAnimals(false);
        for (int i = 0; i < this.getLuredAnimals().size(); ++i) {
            IsoAnimal animal = this.getLuredAnimals().get(i);
            if (eatFood) {
                animal.eatFromLured(this, this.getPrimaryHandItem());
            }
            animal.cancelLuring();
        }
    }

    public void setIsLuringAnimals(boolean luring) {
        this.isLuringAnimals = luring;
    }

    public int getVoiceType() {
        return this.getDescriptor().getVoiceType();
    }

    public void setVoiceType(int voiceType) {
        this.getDescriptor().setVoiceType(PZMath.clamp(voiceType, 0, 3));
    }

    public void setVoicePitch(float voicePitch) {
        this.getDescriptor().setVoicePitch(PZMath.clamp(voicePitch, -100.0f, 100.0f));
    }

    public boolean isFarming() {
        return this.isFarming;
    }

    public void setIsFarming(boolean isFarmingBool) {
        this.isFarming = isFarmingBool;
    }

    public boolean tooDarkToRead() {
        if (!this.isLocalPlayer()) {
            return false;
        }
        if (this.getSquare() == null) {
            return false;
        }
        int playerNum = this.getIndex();
        if (this.getPrimaryHandItem() != null && this.getPrimaryHandItem().isEmittingLight() || this.getSecondaryHandItem() != null && this.getSecondaryHandItem().isEmittingLight()) {
            return false;
        }
        for (int i = 0; i < this.getAttachedItems().size(); ++i) {
            if (!Objects.requireNonNull(this.getAttachedItems().getItemByIndex(i)).isEmittingLight()) continue;
            return false;
        }
        float light = this.getSquare().getLightLevel(playerNum);
        if (this.getVehicle() != null && (light / this.getWornItemsVisionModifier() + light) / 2.0f < 0.43f) {
            BaseVehicle vehicle = this.getVehicle();
            return vehicle.getBatteryCharge() == 0.0f;
        }
        return (light / this.getWornItemsVisionModifier() + light) / 2.0f < 0.43f;
    }

    public boolean isWalking() {
        return this.isWalking;
    }

    public boolean isInvPageDirty() {
        return this.invPageDirty;
    }

    public void setInvPageDirty(boolean b) {
        this.invPageDirty = b;
    }

    public float getVoicePitch() {
        return this.getDescriptor().getVoicePitch();
    }

    public void setCombatSpeed(float combatSpeed) {
        this.combatSpeed = combatSpeed;
    }

    public float getCombatSpeed() {
        return this.combatSpeed;
    }

    public boolean isMeleePressed() {
        return this.meleePressed;
    }

    public boolean isGrapplePressed() {
        return this.grapplePressed;
    }

    public void setRole(Role newRole) {
        if (!newRole.hasCapability(Capability.ToggleWriteRoleNameAbove)) {
            this.setShowAdminTag(false);
        }
        if (!newRole.hasCapability(Capability.UseZombieDontAttackCheat)) {
            this.setZombiesDontAttack(false);
        }
        if (!newRole.hasCapability(Capability.ToggleGodModHimself)) {
            this.setGodMod(false);
        }
        if (!newRole.hasCapability(Capability.ToggleInvisibleHimself)) {
            this.setInvisible(false);
        }
        if (!newRole.hasCapability(Capability.ToggleUnlimitedEndurance)) {
            this.setUnlimitedEndurance(false);
        }
        if (!newRole.hasCapability(Capability.ToggleUnlimitedAmmo)) {
            this.setUnlimitedAmmo(false);
        }
        if (!newRole.hasCapability(Capability.ToggleKnowAllRecipes)) {
            this.setKnowAllRecipes(false);
        }
        if (!newRole.hasCapability(Capability.ToggleUnlimitedCarry)) {
            this.setUnlimitedCarry(false);
        }
        if (!newRole.hasCapability(Capability.UseMovablesCheat)) {
            this.setMovablesCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseFastMoveCheat)) {
            this.setFastMoveCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseBuildCheat)) {
            this.setBuildCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseFarmingCheat)) {
            this.setFarmingCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseFishingCheat)) {
            this.setFishingCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseHealthCheat)) {
            this.setHealthCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseMechanicsCheat)) {
            this.setMechanicsCheat(false);
        }
        if (!newRole.hasCapability(Capability.UseTimedActionInstantCheat)) {
            this.setTimedActionInstantCheat(false);
        }
        if (!newRole.hasCapability(Capability.ToggleNoclipHimself)) {
            this.setNoClip(false);
        }
        if (!newRole.hasCapability(Capability.CanSeeAll)) {
            this.setCanSeeAll(false);
        }
        if (!newRole.hasCapability(Capability.CanHearAll)) {
            this.setCanHearAll(false);
        }
        if (!newRole.hasCapability(Capability.UseBrushToolManager)) {
            this.setCanUseBrushTool(false);
        }
        if (!newRole.hasCapability(Capability.UseLootZed)) {
            this.setCanUseLootZed(false);
        }
        if (!newRole.hasCapability(Capability.UseLootLog)) {
            this.setCanUseLootLog(false);
        }
        if (!newRole.hasCapability(Capability.UseDebugContextMenu)) {
            this.setCanUseDebugContextMenu(false);
        }
        this.role = newRole;
    }

    public boolean wasLastAttackHandToHand() {
        return this.lastAttackWasHandToHand;
    }

    public void setLastAttackWasHandToHand(boolean lastAttackWasHandToHand) {
        this.lastAttackWasHandToHand = lastAttackWasHandToHand;
    }

    public void petAnimal() {
        if (GameTime.getInstance().getCalender().getTimeInMillis() - this.lastAnimalPet > 3600000L) {
            this.lastAnimalPet = GameTime.getInstance().getCalender().getTimeInMillis();
            if (!GameClient.client) {
                this.getStats().remove(CharacterStat.STRESS, Rand.Next(0.15f, 0.35f));
                this.getStats().remove(CharacterStat.UNHAPPINESS, Rand.Next(15.0f, 35.0f));
                this.getStats().remove(CharacterStat.PAIN, Rand.Next(15.0f, 35.0f));
                this.getStats().remove(CharacterStat.BOREDOM, Rand.Next(15.0f, 35.0f));
            }
            if (GameServer.server) {
                INetworkPacket.send(this, PacketTypes.PacketType.SyncPlayerStats, this, SyncPlayerStatsPacket.getBitMaskForStat(CharacterStat.STRESS) + SyncPlayerStatsPacket.getBitMaskForStat(CharacterStat.UNHAPPINESS) + SyncPlayerStatsPacket.getBitMaskForStat(CharacterStat.PAIN) + SyncPlayerStatsPacket.getBitMaskForStat(CharacterStat.BOREDOM));
            }
        }
    }

    public IsoAnimal getUseableAnimal() {
        if (this.getCurrentSquare() == null) {
            return null;
        }
        ArrayList<IsoAnimal> animals = new ArrayList<IsoAnimal>();
        for (int x = this.getCurrentSquare().getX() - 2; x < this.getCurrentSquare().getX() + 2; ++x) {
            for (int y = this.getCurrentSquare().getY() - 2; y < this.getCurrentSquare().getY() + 2; ++y) {
                IsoGridSquare sq = this.getCurrentSquare().getCell().getGridSquare(x, y, this.getCurrentSquare().getZ());
                if (sq == null) continue;
                animals.addAll(sq.getAnimals());
            }
        }
        float closestDist = 99.0f;
        IsoAnimal closestAnimal = null;
        for (int i = 0; i < animals.size(); ++i) {
            IsoGridSquare lookingSq;
            IsoAnimal animal = (IsoAnimal)animals.get(i);
            if (animal.getCurrentSquare() == null || !((lookingSq = this.getCurrentSquare().getAdjacentSquare(this.getDir())).DistToProper(animal.getCurrentSquare()) < closestDist)) continue;
            closestAnimal = animal;
            closestDist = lookingSq.DistToProper(animal.getCurrentSquare());
        }
        return closestAnimal;
    }

    public LuaTimedActionNew getTimedActionToRetrigger() {
        return this.timedActionToRetrigger;
    }

    public void setTimedActionToRetrigger(LuaTimedActionNew timedActionToRetrigger) {
        this.timedActionToRetrigger = timedActionToRetrigger;
    }

    public PlayerCraftHistory getPlayerCraftHistory() {
        return this.craftHistory;
    }

    public boolean isFavouriteRecipe(String recipe) {
        String favString = BaseCraftingLogic.getFavouriteModDataString(recipe);
        Object isFavourite = this.getModData().rawget(favString);
        return isFavourite != null && (Boolean)isFavourite != false;
    }

    public boolean isFavouriteRecipe(CraftRecipe recipe) {
        return this.isFavouriteRecipe(recipe.getName());
    }

    public boolean isUnwanted(String item) {
        String unwantedString = IsoPlayer.getUnwantedModDataString(item);
        Object isUnwanted = this.getModData().rawget(unwantedString);
        return isUnwanted != null && (Boolean)isUnwanted != false;
    }

    public void setUnwanted(String item, Boolean unwanted) {
        String unwantedString = IsoPlayer.getUnwantedModDataString(item);
        this.getModData().rawset(unwantedString, (Object)unwanted);
        if (GameClient.client && this.isLocalPlayer()) {
            this.transmitModData();
        }
    }

    public static String getUnwantedModDataString(String item) {
        if (item != null) {
            return "itemUnwanted:" + item;
        }
        return null;
    }

    public int getTimeSinceLastNetData() {
        return this.timeSinceLastNetData;
    }

    public void setTimeSinceLastNetData(int timeSinceLastNetData) {
        this.timeSinceLastNetData = timeSinceLastNetData;
    }

    public long getLastRemoteUpdate() {
        return this.lastRemoteUpdate;
    }

    public void setLastRemoteUpdate(long lastRemoteUpdate) {
        this.lastRemoteUpdate = lastRemoteUpdate;
    }

    public boolean getAutoDrink() {
        return this.autoDrink;
    }

    public void setAutoDrink(boolean autoDrink) {
        this.autoDrink = autoDrink;
    }

    public short getAnticheatMask(UdpConnection connection) {
        boolean cheatEnable = this.cheats.isSet(CheatType.GOD_MODE);
        short mask = 0;
        if (!connection.getRole().hasCapability(Capability.ConnectWithDebug)) {
            mask = (short)(mask | 0x800);
        }
        if (!cheatEnable && System.currentTimeMillis() - this.lastCheatToggleMillis > 500L) {
            mask = (short)(mask | 0x400);
        }
        return mask;
    }

    public void setLastCheatToggleMillis(long lastCheatToggleMillis) {
        this.lastCheatToggleMillis = lastCheatToggleMillis;
    }

    public static void forEachPlayer(Invokers.Params1.ICallback<IsoPlayer> visitor) {
        for (int i = 0; i < numPlayers; ++i) {
            if (players[i] == null) continue;
            visitor.accept(players[i]);
        }
    }

    public void syncVisuals() {
        if (GameServer.server) {
            GameServer.syncVisuals(this);
            GameServer.syncHumanVisual(this);
        } else if (GameClient.client && this.isLocalPlayer() && this.onlineId != -1) {
            INetworkPacket.send(PacketTypes.PacketType.SyncClothing, this);
            INetworkPacket.send(PacketTypes.PacketType.SyncVisuals, this);
            INetworkPacket.send(PacketTypes.PacketType.HumanVisual, this);
        }
    }

    @UsedFromLua
    public IsoDeadBody findClosestCorpseOnGroundToPickup() {
        if (this.isDraggingCorpse()) {
            return null;
        }
        return this.getClosestStaticMovingObjectInNearbySquares(IsoDeadBody.class, IsoDeadBody::canPickUpBodyFromSquare, IsoDeadBody::canBeGrabbed);
    }

    static {
        numPlayers = 1;
        players = new IsoPlayer[4];
        instanceLock = "IsoPlayer.instance Lock";
        testHitPosition = new Vector2();
        followDeadCount = 240;
        tempo = new Vector2();
        tempVector2 = new Vector2();
        m_isoPlayerTriggerWatcher = new PredicatedFileWatcher(ZomboidFileSystem.instance.getMessagingDirSub("Trigger_ResetIsoPlayerModel.xml"), IsoPlayer::onTrigger_ResetIsoPlayerModel);
        tempVector2_1 = new Vector2();
        tempVector2_2 = new Vector2();
        tempVector3f = new Vector3f();
        templwjglVector3f = new org.lwjgl.util.vector.Vector3f();
        s_targetsProne = new PZArrayList<HitInfo>(HitInfo.class, 8);
        s_targetsStanding = new PZArrayList<HitInfo>(HitInfo.class, 8);
        RecentlyRemoved = new ArrayList();
        s_moveVars = new MoveVars();
    }

    public static class InputState {
        public boolean melee;
        public boolean grapple;
        public boolean isAttacking;
        public float movementRate;
        public boolean sprinting;
        public boolean isAiming;
        public boolean isCharging;
        public boolean isChargingLt;

        public void reset() {
            this.melee = false;
            this.grapple = false;
            this.isAttacking = false;
            this.movementRate = 0.0f;
            this.sprinting = false;
            this.isAiming = false;
            this.isCharging = false;
            this.isChargingLt = false;
        }

        public boolean isMoving() {
            return this.movementRate > 0.0f;
        }

        public boolean isRunning() {
            return this.movementRate > 1.95f;
        }

        public boolean isWalking() {
            return this.isMoving() && this.movementRate < 1.95f;
        }

        public void setRunning(boolean isRunning) {
            this.movementRate = isRunning ? 2.0f : 1.95f;
        }
    }

    private static class VehicleContainerData {
        private final ArrayList<VehicleContainer> tempContainers = new ArrayList();
        private final ArrayList<VehicleContainer> containers = new ArrayList();
        private final Stack<VehicleContainer> freeContainers = new Stack();

        private VehicleContainerData() {
        }
    }

    private final class GrapplerGruntChance {
        private final int baseChance = 15;
        private final int chanceIncrement = 5;
        private int gruntChance;

        private GrapplerGruntChance(IsoPlayer isoPlayer) {
            Objects.requireNonNull(isoPlayer);
            this.baseChance = 15;
            this.chanceIncrement = 5;
            this.gruntChance = 15;
        }
    }

    public static final class MoveVars {
        public float moveX;
        public float moveY;
        public float strafeX;
        public float strafeY;
        public IsoDirections newFacing;
    }

    static final class VehicleHitDamageConstants {
        static final int MIN_BODY_PARTS_DAMAGED = 2;
        static final float BODY_PART_DAMAGE_FACTOR = 0.07f;
        static final float DAMAGE_RANDOM_RANGE = 15.0f;
        static final float DAMAGE_MINIMUM = 5.0f;
        static final float FAST_HEALER_MODIFIER = 0.8f;
        static final float SLOW_HEALER_MODIFIER = 1.2f;
        static final float DAMAGE_BALANCE_FACTOR = 0.9f;
        static final float DEEP_WOUND_THRESHOLD = 40.0f;
        static final int DEEP_WOUND_CHANCE = 12;
        static final float FRACTURE_DAMAGE_THRESHOLD = 10.0f;
        static final float FRACTURE_DAMAGE_THRESHOLD_HEAD = 30.0f;
        static final float FRACTURE_DAMAGE_THRESHOLD_GROIN = 10.0f;
        static final int FRACTURE_CHANCE_PERCENT = 10;
        static final int FRACTURE_CHANCE_PERCENT_HEAD = 80;
        static final int FRACTURE_CHANCE_PERCENT_GROIN = 60;
        static final float FRACTURE_TIME_MIN = 10.0f;
        static final float FRACTURE_TIME_MIN_MAX = 10.0f;
        static final float FRACTURE_TIME_MAX = 20.0f;
        static final float FRACTURE_TIME_MAX_MAX = 30.0f;
        static final float FRACTURE_TIME_MIN_GROIN = 10.0f;
        static final float FRACTURE_TIME_MIN_GROIN_MAX = 20.0f;
        static final float FRACTURE_TIME_MAX_GROIN = 30.0f;
        static final float FRACTURE_TIME_MAX_GROIN_MAX = 40.0f;

        VehicleHitDamageConstants() {
        }

        public static float generateFractureTime(float realDamage) {
            float fractureTimeMin = Rand.Next(10.0f, realDamage + 10.0f);
            float fractureTimeMax = Rand.Next(realDamage + 20.0f, realDamage + 30.0f);
            return Rand.Next(fractureTimeMin, fractureTimeMax);
        }

        public static float generateFractureTimeGroin(float realDamage) {
            float fractureTimeMin = Rand.Next(10.0f, realDamage + 20.0f);
            float fractureTimeMax = Rand.Next(realDamage + 30.0f, realDamage + 40.0f);
            return Rand.Next(fractureTimeMin, fractureTimeMax);
        }
    }

    private static class s_performance {
        private static final PerformanceProfileProbe postUpdate = new PerformanceProfileProbe("IsoPlayer.postUpdate");
        private static final PerformanceProfileProbe update = new PerformanceProfileProbe("IsoPlayer.update");

        private s_performance() {
        }
    }

    private static class VehicleContainer {
        private BaseVehicle vehicle;
        private int containerIndex;

        private VehicleContainer() {
        }

        public VehicleContainer set(BaseVehicle vehicle, int containerIndex) {
            this.vehicle = vehicle;
            this.containerIndex = containerIndex;
            return this;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object other) {
            if (!(other instanceof VehicleContainer)) return false;
            VehicleContainer vehicleContainer = (VehicleContainer)other;
            if (this.vehicle != vehicleContainer.vehicle) return false;
            if (this.containerIndex != vehicleContainer.containerIndex) return false;
            return true;
        }
    }
}

