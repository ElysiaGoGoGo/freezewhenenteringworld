package top.purpletears.freezewhenenteringworld.mixin;

import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

/**
 * 冻结计划刻(block ticks)与流体刻(fluid ticks),避免冻结期间方块更新仍在发生。
 */
@Mixin(LevelTicks.class)
public abstract class LevelTicksMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipScheduledTicks(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }
}
