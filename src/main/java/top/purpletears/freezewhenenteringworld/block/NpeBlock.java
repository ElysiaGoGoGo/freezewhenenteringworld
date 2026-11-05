package top.purpletears.freezewhenenteringworld.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import top.purpletears.freezewhenenteringworld.FreezeWhenEnteringWorld;
import top.purpletears.freezewhenenteringworld.blockentity.NpeBlockEntity;

/**
 * 测试方块:拥有方块实体,方块实体每 tick 抛 NPE。
 */
public class NpeBlock extends BaseEntityBlock {
    public NpeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * BaseEntityBlock 默认渲染形状是 INVISIBLE,这里改回普通模型,否则方块不可见。
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NpeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, FreezeWhenEnteringWorld.NPE_BLOCK_ENTITY.get(), NpeBlockEntity::tick);
    }
}
