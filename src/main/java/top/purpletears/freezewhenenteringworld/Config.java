package top.purpletears.freezewhenenteringworld;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 模组配置。
 * <p>
 * 对于 Forge 1.20.1,common 配置位于 config/freezewhenenteringworld-common.toml。
 */
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue FREEZE_ON_WORLD_ENTER = BUILDER
            .comment("进入世界后是否立刻冻结世界。",
                    "true: 服务器/单人世界启动后,在第一个 tick 之前冻结(通过 Mixin 实现)。",
                    "默认 false（关闭）。")
            .define("freezeOnWorldEnter", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
