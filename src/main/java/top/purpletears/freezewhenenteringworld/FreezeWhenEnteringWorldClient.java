package top.purpletears.freezewhenenteringworld;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = FreezeWhenEnteringWorld.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = FreezeWhenEnteringWorld.MODID, value = Dist.CLIENT)
public class FreezeWhenEnteringWorldClient {
    /** 开始界面开关按钮的占位尺寸；放在左上角，避免和原版按钮重叠。 */
    private static final int TOGGLE_WIDTH = 170;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int TOGGLE_X = 6;
    private static final int TOGGLE_Y = 6;

    public FreezeWhenEnteringWorldClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking your mod > clicking config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * 在标题界面（开始界面）插入一个开关按钮。
     * <p>
     * 这个按钮控制的是**本次游戏会话**的临时开关 {@link FreezeWhenEnteringWorld#sessionFreezeOnWorldEnter}：
     * 默认关闭，不写盘，重启游戏后回到关闭；开启后进入任何存档都会在进入世界时立刻冻结。
     * </p>
     * <p>
     * 持久/服务端开关是 TOML 配置里的 {@link Config#FREEZE_ON_WORLD_ENTER}（默认关闭，专服用它控制）。
     * 当配置已经开启时，按钮会显示「开（配置文件）」并禁用，避免两个开关互相打架。
     * </p>
     */
    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }

        boolean configForced = isConfigForced();
        Button toggleButton = Button.builder(configForced ? configForcedMessage() : sessionMessage(), button -> {
            // 由 TOML 配置开启时按钮已禁用，这里再兜底一次
            if (isConfigForced()) {
                return;
            }
            FreezeWhenEnteringWorld.sessionFreezeOnWorldEnter = !FreezeWhenEnteringWorld.sessionFreezeOnWorldEnter;
            button.setMessage(sessionMessage());
            FreezeWhenEnteringWorld.LOGGER.info("[FreezeWhenEnteringWorld] 开始界面本次会话开关切换为: {}",
                    FreezeWhenEnteringWorld.sessionFreezeOnWorldEnter ? "开" : "关");
        }).bounds(TOGGLE_X, TOGGLE_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable(configForced
                        ? "freezewhenenteringworld.title.freeze_toggle.tooltip.config"
                        : "freezewhenenteringworld.title.freeze_toggle.tooltip")))
                .build();
        toggleButton.active = !configForced;

        event.addListener(toggleButton);
    }

    /** 会话开关按钮文本：开启为绿色 "进入世界冻结: 开"，关闭为灰色 "进入世界冻结: 关"。 */
    private static Component sessionMessage() {
        boolean enabled = FreezeWhenEnteringWorld.sessionFreezeOnWorldEnter;
        return Component.translatable(
                        enabled ? "freezewhenenteringworld.title.freeze_toggle.on"
                                : "freezewhenenteringworld.title.freeze_toggle.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    /** 被 TOML 配置强制开启时的按钮文本（按钮同时会被禁用）。 */
    private static Component configForcedMessage() {
        return Component.translatable("freezewhenenteringworld.title.freeze_toggle.config_on")
                .withStyle(ChatFormatting.GREEN);
    }

    /**
     * 读取 TOML 配置开关。common 配置在标题界面之前已经加载，这里做防御性处理，
     * 避免配置尚未加载时打开标题界面导致崩溃。
     */
    private static boolean isConfigForced() {
        try {
            return Config.FREEZE_ON_WORLD_ENTER.getAsBoolean();
        } catch (Exception exception) {
            FreezeWhenEnteringWorld.LOGGER.error("读取 TOML 配置 freezeOnWorldEnter 失败，按关闭处理", exception);
            return false;
        }
    }
}
