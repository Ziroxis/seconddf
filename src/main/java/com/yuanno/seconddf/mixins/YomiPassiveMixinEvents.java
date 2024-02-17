package com.yuanno.seconddf.mixins;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.yomi.YomiNoReikiAbility;
import xyz.pixelatedw.mineminenomi.api.abilities.Ability;
import xyz.pixelatedw.mineminenomi.api.events.ability.YomiTriggerEvent;
import xyz.pixelatedw.mineminenomi.api.helpers.MorphHelper;
import xyz.pixelatedw.mineminenomi.api.helpers.RendererHelper;
import xyz.pixelatedw.mineminenomi.api.morph.MorphInfo;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.entities.zoan.YomiMorphInfo;
import xyz.pixelatedw.mineminenomi.events.passives.YomiPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModAdvancements;
import xyz.pixelatedw.mineminenomi.models.morphs.YomiModel;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncDevilFruitPacket;
import xyz.pixelatedw.mineminenomi.renderers.morphs.ZoanMorphRenderer;
import xyz.pixelatedw.mineminenomi.wypi.WyHelper;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

import java.awt.*;

@Mixin(value = YomiPassiveEvents.class)
public class YomiPassiveMixinEvents {

    /*
    @Inject(method = "onRenderOverlay", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onRenderOverlay(RenderGameOverlayEvent.Pre event, CallbackInfo ci)
    {
        PlayerEntity player = Minecraft.getInstance().player;
        IDevilFruit props = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);

        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD && (props.hasDevilFruit(ModAbilities.YOMI_YOMI_NO_MI) || allow.getSecondFruit().equals("yomi_yomi")) && YomiMorphInfo.INSTANCE.isActive(player))
            event.setCanceled(true);
        ci.cancel();
    }
    
     */

    @Inject(method = "onClonePlayer", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onClonePlayer(PlayerEvent.Clone event, CallbackInfo ci)
    {
        if (event.isWasDeath())
        {
            IDevilFruit oldPlayerProps = DevilFruitCapability.get(event.getOriginal());
            IDevilFruit newPlayerProps = DevilFruitCapability.get(event.getPlayer());

            YomiTriggerEvent yomiEvent = new YomiTriggerEvent(event.getPlayer(), oldPlayerProps, newPlayerProps);
            MinecraftForge.EVENT_BUS.post(yomiEvent);

            if(event.getPlayer() instanceof ServerPlayerEntity)
                ModAdvancements.YOMI_REVIVE.trigger((ServerPlayerEntity) event.getOriginal());
        }
        ci.cancel();
    }



    @Inject(method = "onEntityUpdate", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityUpdate(LivingEvent.LivingUpdateEvent event, CallbackInfo ci)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        if (player.level.isClientSide)
            return;

        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);

        if ((!devilFruitProps.hasDevilFruit(ModAbilities.YOMI_YOMI_NO_MI) && !allow.getSecondFruit().equals("yomi_yomi")) || !YomiMorphInfo.INSTANCE.isActive(player))
            return;

        player.getFoodData().setFoodLevel(18);

        player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 10, player.isSprinting() ? 4 : 0, false, false));

        if(player.tickCount % 500 == 0)
        {
            WyNetwork.sendTo(new SSyncDevilFruitPacket(player.getId(), devilFruitProps), player);
            WyNetwork.sendToAllAround(new SSyncDevilFruitPacket(player.getId(), devilFruitProps), player);
        }

        BlockState state = player.level.getBlockState(player.blockPosition().below());
        if (state.getFluidState().isSource() && state.getMaterial().equals(Material.WATER) && player.isSprinting())
        {
            player.setOnGround(true);
            if (player.getDeltaMovement().y <= 0.0D && player.getDeltaMovement().y < 0.009)
            {
                Vector3d speed = player.getLookAngle().normalize().multiply(0.5, 0, 0.5);
                double ySpeed = 0 - player.getDeltaMovement().y;
                if (ySpeed > 0.002) {
                    ySpeed = 0.002;
                }
                player.setDeltaMovement(speed.x, 0, speed.z);
                ((ServerPlayerEntity) player).connection.send(new SEntityVelocityPacket(player));
                player.fallDistance = 0.0F;
            }

            for (int i = 0; i < 10; i++)
            {
                double newPosX = player.getX() + WyHelper.randomDouble();
                double newPosY = player.getY();
                double newPosZ = player.getZ() + WyHelper.randomDouble();

                ((ServerWorld) player.level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, state), newPosX, newPosY, newPosZ, 1, 0, 0, 0, 0);
            }
        }
        ci.cancel();
    }

    @Inject(method = "onHeal", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onHeal(LivingHealEvent event, CallbackInfo ci)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);

        if ((!devilFruitProps.hasDevilFruit(ModAbilities.YOMI_YOMI_NO_MI) && !allow.getSecondFruit().equals("yomi_yomi")) || !YomiMorphInfo.INSTANCE.isActive(player))
            return;

        event.setAmount(event.getAmount());
        ci.cancel();
    }


    @Inject(method = "onEntityRendered", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityRendered(RenderLivingEvent.Pre event, CallbackInfo ci)
    {
        if (!(event.getEntity() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntity();

        if (!isSpirit(player))
            return;

        MorphInfo info = MorphHelper.getZoanInfo(player);
        if(info == null)
            return;

        ZoanMorphRenderer render = (ZoanMorphRenderer) info.getRendererFactory().createRenderFor(Minecraft.getInstance().getEntityRenderDispatcher());
        IVertexBuilder vertex = event.getBuffers().getBuffer(RenderType.entityTranslucent(render.getTextureLocation((AbstractClientPlayerEntity) player)));
        event.setCanceled(true);

        event.getMatrixStack().pushPose();
        {
            event.getMatrixStack().translate(0, 1.5, 0);
            event.getMatrixStack().mulPose(new Quaternion(Vector3f.XP, 180, true));
            event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, 180, true));
            event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, player.yRotO + (player.yRot - player.yRotO) * event.getPartialRenderTick() - 180.0F, true));
            event.getMatrixStack().mulPose(new Quaternion(Vector3f.XP, player.xRotO + (player.xRot - player.xRotO) * event.getPartialRenderTick(), true));

            ((YomiModel)render.getModel()).head.render(event.getMatrixStack(), vertex, event.getLight(), OverlayTexture.NO_OVERLAY, 0.3F, 0.9F, 0.5F, 0.6F);

            event.getMatrixStack().pushPose();
            {
                IVertexBuilder vertexBuilder = event.getBuffers().getBuffer(RenderType.lightning());
                event.getMatrixStack().translate(0.0D, -0.35D, 0.0D);
                float randMovement = ((player.tickCount / 200) + event.getPartialRenderTick()) / 500.0F;
                for (int i = 0; i < 100; ++i)
                {
                    event.getMatrixStack().mulPose(Vector3f.XP.rotationDegrees(player.getRandom().nextFloat() * 360.0F));
                    event.getMatrixStack().mulPose(Vector3f.YP.rotationDegrees(player.getRandom().nextFloat() * 360.0F));
                    event.getMatrixStack().mulPose(Vector3f.ZP.rotationDegrees(player.getRandom().nextFloat() * 360.0F));
                    event.getMatrixStack().mulPose(Vector3f.XP.rotationDegrees(player.getRandom().nextFloat() * 360.0F));
                    event.getMatrixStack().mulPose(Vector3f.YP.rotationDegrees(player.getRandom().nextFloat() * 360.0F));
                    event.getMatrixStack().mulPose(Vector3f.ZP.rotationDegrees(player.getRandom().nextFloat() * 360.0F + randMovement * 90.0F));
                    float f3 = 0.6f * player.getRandom().nextFloat();
                    float f4 = 0.6f * player.getRandom().nextFloat();
                    Matrix4f matrix4f = event.getMatrixStack().last().pose();

                    int alpha = 5;
                    Color primaryColor = new Color(0, 255, 0, alpha);
                    Color secondaryColor = new Color(0, 255, 50, alpha);

                    RendererHelper.drawA(vertexBuilder, matrix4f, primaryColor);
                    RendererHelper.drawB(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                    RendererHelper.drawC(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                    RendererHelper.drawA(vertexBuilder, matrix4f, primaryColor);
                    RendererHelper.drawC(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                    RendererHelper.drawD(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                    RendererHelper.drawA(vertexBuilder, matrix4f, primaryColor);
                    RendererHelper.drawD(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                    RendererHelper.drawB(vertexBuilder, matrix4f, f3, f4, secondaryColor);
                }
            }
            event.getMatrixStack().popPose();
        }
        event.getMatrixStack().popPose();
        ci.cancel();
    }



    @Inject(method = "onDrinkMilk", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onDrinkMilk(LivingEntityUseItemEvent.Finish event, CallbackInfo ci)
    {
        if (!(event.getEntityLiving() instanceof PlayerEntity))
            return;

        IDevilFruit devilFruitProps = DevilFruitCapability.get(event.getEntityLiving());
        IAllow allow = AllowCapability.get(event.getEntityLiving());
        if (!devilFruitProps.hasDevilFruit(ModAbilities.YOMI_YOMI_NO_MI) && !allow.getSecondFruit().equals("yomi_yomi"))
            return;

        if (event.getItem().getItem() == Items.MILK_BUCKET && YomiMorphInfo.INSTANCE.isActive(event.getEntityLiving()))
            event.getEntityLiving().heal(8);
        ci.cancel();
    }

    private static boolean isSpirit(PlayerEntity player)
    {
        IAbilityData abilityProps = AbilityDataCapability.get(player);

        if (player.isCreative() || player.isSpectator())
            return false;

        Ability ability = abilityProps.getEquippedAbility(YomiNoReikiAbility.INSTANCE);

        return ability != null && ability.isContinuous();
    }

}
