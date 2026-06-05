package net.encryptedthoughts.portallinker.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.encryptedthoughts.portallinker.PortalLinkerMod;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class PortalLinkerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredCommandRegistryAccess, Commands.CommandSelection ignoredRegistrationEnvironment) {
        dispatcher.register(
            literal("portalLinker")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    if (player != null) {
                        for (var i = 0; i < PortalLinkerMod.CONFIG.Dimensions.size(); i++) {
                            var text = PortalLinkerMod.CONFIG.Dimensions.get(i).getText();
                            if (i < PortalLinkerMod.CONFIG.Dimensions.size()-1)
                                text.append("\n");
                            player.sendSystemMessage(text);
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("save").executes(ctx -> {
                    PortalLinkerMod.CONFIG.SaveToFile();
                    var player = ctx.getSource().getPlayer();
                    if (player != null)
                        player.sendSystemMessage(Component.literal("Portal linker settings saved successfully"));
                    return Command.SINGLE_SUCCESS;
                }))
                .then(argument("dimension", DimensionArgument.dimension())
                    .executes(ctx -> {
                        var player = ctx.getSource().getPlayer();
                        var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                        var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                        if (player != null && dimensionInfo != null)
                            player.sendSystemMessage(dimensionInfo.getText());
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(literal("netherPortal")
                        .then(literal("false")
                            .executes(ctx -> {
                                var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                                var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                                if (dimensionInfo != null) {
                                    dimensionInfo.IsNetherPortalEnabled = false;
                                    var player = ctx.getSource().getPlayer();
                                    if (player != null)
                                        player.sendSystemMessage(dimensionInfo.getText());
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                        .then(literal("true")
                            .then(argument("destination", DimensionArgument.dimension())
                                .executes(ctx -> {
                                    var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                                    var destinationDimension = DimensionArgument.getDimension(ctx, "destination");
                                    var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                                    if (dimensionInfo != null) {
                                        dimensionInfo.IsNetherPortalEnabled = true;
                                        dimensionInfo.NetherPortalDestinationDimension = destinationDimension.dimension().identifier().toString();
                                        var player = ctx.getSource().getPlayer();
                                        if (player != null)
                                            player.sendSystemMessage(dimensionInfo.getText());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                    .then(literal("endPortal")
                        .then(literal("false")
                            .executes(ctx -> {
                                var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                                var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                                if (dimensionInfo != null) {
                                    dimensionInfo.IsEndPortalEnabled = false;
                                    var player = ctx.getSource().getPlayer();
                                    if (player != null)
                                        player.sendSystemMessage(dimensionInfo.getText());
                                }
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                        .then(literal("true")
                            .then(argument("destination", DimensionArgument.dimension())
                                .executes(ctx -> {
                                    var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                                    var destinationDimension = DimensionArgument.getDimension(ctx, "destination");
                                    var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                                    if (dimensionInfo != null) {
                                        dimensionInfo.IsEndPortalEnabled = true;
                                        dimensionInfo.EndPortalDestinationDimension = destinationDimension.dimension().identifier().toString();
                                        var player = ctx.getSource().getPlayer();
                                        if (player != null)
                                            player.sendSystemMessage(dimensionInfo.getText());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                    .then(literal("spawn")
                        .then(argument("overrideWorldSpawn", BoolArgumentType.bool())
                            .then(argument("overridePlayerSpawn", BoolArgumentType.bool())
                                .then(argument("spawnDimension", DimensionArgument.dimension())
                                    .then(argument("spawnPosition", BlockPosArgument.blockPos())
                                        .executes(ctx -> {
                                            var selectedDimension = DimensionArgument.getDimension(ctx, "dimension");
                                            var spawnDimension = DimensionArgument.getDimension(ctx, "spawnDimension");
                                            var overrideWorldSpawn = BoolArgumentType.getBool(ctx, "overrideWorldSpawn");
                                            var overridePlayerSpawn = BoolArgumentType.getBool(ctx, "overridePlayerSpawn");
                                            var spawnPosition = BlockPosArgument.getBlockPos(ctx, "spawnPosition");
                                            var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.dimension().identifier().toString());
                                            if (dimensionInfo != null) {
                                                dimensionInfo.OverrideWorldSpawn = overrideWorldSpawn;
                                                dimensionInfo.OverridePlayerSpawn = overridePlayerSpawn;
                                                dimensionInfo.SpawnDimension = spawnDimension.dimension().identifier().toString();
                                                dimensionInfo.SpawnPoint = spawnPosition.toShortString();
                                                var player = ctx.getSource().getPlayer();
                                                if (player != null)
                                                    player.sendSystemMessage(dimensionInfo.getText());
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                    )
                                )
                            )
                        )
                    )
                )
        );
    }
}
