package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.VisorTacz;
import dev.visorcompat.tacz.Profiles;
import dev.visorcompat.tacz.Calibration;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.commands.Commands;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.vmstudio.visor.api.VisorAPI;

@Mod.EventBusSubscriber(modid = VisorTacz.ID, value = Dist.CLIENT)
public final class CompatSettings {
    private static Boolean lastSentActive;
    private static boolean lastPhysical,lastTransferAnytime,lastDescriptors;
    private static String lastCalibrationKey;
    private static Calibration lastCalibration;
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.BooleanValue PHYSICAL;
    private static final ForgeConfigSpec.BooleanValue AUTO_ADS, OPTICS, TWO_HAND_ADS, TRANSFER_ANYTIME;
    private static final ForgeConfigSpec.BooleanValue DEBUG_CUBES;
    private static final ForgeConfigSpec.BooleanValue ADVANCED,TOSS;
    private static final ForgeConfigSpec.IntValue INSPECT_WINDOW;
    private static final ForgeConfigSpec.IntValue CATCH_WINDOW;
    public enum HandlingMode { BUTTON, PHYSICAL, ADVANCED }
    public static HandlingMode handlingMode(){return !SPEC.isLoaded()?HandlingMode.BUTTON:ADVANCED.get()?HandlingMode.ADVANCED:PHYSICAL.get()?HandlingMode.PHYSICAL:HandlingMode.BUTTON;}
    public static void cycleHandling(){var modes=HandlingMode.values();setHandlingMode(modes[(handlingMode().ordinal()+1)%modes.length]);}
    public static void setHandlingMode(HandlingMode mode){
        AdvancedClient.sessionReset();PhysicalClient.reset();ClientControls.clearInput();
        ADVANCED.set(mode==HandlingMode.ADVANCED);PHYSICAL.set(mode!=HandlingMode.BUTTON);SPEC.save();forceSync();
    }
    private static boolean profilesCompatible=true;
    public static boolean descriptors(){return true;}
    public static void backendAck(boolean accepted){
        if(profilesCompatible==accepted)return;profilesCompatible=accepted;
        AdvancedClient.sessionReset();PhysicalClient.reset();ClientControls.clearInput();forceSync();
        var p=Minecraft.getInstance().player;if(p!=null&&!accepted)p.displayClientMessage(Component.literal("TaCZ VR: TOML profiles differ from server or are invalid. Compatibility paused; match profile files and reconnect."),false);
    }
    public enum Grab { USE, TRIGGER }
    public enum Display { GUN, WRIST, HUD, OFF }
    private static final ForgeConfigSpec.EnumValue<Grab> GRAB;
    private static final ForgeConfigSpec.EnumValue<Display> DISPLAY;
    static {
        var builder = new ForgeConfigSpec.Builder();
        ADVANCED=builder.comment("Experimental holster/draw, inventory-backed toss and inspection. Off by default.").define("advancedHandling",false);
        TOSS=builder.comment("Advanced Handling: true allows a catch window before auto-holstering; false returns after inspection grace. Never drops inventory items.").define("advancedToss",true);
        INSPECT_WINDOW=builder.comment("Milliseconds after releasing Grip in which Use can enter pistol inspection.").defineInRange("advancedInspectWindowMs",250,50,250);
        CATCH_WINDOW=builder.comment("Airborne gun: Grip shows a provisional hotbar radial; catch wins during this many milliseconds. 0 disables grace.").defineInRange("advancedCatchWindowMs",250,0,500);
        // Keep the original key so existing explicit OFF overrides survive upgrades.
        ENABLED = builder.comment("true = AUTO (default): follow Visor's live VR state; false = manual OFF.",
                "AUTO activates in VR and stays inactive in flatscreen, without commands.",
                "Use /visor_tacz auto to restore AUTO, or /visor_tacz off for comparison testing.")
                .define("enabled", true);
        PHYSICAL = builder.comment("Prefer physical handling where implemented; other registered guns use button controls automatically. Default: buttons.").define("physicalHandling",false);
        TRANSFER_ANYTIME=builder.comment("M700: false permits hand transfer only when bolt handling is needed; true permits transfer anytime. Applies to automatic transfer and main-hand Use.").define("m700TransferAnytime",false);
        TWO_HAND_ADS=builder.comment("Require a held support grip for automatic ADS in physical mode; geometric support in button mode.").define("twoHandAds",true);
        AUTO_ADS=builder.comment("Use physical sight alignment to drive TaCZ ADS.").define("autoAds",true);
        OPTICS=builder.comment("Experimental per-eye reticles and screen-space lens magnification; shaders unsupported.").define("vrOptics",true);
        DEBUG_CUBES=builder.comment("Show colored interaction and calibration cubes. Visual only; does not change grab zones.").define("debugCubes",true);
        GRAB=builder.comment("Contextual empty-offhand grab: USE or TRIGGER; other interactions pass through.").defineEnum("grabInput",Grab.USE);
        DISPLAY=builder.comment("Live ammo and ADS panel location: GUN, WRIST, HUD (standard GUI overlay) or OFF.").defineEnum("statusDisplay",Display.GUN);
        SPEC = builder.build();
    }
    private CompatSettings() {}
    public static boolean advanced(){return handlingMode()==HandlingMode.ADVANCED;}
    public static boolean toss(){return !SPEC.isLoaded()||TOSS.get();}
    public static int inspectWindow(){return SPEC.isLoaded()?INSPECT_WINDOW.get():250;}
    public static void setAdvanced(boolean v){setHandlingMode(v?HandlingMode.ADVANCED:HandlingMode.PHYSICAL);}
    public static void setToss(boolean v){TOSS.set(v);SPEC.save();}
    public static void setInspectWindow(int v){INSPECT_WINDOW.set(v);SPEC.save();}
    public static int catchWindow(){return SPEC.isLoaded()?CATCH_WINDOW.get():250;}
    public static void setCatchWindow(int v){CATCH_WINDOW.set(v);SPEC.save();}
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    }
    public static Grab grab(){return SPEC.isLoaded()?GRAB.get():Grab.USE;}
    public static Display display(){return SPEC.isLoaded()?DISPLAY.get():Display.GUN;}
    public static void setGrab(Grab value){ClientControls.clearInput();GRAB.set(value);SPEC.save();}
    public static void setDisplay(Display value){DISPLAY.set(value);SPEC.save();}
    public static void setPhysical(boolean value){setHandlingMode(value?HandlingMode.PHYSICAL:HandlingMode.BUTTON);}
    public static void setTwoHandAds(boolean value){TWO_HAND_ADS.set(value);SPEC.save();}
    public static void setAutoAds(boolean value){AUTO_ADS.set(value);SPEC.save();}
    public static void setOptics(boolean value){OPTICS.set(value);SPEC.save();}
    public static void setDebugCubes(boolean value){DEBUG_CUBES.set(value);SPEC.save();}
    public static boolean debugCubes(){return !SPEC.isLoaded() || DEBUG_CUBES.get();}
    public static boolean transferAnytime(){return SPEC.isLoaded()&&TRANSFER_ANYTIME.get();}
    public static void setTransferAnytime(boolean value){ClientControls.clearInput();TRANSFER_ANYTIME.set(value);SPEC.save();syncState();}
    public static boolean twoHandAds(){return !SPEC.isLoaded()||TWO_HAND_ADS.get();}
    public static boolean autoAds() {return !SPEC.isLoaded() || AUTO_ADS.get();}
    public static boolean optics() {return !SPEC.isLoaded() || OPTICS.get();}
    public static boolean physical() { return handlingMode()!=HandlingMode.BUTTON; }
    public static boolean followsVisor() { return !SPEC.isLoaded() || ENABLED.get(); }
    public static boolean active() {
        return profilesCompatible && followsVisor() && VisorAPI.clientState().stateMode().isActive();
    }
    public static String handlingLabel(){
        if(!physical())return "BUTTON";
        var player=Minecraft.getInstance().player;
        return player!=null&&Profiles.get(player.getMainHandItem())!=null&&!Profiles.physical(player.getMainHandItem())?(advanced()?"ADVANCED (button reload)":"BUTTON (AUTO)"):handlingMode().name();
    }
    private static String statusText() {
        if (!followsVisor()) {
            return "TaCZ VR: OFF (manual override). Use /visor_tacz auto to follow Visor again.";
        }
        return "TaCZ VR: AUTO — " + (active()
                ? "active (Visor VR is active). Handling: "+handlingLabel()+". ADS: " + (AutoAds.aiming()?"aiming":"lowered")
                : "inactive (Visor VR is not active; normal TaCZ behavior).");
    }
    private static void set(boolean enabled) {
        ENABLED.set(enabled);
        SPEC.save();
        syncState();
        ClientControls.clearInput();
    }
    static void forceSync() {lastSentActive=null;lastCalibrationKey=null;syncState();}
    static boolean reloadCalibration() {
        try {
            InspectionStore.reload();HolsterStore.reload();CalibrationStore.reload();ClientControls.clearInput();forceSync();
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Reloaded gun, holster and inspection calibration; bundled defaults fill missing profiles."));
            return true;
        } catch(java.io.IOException e) {
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(e.getMessage()+" The failing file was not applied."));
            return false;
        }
    }
    static void syncState() {
        if (Minecraft.getInstance().getConnection() == null || Minecraft.getInstance().player == null) {
            lastSentActive = null;
            lastCalibrationKey = null;
            return;
        }
        var stack = Minecraft.getInstance().player.getMainHandItem();
        if (Profiles.get(stack) != null) {
            String key = Profiles.key(stack);
            Calibration calibration = CalibrationStore.get(key);
            if (!key.equals(lastCalibrationKey) || !calibration.equals(lastCalibration)) {
                CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.Grip(key, calibration));
                lastCalibrationKey=key;
                lastCalibration=calibration;
            }
        }
        boolean current = active();
        if (lastSentActive == null || lastSentActive != current || lastPhysical != physical() || lastTransferAnytime != transferAnytime() || lastDescriptors != descriptors()) {
            CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.Mode(current,physical(),transferAnytime(),descriptors(),descriptors()?dev.visorcompat.tacz.DescriptorProfiles.digest():""));
            lastSentActive = current;
            lastPhysical = physical();lastTransferAnytime=transferAnytime();lastDescriptors=descriptors();
            ClientControls.clearInput();
        }
    }
    @SubscribeEvent public static void login(ClientPlayerNetworkEvent.LoggingIn event) {
        profilesCompatible=true;lastSentActive = null;
        lastCalibrationKey = null;
        syncState();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ShotVisual.clear();AdvancedClient.sessionReset();AdvancedRemote.clear();
        lastSentActive = null;
        ClientControls.clearInput();
        PhysicalClient.reset();
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) syncState();
    }
    @SubscribeEvent public static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("visor_tacz")
                .executes(context -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(statusText()));
                    return 1;
                })
                .then(Commands.literal("menu").executes(c->{Minecraft.getInstance().tell(()->Minecraft.getInstance().setScreen(new ControlsScreen()));return 1;}))
                .then(Commands.literal("reset_input").executes(c->{AdvancedClient.sessionReset();ClientControls.clearInput();forceSync();Minecraft.getInstance().gui.getChat().addMessage(Component.literal("TaCZ VR input and hotbar catch ownership reset."));return 1;}))
                .then(Commands.literal("recenter_holster").executes(c->{AdvancedClient.recenterHolster();return 1;}))
                .then(Commands.literal("reload_calibration").executes(c->reloadCalibration()?1:0))
                .then(Commands.literal("grab")
                    .then(Commands.literal("use").executes(c->{setGrab(Grab.USE);return 1;}))
                    .then(Commands.literal("trigger").executes(c->{setGrab(Grab.TRIGGER);return 1;})))
                .then(Commands.literal("display")
                    .then(Commands.literal("gun").executes(c->{setDisplay(Display.GUN);return 1;}))
                    .then(Commands.literal("wrist").executes(c->{setDisplay(Display.WRIST);return 1;}))
                    .then(Commands.literal("hud").executes(c->{setDisplay(Display.HUD);return 1;}))
                    .then(Commands.literal("off").executes(c->{setDisplay(Display.OFF);return 1;})))
                .then(Commands.literal("ads")
                    .then(Commands.literal("auto").executes(c->{AUTO_ADS.set(true);SPEC.save();c.getSource().sendSuccess(()->Component.literal("Physical ADS enabled."),false);return 1;}))
                    .then(Commands.literal("off").executes(c->{AUTO_ADS.set(false);SPEC.save();c.getSource().sendSuccess(()->Component.literal("Physical ADS disabled."),false);return 1;})))
                .then(Commands.literal("optics")
                    .then(Commands.literal("on").executes(c->{OPTICS.set(true);SPEC.save();c.getSource().sendSuccess(()->Component.literal("VR optics enabled."),false);return 1;}))
                    .then(Commands.literal("off").executes(c->{OPTICS.set(false);SPEC.save();c.getSource().sendSuccess(()->Component.literal("VR optics disabled."),false);return 1;})))
                .then(Commands.literal("handling")
                    .then(Commands.literal("physical").executes(context -> {
                        PHYSICAL.set(true);SPEC.save();syncState();ClientControls.clearInput();
                        context.getSource().sendSuccess(()->Component.literal("Physical handling preferred. Guns without a physical profile automatically use button handling."),false);return 1;
                    }))
                    .then(Commands.literal("buttons").executes(context -> {
                        PHYSICAL.set(false);SPEC.save();PhysicalClient.reset();syncState();ClientControls.clearInput();
                        context.getSource().sendSuccess(()->Component.literal("Button handling restored."),false);return 1;
                    })))
                .then(Commands.literal("calibrate").executes(context -> {
                    var mc = Minecraft.getInstance();
                    if (mc.player == null || Profiles.get(mc.player.getMainHandItem()) == null) {
                        context.getSource().sendFailure(Component.literal("Hold a gun with a VR geometry profile to calibrate."));
                        return 0;
                    }
                    String key = Profiles.key(mc.player.getMainHandItem());
                    mc.tell(() -> mc.setScreen(new CalibrationScreen(key)));
                    return 1;
                }))
                .then(Commands.literal("auto").executes(context -> {
                    set(true);
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(statusText()));
                    return 1;
                }))
                .then(Commands.literal("on").executes(context -> {
                    set(true);
                    // Backwards-compatible alias. Never force VR behavior on a desktop player.
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(statusText()));
                    return 1;
                }))
                .then(Commands.literal("off").executes(context -> {
                    set(false);
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(statusText()));
                    return 1;
                }))
                .then(Commands.literal("status").executes(context -> {
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(statusText()));
                    return 1;
                })));
    }
}
