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
    private AnimationStateMachine<?> lastAnimation;
    private static ClientControls instance;

    public ClientControls() { instance = this; }
    public static void clearInput() {
        if (instance != null) {
            instance.triggerHeld = false;
            instance.ownsTrigger = false;
        }
        PhysicalClient.releaseGrip();
        dev.visorcompat.tacz.mixin.ShootKeyAccess.visorTacz$success(false);
        ShootKey.shootControllerTick(false);
        ShootKey.SHOOT_KEY.setDown(false);
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
                && !VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(HandType.MAIN)
                && VisorAPI.client().getDecorationRenderer().getHandState(HandType.MAIN).isWorldHand();
    }

    @VREventHandler public void session(org.vmstudio.visor.api.client.events.SessionStateChangedVREvent event) {
        if(event.becameUnfocused() || event.becameFocused() || event.becameInactive()) {
            clearInput();
            if(event.becameFocused())CompatSettings.forceSync();
        }
    }
    @VREventHandler
    public void onAction(ActionButtonVREvent event) {
        if (PhysicalClient.action(event,canInput())) return;
        if (!vrActive()) return;
        var input = VisorAPI.client().getInputManager();
        if (event.getActionButton() == input.getActionLeftMouse(HandType.MAIN)) {
            if (!event.isPressEvent() && ownsTrigger) {
                triggerHeld = false;
                ownsTrigger = false;
                event.setCanceled(true);
            } else if (holdingGun() && canInput()) {
                event.setCanceled(true);
                ownsTrigger = event.isPressEvent();
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
                && holdingGun() && canInput()) {
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
        PhysicalClient.tick(canInput());
        boolean active = vrActive() && holdingGun();
        if (!active || !canInput() || !supported() || !PhysicalClient.canFire()) triggerHeld = false;
        if (active || ownedLastTick) {
            ShootKey.shootControllerTick(active && triggerHeld);
            // Prevent the mouse-emulation path from adding a second held trigger source.
            ShootKey.SHOOT_KEY.setDown(false);
        }
        ownedLastTick = active;
        if (!active) ownsTrigger = false;
    }

    @VREventHandler
    public void handState(HandRenderStateVREvent event) {
        if (vrActive() && supported() && event.getHandType() == HandType.MAIN
                && (event.getState().isWorldHand() || Minecraft.getInstance().screen instanceof CalibrationScreen || Minecraft.getInstance().screen instanceof com.tacz.guns.client.gui.GunRefitScreen)) event.setState(HandRenderState.WORLD_HAND_NO_ITEM);
    }

    @SubscribeEvent
    public void animation(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
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
                && vrActive() && (!supported() || !canInput() || !PhysicalClient.canFire())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void fired(GunFireEvent event) {
        if (event.getLogicalSide().isClient() && event.getShooter() == Minecraft.getInstance().player
                && vrActive() && supported()) {
            VisorAPI.client().getInputManager().triggerHapticPulse(HandType.MAIN, 120f, .65f, .035f);
        }
    }
}
