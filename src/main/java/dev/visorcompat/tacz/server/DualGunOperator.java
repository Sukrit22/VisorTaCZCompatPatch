package dev.visorcompat.tacz.server;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.*;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import dev.visorcompat.tacz.GunPose;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** A native TaCZ operation machine for one physical weapon, without inventory mutation. */
public final class DualGunOperator implements IGunOperator {
    private static final NativeOperationScope<DualGunOperator> SCOPE = new NativeOperationScope<>();
    private final ServerPlayer player;
    private final Supplier<ItemStack> slot;
    private final Supplier<GunPose> pose;
    private final ItemStack weapon;
    private final ShooterDataHolder data;
    private final LivingEntityDrawGun draw;
    private final LivingEntityShoot shoot;
    private final LivingEntityReload reload;
    private final LivingEntityBolt bolt;
    private final LivingEntityAmmoCheck ammo;
    private ReloadState reloadState = new ReloadState();
    private boolean closed;
    private final DualCharge charge = new DualCharge();

    public DualGunOperator(ServerPlayer player, Supplier<ItemStack> slot, Supplier<GunPose> pose) {
        this(player, slot, pose, slot.get(), new ShooterDataHolder(), true);
    }

    private DualGunOperator(ServerPlayer player, Supplier<ItemStack> slot, Supplier<GunPose> pose,
                            ItemStack weapon, ShooterDataHolder data, boolean initializeDraw) {
        this.player = player;
        this.slot = slot;
        this.pose = pose;
        this.weapon = weapon;
        this.data = data;
        draw = new LivingEntityDrawGun(player, data);
        shoot = new LivingEntityShoot(player, data, draw);
        reload = new LivingEntityReload(player, data, draw, shoot);
        bolt = new LivingEntityBolt(data, player, draw, shoot);
        ammo = new LivingEntityAmmoCheck(player);
        if (initializeDraw) scoped(() -> { draw.draw(() -> weapon); return null; });
    }

    public static DualGunOperator current(LivingEntity entity) {
        DualGunOperator context = SCOPE.current();
        return context != null && context.player == entity ? context : null;
    }

    /** Wrap the native burst callback at registration, so every later round has this context. */
    public static BooleanSupplier wrap(BooleanSupplier task) {
        return SCOPE.capture(task, context -> context.valid() && context.pose() != null);
    }

    private <T> T scoped(Supplier<T> operation) {
        return SCOPE.run(this, operation);
    }

    public boolean valid() { return !closed && !player.isDeadOrDying() && slot.get() == weapon && !weapon.isEmpty(); }
    public ItemStack stack() { return weapon; }
    public ShooterDataHolder data() { return data; }
    /** Bullet-local state keeps native script modifiers bound after the held slot changes. */
    public DualGunOperator bulletProperties() {
        ItemStack capturedWeapon = weapon.copy();
        ShooterDataHolder captured = new ShooterDataHolder();
        captured.baseTimestamp = data.baseTimestamp;
        captured.shootTimestamp = data.shootTimestamp;
        captured.lastShootTimestamp = data.lastShootTimestamp;
        captured.meleeTimestamp = data.meleeTimestamp;
        captured.meleePrepTickCount = data.meleePrepTickCount;
        captured.drawTimestamp = data.drawTimestamp;
        captured.boltTimestamp = data.boltTimestamp;
        captured.isBolting = data.isBolting;
        captured.aimingProgress = data.aimingProgress;
        captured.aimingTimestamp = data.aimingTimestamp;
        captured.isAiming = data.isAiming;
        captured.reloadTimestamp = data.reloadTimestamp;
        captured.reloadStateType = data.reloadStateType;
        captured.currentGunItem = () -> capturedWeapon;
        captured.currentPutAwayTimeS = data.currentPutAwayTimeS;
        captured.sprintTimeS = data.sprintTimeS;
        captured.sprintTimestamp = data.sprintTimestamp;
        captured.knockbackStrength = data.knockbackStrength;
        captured.shootCount = data.shootCount;
        captured.chargeProgress = data.chargeProgress;
        captured.isCrawling = data.isCrawling;
        captured.scriptData = copyScriptData(data.scriptData, new java.util.IdentityHashMap<>());
        captured.heatTimestamp = data.heatTimestamp;
        captured.cacheProperty = data.cacheProperty;
        return new DualGunOperator(player, () -> capturedWeapon, () -> null, capturedWeapon, captured, false);
    }
    private static org.luaj.vm2.LuaValue copyScriptData(org.luaj.vm2.LuaValue value,
            java.util.IdentityHashMap<org.luaj.vm2.LuaValue, org.luaj.vm2.LuaValue> copies) {
        if (value == null || !value.istable()) return value;
        var existing = copies.get(value);
        if (existing != null) return existing;
        var copy = new org.luaj.vm2.LuaTable();
        copies.put(value, copy);
        org.luaj.vm2.LuaValue key = org.luaj.vm2.LuaValue.NIL;
        while (true) {
            var next = value.next(key);
            key = next.arg1();
            if (key.isnil()) break;
            copy.rawset(copyScriptData(key, copies), copyScriptData(next.arg(2), copies));
        }
        var metatable = value.getmetatable();
        if (metatable != null) copy.setmetatable(metatable);
        return copy;
    }
    public <T> T modifyBulletProperty(String id, Class<T> type, T original) {
        if (!(weapon.getItem() instanceof AbstractGunItem gun)) return original;
        return scoped(() -> gun.modifyProperty(data, weapon, player, id, type, original));
    }
    public GunPose pose() { return valid() ? pose.get() : null; }
    public ShootResult shot(float pitch, float yaw) {
        return shot(pitch, yaw, System.currentTimeMillis() - data.baseTimestamp);
    }
    public ShootResult shot(float pitch, float yaw, long timestamp) {
        if (!valid() || pose() == null) return ShootResult.NOT_DRAW;
        // This timestamp is relative to this weapon's stable server clock.
        return shoot(() -> pitch, () -> yaw, timestamp);
    }
    /** Advance native charge semantics once per server tick, using this hand's trigger only. */
    public ShootResult triggerShot(boolean requested,boolean down,float pitch,float yaw) {
        var gun=com.tacz.guns.api.item.IGun.getIGunOrNull(weapon);
        var index=gun==null?null:com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(weapon)).orElse(null);
        var cfg=index==null?null:index.getGunData().getChargeData(gun.getFireMode(weapon));
        if(cfg==null){charge.reset();return requested?shot(pitch,yaw):null;}
        boolean usable=valid()&&pose()!=null&&!data.reloadStateType.isReloading()&&!data.isBolting&&draw.getDrawCoolDown()==0;
        if(!usable){charge.reset();return null;}
        var settings=new DualCharge.Settings(DualCharge.Mode.valueOf(cfg.getChargeType().name()),Math.max(0,cfg.getIncreasePerTick()),Math.max(0,cfg.getDecreasePerTick()),Math.max(0,cfg.getDecreaseOnFire()),cfg.getMaxCharge(),Math.min(cfg.getFireThreshold(),cfg.getMaxCharge()));
        boolean ready=shoot.getShootCoolDown()==0;
        if(!charge.tick(settings,requested,down,cfg.isChargeDuringCooldown()||ready,ready))return null;
        var result=shoot(()->pitch,()->yaw,System.currentTimeMillis()-data.baseTimestamp,charge.progress());
        if(result==ShootResult.SUCCESS)charge.fired(settings);
        return result;
    }
    /** Complete a validated physical magazine insertion within this weapon's native scope. */
    public boolean loadPhysicalMagazine(Runnable restoreRemovedRounds){
        if(!valid())return false;
        return scoped(()->{
            var api=new com.tacz.guns.item.ModernKineticGunScriptAPI();api.setItemStack(weapon);api.setShooter(player);api.setDataHolder(data);
            if(api.getGunIndex()==null||net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.tacz.guns.api.event.common.GunReloadEvent(player,weapon,net.minecraftforge.fml.LogicalSide.SERVER)))return false;
            restoreRemovedRounds.run();
            int needed=Math.max(0,api.getNeededAmmoAmount());
            boolean consume=api.isReloadingNeedConsumeAmmo()&&!api.getGunIndex().getGunData().getReloadData().isInfinite();
            if(needed>0)api.putAmmoInMagazine(consume?api.consumeAmmoFromPlayer(needed):needed);
            return true;
        });
    }
    public void beginPhysicalHandling(){
        scoped(()->{reload.cancelReload();data.reloadTimestamp=-1;data.reloadStateType=ReloadState.StateType.NOT_RELOADING;reloadState=new ReloadState();return null;});charge.reset();
    }
    public void resetCharge(){charge.reset();}
    public void tick() {
        if (!valid()) { close(); return; }
        scoped(() -> {
            reloadState = reload.tickReloadState();
            bolt.tickBolt();
            // Native desktop auto-bolt belongs to the selected-hand client. Dual hands need their own route.
            var gun=com.tacz.guns.api.item.IGun.getIGunOrNull(weapon);
            if(gun!=null&&!gun.hasBulletInBarrel(weapon)&&pose()!=null){
                var index=com.tacz.guns.api.TimelessAPI.getCommonGunIndex(gun.getGunId(weapon)).orElse(null);
                if(index!=null&&index.getGunData().getBolt()==com.tacz.guns.resource.pojo.data.gun.Bolt.MANUAL_ACTION
                        &&(gun.useInventoryAmmo(weapon)||gun.getCurrentAmmoCount(weapon)>0))bolt.bolt();
            }
            if (weapon.getItem() instanceof AbstractGunItem heatGun) heatGun.tickHeat(data, weapon, player);
            return null;
        });
    }
    public void close() {
        if (closed) return;
        scoped(() -> { reload.cancelReload(); return null; });
        closed = true;
        data.initialData();
        data.currentGunItem = null;
    }

    @Override public long getSynShootCoolDown() { return shoot.getShootCoolDown(); }
    @Override public long getSynMeleeCoolDown() { return 0; }
    @Override public long getSynDrawCoolDown() { return draw.getDrawCoolDown(); }
    @Override public boolean getSynIsBolting() { return data.isBolting; }
    @Override public ReloadState getSynReloadState() { return reloadState; }
    @Override public float getSynAimingProgress() { return data.aimingProgress; }
    @Override public boolean getSynIsAiming() { return data.isAiming; }
    @Override public float getSynSprintTime() { return data.sprintTimeS; }
    @Override public void initialData() { scoped(() -> { draw.draw(() -> weapon); return null; }); }
    @Override public void draw(Supplier<ItemStack> ignored) { initialData(); }
    @Override public void bolt() { if (valid()) scoped(() -> { bolt.bolt(); return null; }); }
    @Override public void reload() { if (valid()) scoped(() -> { reload.reload(); return null; }); }
    @Override public void cancelReload() { scoped(() -> { reload.cancelReload(); return null; }); }
    @Override public void fireSelect() { if (valid()) scoped(() -> { new LivingEntityFireSelect(player, data).fireSelect(); return null; }); }
    @Override public void zoom() { }
    @Override public void melee() { }
    @Override public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return shoot(pitch, yaw, System.currentTimeMillis() - data.baseTimestamp);
    }
    @Override public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        return scoped(() -> shoot.shoot(pitch, yaw, timestamp));
    }
    @Override public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float charge) {
        return scoped(() -> shoot.shoot(pitch, yaw, timestamp, charge));
    }
    @Override public boolean needCheckAmmo() { return ammo.needCheckAmmo(); }
    @Override public boolean consumesAmmoOrNot() { return ammo.consumesAmmoOrNot(); }
    @Override public boolean getProcessedSprintStatus(boolean sprint) { return sprint; }
    @Override public void aim(boolean aim) { data.isAiming = aim; data.aimingProgress = aim ? 1 : 0; }
    @Override public void crawl(boolean crawl) { data.isCrawling = crawl; }
    @Override public void updateCacheProperty(AttachmentCacheProperty cache) { data.cacheProperty = cache; }
    @Override public AttachmentCacheProperty getCacheProperty() { return data.cacheProperty; }
    @Override public ShooterDataHolder getDataHolder() { return data; }
    @Override public boolean nextBulletIsTracer(int interval) {
        data.shootCount++;
        return interval != -1 && data.shootCount % (interval + 1) == 0;
    }
}
