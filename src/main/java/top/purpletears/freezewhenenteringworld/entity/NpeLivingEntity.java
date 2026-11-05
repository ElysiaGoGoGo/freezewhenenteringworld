package top.purpletears.freezewhenenteringworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * NPE 测试生物实体：每一次 tick 都直接抛出 {@link NullPointerException}。
 * <p>
 * 与 NPE 方块实体一样，它只用于观测“世界是否已经在 tick”：
 * 用 {@code /summon freezewhenenteringworld:npe_living} 生成后，只要它被 tick 到就会崩服/崩端。
 * 这里刻意不调用 {@code super.tick()}，确保 NPE 发生在实体 tick 的第一时间。
 */
public class NpeLivingEntity extends Mob {
    public NpeLivingEntity(EntityType<? extends NpeLivingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    public void tick() {
        throw new NullPointerException("NpeLivingEntity.tick() 被调用：实体正在 tick。pos="
                + this.position()
                + ", side=" + (this.level().isClientSide ? "client" : "server")
                + ", gameTime=" + this.level().getGameTime());
    }
}
