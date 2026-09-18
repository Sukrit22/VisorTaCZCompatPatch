package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.network.CompatNetwork;
import dev.visorcompat.tacz.physical.Handling;
import dev.visorcompat.tacz.physical.Handling.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;

public final class PhysicalClient {
    private static CompatNetwork.PhysicalState state;
    private static boolean held;
    private static int lastSlot=-1;
    private static String lastKey="";
    public static boolean active() { return CompatSettings.physical() && CompatSettings.active() && ClientControls.supported(); }
    public static Phase phase() {
        var p=Minecraft.getInstance().player;
        return p!=null && state!=null && state.slot()==p.getInventory().selected && state.key().equals(Profiles.key(p.getMainHandItem())) ? state.phase():Phase.READY;
    }
    public static float pull() { return state!=null && (Handling.racking(phase()) || phase()==Phase.PUMP_HOLD || phase()==Phase.PUMP_OPEN || phase()==Phase.REMOVING) ? state.pull()*.005f:0; }
    public static boolean canFire() {
        if(!active())return true;
        var stack=Minecraft.getInstance().player.getMainHandItem();
        if(Profiles.pump(stack) && (dev.visorcompat.tacz.server.ServerPump.open(stack) || dev.visorcompat.tacz.server.ServerPump.spent(stack) || pull()>=.02f))return false;
        return !dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack) && Handling.fireable(phase()) && com.tacz.guns.api.item.IGun.getIGunOrNull(stack).hasBulletInBarrel(stack);
    }
    public static boolean supporting() { return !active() || phase()==Phase.SUPPORT || (phase()==Phase.PUMP_HOLD && pull()<.02f); }
    public static void releaseGrip() {releaseGrip(true);}
    private static void releaseGrip(boolean canceled) {
        if(held && Minecraft.getInstance().getConnection()!=null) CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(false,canceled));
        held=false;
    }
    public static void reset() { releaseGrip();state=null;lastSlot=-1;lastKey=""; }
    public static void tick(boolean input) {
        var p=Minecraft.getInstance().player;
        String key=p==null?"":Profiles.key(p.getMainHandItem());
        int slot=p==null?-1:p.getInventory().selected;
        if(slot!=lastSlot || !key.equals(lastKey)) { releaseGrip();state=null;lastSlot=slot;lastKey=key; }
        if(!active() || !input || !p.getOffhandItem().isEmpty()
            || !VisorAPI.client().getVRLocalPlayer().getRawController(HandType.OFFHAND).isTracking()) releaseGrip();
    }
    public static Handling.Target target() {
        var mc=Minecraft.getInstance();
        if(!active() || mc.screen!=null || !mc.player.getOffhandItem().isEmpty()
            || VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(HandType.OFFHAND)
            || !VisorAPI.client().getDecorationRenderer().getHandState(HandType.OFFHAND).isWorldHand())return Handling.Target.NONE;
        var vr=VisorAPI.client().getVRLocalPlayer();
        if(!vr.getRawController(HandType.OFFHAND).isTracking())return Handling.Target.NONE;
        var pose=vr.getPoseData(org.vmstudio.visor.api.client.player.pose.PlayerPoseType.TICK);
        var stack=mc.player.getMainHandItem();var c=CalibrationStore.get(Profiles.key(stack));var profile=Profiles.get(stack);
        var gun=GunPose.resolve(pose,profile,supporting(),c);if(gun==null)return Handling.Target.NONE;
        var off=pose.getOffhand().getPosition();
        var forward=pose.getHmd().getRotation().transformDirection(new org.joml.Vector3f(0,0,-1));
        boolean pouch=off.distance(Handling.pouch(pose.getHmd().getPosition(),forward,gun.worldScale(),c))<.25f*gun.worldScale();
        var local=Handling.local(gun,off);
        var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
        if((phase()==Phase.READY || phase()==Phase.NEED_RACK) && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE && jam.remaining()>0
            && Handling.near(local,dev.visorcompat.tacz.physical.JamProfile.of(profile,c).port(),.035f))return Handling.Target.CASING;
        var target=Handling.target(phase(),local,pouch,profile,c);
        if(target==Handling.Target.POUCH && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.DOUBLE_FEED && jam.remaining()>0)return Handling.Target.NONE;
        return target;
    }
    public static boolean suppressSwing() {
        return active() && Minecraft.getInstance().player.getOffhandItem().isEmpty()
            && (held || target()!=Handling.Target.NONE);
    }
    public static boolean action(ActionButtonVREvent event,boolean canInput) {
        var input=VisorAPI.client().getInputManager();
        var grab=CompatSettings.grab()==CompatSettings.Grab.TRIGGER?input.getActionLeftMouse(HandType.OFFHAND):input.getActionRightMouse(HandType.OFFHAND);
        if(event.getActionButton()==grab && !event.isPressEvent() && held) {
            releaseGrip(!canInput);event.setCanceled(true);return true;
        }
        if(!active() || !canInput)return false;
        if(event.getActionButton()==grab && event.isPressEvent() && !held) {
            var target=target();if(target==Handling.Target.NONE)return false;
            held=true;event.setCanceled(true);
            if(target==Handling.Target.SELECTOR)CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalSelector());
            else CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true));
            return true;
        }
        if(event.getActionButton()==input.getActionRightMouse(HandType.MAIN)) {
            event.setCanceled(true);
            if(event.isPressEvent())CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalSelector());
            return true;
        }
        return false;
    }
    public static void receive(CompatNetwork.PhysicalState update) {
        var p=Minecraft.getInstance().player;
        if(p==null || !active() || p.getInventory().selected!=update.slot() || !Profiles.key(p.getMainHandItem()).equals(update.key())) return;
        boolean changed=state==null || state.phase()!=update.phase();state=update;
        if(changed) {
            String hint=switch(update.phase()) {
                case PUMP_HOLD -> "Hold fore-end: pull back fully, then push forward to chamber";
                case PUMP_OPEN -> "Pump open: grab the rearward fore-end and push forward";
                case SHELL -> "Move shell from pouch to loading port and release";
                case READY -> "Ready: grab magazine and pull down to reload";
                case REMOVING -> "Pull magazine down 7 cm, holding offhand use";
                case OLD_MAG -> "Release old magazine to stow its remaining rounds";
                case NO_MAG -> "Grab a replacement at waist pouch (below headset)";
                case NEW_MAG -> "Move magazine to magwell and release to insert";
                case LOADING -> "TaCZ is supplying ammo...";
                case NEED_RACK -> "Grab slide / charging handle, pull back 6 cm, release";
                case PLUCKING -> "Pull the casing away from the port, then release";
                case RACKING, RACKING_EMPTY -> "Pull backward, then release";
                case SUPPORT -> "Two-hand grip engaged; release offhand use to let go";
            };
            p.displayClientMessage(Component.literal("TaCZ VR: "+hint),true);
            VisorAPI.client().getInputManager().triggerHapticPulse(HandType.OFFHAND,90f,.35f,.025f);
        }
    }
}
