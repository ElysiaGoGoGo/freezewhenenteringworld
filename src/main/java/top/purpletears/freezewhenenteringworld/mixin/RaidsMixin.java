package top.purpletears.freezewhenenteringworld.mixin;

import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;

/**
 * 冻结袭击进度(袭击计时器),否则冻结期间袭击仍会推进。
 */
@Mixin(Raids.class)
public abstract class RaidsMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void freezewhenenteringworld$skipRaids(CallbackInfo ci) {
        if (FreezeState.isFrozen()) {
            ci.cancel();
        }
    }
}
