package top.purpletears.freezewhenenteringworld;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import top.purpletears.freezewhenenteringworld.block.NpeBlock;
import top.purpletears.freezewhenenteringworld.blockentity.NpeBlockEntity;
import top.purpletears.freezewhenenteringworld.command.FreezeCommand;
import top.purpletears.freezewhenenteringworld.entity.NpeLivingEntity;
import top.purpletears.freezewhenenteringworld.freeze.FreezeState;
import top.purpletears.freezewhenenteringworld.item.EntityRemovalWandItem;
import top.purpletears.freezewhenenteringworld.network.FreezeNetwork;

@Mod(FreezeWhenEnteringWorld.MODID)
public class FreezeWhenEnteringWorld {
    public static final String MODID = "freezewhenenteringworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<NpeBlock> NPE_BLOCK = BLOCKS.register("npe_block",
            () -> new NpeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<BlockItem> NPE_BLOCK_ITEM = ITEMS.register("npe_block",
            () -> new BlockItem(NPE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<NpeBlockEntity>> NPE_BLOCK_ENTITY = BLOCK_ENTITIES.register("npe_block",
            () -> BlockEntityType.Builder.of(NpeBlockEntity::new, NPE_BLOCK.get()).build(null));

    // 生物实体移除杖：右键生物强制移除，Shift+右键空气/方块切换危险清除模式
    public static final RegistryObject<EntityRemovalWandItem> ENTITY_REMOVAL_WAND = ITEMS.register("entity_removal_wand",
            () -> new EntityRemovalWandItem(new Item.Properties()));

    // NPE 测试生物：只要被 tick 到就抛 NullPointerException
    public static final RegistryObject<EntityType<NpeLivingEntity>> NPE_LIVING = ENTITY_TYPES.register("npe_living",
            () -> EntityType.Builder.of(NpeLivingEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build(MODID + ":npe_living"));

    public FreezeWhenEnteringWorld() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::onEntityAttributeCreation);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        FreezeNetwork.register();
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NPE_LIVING.get(), NpeLivingEntity.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(NPE_BLOCK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ENTITY_REMOVAL_WAND);
        }
    }

    /**
     * 注册 /freeze on|off|status(权限等级 4,仅影响本次会话)。
     */
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        FreezeCommand.register(event.getDispatcher());
    }

    /**
     * 在服务器世界加载完成之后、第一个 tick 之前触发。
     * <p>
     * 1.20.1 没有 TickRateManager,这里把状态写入 {@link FreezeState},
     * 由 Mixin 在 tick 管线的各个入口处取消世界内容的 tick。
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        boolean freeze = Config.FREEZE_ON_WORLD_ENTER.get();
        FreezeState.setFrozen(freeze);

        if (freeze) {
            LOGGER.info("进入世界冻结开关: 开 -> 世界已加载,在第一个 tick 之前冻结(Mixin 实现)");
        } else {
            LOGGER.info("进入世界冻结开关: 关 -> 世界正常 tick");
        }
    }

    /**
     * 玩家(含单人客户端)进入服务器时同步冻结状态,避免专用服务器上客户端继续 tick 世界。
     */
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FreezeNetwork.sendTo(serverPlayer, FreezeState.isFrozen());
        }
    }

    /**
     * 末影龙等多部件生物的本体 {@code isPickable() == false}，客户端右键时命中的是
     * {@link PartEntity}（如 EnderDragonPart）。PartEntity 不是 LivingEntity，
     * 所以物品的 {@code interactLivingEntity} 永远不会被调用；这里把交互转交给父实体。
     */
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof PartEntity<?> part)) {
            return;
        }
        if (!(event.getItemStack().getItem() instanceof EntityRemovalWandItem wand)) {
            return;
        }
        if (!(part.getParent() instanceof LivingEntity parent)) {
            return;
        }

        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            wand.interactLivingEntity(event.getItemStack(), player, parent, event.getHand());
        }

        // 阻止后续的 part.interact / 空交互回退；客户端也会返回 SUCCESS
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
