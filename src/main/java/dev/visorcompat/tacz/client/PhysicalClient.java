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
    private static boolean held,mainHeld;
    public static HandAnchor anchor(){return active()&&state!=null&&state.slot()==Minecraft.getInstance().player.getInventory().selected&&state.key().equals(Profiles.key(Minecraft.getInstance().player.getMainHandItem()))?state.anchor():null;}
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
        if(anchor()!=null)return false;
        var stack=Minecraft.getInstance().player.getMainHandItem();
        if(Profiles.pump(stack) && (dev.visorcompat.tacz.server.ServerPump.open(stack) || dev.visorcompat.tacz.server.ServerPump.spent(stack) || pull()>=.02f))return false;
        if(dev.visorcompat.tacz.physical.ActionState.locked(stack))return false;
        if(Profiles.bolt(stack) && dev.visorcompat.tacz.physical.BoltState.blocked(stack))return false;
        return !dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack) && Handling.fireable(phase()) && com.tacz.guns.api.item.IGun.getIGunOrNull(stack).hasBulletInBarrel(stack);
    }
    public static boolean supporting() { return !active() || phase()==Phase.SUPPORT || (phase()==Phase.PUMP_HOLD && pull()<.02f); }
    public static void releaseGrip() {releaseGrip(true);}
    private static void releaseGrip(boolean canceled) {
        if(held && Minecraft.getInstance().getConnection()!=null) CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(false,canceled));
        if(mainHeld && Minecraft.getInstance().getConnection()!=null)CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,false,true));
        mainHeld=false;held=false;
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
        var gun=GunPose.resolve(pose,profile,supporting(),c,anchor());if(gun==null)return Handling.Target.NONE;
        var off=anchor()!=null?pose.getMainHand().getPosition():pose.getOffhand().getPosition();
        var forward=pose.getHmd().getRotation().transformDirection(new org.joml.Vector3f(0,0,-1));
        boolean pouch=Handling.inPouch(off,pose.getHmd().getPosition(),forward,gun.worldScale(),c);
        var local=Handling.local(gun,off);
        var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
        if((phase()==Phase.READY || phase()==Phase.NEED_RACK) && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE && jam.remaining()>0
            && Handling.inside(local,dev.visorcompat.tacz.physical.JamProfile.of(profile,c).port(),c,ZoneSizes.Zone.PORT))return Handling.Target.CASING;
        var target=Handling.target(phase(),local,pouch,profile,c,dev.visorcompat.tacz.physical.BoltState.open(stack),dev.visorcompat.tacz.physical.ActionState.locked(stack),dev.visorcompat.tacz.physical.ActionState.lifted(stack));
        if(target==Handling.Target.POUCH && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.DOUBLE_FEED && jam.remaining()>0)return Handling.Target.NONE;
        return target;
    }
    public static boolean suppressSwing() {
        return active() && Minecraft.getInstance().player.getOffhandItem().isEmpty()
            && (held || target()!=Handling.Target.NONE);
    }
    public static boolean action(ActionButtonVREvent event,boolean canInput) {
        var input=VisorAPI.client().getInputManager();
        if(mainHeld && !event.isPressEvent() && event.getActionButton()==input.getActionLeftMouse(HandType.MAIN)){
            CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,false,!canInput));mainHeld=false;event.setCanceled(true);return true;
        }
        if(active() && canInput && Profiles.bolt(Minecraft.getInstance().player.getMainHandItem())){
            if(event.getActionButton()==input.getActionRightMouse(HandType.MAIN) && (anchor()!=null || phase()==Phase.SUPPORT)){
                event.setCanceled(true);if(event.isPressEvent())CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(true,false,false));return true;
            }
            if(anchor()!=null && event.getActionButton()==input.getActionLeftMouse(HandType.MAIN)){
                event.setCanceled(true);if(event.isPressEvent()&&target()==Handling.Target.RACK){mainHeld=true;CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,true,false));}return true;
            }
        }
        var grab=CompatSettings.grab()==CompatSettings.Grab.TRIGGER?input.getActionLeftMouse(HandType.OFFHAND):input.getActionRightMouse(HandType.OFFHAND);
        if(event.getActionButton()==grab && !event.isPressEvent() && held) {
            releaseGrip(!canInput);event.setCanceled(true);return true;
        }
        if(!active() || !canInput)return false;
        if(event.getActionButton()==grab && event.isPressEvent() && !held) {
            var target=target();if(target==Handling.Target.NONE)return false;
            if(target==Handling.Target.POUCH && Profiles.pump(Minecraft.getInstance().player.getMainHandItem())
                && !dev.visorcompat.tacz.physical.PouchAmmo.available(Minecraft.getInstance().player,Minecraft.getInstance().player.getMainHandItem())){
                Minecraft.getInstance().player.displayClientMessage(Component.literal("TaCZ VR: OUT OF AMMO"),true);event.setCanceled(true);return true;
            }
            held=true;event.setCanceled(true);
            if(target==Handling.Target.SELECTOR)CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalSelector());
            else CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true));
            return true;
        }
        if(event.getActionButton()==input.getActionRightMouse(HandType.OFFHAND) && target()==Handling.Target.RELEASE){
            event.setCanceled(true);if(event.isPressEvent()){CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true));CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(false));}return true;
        }
        if(event.getActionButton()==input.getActionRightMouse(HandType.MAIN)) {
            event.setCanceled(true);
            if(event.isPressEvent()){if(Profiles.get(Minecraft.getInstance().player.getMainHandItem()).supportDistance()==0)CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PistolRelease());else CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalSelector());}
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
                case PUMP_OPEN -> "Pump open: insert a shell at the side port, or push fore-end forward";
                case SHELL -> "Move shell from pouch to loading port and release";
                case READY -> "Ready: grab magazine and pull down to reload";
                case REMOVING -> "Pull magazine down 7 cm, holding offhand use";
                case OLD_MAG -> "Release old magazine to stow its remaining rounds";
                case NO_MAG -> "Grab a replacement at waist pouch (below headset)";
                case NEW_MAG -> "Move magazine to magwell and release to insert";
                case LOADING -> "TaCZ is supplying ammo...";
                case NEED_RACK -> Profiles.bolt(p.getMainHandItem())?"Bolt: lift 4 cm, back 8 cm, forward, then lower":"Grab slide / charging handle, pull back 6 cm, release";
                case PLUCKING -> "Pull the casing away from the port, then release";
                case RACKING, RACKING_EMPTY -> Profiles.bolt(p.getMainHandItem())?"Lift, pull back, push forward, lower; release keeps the action state":"Pull backward, then release";
                case SUPPORT -> "Two-hand grip engaged; release offhand use to let go";
            };
            p.displayClientMessage(Component.literal("TaCZ VR: "+hint),true);
            VisorAPI.client().getInputManager().triggerHapticPulse(HandType.OFFHAND,90f,.35f,.025f);
        }
    }
}
