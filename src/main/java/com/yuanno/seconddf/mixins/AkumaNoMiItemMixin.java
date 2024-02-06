package com.yuanno.seconddf.mixins;

import com.google.common.base.Strings;
import com.yuanno.seconddf.Main;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import com.yuanno.seconddf.network.SSyncAllowPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.pixelatedw.mineminenomi.api.DFEncyclopediaEntry;
import xyz.pixelatedw.mineminenomi.api.OneFruitEntry;
import xyz.pixelatedw.mineminenomi.api.abilities.AbilityCore;
import xyz.pixelatedw.mineminenomi.api.events.EatDevilFruitEvent;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.api.helpers.ItemsHelper;
import xyz.pixelatedw.mineminenomi.config.CommonConfig;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.EntityStatsCapability;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.IEntityStats;
import xyz.pixelatedw.mineminenomi.data.world.ExtendedWorldData;
import xyz.pixelatedw.mineminenomi.init.*;
import xyz.pixelatedw.mineminenomi.items.AkumaNoMiItem;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncAbilityDataPacket;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncDevilFruitPacket;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncEntityStatsPacket;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

@Mixin(value = AkumaNoMiItem.class, priority = 5)
public class AkumaNoMiItemMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"), remap = false, cancellable = true)
    private void onCompleteCreation(ItemStack itemStack, World world, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        Main.LOGGER.info("Eating devil fruit");
        if (!(livingEntity instanceof PlayerEntity))
            cir.setReturnValue(itemStack);

        PlayerEntity player = (PlayerEntity) livingEntity;
        EatDevilFruitEvent.Pre preEvent = new EatDevilFruitEvent.Pre(player, itemStack);
        if (MinecraftForge.EVENT_BUS.post(preEvent))
            cir.setReturnValue(itemStack);

        if (!player.level.isClientSide)
        {
            IDevilFruit devilFruitProps = DevilFruitCapability.get(player);
            IEntityStats entityStatsProps = EntityStatsCapability.get(player);
            IAbilityData abilityDataProps = AbilityDataCapability.get(player);
            ExtendedWorldData worldData = ExtendedWorldData.get(world);
            IAllow allow = AllowCapability.get(player);
            AkumaNoMiItem eatenItem = (AkumaNoMiItem) itemStack.getItem();
            String eatenFruit = eatenItem.getFruitKey();

            boolean hasFruit = !Strings.isNullOrEmpty(devilFruitProps.getDevilFruit());
            boolean hasYami = devilFruitProps.hasDevilFruit(ModAbilities.YAMI_YAMI_NO_MI);

            if(CommonConfig.INSTANCE.getRandomizedFruits())
            {
                eatenItem = eatenItem.getShiftedFruit(world);
                eatenFruit = eatenItem.getFruitKey();
            }

            boolean flag = worldData.isFruitInUse(eatenFruit);
            flag |= !worldData.updateOneFruit(eatenFruit, player.getUUID(), OneFruitEntry.Status.IN_USE);
            if(CommonConfig.INSTANCE.hasOneFruitPerWorldSimpleLogic() && flag)
            {
                Main.LOGGER.error("Check 1");
                player.sendMessage(new TranslationTextComponent(ModI18n.ITEM_MESSAGE_FRUIT_ALREADY_USED), Util.NIL_UUID);
                itemStack.shrink(1);
                cir.setReturnValue(itemStack);
            }

            // kills player yami or not if config not enabled
            // kills player if the player is not allowed to have two fruits and has two devil fruits
            if(!CommonConfig.INSTANCE.isYamiPowerEnabled() && hasFruit && !allow.hasAllow() || allow.hasTwoDevilFruits())
            {
                Main.LOGGER.error("Check 2");
                this.applyCurseDeath(player);
                itemStack.shrink(1);
                cir.setReturnValue(itemStack);
            }

            if(CommonConfig.INSTANCE.isYamiPowerEnabled())
            {
                // If the player eats any fruit besides yami and it currently doesn't have Yami ate: death
                // ex: mera + pika = death
                // plus a check if it has already two devil fruits and the player is not allowed to have two fruits
                Main.LOGGER.error("Check 3");
                if(hasFruit && !allow.hasAllow() || allow.hasTwoDevilFruits())
                {
                    this.applyCurseDeath(player);
                    itemStack.shrink(1);
                    worldData.lostOneFruit(eatenFruit, player.getUUID(), "Devil Fruit's Curse");
                    cir.setReturnValue(itemStack);
                }
            }



            // Logic for if the player eats Hito Hito no Mi
            if (eatenItem == ModAbilities.HITO_HITO_NO_MI)
            {
                player.sendMessage(new TranslationTextComponent(ModI18n.ITEM_MESSAGE_GAINED_ENLIGHTENMENT), Util.NIL_UUID);
                entityStatsProps.setRace(ModValues.HUMAN);

                AbilityHelper.validateStyleMoves(player);
                AbilityHelper.validateRacialAndHakiAbilities(player);

                WyNetwork.sendTo(new SSyncEntityStatsPacket(player.getId(), entityStatsProps), player);
            }

            // Logic for if the player eats anything other than Yomi Yomi no Mi, at which point the player won't get any abilities from the start!
            if (eatenItem != ModAbilities.YOMI_YOMI_NO_MI)
            {
                for (AbilityCore core : eatenItem.getAbilities())
                {
                    if (!AbilityHelper.verifyIfAbilityIsBanned(core) && !abilityDataProps.hasUnlockedAbility(core))
                    {
                        abilityDataProps.addUnlockedAbility(core);
                    }
                }

                WyNetwork.sendTo(new SSyncDevilFruitPacket(player.getId(), devilFruitProps), player);
                WyNetwork.sendTo(new SSyncAbilityDataPacket(player.getId(), abilityDataProps), player);
            }

            if (player instanceof ServerPlayerEntity)
                ModAdvancements.CONSUME_DEVIL_FRUIT.trigger((ServerPlayerEntity) player, itemStack);

            // If the player doesn't have a fruit (or has yami) replace it with the currently eaten one.
            if(!allow.hasTwoDevilFruits())
            {
                DFEncyclopediaEntry elements = eatenItem.getRandomElements(world);
                ItemsHelper.updateEncyclopediae(player, eatenFruit, elements);

                if (devilFruitProps.hasDevilFruit()) {
                    allow.setTwoDevilFruits(true);
                }
                else
                    allow.setSecondFruit(eatenItem.getFruitKey());
                WyNetwork.sendTo(new SSyncAllowPacket(player.getId(), allow), player);
                devilFruitProps.setDevilFruit(eatenItem);
                WyNetwork.sendTo(new SSyncDevilFruitPacket(player.getId(), devilFruitProps), player);
            }

            player.eat(world, itemStack);
        }

        EatDevilFruitEvent.Post postEvent = new EatDevilFruitEvent.Post(player, itemStack);
        MinecraftForge.EVENT_BUS.post(postEvent);

        itemStack.shrink(1);

        cir.setReturnValue(itemStack);
    }

    private void applyCurseDeath(PlayerEntity player)
    {
        player.hurt(ModDamageSource.DEVILS_CURSE, Float.MAX_VALUE);
    }
}
