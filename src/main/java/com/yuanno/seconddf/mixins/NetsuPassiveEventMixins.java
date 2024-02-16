package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.mogu.MoguHeavyPointAbility;
import xyz.pixelatedw.mineminenomi.abilities.netsu.NetsuEnhancementAbility;
import xyz.pixelatedw.mineminenomi.api.events.SetOnFireEvent;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.MoguPassiveEvents;
import xyz.pixelatedw.mineminenomi.events.passives.NetsuPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;

@Mixin(value = NetsuPassiveEvents.class)
public class NetsuPassiveEventMixins {

    @Inject(method = "onEntityAttackEvent", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUsed(LivingAttackEvent event, CallbackInfo ci) {
        if (event.getEntityLiving() == null || !(event.getEntityLiving() instanceof PlayerEntity))
            return;

        LivingEntity entity = event.getEntityLiving();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(entity);
        IAllow allow = AllowCapability.get(entity);
        DamageSource damageSource = event.getSource();

        if ((devilFruitProps.hasDevilFruit(ModAbilities.NETSU_NETSU_NO_MI) || allow.getSecondFruit().equals("netsu_netsu")) && damageSource.getMsgId().equals(DamageSource.IN_FIRE.getMsgId()))
        {
            entity.clearFire();
            event.setCanceled(true);
        }


        if (damageSource.getDirectEntity() instanceof LivingEntity)
        {
            LivingEntity netsuAttacker = (LivingEntity) damageSource.getDirectEntity();
            IAbilityData abilityProps = AbilityDataCapability.get(netsuAttacker);
            NetsuEnhancementAbility ability = abilityProps.getEquippedAbility(NetsuEnhancementAbility.INSTANCE);

            if (!(ability != null && ability.isContinuous()) || netsuAttacker.getMainHandItem().isEmpty())
                return;

            SetOnFireEvent e = new SetOnFireEvent((LivingEntity) damageSource.getDirectEntity(), entity, 6);
            if (!MinecraftForge.EVENT_BUS.post(e))
                entity.setSecondsOnFire(6);
        }
        ci.cancel();
    }

}
