package top.purpletears.freezewhenenteringworld;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

/**
 * 客户端逻辑:在标题界面加一个开关按钮(切换并保存持久化 TOML 配置)。
 * <p>
 * 只注册到客户端的事件总线;专用服务器不会加载本类。
 */
@Mod.EventBusSubscriber(modid = FreezeWhenEnteringWorld.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FreezeWhenEnteringWorldClient {
    private static final int TOGGLE_WIDTH = 170;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int TOGGLE_X = 6;
    private static final int TOGGLE_Y = 6;

    private FreezeWhenEnteringWorldClient() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }

        Button toggleButton = Button.builder(toggleMessage(), button -> {
                    boolean enabled = !isFreezeEnabled();
                    setFreezeEnabled(enabled);
                    button.setMessage(toggleMessage());
                    FreezeWhenEnteringWorld.LOGGER.info("进入世界冻结开关切换为: {}", enabled ? "开" : "关");
                })
                .bounds(TOGGLE_X, TOGGLE_Y, TOGGLE_WIDTH, TOGGLE_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("freezewhenenteringworld.title.freeze_toggle.tooltip")))
                .build();

        event.addListener(toggleButton);
    }

    /**
     * 离开世界后重置冻结状态,避免下次进入非冻结世界时客户端仍停在冻结状态。
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        FreezeState.setFrozen(false);
    }

    private static Component toggleMessage() {
        boolean enabled = isFreezeEnabled();
        return Component.translatable(enabled
                        ? "freezewhenenteringworld.title.freeze_toggle.on"
                        : "freezewhenenteringworld.title.freeze_toggle.off")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    private static boolean isFreezeEnabled() {
        try {
            return Boolean.TRUE.equals(Config.FREEZE_ON_WORLD_ENTER.get());
        } catch (RuntimeException exception) {
            FreezeWhenEnteringWorld.LOGGER.warn("读取 freezeOnWorldEnter 配置失败,按关闭处理", exception);
            return false;
        }
    }

    private static void setFreezeEnabled(boolean enabled) {
        try {
            Config.FREEZE_ON_WORLD_ENTER.set(enabled);
            Config.FREEZE_ON_WORLD_ENTER.save();
        } catch (RuntimeException exception) {
            FreezeWhenEnteringWorld.LOGGER.error("保存 freezeOnWorldEnter 配置失败", exception);
        }
    }
}
