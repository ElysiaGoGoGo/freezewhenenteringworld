package top.purpletears.freezewhenenteringworld.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

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
