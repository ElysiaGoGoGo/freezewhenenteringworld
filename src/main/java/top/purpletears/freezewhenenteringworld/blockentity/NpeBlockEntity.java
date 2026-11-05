package top.purpletears.freezewhenenteringworld.blockentity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;

/**
 * 每 tick 抛 NPE 的方块实体。
 * <p>
 * 默认只在服务端抛(THROW_ON_CLIENT = false),避免单人游戏时客户端先崩而看不到服务端堆栈。
 */
public class NpeBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 是否在客户端也抛 NPE。默认 false,只在服务端抛。 */
    public static final boolean THROW_ON_CLIENT = false;

    private int tickCount;

    public NpeBlockEntity(BlockPos pos, BlockState state) {
        super(FreezeWhenEnteringWorld.NPE_BLOCK_ENTITY.get(), pos, state);
    }

    public int getTickCount() {
        return tickCount;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NpeBlockEntity blockEntity) {
        blockEntity.tickCount++;

        String side = level.isClientSide ? "CLIENT" : "SERVER";

        if (level.isClientSide && !THROW_ON_CLIENT) {
            LOGGER.info("NpeBlockEntity.tick() 在客户端被调用(不抛异常): pos={}, gameTime={}, tickCount={}",
                    pos, level.getGameTime(), blockEntity.tickCount);
            return;
        }

        LOGGER.error("NpeBlockEntity.tick() 被调用：世界正在 tick。pos={}, side={}, gameTime={}, tickCount={}",
                pos, side, level.getGameTime(), blockEntity.tickCount);

        throw new NullPointerException("NpeBlockEntity.tick() 被调用：世界正在 tick。pos=" + pos
                + ", side=" + side
                + ", gameTime=" + level.getGameTime()
                + ", tickCount=" + blockEntity.tickCount);
    }
}
