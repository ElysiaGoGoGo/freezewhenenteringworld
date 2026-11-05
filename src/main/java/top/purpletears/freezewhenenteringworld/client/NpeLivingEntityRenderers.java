package top.purpletears.freezewhenenteringworld.client;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;

/**
 * 为 NPE 测试生物注册一个空渲染器：实体本身不可见，但客户端不会因为
 * “No renderer registered for ...” 而崩溃，方便在服务端观察 NPE 堆栈。
 */
@Mod.EventBusSubscriber(modid = FreezeWhenEnteringWorld.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NpeLivingEntityRenderers {
    private NpeLivingEntityRenderers() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FreezeWhenEnteringWorld.NPE_LIVING.get(), NoopRenderer::new);
    }
}
