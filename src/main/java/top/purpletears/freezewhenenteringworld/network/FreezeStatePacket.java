package top.purpletears.freezewhenenteringworld.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端:同步当前世界是否处于冻结状态。
 */
public record FreezeStatePacket(boolean frozen) {
    public static void encode(FreezeStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.frozen());
    }

    public static FreezeStatePacket decode(FriendlyByteBuf buffer) {
        return new FreezeStatePacket(buffer.readBoolean());
    }

    public static void handle(FreezeStatePacket packet, Supplier<NetworkEvent.Context> context) {
        FreezeState.setFrozen(packet.frozen());
    }
}
