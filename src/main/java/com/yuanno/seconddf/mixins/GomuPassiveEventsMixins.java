package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.api.damagesource.SourceElement;
import xyz.pixelatedw.mineminenomi.api.helpers.HakiHelper;
import xyz.pixelatedw.mineminenomi.api.helpers.ItemsHelper;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.entities.projectiles.AbilityProjectileEntity;
import xyz.pixelatedw.mineminenomi.entities.projectiles.extra.CannonBallProjectile;
import xyz.pixelatedw.mineminenomi.entities.projectiles.extra.NormalBulletProjectile;
import xyz.pixelatedw.mineminenomi.entities.projectiles.extra.PopGreenProjectile;
import xyz.pixelatedw.mineminenomi.events.passives.GomuPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModDamageSource;
import xyz.pixelatedw.mineminenomi.items.weapons.ClimaTactItem;
import xyz.pixelatedw.mineminenomi.items.weapons.CoreSwordItem;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(value = GomuPassiveEvents.class)
public class GomuPassiveEventsMixins {

    @Inject(method = "onEntityHurt", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityHurt(LivingHurtEvent event, CallbackInfo callbackInfo)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        DamageSource source = event.getSource();
        Entity instantSource = source.getDirectEntity();
        Entity trueSource = source.getEntity();
        PlayerEntity attacked = (PlayerEntity) event.getEntityLiving();
        IDevilFruit props = DevilFruitCapability.get(attacked);
        IAllow allow = AllowCapability.get(attacked);

        if ((!props.hasDevilFruit(ModAbilities.GOMU_GOMU_NO_MI) && !allow.getSecondFruit().equals("gomu_gomu")) || source.isMagic())
            return;

        float reduction = 0;
        ArrayList<String> instantSources = new ArrayList<>(Arrays.asList("mob", "player"));

        boolean a = false;
        if(instantSource instanceof LivingEntity)
        {
            ItemStack mainhandGear = ((LivingEntity) instantSource).getItemBySlot(EquipmentSlotType.MAINHAND);
            a = trueSource instanceof LivingEntity && !HakiHelper.hasHardeningActive((LivingEntity) instantSource) && instantSources.contains(source.getMsgId()) && !source.isProjectile() && getGomuDamagingItems(mainhandGear.getItem()) && !ItemsHelper.isKairosekiWeapon(mainhandGear);
        }

        boolean b = source.isProjectile() && instantSource instanceof AbilityProjectileEntity && ((AbilityProjectileEntity) instantSource).isPhysical() && !((AbilityProjectileEntity) instantSource).isAffectedByHaki();

        if ((a || b) && !source.isExplosion())
            reduction = 0.75F;

        if (source.getMsgId().equals(DamageSource.LIGHTNING_BOLT.getMsgId()))
            reduction = 1;

        if(source instanceof ModDamageSource && ((ModDamageSource)source).getElement() == SourceElement.LIGHTNING) {
            reduction = 1;
        }

        event.setAmount(event.getAmount() * (1 - reduction));
        callbackInfo.cancel();
    }

    @Inject(method = "onEntityAttackEvent", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityAttackEvent(LivingAttackEvent event, CallbackInfo ci)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity attacked = (PlayerEntity) event.getEntityLiving();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(attacked);
        IAllow allow = AllowCapability.get(attacked);

        if (!devilFruitProps.hasDevilFruit(ModAbilities.GOMU_GOMU_NO_MI) && !allow.getSecondFruit().equals("gomu_gomu"))
            return;

        DamageSource source = event.getSource();
        Entity instantSource = source.getDirectEntity();

        if (!(instantSource instanceof NormalBulletProjectile || instantSource instanceof CannonBallProjectile || instantSource instanceof PopGreenProjectile)) {
            return;
        }

        AbilityProjectileEntity ablProj = (AbilityProjectileEntity) instantSource;

        if (ablProj.getThrower() != null && ablProj.isAffectedByHaki()) {
            LivingEntity thrower = ablProj.getThrower();

            boolean isImbued = (ablProj.isAffectedByImbuing() && HakiHelper.hasImbuingActive(thrower, true));

            if (isImbued) {
                return;
            }
        }

        event.setCanceled(true);

        ((AbilityProjectileEntity) instantSource).setThrower(attacked);
        ((AbilityProjectileEntity) instantSource).shoot(-instantSource.getDeltaMovement().x, -instantSource.getDeltaMovement().y, -instantSource.getDeltaMovement().z, 0.8f, 20);
        ci.cancel();
    }

    private static boolean getGomuDamagingItems(Item item)
    {
        if ((item instanceof SwordItem && !(item instanceof CoreSwordItem))  || item instanceof PickaxeItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof ClimaTactItem)
            return false;

        if (item instanceof CoreSwordItem)
            return ((CoreSwordItem) item).isBlunt();

        return true;
    }
}
