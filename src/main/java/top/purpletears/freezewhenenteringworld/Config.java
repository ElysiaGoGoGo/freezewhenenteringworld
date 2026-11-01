package top.purpletears.freezewhenenteringworld;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 本模组的配置。
 *
 * <p>只有一个开关 {@link #FREEZE_ON_WORLD_ENTER}：进入世界后是否立刻冻结。</p>
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * TOML 配置里的开关（common，默认 {@code false}），是模组<b>唯一</b>的「进入世界后是否立刻冻结」开关。
     * <p>
     * true 时：服务器（单人/集成服务器/专用服务器）启动完成、第一个 tick 之前，直接
     * {@code server.tickRateManager().setFrozen(true)}，效果等价于进入世界时立刻执行 {@code /tick freeze}。
     * </p>
     * <p>
     * 默认关闭。开始界面按钮切换的就是这个值（写入并保存 TOML，因此会记住上次状态）；
     * 纯服务端（专服）没有开始界面，直接改配置文件即可控制。两处操作的是同一个开关。
     * </p>
     */
    public static final ModConfigSpec.BooleanValue FREEZE_ON_WORLD_ENTER = BUILDER
            .comment("If true, freeze the world (like /tick freeze) as soon as it is loaded, before the first tick.",
                    "Default: false. The title-screen button toggles and saves this value, so the last state is remembered.",
                    "Dedicated servers can enable it here without any client.")
            .define("freezeOnWorldEnter", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
