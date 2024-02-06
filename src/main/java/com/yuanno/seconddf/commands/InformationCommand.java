package com.yuanno.seconddf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;


public class InformationCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("information").requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                                .executes((command) ->
                                {

                                    return information(command.getSource(), EntityArgument.getPlayer(command, "target"));
                                })));
    }


    private static int information(CommandSource commandSource, PlayerEntity player)
    {
        IAllow allow = AllowCapability.get(player);
        if (allow.hasAllow())
            commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " is allowed to have two devil fruits"), true);
        else
            commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " is not allowed to have two devil fruits"), true);
        commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " has: " +allow.getSecondFruit()), true);
        if (allow.hasTwoDevilFruits())
            commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " has two devil fruits"), true);


        return 1;
    }
}
