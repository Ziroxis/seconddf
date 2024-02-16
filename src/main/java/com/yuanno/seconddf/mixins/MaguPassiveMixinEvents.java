package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;

import java.util.Arrays;
import java.util.List;

@Mixin(value = xyz.pixelatedw.mineminenomi.events.passives.MaguPassiveEvents.class)
public class MaguPassiveMixinEvents {

    private static final List<String> fireFruits = Arrays.asList("magu_magu", "mera_mera", "goro_goro");


    @Inject(method = "onEntityUpdate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityUpdate(LivingEvent.LivingUpdateEvent event, CallbackInfo ci) {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);

        if (!devilFruitProps.hasDevilFruit(ModAbilities.MAGU_MAGU_NO_MI) && !allow.getSecondFruit().equals("magu_magu"))
            return;

        if (player.isInLava() && !player.abilities.flying) {
            float a = (player.zza > 0) ? 2f : 0.5f;
            float y = player.isCrouching() && !(player.getY() - player.yo > 0) ? 2f : 0f;
            Vector3d Vector3d = player.getDeltaMovement().multiply(a, y, a);

            if(AbilityHelper.isJumping(player))
                player.setDeltaMovement(Vector3d.add(0, 0.4f, 0));
            else
                player.setDeltaMovement(Vector3d);
        }

        ci.cancel();
    }

    @Inject(method = "onRenderBlockOverlay", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onRenderBlockOverlay(RenderBlockOverlayEvent event, CallbackInfo ci) {
        PlayerEntity player = Minecraft.getInstance().player;
        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!fireFruits.contains(devilFruitProps.getDevilFruit()) && !fireFruits.contains(allow.getSecondFruit()))
            return;

        if(event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE))
            event.setCanceled(true);

        ci.cancel();
    }

    @Inject(method = "onEntityInLava", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityInLava(EntityViewRenderEvent.FogDensity event, CallbackInfo ci) {
        PlayerEntity player = Minecraft.getInstance().player;
        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!devilFruitProps.hasDevilFruit(ModAbilities.MAGU_MAGU_NO_MI) && !allow.getSecondFruit().equals("magu_magu"))
            return;

        if(player.isEyeInFluid(FluidTags.LAVA))
        {
            event.setCanceled(true);
            event.setDensity(0.025f);
        }

        ci.cancel();
    }
}
