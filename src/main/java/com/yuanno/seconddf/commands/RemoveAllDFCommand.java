package com.yuanno.seconddf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import com.yuanno.seconddf.network.SSyncAllowPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.pixelatedw.mineminenomi.abilities.FlyAbility;
import xyz.pixelatedw.mineminenomi.api.abilities.*;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.config.GeneralConfig;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.data.world.ExtendedWorldData;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncAbilityDataPacket;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncDevilFruitPacket;
import xyz.pixelatedw.mineminenomi.wypi.WyDebug;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

import javax.annotation.Nullable;

public class RemoveAllDFCommand
{
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("removealldf")
			.requires(source -> source.hasPermission(GeneralConfig.PUBLIC_REMOVEDF.get() ? 0 : 2))
			.executes(context -> removesDF(context, context.getSource().getPlayerOrException()))
			.then(Commands.argument("target", EntityArgument.player())
				.requires(source -> source.hasPermission(2))
				.executes(context -> removesDF(context, EntityArgument.getPlayer(context, "target")))));
		

	}

	private static int removesDF(CommandContext<CommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException
	{
		try
		{
			IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
			IAbilityData abilityDataProps = AbilityDataCapability.get(player);
			IAllow allow = AllowCapability.get(player);
			ExtendedWorldData worldData = ExtendedWorldData.get(player.level);
	
			worldData.lostOneFruit(devilFruitProps.getDevilFruit(), player.getUUID(), "Removed via Command");
			if(devilFruitProps.hasYamiPower()) {
				worldData.lostOneFruit("yami_yami", player.getUUID(), "Removed via Command");
			}
			
			devilFruitProps.removeDevilFruit();
			allow.setSecondFruit("");
			allow.setTwoDevilFruits(false);
			for (Ability ability : abilityDataProps.getEquippedAbilities(AbilityCategory.DEVIL_FRUITS.isPartofCategory()))
			{
				if (ability != null)
				{
					if (ability instanceof ContinuousAbility)
						((ContinuousAbility)ability).stopContinuity(player);
					else if (ability instanceof ChargeableAbility)
						((ChargeableAbility)ability).stopCharging(player);
					
					ability.stopCooldown(player);
				}
			}
	
			boolean hasUnlockedFlying = abilityDataProps.getUnlockedAbilities(abl -> abl instanceof FlyAbility && !((FlyAbility)abl).isPaused()).size() > 0;
			if (hasUnlockedFlying && !player.isCreative() && !player.isSpectator())
			{
				player.abilities.mayfly = false;
				player.abilities.flying = false;
				player.connection.send(new SPlayerAbilitiesPacket(player.abilities));
			}
	
			abilityDataProps.clearUnlockedAbilities(AbilityCategory.DEVIL_FRUITS.isPartofCategory());
			abilityDataProps.clearEquippedAbilities(AbilityCategory.DEVIL_FRUITS.isPartofCategory());
	
			if(WyDebug.isDebug())
				player.removeAllEffects();
			
			WyNetwork.sendToAllTrackingAndSelf(new SSyncDevilFruitPacket(player.getId(), devilFruitProps), player);
			WyNetwork.sendToAllTrackingAndSelf(new SSyncAbilityDataPacket(player.getId(), abilityDataProps), player);
			WyNetwork.sendToAllTrackingAndSelf(new SSyncAllowPacket(player.getId(), allow), player);

			// Removing all the attributes received from abilities, dying automatically resets these
			ForgeRegistries.ATTRIBUTES.forEach(attr -> {
				if (player.getAttribute(attr) != null) {
					player.getAttribute(attr).getModifiers().stream().filter(AbilityAttributeModifier.class::isInstance).map(AbilityAttributeModifier.class::cast).forEach(mod -> {
						player.getAttribute(attr).removeModifier(mod);
					});
				}
			});
			
			// Forces abilities to enable back as the user will no longer be a DF user
			AbilityHelper.enableAbilities(player, (ability) -> true);
			
			context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Removed Devil Fruit for " + player.getDisplayName().getString()), true); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return 1;
	}
}
