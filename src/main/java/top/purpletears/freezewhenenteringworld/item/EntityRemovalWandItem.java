package top.purpletears.freezewhenenteringworld.item;

import java.util.UUID;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 生物实体移除杖。
 * <p>
 * 普通右键生物实体：按“kill -> discard -> remove(KILLED) -> remove(DISCARDED)”的顺序全部尝试一遍，
 * 正常情况下实体在 discard 阶段就会被移除。若仍然没有被移除，并且这根杖的危险清除模式已经开启，
 * 则尝试把该实体“替换”成一只新的猪（猪咪 / pigee）。
 * </p>
 * <p>
 * Shift + 右键空气或方块：切换这根杖的危险清除模式（默认关闭，状态保存在物品的自定义数据组件上，每根杖独立）。
 * 强制替换会让旧实体对象从世界引用中消失，其他仍持有旧引用的代码可能访问到已失效对象，
 * 因此只有危险模式才允许执行。
 * </p>
 */
public class EntityRemovalWandItem extends Item {
    /** 危险清除模式在物品自定义数据中的键；每根杖独立保存，默认没有该标签 = 关闭。 */
    private static final String DANGEROUS_MODE_TAG = "dangerous_mode";

    public EntityRemovalWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        // 客户端只负责表现，真正的删除逻辑在服务端执行
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (target instanceof Player) {
            player.displayClientMessage(Component.translatable("message.freezewhenenteringworld.entity_removal_wand.player"), true);
            return InteractionResult.SUCCESS;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        RemovalResult result = removeEntity(serverLevel, target, isDangerousMode(stack));
        player.displayClientMessage(messageFor(result, target), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                toggleDangerousMode(stack, player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                toggleDangerousMode(context.getItemInHand(), player);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static boolean isDangerousMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean(DANGEROUS_MODE_TAG);
    }

    private static void toggleDangerousMode(ItemStack stack, Player player) {
        boolean enabled = !isDangerousMode(stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(DANGEROUS_MODE_TAG, enabled);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        player.displayClientMessage(Component.translatable(enabled
                ? "message.freezewhenenteringworld.entity_removal_wand.dangerous_on"
                : "message.freezewhenenteringworld.entity_removal_wand.dangerous_off"), true);
    }

    private static Component messageFor(RemovalResult result, LivingEntity target) {
        Component name = target.getDisplayName();
        return switch (result) {
            case REMOVED -> Component.translatable("message.freezewhenenteringworld.entity_removal_wand.removed", name);
            case REPLACED -> Component.translatable("message.freezewhenenteringworld.entity_removal_wand.replaced", name);
            case FAILED -> Component.translatable("message.freezewhenenteringworld.entity_removal_wand.failed", name);
        };
    }

    private static RemovalResult removeEntity(ServerLevel level, LivingEntity target, boolean dangerous) {
        // 用户要求：让实体死亡、discard、remove 全部来一遍（后两步在已移除时是安全的空操作）
        target.kill();
        target.discard();
        target.remove(Entity.RemovalReason.KILLED);
        target.remove(Entity.RemovalReason.DISCARDED);

        if (target.isRemoved()) {
            return RemovalResult.REMOVED;
        }

        // 常规手段都失败：只有危险模式才允许强行把世界里的引用换成一只新猪
        if (!dangerous) {
            return RemovalResult.FAILED;
        }
        return replaceWithPig(level, target) ? RemovalResult.REPLACED : RemovalResult.FAILED;
    }

    /**
     * 危险操作：把仍然存在的实体从世界引用中替换成一只新的猪。
     * 先让区块追踪器向客户端发送移除包，再把旧实体的 UUID 转交给新猪（若 UUID 冲突则用新的 UUID），
     * 最后把猪加入世界。旧对象如果仍被其他代码引用，本方法无法处理，这正是危险模式的意义。
     */
    private static boolean replaceWithPig(ServerLevel level, LivingEntity target) {
        // 先移除客户端追踪，避免客户端继续保留旧实体
        level.getChunkSource().removeEntity(target);
        // 再尽力把旧实体标记为已移除
        target.remove(Entity.RemovalReason.KILLED);

        Pig pig = EntityType.PIG.create(level);
        if (pig == null) {
            return false;
        }

        pig.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        pig.setYHeadRot(target.getYHeadRot());
        pig.setCustomName(Component.translatable("entity.freezewhenenteringworld.pigee"));
        pig.setCustomNameVisible(true);
        pig.setPersistenceRequired();

        // 尽量让旧 UUID 指向新的猪，做到“引用替换”；若旧实体仍占着这个 UUID，则退回随机 UUID
        pig.setUUID(target.getUUID());
        if (level.addFreshEntity(pig)) {
            return true;
        }

        pig.setUUID(UUID.randomUUID());
        return level.addFreshEntity(pig);
    }

    private enum RemovalResult {
        REMOVED,
        REPLACED,
        FAILED
    }
}
