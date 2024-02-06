package com.yuanno.seconddf;

import com.mojang.brigadier.CommandDispatcher;
import com.yuanno.seconddf.commands.AllowSecondDevilfruitCommand;
import com.yuanno.seconddf.commands.InformationCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeSetup {

    @SubscribeEvent
    public static void serverStarting(FMLServerStartingEvent event)
    {
        CommandDispatcher dispatcher = event.getServer().getCommands().getDispatcher();
        AllowSecondDevilfruitCommand.register(dispatcher);
        InformationCommand.register(dispatcher);

    }
}
