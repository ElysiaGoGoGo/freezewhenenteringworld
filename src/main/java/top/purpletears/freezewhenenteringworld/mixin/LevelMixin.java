package top.purpletears.freezewhenenteringworld.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

import java.util.function.Consumer;

/**
 * 方块实体与实体的 tick 在服务端/客户端都经过 Level,因此在 Level 层面统一冻结。
 *
 * <p>为何不直接取消 {@code ServerLevel#tick}:那样会连区块加载/实体追踪一起停掉,
 * 多人游戏中玩家移动和视距更新会失效。分点冻结只停「世界内容」,玩家仍然正常 tick。</p>
 */
@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipBlockEntities(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipEntityTick(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        // 玩家豁免:冻结时仍允许玩家移动、交互,与原版 /tick freeze 的行为一致。
        if (FreezeState.isFrozen() && !(entity instanceof Player)) {
            ci.cancel();
        }
    }
}
