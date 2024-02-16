package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.ito.BlackKnightAbility;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.entities.mobs.ability.BlackKnightEntity;
import xyz.pixelatedw.mineminenomi.events.passives.ItoPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.wypi.WyHelper;

@Mixin(value = ItoPassiveEvents.class)
public class ItoPassiveEventsMixins {

    @Inject(method = "onEntityAttack", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityAttack(LivingHurtEvent event, CallbackInfo ci) {
        if (!(event.getSource().getEntity() instanceof PlayerEntity))
            return;

        PlayerEntity attacker = (PlayerEntity) event.getSource().getEntity();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(attacker);
        IAbilityData abilityProps = AbilityDataCapability.get(attacker);
        IAllow allow = AllowCapability.get(attacker);
        LivingEntity attacked = event.getEntityLiving();

        if (!devilFruitProps.hasDevilFruit(ModAbilities.ITO_ITO_NO_MI) && !allow.getSecondFruit().equals("ito_ito"))
            return;

        BlackKnightAbility ability = abilityProps.getEquippedAbility(BlackKnightAbility.INSTANCE);
        boolean isActive = ability != null && ability.isContinuous();

        if(!isActive)
            return;

        BlackKnightEntity knight = WyHelper.getNearbyEntities(attacker.blockPosition(), attacker.level, 20, null, BlackKnightEntity.class).stream().findFirst().orElse(null);

        if (knight != null && knight.getOwner() == attacker)
            knight.forcedTargets.add(attacked);
        ci.cancel();
    }

    @Inject(method = "onEntityDamaged", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityDamaged(LivingDamageEvent event, CallbackInfo callbackInfo)
    {
        if(!(event.getEntityLiving() instanceof BlackKnightEntity))
            return;

        BlackKnightEntity entity = (BlackKnightEntity) event.getEntityLiving();
        PlayerEntity owner = entity.getOwner();

        if(owner == null)
            return;

        IDevilFruit props = DevilFruitCapability.get(owner);
        IAllow allow = AllowCapability.get(owner);

        if((!props.hasDevilFruit(ModAbilities.ITO_ITO_NO_MI) && !allow.getSecondFruit().equals("ito_ito")) || entity.getHealth() - event.getAmount() >= 0)
            return;

        IAbilityData abilityProps = AbilityDataCapability.get(owner);

        BlackKnightAbility ability = abilityProps.getEquippedAbility(BlackKnightAbility.INSTANCE);
        boolean isActive = ability != null && ability.isContinuous();

        if(!isActive)
            return;

        ability.setMaxCooldown(60);
        ability.tryStoppingContinuity(owner);

        callbackInfo.cancel();
    }
}
