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
    private static boolean held,mainHeld,cylinderTrigger,cylinderUse;
    public static HandAnchor anchor(){return active()&&state!=null&&state.slot()==Minecraft.getInstance().player.getInventory().selected&&state.key().equals(Profiles.key(Minecraft.getInstance().player.getMainHandItem()))?state.anchor():null;}
    public static boolean supportAnchor(){var a=anchor();return a!=null&&!a.mainHand();}
    private static int lastSlot=-1;
    private static String lastKey="";
    public static boolean active() { return CompatSettings.physical() && CompatSettings.active() && ClientControls.supported() && Profiles.physical(Minecraft.getInstance().player.getMainHandItem()); }
    public static Phase phase() {
        var p=Minecraft.getInstance().player;
        return p!=null && state!=null && state.slot()==p.getInventory().selected && state.key().equals(Profiles.key(p.getMainHandItem())) ? state.phase():Phase.READY;
    }
    public static float pull() { return state!=null && (Handling.racking(phase()) || phase()==Phase.PUMP_HOLD || phase()==Phase.PUMP_OPEN || phase()==Phase.REMOVING || phase()==Phase.CYLINDER_HOLD) ? state.pull()*.005f:0; }
    public static boolean canFire() {
        if(!AdvancedClient.canFire())return false;
        if(!active())return true;
        if(supportAnchor())return false;
        var stack=Minecraft.getInstance().player.getMainHandItem();
        if(Profiles.cylinder(stack))return phase()==Phase.READY&&!dev.visorcompat.tacz.server.ServerCylinder.open(stack)&&!dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack)&&com.tacz.guns.api.item.IGun.getIGunOrNull(stack).getCurrentAmmoCount(stack)>0;
        if(Profiles.pump(stack) && (dev.visorcompat.tacz.server.ServerPump.open(stack) || dev.visorcompat.tacz.server.ServerPump.spent(stack) || pull()>=.02f))return false;
        if(dev.visorcompat.tacz.physical.ManualCycle.spent(stack))return false;
        if(dev.visorcompat.tacz.physical.ActionState.locked(stack))return false;
        if(Profiles.bolt(stack) && dev.visorcompat.tacz.physical.BoltState.blocked(stack))return false;
        return !dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack) && Handling.fireable(phase()) && com.tacz.guns.api.item.IGun.getIGunOrNull(stack).hasBulletInBarrel(stack);
    }
    public static boolean supporting() {
        if(!active())return true;
        if(supportAnchor())return false;
        var mc=Minecraft.getInstance();var stack=mc.player.getMainHandItem();var profile=Profiles.get(stack);
        if(profile.supportDistance()!=0)return phase()==Phase.SUPPORT || (phase()==Phase.PUMP_HOLD && pull()<.02f);
        if(mc.screen!=null || !mc.player.getOffhandItem().isEmpty()
            || (phase()!=Phase.READY && phase()!=Phase.NEED_RACK && phase()!=Phase.SUPPORT))return false;
        var vr=VisorAPI.client().getVRLocalPlayer();
        if(!vr.getRawController(HandType.OFFHAND).isTracking()
            || VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(HandType.OFFHAND)
            || !VisorAPI.client().getDecorationRenderer().getHandState(HandType.OFFHAND).isWorldHand())return false;
        var pose=vr.getPoseData(org.vmstudio.visor.api.client.player.pose.PlayerPoseType.TICK);
        var c=CalibrationStore.get(Profiles.key(stack));var gun=GunPose.resolve(pose,profile,false,c);
        if(gun==null)return false;var local=Handling.local(gun,pose.getOffhand().getPosition());
        return phase()==Phase.SUPPORT?Handling.supportRetained(local,Handling.support(profile,c),c):Handling.inside(local,Handling.support(profile,c),c,ZoneSizes.Zone.SUPPORT);
    }
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
        if(!AdvancedClient.manipulating())return Handling.Target.NONE;
        var mc=Minecraft.getInstance();
        var interactionHand=AdvancedClient.freeMain()||supportAnchor()?HandType.MAIN:HandType.OFFHAND;
        if(!active() || mc.screen!=null || !mc.player.getOffhandItem().isEmpty()
            || VisorAPI.client().getGuiManager().getCursorHandler().isHandFocused(interactionHand)
            || !VisorAPI.client().getDecorationRenderer().getHandState(interactionHand).isWorldHand())return Handling.Target.NONE;
        var vr=VisorAPI.client().getVRLocalPlayer();
        if(!vr.getRawController(interactionHand).isTracking())return Handling.Target.NONE;
        var pose=vr.getPoseData(org.vmstudio.visor.api.client.player.pose.PlayerPoseType.TICK);
        var stack=mc.player.getMainHandItem();var c=CalibrationStore.get(Profiles.key(stack));var profile=Profiles.get(stack);
        var gun=AdvancedClient.interactionPose(GunPose.resolve(pose,profile,supporting(),c,anchor()));if(gun==null)return Handling.Target.NONE;
        var off=AdvancedClient.freeMain()||supportAnchor()?pose.getMainHand().getPosition():pose.getOffhand().getPosition();
        var forward=pose.getHmd().getRotation().transformDirection(new org.joml.Vector3f(0,0,-1));
        boolean pouch=Handling.inPouch(off,pose.getHmd().getPosition(),forward,gun.worldScale(),c);
        var local=Handling.local(gun,off);
        if(AdvancedClient.active()&&supportAnchor()&&Handling.boltGrab(local,profile,c,dev.visorcompat.tacz.physical.BoltState.open(stack),dev.visorcompat.tacz.physical.ActionState.lifted(stack)))return Handling.Target.RACK;
        var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
        if((phase()==Phase.READY || phase()==Phase.NEED_RACK || profile.supportDistance()==0 && phase()==Phase.SUPPORT) && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.STOVEPIPE && jam.remaining()>0
            && Handling.inside(local,dev.visorcompat.tacz.physical.JamProfile.of(profile,c).port(),c,ZoneSizes.Zone.PORT))return Handling.Target.CASING;
        var target=Handling.target(profile.supportDistance()==0 && phase()==Phase.SUPPORT?Phase.READY:phase(),local,pouch,profile,c,dev.visorcompat.tacz.physical.BoltState.open(stack),dev.visorcompat.tacz.physical.ActionState.locked(stack),dev.visorcompat.tacz.physical.ActionState.lifted(stack));
        if(target==Handling.Target.POUCH && jam.kind()==dev.visorcompat.tacz.physical.Jam.Kind.DOUBLE_FEED && jam.remaining()>0)return Handling.Target.NONE;
        return target;
    }
    public static boolean suppressSwing() {
        return active() && Minecraft.getInstance().player.getOffhandItem().isEmpty()
            && (held || target()!=Handling.Target.NONE);
    }
    public static boolean action(ActionButtonVREvent event,boolean canInput) {
        if(!AdvancedClient.manipulating()||AdvancedClient.freeMain())return false;
        var input=VisorAPI.client().getInputManager();
        if(mainHeld && !event.isPressEvent() && event.getActionButton()==input.getActionLeftMouse(HandType.MAIN)){
            CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,false,!canInput));mainHeld=false;event.setCanceled(true);return true;
        }
        if(active() && canInput && dev.visorcompat.tacz.server.ServerPhysical.transferable(Profiles.get(Minecraft.getInstance().player.getMainHandItem()))){
            if(event.getActionButton()==input.getActionRightMouse(HandType.MAIN) && (supportAnchor() || phase()==Phase.SUPPORT || phase()==Phase.PUMP_HOLD)){
                event.setCanceled(true);if(event.isPressEvent())CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(true,false,false));return true;
            }
            if(supportAnchor() && event.getActionButton()==input.getActionLeftMouse(HandType.MAIN)){
                event.setCanceled(true);if(event.isPressEvent()&&target()==Handling.Target.RACK){mainHeld=true;CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,true,false));}return true;
            }
        }
        if(cylinderUse&&!event.isPressEvent()&&event.getActionButton()==input.getActionRightMouse(HandType.OFFHAND)){cylinderUse=false;releaseGrip(!canInput);event.setCanceled(true);return true;}
        if(active()&&canInput&&event.isPressEvent()&&!held&&Profiles.cylinder(Minecraft.getInstance().player.getMainHandItem())&&target()==Handling.Target.POUCH&&event.getActionButton()==input.getActionRightMouse(HandType.OFFHAND)){
            cylinderUse=true;held=true;event.setCanceled(true);CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true,false,true));return true;
        }
        if(cylinderTrigger&&!event.isPressEvent()&&event.getActionButton()==input.getActionLeftMouse(HandType.OFFHAND)){cylinderTrigger=false;releaseGrip(!canInput);event.setCanceled(true);return true;}
        if(active()&&canInput&&event.isPressEvent()&&!held&&Profiles.cylinder(Minecraft.getInstance().player.getMainHandItem())&&target()==Handling.Target.POUCH&&event.getActionButton()==input.getActionLeftMouse(HandType.OFFHAND)){
            cylinderTrigger=true;held=true;event.setCanceled(true);CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true,false,false));return true;
        }
        var grab=AdvancedClient.active()?null:CompatSettings.grab()==CompatSettings.Grab.TRIGGER?input.getActionLeftMouse(HandType.OFFHAND):input.getActionRightMouse(HandType.OFFHAND);
        if(event.getActionButton()==grab && !event.isPressEvent() && held) {
            releaseGrip(!canInput);event.setCanceled(true);return true;
        }
        if(!active() || !canInput)return false;
        if(event.getActionButton()==grab && event.isPressEvent() && !held) {
            var target=target();if(target==Handling.Target.NONE)return false;
            if(target==Handling.Target.POUCH && (Profiles.pump(Minecraft.getInstance().player.getMainHandItem())||Profiles.cylinder(Minecraft.getInstance().player.getMainHandItem()))
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
                case CYLINDER_OPEN -> "Open: pouch Grip/Use = speedloader, Trigger = one round; pink pull ejects all; tilt up and shake also ejects";
                case CYLINDER_HOLD -> "Orange: move sideways 5 cm to open/close, then release; pink: pull back to eject";
                case PUMP_HOLD -> "Hold fore-end: pull back fully, then push forward to chamber";
                case PUMP_OPEN -> "Pump open: insert a shell at the side port, or push fore-end forward";
                case LOADER -> "Move speedloader to green loading zone and release; only available inventory rounds are loaded";
                case SHELL -> "Move shell from pouch to loading port and release";
                case READY -> Profiles.cylinder(p.getMainHandItem())?"Ready: grab orange action and move sideways 5 cm to open":"Ready: grab magazine and pull down to reload";
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
    public static void advancedGrip(boolean down){advancedGrip(down,true);}
    public static void advancedGrip(boolean down,boolean loader){
        if(!down){releaseGrip(false);return;}
        if(!active()||target()==Handling.Target.NONE)return;
        held=true;CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalGrip(true,false,loader));
    }
}
