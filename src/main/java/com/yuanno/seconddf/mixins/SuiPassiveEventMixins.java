package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.netsu.NetsuEnhancementAbility;
import xyz.pixelatedw.mineminenomi.abilities.sui.FreeSwimmingAbility;
import xyz.pixelatedw.mineminenomi.api.events.SetOnFireEvent;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.NetsuPassiveEvents;
import xyz.pixelatedw.mineminenomi.events.passives.SuiPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;

@Mixin(value = SuiPassiveEvents.class)
public class SuiPassiveEventMixins {



    @Inject(method = "onEntityUpdate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUsed(LivingEvent.LivingUpdateEvent event, CallbackInfo ci) {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        IAbilityData AbilityProps = AbilityDataCapability.get(player);
        IDevilFruit props = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!props.hasDevilFruit(ModAbilities.SUI_SUI_NO_MI) && ! allow.getSecondFruit().equals("sui_sui"))
            return;

        boolean flying = player.abilities.flying;
        boolean creative = player.isCreative();
        boolean isOnGround = player.isOnGround();
        BlockPos pos = player.blockPosition();
        boolean isMidAir = player.level.getBlockState(pos.above()).isAir() &&  player.level.getBlockState(pos.below(2)).isAir();

        FreeSwimmingAbility ability = AbilityProps.getEquippedAbility(FreeSwimmingAbility.INSTANCE);

        if (ability != null)
        {
            if (ability.isContinuous())
            {
                if (player.isInWater())
                {
                    ability.isSwimming = false;
                }
                else
                {
                    if(isMidAir)
                        ability.stopContinuity(player);

                    if (isOnGround && player.isSprinting() && !flying)
                    {
                        AbilityHelper.setPose(player, Pose.SWIMMING);
                    }

                    player.setSwimming(true);
                    boolean swimming = player.getPose() == Pose.SWIMMING;
                    Vector3d playerMotion = player.getDeltaMovement();

                    if (!player.level.isClientSide)
                        ability.isSwimming = swimming;

                    if (swimming)
                    {
                        player.noPhysics = true;
                        player.setDeltaMovement(playerMotion.multiply(1.7D, 0.75D, 1.7D));
                        playerMotion = player.getDeltaMovement();

                        if (!isEntityInsideOpaqueBlock(player))
                        {
                            double fall = -0.15d;

                            if (AbilityHelper.isJumping(player))
                                fall -= 0.065d;
                            else if (player.isCrouching())
                                fall += 0.04d;

                            player.setDeltaMovement(playerMotion.x, fall, playerMotion.z);
                        }
                        else
                        {
                            if (player.getEffect(Effects.WATER_BREATHING) == null)
                            {
                                int airLeft = player.getAirSupply();
                                player.setAirSupply(airLeft - 5);
                                if (airLeft <= 0)
                                {
                                    if (player.tickCount % 10 == 0)
                                    {
                                        player.hurt(DamageSource.DROWN, 2);
                                    }
                                }
                            }

                            if (playerMotion.y < 0.0D)
                                player.setDeltaMovement(playerMotion.multiply(1.0D, 0.8D, 1.0D));

                            Vector3d Vector3d1 = player.getDeltaMovement();
                            double d3 = player.getLookAngle().y;
                            double d4 = d3 < -0.2D ? 0.1D : 0.09D;
                            double upAlready = (d3 - Vector3d1.y) * d4;

                            player.setDeltaMovement(Vector3d1.add(0.0D, (d3 - Vector3d1.y) * d4, 0.0D));

                            if (player.isCrouching())
                            {
                                player.setDeltaMovement(Vector3d1.add(0.0D, creative ? -0.18D : -0.08D, 0.0D));
                            }

                            if (AbilityHelper.isJumping(player))
                            {
                                player.setDeltaMovement(player.getDeltaMovement().add(0.0D, Math.max(upAlready, 0.1D), 0.0D));
                            }

                            if (0 >= player.getY())
                            {
                                player.setDeltaMovement(player.getDeltaMovement().add(0, 3, 0));
                            }

                        }
                    }
                }
            }
            else
            {
                ability.isSwimming = false;
            }

        }
        ci.cancel();
    }

    private static boolean isEntityInsideOpaqueBlock(LivingEntity e)
    {
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        for (int i = 0; i < 8; ++i)
        {
            int j = MathHelper.floor(e.getY() + ((i >> 0) % 2 - 0.5F) * 0.1F + e.getEyeHeight());
            int k = MathHelper.floor(e.getX() + ((i >> 1) % 2 - 0.5F) * e.getBbHeight() * 0.8F);
            int l = MathHelper.floor(e.getZ() + ((i >> 2) % 2 - 0.5F) * e.getBbWidth() * 0.8F);
            if (blockPos.getX() != k || blockPos.getY() != j || blockPos.getZ() != l)
            {
                blockPos.set(k, j, l);
                if (e.level.getBlockState(blockPos).isSuffocating(e.level, blockPos))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
