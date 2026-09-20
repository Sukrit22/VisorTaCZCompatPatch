package dev.visorcompat.tacz.client;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;

/** Shared live values for world panels and the ordinary GUI overlay. */
final class StatusText {
    private StatusText() {}
    static String[] lines() {
        var player=Minecraft.getInstance().player;
        if(player==null)return new String[0];
        var stack=player.getMainHandItem();
        var item=IGun.getIGunOrNull(stack);
        if(item==null)return new String[0];
        boolean pump=dev.visorcompat.tacz.Profiles.pump(stack);
        String ammo=(pump?"TUBE ":"MAG ")+item.getCurrentAmmoCount(stack)+" | CH "+(item.hasBulletInBarrel(stack)?"1":"0");
        boolean reload=IGunOperator.fromLivingEntity(player).getSynReloadState().getStateType().isReloading();
        String state=dev.visorcompat.tacz.physical.GunStatus.describe(
            dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack), item.isOverheatLocked(stack),reload,
            PhysicalClient.active(),PhysicalClient.phase(),item.hasBulletInBarrel(stack),item.getCurrentAmmoCount(stack));
        if(pump && PhysicalClient.active() && !state.equals("JAMMED") && !state.equals("OVERHEATED")) {
            if(dev.visorcompat.tacz.server.ServerPump.open(stack))state="PUMP OPEN";
            else if(dev.visorcompat.tacz.server.ServerPump.spent(stack) || !item.hasBulletInBarrel(stack))state="PUMP TO CHAMBER";
            if(PhysicalClient.phase()==dev.visorcompat.tacz.physical.Handling.Phase.SHELL)state="INSERT SHELL";
        }
        if(dev.visorcompat.tacz.Profiles.bolt(stack) && PhysicalClient.active() && !state.equals("JAMMED")){
            if(dev.visorcompat.tacz.physical.BoltState.open(stack))state="BOLT OPEN";
            else if(dev.visorcompat.tacz.physical.ActionState.lifted(stack))state="LOWER BOLT HANDLE";
            else if(dev.visorcompat.tacz.physical.BoltState.spent(stack) || !item.hasBulletInBarrel(stack))state="CYCLE BOLT";
        }
        if(PhysicalClient.active()&&dev.visorcompat.tacz.physical.ActionState.locked(stack))state="ACTION LOCKED OPEN";
        if(PhysicalClient.anchor()!=null)state="SUPPORT HAND HOLD | MAIN USE AT GRIP TO RETURN";
        var jam=dev.visorcompat.tacz.server.ServerJams.read(stack);
        if(state.equals("JAMMED") && jam.kind()!=dev.visorcompat.tacz.physical.Jam.Kind.NONE)
            state=jam.kind().name().replace('_',' ')+" | LEFT "+jam.remaining();
        if(state.isEmpty())state=AutoAds.aiming()?"ADS":"HIP FIRE";
        String handling=PhysicalClient.active()?PhysicalClient.phase().name().replace('_',' '):"BUTTONS";
        String target=PhysicalClient.target().name().replace('_',' ');
        if(PhysicalClient.active() && (target.equals("POUCH") || PhysicalClient.phase()==dev.visorcompat.tacz.physical.Handling.Phase.NEW_MAG) && !dev.visorcompat.tacz.physical.PouchAmmo.available(player,stack))target="OUT OF AMMO";
        String optical=com.tacz.guns.compat.oculus.OculusCompat.isUsingRenderPack()?"OPTICS: CLEAR ONLY (SHADERS)":"";
        return new String[]{ammo,state+" | "+handling,target.equals("NONE")?optical:"GRAB: "+target};
    }
}
