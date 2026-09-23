package dev.visorcompat.tacz.client;

import dev.visorcompat.tacz.*;
import dev.visorcompat.tacz.physical.AdvancedHold;
import dev.visorcompat.tacz.network.CompatNetwork;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.events.input.ActionButtonVREvent;

/** Opt-in inventory-backed prop. No item copies, slot mutation or world item entities. */
public final class AdvancedClient {
    private static final AdvancedHold hold=new AdvancedHold();
    private static String key="";
    private static int slot=-1;
    private static GunPose current,last;
    private static final Vector3f velocity=new Vector3f();
    private static final Vector3f spin=new Vector3f();
    private static boolean transferred,ownedFingerGrab,ownedCylinderUse;
    private static boolean ownedGrip,ownedOffGrip,ownedUse,ownedFingerTrigger,inspectTrigger,sent,mainTriggerDown;
    private static long lastTime,transferRequestedAt;
    private static long renderTime;
    private static float headingOffset=Float.NaN;
    private static Vector3f lastHolsterHead;
    private static float walkingDistance;
    private static int followTicks;
    private static boolean mainBoltGrip,genericSupport;
    private static final dev.visorcompat.tacz.physical.RegripZone regripCue=new dev.visorcompat.tacz.physical.RegripZone();
    private static HandType carryHand=HandType.OFFHAND;
    private static dev.visorcompat.tacz.physical.PropGrip carryGrip;
    private static HandType holsterSide=HandType.MAIN;
    private AdvancedClient(){}
    public static boolean active(){return CompatSettings.advanced()&&CompatSettings.active()&&ClientControls.supported();}
    public static boolean firingHold(){return !active()||!transferred&&hold.state()==AdvancedHold.State.MAIN;}
    public static boolean canFire(){return !active()||!transferred&&hold.canFire();}
    private static boolean cylinderCarry(){return active()&&hold.state()==AdvancedHold.State.SUPPORT&&Profiles.cylinder(Minecraft.getInstance().player.getMainHandItem());}
    public static boolean freeMain(){
        if(!active())return false;
        var p=Profiles.get(Minecraft.getInstance().player.getMainHandItem());
        return p!=null&&p.supportDistance()==0&&!p.manualAction()
            &&(hold.state()==AdvancedHold.State.HOLSTERED||hold.state()==AdvancedHold.State.SUPPORT&&carryHand==HandType.OFFHAND);
    }
    public static GunPose interactionPose(GunPose normal){return freeMain()&&current!=null?current:normal;}
    public static boolean manipulating(){return !active()||hold.state()==AdvancedHold.State.MAIN||freeMain();}
    public static float inspectionPull(){if(Minecraft.getInstance().screen instanceof InspectionScreen screen)return screen.partial()?.022f:0;return active()&&hold.state()==AdvancedHold.State.INSPECT&&inspectTrigger?.022f:0;}
    public static String status(){return active()?(transferred?"SUPPORT TRANSFER":hold.state().name()):"OFF";}
    public static boolean catchPending(){return CatchRadial.pending();}
    public static GunPose renderPose(GunPose normal){
        var screen=Minecraft.getInstance().screen;
        if(screen instanceof InspectionScreen&&normal!=null)return inspectionPose(normal);
        if(!active()||current==null||screen!=null&&!(screen instanceof HolsterScreen)||hold.state()==AdvancedHold.State.MAIN)return normal;
        if(hold.state()==AdvancedHold.State.INSPECT){var grip=controller(HandType.MAIN,PlayerPoseType.RENDER);return grip==null?normal:inspectionPose(grip);}
        if(hold.state()==AdvancedHold.State.SUPPORT)return carryPose(PlayerPoseType.RENDER);
        if(hold.state()==AdvancedHold.State.RELEASED){
            // Both eyes and the status panel use one frame timestamp, not different wall times.
            float dt=Math.min(.05f,Math.max(0,(renderTime-lastTime)/1_000_000_000f));
            var pos=dev.visorcompat.tacz.physical.TossMotion.advance(current.hand(),velocity,9.81f*current.worldScale(),dt);
            return make(pos,new Quaternionf(current.rotation()).integrate(dt,spin.x,spin.y,spin.z).normalize(),current.worldScale());
        }
        // Use the exact tick snapshot used by grab detection and remote synchronization.
        // Rebuilding it here used the temporary eye-height camera entity as the floor.
        if(hold.state()==AdvancedHold.State.HOLSTERED)return current;
        return current;
    }
    public static void recenterHolster(){headingOffset=Float.NaN;lastHolsterHead=null;walkingDistance=0;followTicks=0;}
    public static void sessionReset(){
        CatchRadial.sessionReset();ownedGrip=false;ownedOffGrip=false;ownedUse=false;ownedFingerTrigger=false;ownedFingerGrab=false;ownedCylinderUse=false;mainBoltGrip=false;genericSupport=false;reset();recenterHolster();
    }
    private static void stopFire(){ClientControls.clearInput();if(current!=null&&Minecraft.getInstance().getConnection()!=null)send(false);}
    private static void send(boolean held){CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.Advanced(true,held,freeMain()?(hold.state()==AdvancedHold.State.HOLSTERED?1:2):cylinderCarry()?(carryHand==HandType.MAIN?3:4):0,slot,key,current.hand().x,current.hand().y,current.hand().z,current.rotation().x,current.rotation().y,current.rotation().z,current.rotation().w));sent=true;}
    public static void reset(){CatchRadial.cancel();transferred=false;genericSupport=false;mainBoltGrip=false;carryGrip=null;regripCue.reset();hold.holster();current=null;last=null;velocity.zero();spin.zero();inspectTrigger=false;mainTriggerDown=false;lastTime=0;slot=-1;key="";
        // Keep consumed button ownership until its release, even when a menu interrupts.
        if(sent&&Minecraft.getInstance().getConnection()!=null)CompatNetwork.CHANNEL.sendToServer(CompatNetwork.Advanced.disabled());sent=false;
    }
    private static GunPose make(Vector3f position,Quaternionf rotation,float scale){
        var stack=Minecraft.getInstance().player.getMainHandItem();var c=CalibrationStore.get(Profiles.key(stack));var p=Profiles.get(stack);
        return new GunPose(new Vector3f(position),new Quaternionf(rotation),new Quaternionf(rotation).transform(c.muzzleOffset(p.muzzleOffset()).mul(scale)).add(position),new Quaternionf(rotation).transform(new Vector3f(0,0,-1)),scale);
    }
    private static GunPose holster(){
        var mc=Minecraft.getInstance();var vr=VisorAPI.client().getVRLocalPlayer();var pose=vr.getPoseData(PlayerPoseType.TICK);
        float scale=pose.getWorldScale();var head=pose.getHmd().getPosition();
        if(!Float.isFinite(headingOffset)){
            var look=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
            headingOffset=(float)Math.atan2(-look.x,-look.z)-pose.getRotationY();
        }
        if(lastHolsterHead!=null){
            float dx=(head.x()-lastHolsterHead.x)/scale,dz=(head.z()-lastHolsterHead.z)/scale;
            float distance=(float)Math.sqrt(dx*dx+dz*dz);
            if(distance>.003f)walkingDistance+=distance;
            if(walkingDistance>=.15f){followTicks=10;walkingDistance=0;}
        }
        lastHolsterHead=new Vector3f(head);
        if(followTicks>0){
            followTicks--;var look=pose.getHmd().getRotation().transformDirection(new Vector3f(0,0,-1));
            if(look.x*look.x+look.z*look.z>.05f){
                float desired=(float)Math.atan2(-look.x,-look.z)-pose.getRotationY();
                float delta=(float)Math.atan2(Math.sin(desired-headingOffset),Math.cos(desired-headingOffset));
                headingOffset+=Math.max(-.12f,Math.min(.12f,delta));
            }
        }
        float yaw=headingOffset+pose.getRotationY();
        boolean pistol=Profiles.get(mc.player.getMainHandItem()).supportDistance()==0;
        float side=(vr.isLeftHanded()?-1:1)*(holsterSide==HandType.MAIN?1:-1);
        var c=HolsterStore.get(Profiles.key(mc.player.getMainHandItem()),pistol);
        var pos=c.followingCrouch(pose.getOrigin(),head,yaw,scale,side);
        return make(pos,c.rotation(yaw,pistol?side:1),scale);
    }
    private static boolean near(HandType hand,GunPose gun){
        if(gun==null)return false;var vr=VisorAPI.client().getVRLocalPlayer();if(!vr.getRawController(hand).isTracking())return false;
        var p=vr.getPoseData(PlayerPoseType.RENDER);var at=hand==HandType.MAIN?p.getMainHand():p.getOffhand();
        if(gun==current&&hold.state()==AdvancedHold.State.RELEASED){var rendered=renderPose(current);return dev.visorcompat.tacz.physical.TossMotion.nearSegment(current.hand(),rendered.hand(),at.getPosition(),.18f*gun.worldScale());}
        return at.getPosition().distance(gun.hand())<.18f*gun.worldScale();
    }
    private static boolean atHolster(){return current!=null&&current.hand().distance(holster().hand())<.20f*current.worldScale();}
    public static int protectedSlot(){return active()&&hold.held()?slot:-1;}
    private static boolean regripNear(){return gripDistance()<=(transferred&&Profiles.bolt(Minecraft.getInstance().player.getMainHandItem())?dev.visorcompat.tacz.physical.RegripZone.ENTER:.12f);}
    private static float gripDistance(){
        if(current==null)return Float.POSITIVE_INFINITY;
        var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK);var hand=pose.getMainHand();
        var point=CalibrationStore.get(key).position(new Vector3f(hand.getPosition()),hand.getRotation().getNormalizedRotation(new Quaternionf()),pose.getWorldScale());
        return point.distance(current.hand())/current.worldScale();
    }
    private static boolean nearBody(HandType hand){
        if(current==null)return false;
        var vr=VisorAPI.client().getVRLocalPlayer();if(!vr.getRawController(hand).isTracking())return false;
        var pose=vr.getPoseData(PlayerPoseType.RENDER);var at=hand==HandType.MAIN?pose.getMainHand():pose.getOffhand();
        var gun=renderPose(current);var local=dev.visorcompat.tacz.physical.Handling.local(gun,at.getPosition());
        var profile=Profiles.get(Minecraft.getInstance().player.getMainHandItem());var c=CalibrationStore.get(key);
        var muzzle=c.muzzleOffset(profile.muzzleOffset());float size=c.gunScale();
        // Coarse receiver/barrel/stock and grip envelope, not mesh collision or an action zone.
        var back=new Vector3f(0,0,(profile.supportDistance()>0?.30f:.10f)*size);
        return dev.visorcompat.tacz.physical.TossMotion.nearSegment(back,muzzle,local,.11f*size)
            ||dev.visorcompat.tacz.physical.TossMotion.nearSegment(new Vector3f(),new Vector3f(0,-.18f*size,0),local,.10f*size);
    }
    private static void captureCarry(HandType hand){
        carryHand=hand;var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.TICK);
        var at=hand==HandType.MAIN?pose.getMainHand():pose.getOffhand();
        var gun=renderPose(current);
        carryGrip=dev.visorcompat.tacz.physical.PropGrip.capture(gun.hand(),gun.rotation(),at.getPosition(),at.getRotation().getNormalizedRotation(new Quaternionf()),gun.worldScale());
    }
    private static GunPose carryPose(){return carryPose(PlayerPoseType.TICK);}
    private static GunPose carryPose(PlayerPoseType type){
        if(carryGrip==null)return controller(carryHand);
        var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(type);var at=carryHand==HandType.MAIN?pose.getMainHand():pose.getOffhand();
        var q=at.getRotation().getNormalizedRotation(new Quaternionf());
        return make(carryGrip.position(at.getPosition(),q,pose.getWorldScale()),carryGrip.orientation(q),pose.getWorldScale());
    }
    private static GunPose controller(HandType hand){return controller(hand,PlayerPoseType.TICK);}
    private static GunPose controller(HandType hand,PlayerPoseType type){
        var mc=Minecraft.getInstance();var pose=VisorAPI.client().getVRLocalPlayer().getPoseData(type);
        var c=CalibrationStore.get(Profiles.key(mc.player.getMainHandItem()));var p=Profiles.get(mc.player.getMainHandItem());
        if(hand==HandType.MAIN)return GunPose.resolve(pose,p,mc.player.getOffhandItem().isEmpty()&&PhysicalClient.supporting(),c,PhysicalClient.anchor());
        var off=pose.getOffhand();var q=off.getRotation().getNormalizedRotation(new Quaternionf());
        return make(c.position(new Vector3f(off.getPosition()),q,pose.getWorldScale()),c.orientation(q),pose.getWorldScale());
    }
    private static GunPose inspectionPose(GunPose grip){
        var stack=Minecraft.getInstance().player.getMainHandItem();
        var c=InspectionStore.get(Profiles.key(stack),true);
        return make(c.position(grip),c.rotation(grip),grip.worldScale());
    }
    public static void tick(boolean input){
        var mc=Minecraft.getInstance();
        if(!active()){if(sent||slot!=-1)reset();return;}
        var stack=mc.player.getMainHandItem();String next=Profiles.key(stack);
        if(slot!=mc.player.getInventory().selected||!key.equals(next)){reset();slot=mc.player.getInventory().selected;key=next;holsterSide=HandType.MAIN;}
        if(!input||hold.state()==AdvancedHold.State.SUPPORT&&!VisorAPI.client().getVRLocalPlayer().getRawController(carryHand).isTracking()){hold.holster();transferred=false;velocity.zero();inspectTrigger=false;last=null;}
        long now=System.nanoTime();float dt=lastTime==0?.05f:Math.min(.1f,(now-lastTime)/1_000_000_000f);lastTime=now;
        if(transferred&&now-transferRequestedAt>500_000_000L&&PhysicalClient.anchor()==null){
            // A rejected transfer, wall collision or lost pose must not strand input ownership.
            transferred=false;hold.holster();stopFire();
        }
        switch(hold.state()){
            case HOLSTERED -> current=holster();
            case MAIN -> current=controller(HandType.MAIN);
            case SUPPORT -> current=carryPose();
            case INSPECT -> {current=controller(HandType.MAIN);if(current!=null)current=inspectionPose(current);}
            case RELEASED -> {
                if(current==null){hold.holster();current=holster();break;}
                var nextPosition=dev.visorcompat.tacz.physical.TossMotion.advance(current.hand(),velocity,9.81f*current.worldScale(),dt);
                velocity.y-=9.81f*current.worldScale()*dt;
                var from=new net.minecraft.world.phys.Vec3(current.hand());var to=new net.minecraft.world.phys.Vec3(nextPosition);
                var hit=mc.level.clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));
                if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.MISS){velocity.zero();hold.holster();current=holster();}
                else current=make(nextPosition,new Quaternionf(current.rotation()).integrate(dt,spin.x,spin.y,spin.z).normalize(),current.worldScale());
                float distance=current.hand().distance(new Vector3f((float)mc.player.getX(),(float)mc.player.getY(),(float)mc.player.getZ()))/current.worldScale();
                if(hold.recover(now,CompatSettings.inspectWindow(),CompatSettings.toss(),current.hand().y,(float)mc.player.getY(),mc.player.getBbHeight(),distance)){hold.holster();current=holster();}
            }
        }
        if(hold.held()&&current!=null&&last!=null&&dt>0){
            velocity.set(current.hand()).sub(last.hand()).div(dt);float max=6*current.worldScale();if(velocity.length()>max)velocity.normalize(max);
            var delta=new Quaternionf(last.rotation()).conjugate().mul(current.rotation()).normalize();if(delta.w<0)delta.set(-delta.x,-delta.y,-delta.z,-delta.w);
            float angle=2*(float)Math.acos(Math.max(-1,Math.min(1,delta.w)));spin.set(delta.x,delta.y,delta.z);
            if(spin.lengthSquared()>.000001f)spin.normalize(Math.min(20,angle/dt));else spin.zero();
        }
        last=current;
        frame(input);
        if(!mainTriggerDown&&hold.state()==AdvancedHold.State.MAIN)hold.triggerReleased();
        if(current!=null)send(hold.canFire());
    }
    public static void frame(boolean input){
        renderTime=System.nanoTime();
        boolean ready=input&&active()&&transferred&&PhysicalClient.supportAnchor()
            &&Profiles.bolt(Minecraft.getInstance().player.getMainHandItem())
            &&!dev.visorcompat.tacz.physical.BoltState.blocked(Minecraft.getInstance().player.getMainHandItem())
            &&!dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(Minecraft.getInstance().player.getMainHandItem())&&!mainBoltGrip;
        if(regripCue.update(ready,ready?gripDistance():Float.POSITIVE_INFINITY))
            VisorAPI.client().getInputManager().triggerHapticPulse(HandType.MAIN,75f,.25f,.035f);
        if(!active()||!CatchRadial.pending())return;var mc=Minecraft.getInstance();
        for(var h:HandType.values()){
            boolean free=h==HandType.MAIN||mc.player.getOffhandItem().isEmpty();
            boolean eligible=input&&!hold.held()&&free&&VisorAPI.client().getVRLocalPlayer().getRawController(h).isTracking();
            if(CatchRadial.poll(h,eligible,hold.state()==AdvancedHold.State.RELEASED&&(near(h,current)||nearBody(h)))){
                boolean triggerGrip=h==HandType.MAIN&&regripNear();if(!triggerGrip)captureCarry(h);hold.grip(triggerGrip);if(h==HandType.MAIN)ownedGrip=true;else {ownedOffGrip=true;holsterSide=h;}stopFire();
            }
        }
    }
    public static boolean action(ActionButtonVREvent e,boolean input){
        // A screen/cursor owns GUI input, including release events after session recreation.
        if(Minecraft.getInstance().screen!=null)return false;
        var actions=VisorAPI.client().getInputManager();String id=e.getActionButton().getId();boolean press=e.isPressEvent();
        if(!press&&(id.equals("hotbar_main")||id.equals("hotbar_offhand"))&&CatchRadial.release(id.equals("hotbar_main")?HandType.MAIN:HandType.OFFHAND)){e.setCanceled(true);return true;}
        if(!press&&ownedCylinderUse&&e.getActionButton()==actions.getActionRightMouse(HandType.OFFHAND)){ownedCylinderUse=false;e.setCanceled(true);return true;}
        if(!press&&ownedFingerTrigger&&e.getActionButton()==actions.getActionLeftMouse(HandType.OFFHAND)){ownedFingerTrigger=false;if(ownedFingerGrab){ownedFingerGrab=false;PhysicalClient.advancedGrip(false);}e.setCanceled(true);return true;}
        if(!press&&ownedUse&&e.getActionButton()==actions.getActionRightMouse(HandType.MAIN)){
            ownedUse=false;hold.useReleased();if(hold.state()==AdvancedHold.State.INSPECT){hold.release(System.nanoTime(),false);inspectTrigger=false;stopFire();}e.setCanceled(true);return true;
        }
        if(id.equals("hotbar_main")&&!press&&mainBoltGrip){mainBoltGrip=false;CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,false,!input));e.setCanceled(true);return true;}
        if(id.equals("hotbar_main")&&!press&&ownedGrip&&hold.state()==AdvancedHold.State.SUPPORT){
            ownedGrip=false;if(carryHand==HandType.MAIN){if(ownedOffGrip){captureCarry(HandType.OFFHAND);}else hold.release(System.nanoTime(),atHolster());stopFire();}e.setCanceled(true);return true;
        }
        if(id.equals("hotbar_main")&&!press&&ownedGrip){ownedGrip=false;e.setCanceled(true);if(active()&&hold.state()==AdvancedHold.State.MAIN){
            if(ownedOffGrip&&genericSupport){captureCarry(HandType.OFFHAND);hold.grip(false);genericSupport=false;stopFire();return true;}
            if(ownedOffGrip&&dev.visorcompat.tacz.server.ServerPhysical.transferable(Profiles.get(Minecraft.getInstance().player.getMainHandItem()))
                &&(PhysicalClient.phase()==dev.visorcompat.tacz.physical.Handling.Phase.SUPPORT||PhysicalClient.phase()==dev.visorcompat.tacz.physical.Handling.Phase.PUMP_HOLD)){
                transferred=true;transferRequestedAt=System.nanoTime();ClientControls.stopShooting();CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(true,false,false));return true;
            }
            if(ownedOffGrip&&PhysicalClient.phase()==dev.visorcompat.tacz.physical.Handling.Phase.SUPPORT){captureCarry(HandType.OFFHAND);hold.grip(false);holsterSide=HandType.OFFHAND;}
            else hold.release(System.nanoTime(),atHolster());stopFire();}return true;}
        if(id.equals("hotbar_offhand")&&!press&&ownedOffGrip){ownedOffGrip=false;genericSupport=false;e.setCanceled(true);if(transferred){transferred=false;PhysicalClient.advancedGrip(false);hold.release(System.nanoTime(),false);stopFire();return true;}if(active()&&hold.state()==AdvancedHold.State.SUPPORT){if(carryHand==HandType.OFFHAND){if(ownedGrip)captureCarry(HandType.MAIN);else hold.release(System.nanoTime(),atHolster());stopFire();}}else PhysicalClient.advancedGrip(false);return true;}
        if(!active()||!input)return false;
        if(cylinderCarry()&&carryHand==HandType.OFFHAND&&e.getActionButton()==actions.getActionRightMouse(HandType.OFFHAND)){
            e.setCanceled(true);if(press){ownedCylinderUse=true;send(false);CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.AdvancedUse());}return true;
        }
        if(id.equals("hotbar_main")&&press){
            if(freeMain()&&PhysicalClient.target()!=dev.visorcompat.tacz.physical.Handling.Target.NONE
                &&PhysicalClient.target()!=dev.visorcompat.tacz.physical.Handling.Target.SUPPORT){
                mainBoltGrip=true;send(false);CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,true,false));e.setCanceled(true);return true;
            }
            if(transferred){
                // Bolt work wins over nearby trigger-handle re-grip while the support hand owns the rifle.
                if(PhysicalClient.target()==dev.visorcompat.tacz.physical.Handling.Target.RACK&&(!regripNear()||dev.visorcompat.tacz.physical.BoltState.blocked(Minecraft.getInstance().player.getMainHandItem()))){
                    mainBoltGrip=true;CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(false,true,false));e.setCanceled(true);return true;
                }
                var stack=Minecraft.getInstance().player.getMainHandItem();
                if(Profiles.bolt(stack)&&(dev.visorcompat.tacz.physical.BoltState.blocked(stack)||dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack))){e.setCanceled(true);return true;}
            }
            if(transferred&&regripNear()){
                transferred=false;ownedGrip=true;ClientControls.stopShooting();CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.MainAction(true,false,false));e.setCanceled(true);return true;
            }
            if(transferred){e.setCanceled(true);return true;}
            if(hold.state()==AdvancedHold.State.INSPECT||regripNear()){
                ownedGrip=true;hold.grip(true);inspectTrigger=false;stopFire();e.setCanceled(true);return true;
            }
            if(hold.state()==AdvancedHold.State.SUPPORT&&nearBody(HandType.MAIN)){ownedGrip=true;e.setCanceled(true);return true;}
            if((hold.state()==AdvancedHold.State.RELEASED||hold.state()==AdvancedHold.State.HOLSTERED)&&nearBody(HandType.MAIN)){
                captureCarry(HandType.MAIN);ownedGrip=true;hold.grip(false);stopFire();e.setCanceled(true);return true;
            }
            if(hold.state()==AdvancedHold.State.RELEASED&&CatchRadial.start(HandType.MAIN)){e.setCanceled(true);return true;}return false;
        }
        if(id.equals("hotbar_offhand")&&press){
            if(!Minecraft.getInstance().player.getOffhandItem().isEmpty())return false;
            if(hold.state()==AdvancedHold.State.MAIN&&PhysicalClient.target()!=dev.visorcompat.tacz.physical.Handling.Target.NONE){ownedOffGrip=true;PhysicalClient.advancedGrip(true);e.setCanceled(true);return true;}
            if(hold.state()==AdvancedHold.State.MAIN&&nearBody(HandType.OFFHAND)){ownedOffGrip=true;genericSupport=true;e.setCanceled(true);return true;}
            if(hold.state()==AdvancedHold.State.SUPPORT&&nearBody(HandType.OFFHAND)){ownedOffGrip=true;e.setCanceled(true);return true;}
            if((hold.state()==AdvancedHold.State.RELEASED||hold.state()==AdvancedHold.State.HOLSTERED)&&nearBody(HandType.OFFHAND)){
                captureCarry(HandType.OFFHAND);ownedOffGrip=true;hold.grip(false);holsterSide=HandType.OFFHAND;stopFire();e.setCanceled(true);return true;
            }
            if(hold.state()==AdvancedHold.State.RELEASED&&CatchRadial.start(HandType.OFFHAND)){e.setCanceled(true);return true;}return false;
        }
        if(transferred&&(e.getActionButton()==actions.getActionLeftMouse(HandType.MAIN)||e.getActionButton()==actions.getActionRightMouse(HandType.MAIN))){
            if(e.getActionButton()==actions.getActionLeftMouse(HandType.MAIN)&&PhysicalClient.action(e,input))return true;e.setCanceled(true);return true;
        }
        if(e.getActionButton()==actions.getActionLeftMouse(HandType.MAIN)){
            mainTriggerDown=press;if(!press)hold.triggerReleased();
            if(hold.state()==AdvancedHold.State.INSPECT){inspectTrigger=press;e.setCanceled(true);return true;}
            if(!hold.canFire()){e.setCanceled(true);return true;}
        }
        if(e.getActionButton()==actions.getActionRightMouse(HandType.MAIN)){
            e.setCanceled(true);
            if(!press){hold.useReleased();if(hold.state()==AdvancedHold.State.INSPECT){hold.release(System.nanoTime(),false);inspectTrigger=false;}return true;}
            ownedUse=true;var stack=Minecraft.getInstance().player.getMainHandItem();
            boolean pistol=Profiles.get(stack).supportDistance()==0&&!Profiles.get(stack).manualAction()&&!dev.visorcompat.tacz.physical.ActionState.locked(stack)&&!dev.visorcompat.tacz.compat.GunDurabilityCompat.jammed(stack);
            if(hold.inspect(System.nanoTime(),CompatSettings.inspectWindow(),pistol)){stopFire();return true;}
            if((hold.state()==AdvancedHold.State.MAIN||freeMain()||cylinderCarry()&&carryHand==HandType.MAIN)&&hold.useArmed()){
                hold.useConsumed();if(PhysicalClient.active())CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.AdvancedUse());
                else com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator.fromLocalPlayer(Minecraft.getInstance().player).reload();
            }return true;
        }
        if(hold.state()==AdvancedHold.State.SUPPORT&&(e.getActionButton()==actions.getActionLeftMouse(HandType.OFFHAND)||e.getActionButton()==actions.getActionRightMouse(HandType.OFFHAND))){e.setCanceled(true);return true;}
        if(hold.state()==AdvancedHold.State.MAIN&&e.getActionButton()==actions.getActionLeftMouse(HandType.OFFHAND)&&PhysicalClient.active()){
            var target=PhysicalClient.target();
            if((target==dev.visorcompat.tacz.physical.Handling.Target.POUCH&&Profiles.cylinder(Minecraft.getInstance().player.getMainHandItem()))||target==dev.visorcompat.tacz.physical.Handling.Target.RACK||target==dev.visorcompat.tacz.physical.Handling.Target.RELEASE||target==dev.visorcompat.tacz.physical.Handling.Target.SELECTOR){
                e.setCanceled(true);if(press){ownedFingerTrigger=true;if(target==dev.visorcompat.tacz.physical.Handling.Target.SELECTOR)CompatNetwork.CHANNEL.sendToServer(new CompatNetwork.PhysicalSelector());else if(target==dev.visorcompat.tacz.physical.Handling.Target.RACK||target==dev.visorcompat.tacz.physical.Handling.Target.POUCH){ownedFingerGrab=true;PhysicalClient.advancedGrip(true,false);}else {PhysicalClient.advancedGrip(true);PhysicalClient.advancedGrip(false);}}return true;
            }
        }
        return false;
    }
}
