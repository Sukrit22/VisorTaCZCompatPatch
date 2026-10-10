package dev.visorcompat.tacz.client;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.input.ShootKey;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import dev.visorcompat.tacz.Profiles;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;
import org.vmstudio.visor.api.client.events.render.HandRenderStateVREvent;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;

public final class ClientControls implements VREventListener {
    private boolean triggerHeld;
    private boolean ownsTrigger;
    private boolean ownedLastTick;
    private int triggerSlot=-1;
    private String triggerKey="";
    private AnimationStateMachine<?> lastAnimation;
    private final dev.visorcompat.tacz.physical.DoubleTrigger doubleTrigger = new dev.visorcompat.tacz.physical.DoubleTrigger();
    private boolean analogOwned;
    private int analogSlot=-1;
    private net.minecraft.world.item.ItemStack analogStack;
    private static ClientControls instance;

    public ClientControls() { instance = this; }
    public static void clearInput() {
        stopShooting();
        PhysicalClient.releaseGrip();
    }
    public static void stopShooting() {
        if (instance != null) {
            instance.triggerHeld = false;
            instance.ownsTrigger = false;
            instance.doubleTrigger.reset();
            instance.analogOwned=false;
            instance.analogStack=null;
        }
        dev.visorcompat.tacz.mixin.ShootKeyAccess.visorTacz$success(false);
        ShootKey.shootControllerTick(false);
        if(vrActive()&&holdingGun()&&!AdvancedClient.holstered())ShootKey.SHOOT_KEY.setDown(false);
    }

    public static boolean vrActive() {
        return CompatSettings.active();
    }
    public static boolean holdingGun() {
        var player = Minecraft.getInstance().player;
        return player != null && IGun.getIGunOrNull(player.getMainHandItem()) != null;
    }
    public static boolean supported() {
        var player = Minecraft.getInstance().player;
        return player != null && Profiles.get(player.getMainHandItem()) != null;
    }
    private static boolean canInput() {
        Minecraft mc = Minecraft.getInstance();
        return vrActive() && VisorAPI.clientState().stateMode().isFocused() && mc.screen == null
                && !mc.isPaused() && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()
                && VisorAPI.client().getVRLocalPlayer().getRawController(HandType.MAIN).isTracking()
                && (AdvancedClient.catchPending() || !VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(HandType.MAIN)
                && VisorAPI.client().getDecorationRenderer().getHandState(HandType.MAIN).isWorldHand());
    }

    @VREventHandler public void session(org.vmstudio.visor.api.client.events.SessionStateChangedVREvent event) {
        if(event.becameUnfocused() || event.becameFocused() || event.becameInactive()) {
            AdvancedClient.sessionReset();
            clearInput();
            if(event.becameFocused())CompatSettings.forceSync();
        }
    }
    @VREventHandler
    public void onAction(ActionButtonVREvent event) {
        if(Minecraft.getInstance().screen!=null){clearInput();return;}
        if(PhysicalClient.magazineUse(event,canInput()))return;
        if(AdvancedClient.action(event,canInput()))return;
        if (PhysicalClient.action(event,canInput())) return;
        if (!vrActive()) return;
        var input = VisorAPI.client().getInputManager();
        if (event.getActionButton() == input.getActionLeftMouse(HandType.MAIN)) {
            if (doubleTriggerAvailable() && canInput() && !AdvancedClient.holstered()) {
                event.setCanceled(true);
                triggerHeld=false;ownsTrigger=false;
                return;
            }
            if (!event.isPressEvent() && ownsTrigger) {
                triggerHeld = false;
                ownsTrigger = false;
                if(holdingGun()&&!AdvancedClient.holstered())event.setCanceled(true);
            } else if (holdingGun() && !AdvancedClient.holstered() && canInput()) {
                event.setCanceled(true);
                ownsTrigger = event.isPressEvent();
                if(ownsTrigger){triggerSlot=Minecraft.getInstance().player.getInventory().selected;triggerKey=Profiles.key(Minecraft.getInstance().player.getMainHandItem());}
                triggerHeld = event.isPressEvent() && supported();
                if(event.isPressEvent() && supported() && PhysicalClient.active() && !PhysicalClient.canFire()) {
                    var player=Minecraft.getInstance().player;
                    com.tacz.guns.api.TimelessAPI.getGunDisplay(player.getMainHandItem()).ifPresent(display -> {
                        com.tacz.guns.client.sound.SoundPlayManager.resetDryFireSound();
                        com.tacz.guns.client.sound.SoundPlayManager.playDryFireSound(player,display);
                    });
                }
                if (event.isPressEvent() && !supported()) {
                    Minecraft.getInstance().player.displayClientMessage(
                            Component.literal("TaCZ VR: this gun/display has no VR geometry profile yet. Base TaCZ guns are supported; custom packs need profiles."), true);
                }
            }
        } else if (event.getActionButton() == input.getActionRightMouse(HandType.MAIN)
                && holdingGun() && canInput() && !AdvancedClient.holsteredUsePassesThrough()) {
            // Natural aiming replaces desktop ADS. Main-hand use button becomes reload.
            event.setCanceled(true);
            if (event.isPressEvent() && supported()) {
                IClientPlayerGunOperator.fromLocalPlayer(Minecraft.getInstance().player).reload();
            }
        } else if (event.getActionButton() == input.getActionRightMouse(HandType.OFFHAND)
                && supported() && canInput() && !PhysicalClient.active()
                && Minecraft.getInstance().player.getOffhandItem().isEmpty()) {
            event.setCanceled(true);
            if (event.isPressEvent()) {
                IClientPlayerGunOperator.fromLocalPlayer(Minecraft.getInstance().player).fireSelect();
            }
        }
    }

    @VREventHandler public void swingEntity(org.vmstudio.visor.api.client.events.SwingEntityVREvent event) {
        if(event.getHand()==HandType.OFFHAND && PhysicalClient.suppressSwing())event.setCanceled(true);
    }
    @VREventHandler public void swingBlock(org.vmstudio.visor.api.client.events.SwingBlockVREvent event) {
        if(event.getHand()==HandType.OFFHAND && PhysicalClient.suppressSwing())event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        AdvancedClient.tick(canInput());
        PhysicalClient.tick(canInput());
        boolean active = vrActive() && holdingGun() && !AdvancedClient.holstered();
        var player=Minecraft.getInstance().player;
        if(ownsTrigger&&(player==null||triggerSlot!=player.getInventory().selected||!triggerKey.equals(Profiles.key(player.getMainHandItem())))){
            triggerHeld=false;ownsTrigger=false;
            dev.visorcompat.tacz.mixin.ShootKeyAccess.visorTacz$success(false);
        }
        if (!active || !canInput() || !supported() || !PhysicalClient.canFire()) triggerHeld = false;
        if(!AdvancedClient.canFire())triggerHeld=false;
        boolean analog=doubleTriggerAvailable();
        if(analog) {
            var stack=player.getMainHandItem();
            var gun=IGun.getIGunOrNull(stack);
            boolean valid=active&&canInput()&&PhysicalClient.canFire()&&AdvancedClient.canFire()
                    &&gun.getCurrentAmmoCount(stack)>0;
            boolean changed=analogStack!=stack||analogSlot!=player.getInventory().selected;
            if(!valid||changed)doubleTrigger.reset();
            analogStack=stack;analogSlot=player.getInventory().selected;
            if(valid) {
                Float value=analogValue();
                if(value!=null)doubleTrigger.sample(value);else doubleTrigger.reset();
                // DB-2 burst consumes both shells itself. Select native SEMI before
                // requesting individual shots; the native selector also informs the server.
                if(gun.getFireMode(stack)!=com.tacz.guns.api.item.gun.FireMode.SEMI) {
                    if(!analogOwned)player.displayClientMessage(Component.literal("DB-2 analog trigger: switching to single-shell mode (33% / 90%)."),true);
                    IClientPlayerGunOperator.fromLocalPlayer(player).fireSelect();
                    triggerHeld=false;
                } else {
                    triggerHeld=false;
                    var operator=IClientPlayerGunOperator.fromLocalPlayer(player);
                    if(operator.chargeShoot(doubleTrigger.pending())&&doubleTrigger.pending()) {
                        var result=operator.shoot();
                        if(result==com.tacz.guns.api.entity.ShootResult.SUCCESS)doubleTrigger.fired();
                        else if(result!=com.tacz.guns.api.entity.ShootResult.COOL_DOWN)doubleTrigger.reset();
                    }
                }
            } else triggerHeld=false;
            analogOwned=true;
        } else {
            if(analogOwned)triggerHeld=false;
            doubleTrigger.reset();analogOwned=false;analogStack=null;
        }
        if (active || ownedLastTick) {
            ShootKey.shootControllerTick(active && triggerHeld);
            // Prevent the mouse-emulation path from adding a second held trigger source.
            if(active)ShootKey.SHOOT_KEY.setDown(false);
        }
        ownedLastTick = active;
        if (!active) ownsTrigger = false;
    }

    private static boolean doubleTriggerAvailable() {
        var player=Minecraft.getInstance().player;
        return player!=null&&vrActive()&&(PhysicalClient.active()||AdvancedClient.active())
                &&holdingGun()&&"tacz:db_short".equals(IGun.getIGunOrNull(player.getMainHandItem()).getGunId(player.getMainHandItem()).toString())&&analogValue()!=null;
    }
    /** Boolean bindings keep the existing Visor input path. */
    private static Float analogValue() {
        var input=VisorAPI.client().getInputManager();
        var profile=input.getProfileManager().getActiveProfile();
        if(profile==null)return null;
        var button=profile.getTriggerButton(HandType.MAIN.asControllerType(input.isLeftHanded()));
        if(button instanceof me.phoenixra.atumvr.core.input.action.types.single.FloatButtonAction action)
            return action.isActive()?action.getCurrentState():null;
        if(button instanceof me.phoenixra.atumvr.core.input.action.types.multi.FloatButtonMultiAction.SubActionFloatButton action)
            return action.isActive()?action.getCurrentState():null;
        return null;
    }

    /** Only world aiming marks; menus retain their own cursor and input ownership. */
    public static boolean hideWorldCursor(HandType hand){
        var mc=Minecraft.getInstance();
        if(!vrActive()||!supported()||mc.screen!=null
            ||VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(hand))return false;
        if(AdvancedClient.active())return AdvancedClient.handOccupied(hand);
        if(PhysicalClient.supportAnchor())return hand==HandType.OFFHAND;
        return hand==HandType.MAIN||PhysicalClient.supportCursorOccupied();
    }

    @VREventHandler
    public void handState(HandRenderStateVREvent event) {
        if (vrActive() && supported() && event.getHandType() == HandType.MAIN
                && (event.getState().isWorldHand() || Minecraft.getInstance().screen instanceof InspectionScreen || Minecraft.getInstance().screen instanceof HolsterScreen || Minecraft.getInstance().screen instanceof CalibrationScreen || Minecraft.getInstance().screen instanceof com.tacz.guns.client.gui.GunRefitScreen)) event.setState(HandRenderState.WORLD_HAND_NO_ITEM);
    }

    @SubscribeEvent
    public void animation(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        AdvancedClient.frame(canInput());
        Minecraft mc = Minecraft.getInstance();
        if (!vrActive() || !supported()) {
            if (lastAnimation != null && lastAnimation.isInitialized()) lastAnimation.exit();
            lastAnimation = null;
            return;
        }
        var stack = mc.player.getMainHandItem();
        if (IClientItemExtensions.of(stack.getItem()).getCustomRenderer() instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            var machine = renderer.getStateMachine(stack);
            if (machine != lastAnimation) {
                if (lastAnimation != null && lastAnimation.isInitialized()) lastAnimation.exit();
                lastAnimation = machine;
            }
            if (renderer.needReInit(stack)) renderer.tryInit(stack, mc.player, event.renderTickTime);
            // Advance sound/timing once per frame without camera-relative model transforms.
            renderer.visualUpdate(stack);
        }
    }

    @SubscribeEvent
    public void beforeShot(GunShootEvent event) {
        if (event.getLogicalSide().isClient() && event.getShooter() == Minecraft.getInstance().player
                && vrActive() && (!supported() || !canInput() || !PhysicalClient.canFire() || !AdvancedClient.canFire())) event.setCanceled(true);
    }

    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public void fired(GunFireEvent event) {
        if(!event.isCanceled()&&event.getLogicalSide().isClient())ShotVisual.fired(event.getShooter(),event.getGunItemStack());
        if (event.getLogicalSide().isClient() && event.getShooter() == Minecraft.getInstance().player
                && vrActive() && supported()) {
            VisorAPI.client().getInputManager().triggerHapticPulse(HandType.MAIN, 120f, .65f, .035f);
        }
    }
}
