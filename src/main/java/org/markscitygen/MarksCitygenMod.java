package org.markscitygen;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.markscitygen.lib.wfc2d.MapSize;
import org.markscitygen.lib.wfc2d.WFC2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarksCitygenMod implements ModInitializer {
    public static final String MOD_ID = "marks-citygen";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    WFC2D wfc2D = null;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext buildContext,
            Commands.CommandSelection environment) {
        var coordType = IntegerArgumentType.integer();

        dispatcher.register(
                Commands.literal("sge")
                        .then(
                                Commands.literal("wfc")
                                        .then(
                                                Commands.literal("learn")
                                                        .then(
                                                                Commands.argument("x", coordType)
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "y",
                                                                                                coordType)
                                                                                        .then(
                                                                                                Commands
                                                                                                        .argument(
                                                                                                                "z",
                                                                                                                coordType)
                                                                                                        .then(
                                                                                                                Commands
                                                                                                                        .argument(
                                                                                                                                "w",
                                                                                                                                coordType)
                                                                                                                        .then(
                                                                                                                                Commands
                                                                                                                                        .argument(
                                                                                                                                                "h",
                                                                                                                                                coordType)
                                                                                                                                        .executes(
                                                                                                                                                this
                                                                                                                                                        ::wfcLearn)))))))
                                        .then(
                                                Commands.literal("generate")
                                                        .then(
                                                                Commands.argument("x", coordType)
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "y",
                                                                                                coordType)
                                                                                        .then(
                                                                                                Commands
                                                                                                        .argument(
                                                                                                                "z",
                                                                                                                coordType)
                                                                                                        .then(
                                                                                                                Commands
                                                                                                                        .argument(
                                                                                                                                "w",
                                                                                                                                coordType)
                                                                                                                        .then(
                                                                                                                                Commands
                                                                                                                                        .argument(
                                                                                                                                                "h",
                                                                                                                                                coordType)
                                                                                                                                        .executes(
                                                                                                                                                this
                                                                                                                                                        ::wfcGenerate)))))))));
    }

    int wfcLearn(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        int w = IntegerArgumentType.getInteger(context, "w");
        int h = IntegerArgumentType.getInteger(context, "h");

        var level = context.getSource().getLevel();

        int[][] sample = new int[w][h];
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < h; dz++) {
                var pos = new BlockPos(x + dx, y, z + dz);
                var blockId = Block.getId(level.getBlockState(pos));
                LOGGER.info("Learned block at {}: {}", pos, blockId);

                sample[dx][dz] = blockId;
            }
        }

        wfc2D = new WFC2D(sample);

        return Command.SINGLE_SUCCESS;
    }

    int wfcGenerate(CommandContext<CommandSourceStack> context) {
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        int w = IntegerArgumentType.getInteger(context, "w");
        int h = IntegerArgumentType.getInteger(context, "h");

        var level = context.getSource().getLevel();

        int[][] map;

        try {
            map = wfc2D.generate(new MapSize(w, h));
        } catch (Exception e) {
            context.getSource()
                    .sendFailure(Component.literal("WFC generation failed: " + e.getMessage()));

            return 0;
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < h; dz++) {
                var blockId = map[dx][dz];
                var block = Block.stateById(blockId);
                var pos = new BlockPos(x + dx, y, z + dz);

                level.setBlock(pos, block, Block.UPDATE_ALL);
            }
        }

        for (var row : map) LOGGER.info(Arrays.toString(row));

        return Command.SINGLE_SUCCESS;
    }
}
