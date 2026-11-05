package top.purpletears.freezewhenenteringworld.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;

/**
 * 冻结状态的网络通道。专用服务器上客户端必须知道服务端是否冻结,否则会出现
 * 客户端世界继续 ticking 的视觉/预测不同步。
 */
public final class FreezeNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(FreezeWhenEnteringWorld.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private FreezeNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(FreezeStatePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FreezeStatePacket::encode)
                .decoder(FreezeStatePacket::decode)
                .consumerMainThread(FreezeStatePacket::handle)
                .add();
    }

    public static void sendTo(ServerPlayer player, boolean frozen) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FreezeStatePacket(frozen));
    }

    /**
     * 向所有在线玩家广播冻结状态,用于 /freeze 指令在运行时切换。
     */
    public static void broadcast(boolean frozen) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), new FreezeStatePacket(frozen));
    }
}
