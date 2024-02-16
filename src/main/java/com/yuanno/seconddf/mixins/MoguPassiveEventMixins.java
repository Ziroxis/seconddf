package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.mogu.MoguHeavyPointAbility;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.MoguPassiveEvents;

@Mixin(value = MoguPassiveEvents.class)
public class MoguPassiveEventMixins {



    @Inject(method = "onPlayerBreakSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUsed(PlayerEvent.BreakSpeed event, CallbackInfo ci) {
        PlayerEntity player = event.getPlayer();
        IAbilityData AbilityProps = AbilityDataCapability.get(player);
        IDevilFruit props = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!props.getDevilFruit().equals("mogu_mogu") && !allow.getSecondFruit().equals("mogu_mogu"))
            return;

        MoguHeavyPointAbility ability = AbilityProps.getEquippedAbility(MoguHeavyPointAbility.INSTANCE);
        if (ability != null && ability.isContinuous()) {
            event.setNewSpeed(event.getOriginalSpeed() * 5);
        }
        ci.cancel();
    }

}
