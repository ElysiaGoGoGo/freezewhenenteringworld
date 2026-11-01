package top.purpletears.freezewhenenteringworld;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.purpletears.freezewhenenteringworld.block.NpeBlock;
import top.purpletears.freezewhenenteringworld.blockentity.NpeBlockEntity;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FreezeWhenEnteringWorld.MODID)
public class FreezeWhenEnteringWorld {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "freezewhenenteringworld";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "freezewhenenteringworld" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold BlockEntityTypes which will all be registered under the "freezewhenenteringworld" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "freezewhenenteringworld" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // NPE 测试方块：方块实体每 tick 抛出一个 NullPointerException，用于观测世界是否已经开始 tick
    public static final DeferredBlock<NpeBlock> NPE_BLOCK = BLOCKS.registerBlock("npe_block", NpeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5F).requiresCorrectToolForDrops());
    // 对应的 BlockItem，让方块能以物品形式获得
    public static final DeferredItem<BlockItem> NPE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("npe_block", NPE_BLOCK);
    // 方块实体类型：把 NpeBlockEntity 绑定到 npe_block 上
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NpeBlockEntity>> NPE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("npe_block", () -> BlockEntityType.Builder.of(NpeBlockEntity::new, NPE_BLOCK.get()).build(null));

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public FreezeWhenEnteringWorld(IEventBus modEventBus, ModContainer modContainer) {
        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entity types get registered
        BLOCK_ENTITIES.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // 把 NPE 测试方块加入建筑方块创造标签，方便在创造模式里取得
        modEventBus.addListener(this::addCreative);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (FreezeWhenEnteringWorld) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // 把 NPE 测试方块加入建筑方块创造标签，方便在创造模式里取得
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(NPE_BLOCK_ITEM);
        }
    }

    /**
     * 世界加载完成、但**第一个 tick 还没跑**的唯一时机（详见 docs/02-world-start-tick-timing.md）。
     * 在这里冻结世界，可以让第 1 个 tick 起世界内容就不再更新（gameTime 停在 0）。
     * <p>
     * 单人/集成服务器与专用服务器都会触发该事件。开关只有一个：
     * TOML 配置 {@link Config#FREEZE_ON_WORLD_ENTER}（默认 false，持久化）。
     * 单人时由开始界面按钮直接切换它（按钮会写回并保存配置）；纯服务端（专服）
     * 没有开始界面，直接改配置文件控制。
     * </p>
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!Config.FREEZE_ON_WORLD_ENTER.getAsBoolean()) {
            LOGGER.info("[FreezeWhenEnteringWorld] 进入世界冻结开关: 关 -> 世界正常 tick");
            return;
        }

        // ServerTickRateManager#setFrozen 会自动把 ticking_state 广播给已在线玩家（新玩家由 PlayerList 补发）
        event.getServer().tickRateManager().setFrozen(true);
        LOGGER.info("[FreezeWhenEnteringWorld] 进入世界冻结开关: 开(来源: TOML 配置 freezeOnWorldEnter) -> 世界已加载,在第一个 tick 之前冻结");
    }
}
