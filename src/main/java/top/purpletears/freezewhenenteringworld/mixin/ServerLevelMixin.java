package top.purpletears.freezewhenenteringworld.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

/**
 * 服务端世界内容冻结:时间、随机刻、天气、方块事件、袭击。
 * 保留 {@code ServerLevel#tick} 中的区块源 tick 与 entityManager tick,
 * 以便多人游戏中玩家仍能移动、实体仍能被追踪。
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipTime(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipChunkTicks(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipWeather(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "runBlockEvents", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipBlockEvents(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }
}
