package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.pixelatedw.mineminenomi.abilities.kuku.GourmetamorphosisAbility;
import xyz.pixelatedw.mineminenomi.api.helpers.AbilityHelper;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.IDevilFruit;
import xyz.pixelatedw.mineminenomi.events.passives.KukuPassiveEvents;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;
import xyz.pixelatedw.mineminenomi.init.ModI18n;
import xyz.pixelatedw.mineminenomi.items.AkumaNoMiItem;

@Mixin(value = KukuPassiveEvents.class)
public class KukuPassiveMixinEvents {



    @Inject(method = "onItemUsed", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onItemUsed(PlayerInteractEvent.RightClickItem event, CallbackInfo ci) {
        PlayerEntity player = event.getPlayer();
        IDevilFruit props = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if(!props.hasDevilFruit(ModAbilities.KUKU_KUKU_NO_MI) && !allow.getSecondFruit().equals("kuku_kuku"))
            return;

        IAbilityData abilityProps = AbilityDataCapability.get(player);

        GourmetamorphosisAbility ability = abilityProps.getEquippedAbility(GourmetamorphosisAbility.INSTANCE);
        boolean hasAbilityActive = ability != null && ability.isContinuous();

        if(hasAbilityActive)
        {
            ItemStack stack = event.getItemStack();
            int foodlevel = 2;
            float saturation = 0.25f;

            if(stack.getItem() instanceof AkumaNoMiItem)
                return;

            if(stack.isEdible())
            {
                Food food = stack.getItem().getFoodProperties();
                foodlevel += food.getNutrition();
                saturation += food.getSaturationModifier();
            }

            player.broadcastBreakEvent(Hand.MAIN_HAND);
            player.getFoodData().eat(foodlevel, saturation);
            stack.shrink(1);
        }
        ci.cancel();
    }

    @Inject(method = "onEdibleCheck", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onRenderBlockOverlay(ItemTooltipEvent event, CallbackInfo ci) {
        PlayerEntity player = event.getPlayer();

        if(player == null)
            return;

        IDevilFruit props = DevilFruitCapability.get(player);
        IAllow allow = AllowCapability.get(player);
        if(player == null || (!props.hasDevilFruit(ModAbilities.KUKU_KUKU_NO_MI) && ! allow.getSecondFruit().equals("kuku_kuku")))
            return;

        IAbilityData abilityProps = AbilityDataCapability.get(player);

        GourmetamorphosisAbility ability = abilityProps.getEquippedAbility(GourmetamorphosisAbility.INSTANCE);
        boolean hasAbilityActive = ability != null && ability.isContinuous();

        if(hasAbilityActive)
        {
            if(event.getItemStack().getItem() instanceof AkumaNoMiItem)
                return;

            StringTextComponent foodString = new StringTextComponent(TextFormatting.YELLOW + "" + new TranslationTextComponent(ModI18n.ITEM_GOURMETAMORPHOSIS_FOOD).getString());
            if(!event.getToolTip().contains(foodString))
            {
                event.getToolTip().add(new StringTextComponent(""));
                event.getToolTip().add(foodString);
            }
        }

        ci.cancel();
    }
}
