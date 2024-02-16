package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.ModMain;
import xyz.pixelatedw.mineminenomi.abilities.zushi.AbareHimatsuriAbility;
import xyz.pixelatedw.mineminenomi.api.abilities.Ability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.ZushiPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.models.abilities.AbareHimatsuriModel;
import xyz.pixelatedw.mineminenomi.renderers.abilities.AbareHimatsuriRenderer.Factory;

@Mixin(value = ZushiPassiveEvents.class)
public class ZushiPassiveMixinEvents
{
	private static final Factory ABARE_HIMATSURI = new Factory(new AbareHimatsuriModel());

	@Inject(method = "onEntityRendered", at = @At("HEAD"), remap = false, cancellable = true)
	private static void onEntityRendered(RenderLivingEvent.Pre event, CallbackInfo ci)
	{
		if (!(event.getEntity() instanceof PlayerEntity))
			return;

		LivingEntity entity = event.getEntity();
		IDevilFruit props = DevilFruitCapability.get(entity);
		IAllow allow = AllowCapability.get(entity);
		if(!props.hasDevilFruit(ModAbilities.ZUSHI_ZUSHI_NO_MI) && !allow.getSecondFruit().equals("zushi_zushi"))
			return;

		IAbilityData abilityProps = AbilityDataCapability.get(entity);

		Ability abareAbility = abilityProps.getEquippedAbility(AbareHimatsuriAbility.INSTANCE);
		boolean isAbareActive = abareAbility != null && abareAbility.isContinuous();

		if (!isAbareActive)
			return;

		// TODO(wynd) - Make it take the texture of the block under the player.
		
		if (!event.getEntity().isOnGround())
			ABARE_HIMATSURI.createRenderFor(Minecraft.getInstance().getEntityRenderDispatcher()).render(entity, entity.yRot, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
		ci.cancel();
	}

}
