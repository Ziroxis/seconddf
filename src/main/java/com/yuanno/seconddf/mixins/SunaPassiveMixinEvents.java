package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.kuku.GourmetamorphosisAbility;
import xyz.pixelatedw.mineminenomi.api.abilities.AbilityCategory;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.KukuPassiveEvents;
import xyz.pixelatedw.mineminenomi.events.passives.SunaPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModI18n;
import xyz.pixelatedw.mineminenomi.items.AkumaNoMiItem;

@Mixin(value = SunaPassiveEvents.class)
public class SunaPassiveMixinEvents {



    @Inject(method = "projectileImpactEvent", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUsed(ProjectileImpactEvent.Throwable event, CallbackInfo ci) {
        if (event.getRayTraceResult().getType() == RayTraceResult.Type.ENTITY)
        {
            EntityRayTraceResult entityHit = (EntityRayTraceResult) event.getRayTraceResult();
            if (entityHit.getEntity() instanceof LivingEntity && event.getThrowable() instanceof PotionEntity)
            {
                LivingEntity entity = (LivingEntity) entityHit.getEntity();
                if (entity instanceof PlayerEntity)
                {
                    IDevilFruit props = DevilFruitCapability.get(entity);
                    IAllow allow = AllowCapability.get(entity);
                    if (props.hasDevilFruit(ModAbilities.SUNA_SUNA_NO_MI) && !allow.getSecondFruit().equals("suna_suna"))
                    {
                        AbilityHelper.disableAbilities((PlayerEntity) entity, 80, (abl) -> abl.getCategory() == AbilityCategory.DEVIL_FRUITS);
                    }
                }
            }
        }
        ci.cancel();
    }

}
