package top.purpletears.freezewhenenteringworld.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;
import top.purpletears.freezewhenenteringworld.blockentity.NpeBlockEntity;

/**
 * 一个用于观测“世界是否已经开始 tick”的测试方块。
 *
 * <p>它本身只做三件事：</p>
 * <ol>
 *     <li>拥有一个 {@link NpeBlockEntity}（方块实体）；</li>
 *     <li>把方块实体的 {@code tick} 注册成每 tick 都会被调用的 ticker；</li>
 *     <li>使用普通方块模型渲染（{@link RenderShape#MODEL}），而不是 {@link BaseEntityBlock} 默认的
 *     {@link RenderShape#INVISIBLE}。</li>
 * </ol>
 *
 * <p>因此只要这个方块被放在世界里，下一个世界 tick 就会执行
 * {@link NpeBlockEntity#tick(Level, BlockPos, BlockState, NpeBlockEntity)}，
 * 从而在服务端线程抛出 {@link NullPointerException}——这正好可以作为“世界已经开始 tick”的信号。</p>
 */
public class NpeBlock extends BaseEntityBlock {
    /** 方块 State 的 Codec（1.21.1 起 Block 需要提供 codec）。 */
    public static final MapCodec<NpeBlock> CODEC = simpleCodec(NpeBlock::new);

    public NpeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /**
     * {@link BaseEntityBlock} 默认返回 {@link RenderShape#INVISIBLE}（因为很多实体方块用 BlockEntityRenderer 渲染）。
     * 这里要显示普通方块模型，必须显式改回 {@link RenderShape#MODEL}，否则方块会是隐形的。
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NpeBlockEntity(pos, state);
    }

    /**
     * 返回方块实体的 ticker。{@code LevelChunk} 在方块实体被加载/放置时会调用
     * {@code BlockState#getTicker} 并把结果缓存起来，之后每 tick 调用一次。
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, FreezeWhenEnteringWorld.NPE_BLOCK_ENTITY.get(), NpeBlockEntity::tick);
    }
}
