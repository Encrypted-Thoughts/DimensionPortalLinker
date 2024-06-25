package net.encryptedthoughts.portallinker.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.encryptedthoughts.portallinker.PortalLinkerMod;
import net.encryptedthoughts.portallinker.util.WorldHelper;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PortalLinkerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess, CommandManager.RegistrationEnvironment ignoredRegistrationEnvironment) {
        dispatcher.register(
            literal("portalLinker")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayer();
                    if (player != null) {
                        for (var i = 0; i < PortalLinkerMod.CONFIG.Dimensions.size(); i++) {
                            var text = PortalLinkerMod.CONFIG.Dimensions.get(i).getText();
                            if (i < PortalLinkerMod.CONFIG.Dimensions.size()-1)
                                text.append("\n");
                            player.sendMessage(text);
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("save").executes(ctx -> {
                    PortalLinkerMod.CONFIG.SaveToFile();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(argument("dimension", DimensionArgumentType.dimension())
                    .executes(ctx -> {
                        var player = ctx.getSource().getPlayer();
                        var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                        var dimension = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                        if (player != null && dimension != null)
                            player.sendMessage(dimension.getText());
                        return Command.SINGLE_SUCCESS;
                    })
                    .then(literal("netherPortal")
                        .then(literal("false")
                            .executes(ctx -> {
                                var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                var dimension = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                                if (dimension != null)
                                    dimension.IsNetherPortalEnabled = false;
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                        .then(literal("true")
                            .then(argument("destination", DimensionArgumentType.dimension())
                                .executes(ctx -> {
                                    var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                    var destinationDimension = DimensionArgumentType.getDimensionArgument(ctx, "destination");
                                    var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                                    if (dimensionInfo != null) {
                                        dimensionInfo.IsNetherPortalEnabled = true;
                                        dimensionInfo.NetherPortalDestinationDimension = destinationDimension.getRegistryKey().getValue().toString();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                    .then(literal("endPortal")
                        .then(literal("false")
                            .executes(ctx -> {
                                var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                var dimension = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                                if (dimension != null)
                                    dimension.IsEndPortalEnabled = false;
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                        .then(literal("true")
                            .then(argument("destination", DimensionArgumentType.dimension())
                                .executes(ctx -> {
                                    var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                    var destinationDimension = DimensionArgumentType.getDimensionArgument(ctx, "destination");
                                    var dimensionInfo = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                                    if (dimensionInfo != null) {
                                        dimensionInfo.IsEndPortalEnabled = true;
                                        dimensionInfo.EndPortalDestinationDimension = destinationDimension.getRegistryKey().getValue().toString();
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                    .then(literal("spawn")
                        .then(argument("overrideWorldSpawn", BoolArgumentType.bool())
                            .then(argument("overridePlayerSpawn", BoolArgumentType.bool())
                                .then(argument("spawnDimension", DimensionArgumentType.dimension())
                                    .then(argument("spawnPosition", BlockPosArgumentType.blockPos())
                                        .executes(ctx -> {
                                            var selectedDimension = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                            var spawnDimension = DimensionArgumentType.getDimensionArgument(ctx, "spawnDimension");
                                            var overrideWorldSpawn = BoolArgumentType.getBool(ctx, "overrideWorldSpawn");
                                            var overridePlayerSpawn = BoolArgumentType.getBool(ctx, "overridePlayerSpawn");
                                            var spawnPosition = BlockPosArgumentType.getBlockPos(ctx, "spawnPosition");
                                            var dimension = WorldHelper.getDimensionInfo(selectedDimension.getRegistryKey().getValue().toString());
                                            if (dimension != null) {
                                                dimension.OverrideWorldSpawn = overrideWorldSpawn;
                                                dimension.OverridePlayerSpawn = overridePlayerSpawn;
                                                dimension.SpawnDimension = spawnDimension.getRegistryKey().getValue().toString();
                                                dimension.SpawnPoint = spawnPosition.toShortString();
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
