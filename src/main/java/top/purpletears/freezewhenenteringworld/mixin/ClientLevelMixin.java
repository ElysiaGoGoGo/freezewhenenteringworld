package top.purpletears.freezewhenenteringworld.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

/**
 * 客户端世界时间冻结。客户端实体/方块实体由 {@link LevelMixin} 与
 * {@link ClientLevel#tickEntities()} 内部共用的 guardEntityTick 统一处理。
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipTime(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }
}
