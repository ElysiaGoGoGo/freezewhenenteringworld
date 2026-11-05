package top.purpletears.freezewhenenteringworld.client;

import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;

/**
 * 为 NPE 测试生物注册一个空渲染器：实体本身不可见，但客户端不会因为
 * “No renderer registered for ...” 而崩溃，方便在服务端观察 NPE 堆栈。
 */
@EventBusSubscriber(modid = FreezeWhenEnteringWorld.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NpeLivingEntityRenderers {
    private NpeLivingEntityRenderers() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FreezeWhenEnteringWorld.NPE_LIVING.get(), NoopRenderer::new);
    }
}
