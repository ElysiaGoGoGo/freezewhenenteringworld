package top.purpletears.freezewhenenteringworld.freeze;

/**
 * 全局冻结状态。
 *
 * <p>服务端在 {@code ServerStartedEvent} 中按配置写入;客户端收到 {@link top.purpletears.freezewhenenteringworld.network.FreezeStatePacket}
 * 后写入。单人游戏时服务端与客户端在同一个 JVM 中,因此共享同一个静态字段。</p>
 */
public final class FreezeState {
    private static volatile boolean frozen;

    private FreezeState() {
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static void setFrozen(boolean value) {
        frozen = value;
    }
}
