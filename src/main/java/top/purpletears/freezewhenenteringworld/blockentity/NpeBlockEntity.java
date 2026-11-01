package top.purpletears.freezewhenenteringworld.blockentity;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;

/**
 * 每 tick 都抛出一个 {@link NullPointerException} 的方块实体。
 *
 * <p>它被 {@code NpeBlock} 注册成 ticker：世界一旦开始 tick（并且这个方块所在区块已经加载、方块实体已经加入
 * tick 列表），{@link #tick(Level, BlockPos, BlockState, NpeBlockEntity)} 就会在服务端线程被调用，
 * 随后立即抛出 NPE。用途：</p>
 * <ul>
 *     <li>如果 NPE 出现 —— 世界确实在 tick；</li>
 *     <li>如果世界被冻结（例如 {@code /tick freeze}）—— 方块实体 tick 被跳过，NPE 不会出现。</li>
 * </ul>
 *
 * <p>默认只在服务端抛 NPE（客户端仅打印日志），因为抛在客户端会先崩掉客户端，不利于观察服务端时序。
 * 想让两端都抛，把 {@link #THROW_ON_CLIENT} 改成 {@code true} 即可。</p>
 */
public class NpeBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 是否在客户端也抛 NPE。
     * <ul>
     *     <li>{@code false}（默认）：客户端每 tick 只打一条日志，NPE 只由服务端线程抛出，便于观察/定位；</li>
     *     <li>{@code true}：两端都抛（严格满足“每 tick 抛 NPE”的字面要求，但客户端会先崩溃）。</li>
     * </ul>
     */
    public static final boolean THROW_ON_CLIENT = false;

    /** 本方块实体自加载以来被 tick 的次数；抛异常时会写进 NPE 消息，便于确认确实在逐 tick 执行。 */
    private int tickCount;

    public NpeBlockEntity(BlockPos pos, BlockState state) {
        super(FreezeWhenEnteringWorld.NPE_BLOCK_ENTITY.get(), pos, state);
    }

    public int getTickCount() {
        return tickCount;
    }

    /**
     * 方块实体 ticker 本体（由 {@code NpeBlock#getTicker} 注册）。
     *
     * <p>注意：这个方法在服务端和客户端都会被调用（客户端区块也会 tick 方块实体），
     * 因此用 {@link Level#isClientSide} 区分。</p>
     */
    public static void tick(Level level, BlockPos pos, BlockState state, NpeBlockEntity blockEntity) {
        blockEntity.tickCount++;

        if (level.isClientSide && !THROW_ON_CLIENT) {
            LOGGER.info("[NpeBlockEntity] 客户端 tick #{}, pos={}, gameTime={}（客户端 tick 仍在运行；默认不在客户端抛 NPE）",
                    blockEntity.tickCount, pos, level.getGameTime());
            return;
        }

        String side = level.isClientSide ? "CLIENT" : "SERVER";
        LOGGER.error("[NpeBlockEntity] {} 线程正在 tick 世界：pos={}, gameTime={}, 第 {} 次 tick —— 现在抛出 NPE",
                side, pos, level.getGameTime(), blockEntity.tickCount);
        throw new NullPointerException(
                "NpeBlockEntity.tick() 被调用：世界正在 tick。pos=" + pos
                        + ", side=" + side
                        + ", gameTime=" + level.getGameTime()
                        + ", tickCount=" + blockEntity.tickCount);
    }
}
