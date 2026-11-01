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
     * 按钮<b>直接切换</b> TOML 配置 {@link Config#FREEZE_ON_WORLD_ENTER}：点击时读取当前值、取反、
     * 写入并保存（{@code ConfigValue#set + save}），所以会持久化、记住上次状态。
     * 默认关闭；开启后进入任何存档都会在进入世界时（第一个 tick 之前）冻结。
     * </p>
     * <p>
     * 纯服务端（专服）没有标题界面，直接改同一个 TOML 配置即可控制。
     * </p>
     */
    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }

        Button toggleButton = Button.builder(toggleMessage(), button -> {
            boolean enabled = !isFreezeEnabled();
            setFreezeEnabled(enabled);
            button.setMessage(toggleMessage());
            FreezeWhenEnteringWorld.LOGGER.info("[FreezeWhenEnteringWorld] 开始界面开关切换为: {}", enabled ? "开" : "关");
        }).bounds(TOGGLE_X, TOGGLE_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("freezewhenenteringworld.title.freeze_toggle.tooltip")))
                .build();

        event.addListener(toggleButton);
    }

    /** 生成按钮文本：开启为绿色 "进入世界冻结: 开"，关闭为灰色 "进入世界冻结: 关"。 */
    private static Component toggleMessage() {
        boolean enabled = isFreezeEnabled();
        return Component.translatable(
                        enabled ? "freezewhenenteringworld.title.freeze_toggle.on"
                                : "freezewhenenteringworld.title.freeze_toggle.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    /**
     * 读取 TOML 配置开关。common 配置在标题界面之前已经加载，这里做防御性处理，
     * 避免配置尚未加载时打开标题界面导致崩溃。
     */
    private static boolean isFreezeEnabled() {
        try {
            return Config.FREEZE_ON_WORLD_ENTER.getAsBoolean();
        } catch (Exception exception) {
            FreezeWhenEnteringWorld.LOGGER.error("读取 TOML 配置 freezeOnWorldEnter 失败，按关闭处理", exception);
            return false;
        }
    }

    /** 写入并保存 TOML 配置，实现「记住上次状态」的持久化。 */
    private static void setFreezeEnabled(boolean enabled) {
        try {
            Config.FREEZE_ON_WORLD_ENTER.set(enabled);
            Config.FREEZE_ON_WORLD_ENTER.save();
        } catch (Exception exception) {
            FreezeWhenEnteringWorld.LOGGER.error("保存 TOML 配置 freezeOnWorldEnter 失败", exception);
        }
    }
}
