package top.purpletears.freezewhenenteringworld.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;
import top.purpletears.freezewhenenteringworld.network.FreezeNetwork;

/**
 * /freeze on|off|status,权限等级 4(OP)。
 *
 * <p>只修改本次会话的 {@link FreezeState},不会写入 TOML 配置;
 * 下次进入世界仍按 {@code freezeOnWorldEnter} 配置决定初始状态。</p>
 */
public final class FreezeCommand {
    private FreezeCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("freeze")
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("on")
                        .executes(context -> setFrozen(context, true)))
                .then(Commands.literal("off")
                        .executes(context -> setFrozen(context, false)))
                .then(Commands.literal("status")
                        .executes(FreezeCommand::status)));
    }

    private static int setFrozen(CommandContext<CommandSourceStack> context, boolean frozen) {
        FreezeState.setFrozen(frozen);
        // 专用服务器必须通知所有客户端,否则服务端冻结/解冻后客户端世界仍按旧状态 tick。
        FreezeNetwork.broadcast(frozen);

        String key = frozen
                ? "commands.freezewhenenteringworld.freeze.on"
                : "commands.freezewhenenteringworld.freeze.off";
        context.getSource().sendSuccess(() -> Component.translatable(key), true);
        FreezeWhenEnteringWorld.LOGGER.info("执行 /freeze {}: 本次会话冻结状态 = {}",
                frozen ? "on" : "off", frozen);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        boolean frozen = FreezeState.isFrozen();
        String key = frozen
                ? "commands.freezewhenenteringworld.freeze.status.frozen"
                : "commands.freezewhenenteringworld.freeze.status.running";
        context.getSource().sendSuccess(() -> Component.translatable(key), false);
        return frozen ? 1 : 0;
    }
}
