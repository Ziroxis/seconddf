package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.kage.DoppelmanAbility;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.entities.mobs.ability.DoppelmanEntity;
import xyz.pixelatedw.mineminenomi.events.passives.KagePassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.wypi.WyHelper;

@Mixin(value = KagePassiveEvents.class)
public class KageMixinEvents {

    @Inject(method = "onEntityHurt", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityHurt(LivingAttackEvent event, CallbackInfo ci) {
        if (!(event.getSource().getEntity() instanceof PlayerEntity))
            return;

        PlayerEntity attacker = (PlayerEntity) event.getSource().getEntity();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(attacker);
        IAbilityData abilityProps = AbilityDataCapability.get(attacker);
        IAllow allow = AllowCapability.get(attacker);
        LivingEntity attacked = event.getEntityLiving();

        if (!devilFruitProps.hasDevilFruit(ModAbilities.KAGE_KAGE_NO_MI) && !allow.getSecondFruit().equals("kage_kage"))
            return;

        DoppelmanAbility ability = abilityProps.getEquippedAbility(DoppelmanAbility.INSTANCE);
        boolean isActive = ability != null && ability.isContinuous();

        if(!isActive)
            return;

        DoppelmanEntity doppelman = WyHelper.getNearbyEntities(attacker.blockPosition(), attacker.level, 20, null, DoppelmanEntity.class).stream().findFirst().orElse(null);

        if (doppelman != null && doppelman.getOwner() == attacker)
            doppelman.forcedTargets.add(attacked);

        ci.cancel();
    }
}
