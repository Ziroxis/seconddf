package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.kilo.KiloPress1Ability;
import xyz.pixelatedw.mineminenomi.api.abilities.Ability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.init.ModWeapons;

@Mixin(value = xyz.pixelatedw.mineminenomi.events.passives.KiloPassiveEvents.class)
public class KiloPassiveMixinEvents {

    @Inject(method = "onEntityUpdate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityUpdate(LivingEvent.LivingUpdateEvent event, CallbackInfo ci)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        IAbilityData abilityProps = AbilityDataCapability.get(player);
        IDevilFruit devilProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);

        if (!devilProps.getDevilFruit().equalsIgnoreCase("kilo_kilo") && ! allow.getSecondFruit().equals("kilo_kilo"))
            return;

        Ability ability = abilityProps.getEquippedAbility(KiloPress1Ability.INSTANCE);
        boolean isActive = ability != null && ability.isContinuous();
        boolean hasUmbrella = player.getMainHandItem().getItem() == ModWeapons.UMBRELLA.get() || player.getOffhandItem().getItem() == ModWeapons.UMBRELLA.get();
        boolean inAir = !player.isOnGround() && player.getDeltaMovement().y < 0;

        if (isActive && hasUmbrella && inAir)
            player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y / 2, player.getDeltaMovement().z);

    }
}
