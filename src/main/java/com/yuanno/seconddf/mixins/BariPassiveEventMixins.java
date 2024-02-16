package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.bari.BariBariNoPistolAbility;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.BariPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModRenderTypes;
import xyz.pixelatedw.mineminenomi.models.abilities.SphereModel;
import xyz.pixelatedw.mineminenomi.wypi.WyHelper;


@Mixin(value = BariPassiveEvents.class)
public class BariPassiveEventMixins {

    private static final SphereModel BARI_SPHERE = new SphereModel();


    @Inject(method = "onEntityRendered", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onEntityRendered(RenderLivingEvent.Post event, CallbackInfo ci)
    {
        if (!(event.getEntity() instanceof PlayerEntity))
            return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        LivingRenderer renderer = event.getRenderer();

        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAbilityData abilityProps = AbilityDataCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!devilFruitProps.hasDevilFruit(ModAbilities.BARI_BARI_NO_MI) && !allow.getSecondFruit().equals("bari_bari"))
            return;

        BariBariNoPistolAbility ability = abilityProps.getEquippedAbility(BariBariNoPistolAbility.INSTANCE);

        if (ability != null && ability.isContinuous())
        {
            event.getMatrixStack().pushPose();
            {
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.XP, 180.0F, true));
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, 180.0F, true));

                float ageInTicks = player.tickCount + event.getPartialRenderTick();
                float headYawOffset = MathHelper.rotLerp(event.getPartialRenderTick(), player.yBodyRotO, player.yBodyRot);

                WyHelper.rotateCorpse(event.getMatrixStack(), player, ageInTicks, headYawOffset, event.getPartialRenderTick());

                event.getMatrixStack().translate(-0.04, -1.3, 0.12);

                ((IHasArm) renderer.getModel()).translateToHand(HandSide.RIGHT, event.getMatrixStack());
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, 90.0F, true));
                event.getMatrixStack().scale(1.25f, 1.25f, 1.25f);
                event.getMatrixStack().translate(0.1, 0.4, -0.01);

                BARI_SPHERE.renderToBuffer(event.getMatrixStack(), event.getBuffers().getBuffer(ModRenderTypes.TRANSPARENT_COLOR), event.getLight(), 0, 0.5f, 1.0f, 0.8f, 0.4f);
            }
            event.getMatrixStack().popPose();
        }
        ci.cancel();
    }

    @Inject(method = "onHandRendering", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onHandRendering(RenderHandEvent event, CallbackInfo ci)
    {
        if(event.getHand() != Hand.MAIN_HAND)
            return;

        if(!event.getItemStack().isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;

        IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
        IAbilityData abilityProps = AbilityDataCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if (!devilFruitProps.hasDevilFruit(ModAbilities.BARI_BARI_NO_MI) && !allow.getSecondFruit().equals("bari_bari"))
            return;

        BariBariNoPistolAbility ability = abilityProps.getEquippedAbility(BariBariNoPistolAbility.INSTANCE);

        if (ability != null && ability.isContinuous())
        {
            EntityRendererManager renderManager = mc.getEntityRenderDispatcher();
            EntityRenderer renderer = renderManager.getRenderer(player);

            if(!(renderer instanceof PlayerRenderer))
                return;

            event.setCanceled(true);

            event.getMatrixStack().pushPose();
            {
                float f = 1.0F;
                float f1 = MathHelper.sqrt(event.getSwingProgress());
                float f2 = -0.3F * MathHelper.sin(f1 * (float) Math.PI);
                float f3 = 0.4F * MathHelper.sin(f1 * ((float) Math.PI * 2F));
                float f4 = -0.4F * MathHelper.sin(event.getSwingProgress() * (float) Math.PI);
                event.getMatrixStack().translate(f * (f2 + 0.64000005F), f3 + -0.6F + event.getEquipProgress() * -0.6F, f4 + -0.71999997F);
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, f * 45.0F, true));
                float f5 = MathHelper.sin(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI);
                float f6 = MathHelper.sin(f1 * (float) Math.PI);
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, f * f6 * 70.0F, true));
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.ZP, f * f5 * -20.0F, true));

                event.getMatrixStack().translate(f * -1.0F, 3.6F, 3.5F);
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.ZP, f * 120.0F, true));
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.XP, 200.0F, true));
                event.getMatrixStack().mulPose(new Quaternion(Vector3f.YP, f * -135.0F, true));
                event.getMatrixStack().translate(f * 5.6F, 0.0F, 0.0F);

                ((PlayerRenderer)renderer).getModel().rightArm.render(event.getMatrixStack(), event.getBuffers().getBuffer(ModRenderTypes.getAbilityBody(renderer.getTextureLocation(player))), event.getLight(), 0, 1, 1, 1, 1);

                event.getMatrixStack().translate(-0.4, 0.8, 0.01);
                event.getMatrixStack().scale(2, 2, 2);

                BARI_SPHERE.renderToBuffer(event.getMatrixStack(), event.getBuffers().getBuffer(ModRenderTypes.TRANSPARENT_COLOR), event.getLight(), 0, 0.5f, 1.0f, 0.8f, 0.4f);
            }
            event.getMatrixStack().popPose();
        }
    }

}
