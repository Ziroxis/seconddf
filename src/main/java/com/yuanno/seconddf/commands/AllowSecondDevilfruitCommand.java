package com.yuanno.seconddf.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.yuanno.seconddf.data.allow.AllowCapability;
import com.yuanno.seconddf.data.allow.IAllow;
import com.yuanno.seconddf.network.SSyncAllowPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import xyz.pixelatedw.mineminenomi.api.abilities.AbilityCategory;
import xyz.pixelatedw.mineminenomi.data.entity.ability.AbilityDataCapability;
import xyz.pixelatedw.mineminenomi.data.entity.ability.IAbilityData;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.EntityStatsCapability;
import xyz.pixelatedw.mineminenomi.data.entity.entitystats.IEntityStats;
import xyz.pixelatedw.mineminenomi.events.abilities.AbilityProgressionEvents;
import xyz.pixelatedw.mineminenomi.init.ModValues;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncAbilityDataPacket;
import xyz.pixelatedw.mineminenomi.packets.server.SSyncEntityStatsPacket;
import xyz.pixelatedw.mineminenomi.wypi.WyNetwork;

import java.util.ArrayList;
import java.util.List;

public class AllowSecondDevilfruitCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("allowsecond").requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("choice", StringArgumentType.string()).suggests(SUGGEST_SET)
                                .executes((command) ->
                                {
                                    String choice = StringArgumentType.getString(command, "choice");

                                    return setAllow(command.getSource(), EntityArgument.getPlayer(command, "target"), choice);
                                }))));
    }

    private static final SuggestionProvider<CommandSource> SUGGEST_SET = (source, builder) -> {
        List<String> suggestions = new ArrayList<>();

        suggestions.add("true");
        suggestions.add("false");


        return ISuggestionProvider.suggest(suggestions.stream(), builder);
    };

    private static int setAllow(CommandSource commandSource, PlayerEntity player, String allow)
    {
        IAllow allowProps = AllowCapability.get(player);
        if (allow.equals("true"))
        {
            allowProps.setAllow(true);
            commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " is allowed to have two devil fruits"), true);

        }
        else
        {
            allowProps.setAllow(false);
            commandSource.sendSuccess(new TranslationTextComponent(player.getDisplayName().getString() + " is not allowed to have two devil fruits"), true);
        }

        WyNetwork.sendTo(new SSyncAllowPacket(player.getId(), allowProps), player);
        return 1;
    }
}
