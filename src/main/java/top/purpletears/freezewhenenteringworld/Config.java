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
     * TOML 配置里的持久开关（common，默认 {@code false}）。
     * <p>
     * true 时：服务器（单人/集成服务器/专用服务器）启动完成、第一个 tick 之前，直接
     * {@code server.tickRateManager().setFrozen(true)}，效果等价于进入世界时立刻执行 {@code /tick freeze}。
     * </p>
     * <p>
     * 纯服务端（专服）没有开始界面按钮，就靠改这个配置文件控制；单人时也可以手动改。
     * 开始界面按钮控制的是另一个「本次游戏会话」临时开关
     * （{@link FreezeWhenEnteringWorld#sessionFreezeOnWorldEnter}），两者是「或」关系。
     * </p>
     */
    public static final ModConfigSpec.BooleanValue FREEZE_ON_WORLD_ENTER = BUILDER
            .comment("If true, freeze the world (like /tick freeze) as soon as it is loaded, before the first tick.",
                    "This is the persistent/server-side switch: a dedicated server can enable it here, no client needed.",
                    "The title-screen button is a separate per-session switch; either one enables the freeze.")
            .define("freezeOnWorldEnter", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
