package com.yuanno.seconddf.init;

import com.yuanno.seconddf.Main;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.AllowProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ModCapabilities {

    public static void init() {
        AllowCapability.register();
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() == null)
            return;

        if (event.getObject() instanceof PlayerEntity)
        {
            event.addCapability(new ResourceLocation(Main.MOD_ID, "allow_data"), new AllowProvider());
        }
    }
}
