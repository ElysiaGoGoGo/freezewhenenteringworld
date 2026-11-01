# FreezeWhenEnteringWorld

一个 NeoForge 模组（Minecraft 1.21.1）：**进入世界后立刻冻结世界**，效果类似原版 `/tick freeze`。

## 功能

- 在 **第一个 tick 之前** 冻结世界（`ServerStartedEvent` → `server.tickRateManager().setFrozen(true)`）：
  `gameTime`、实体、方块实体、随机刻、红石、天气等世界内容不再更新；
- 冻结语义与 `/tick freeze` 一致：**玩家不被冻结**，仍可移动、放/挖方块、开 GUI、聊天；
- 两个开关，**任一开启即冻结**：
  - **标题界面左上角按钮**：本次游戏会话的临时开关，默认关闭，不写盘，重启游戏回到关闭；
  - **TOML 配置 `freezeOnWorldEnter`**：持久开关，默认 `false`，纯服务端（专服）通过它控制；
- 提供一个测试方块 `freezewhenenteringworld:npe_block`：方块实体每 tick 抛 NPE，
  可用来观察「世界是否在 tick」（冻结时不触发，解冻后第一个 tick 触发）。

## 配置

首次启动后自动生成 `config/freezewhenenteringworld-common.toml`：

```toml
#If true, freeze the world (like /tick freeze) as soon as it is loaded, before the first tick.
#This is the persistent/server-side switch: a dedicated server can enable it here, no client needed.
#The title-screen button is a separate per-session switch; either one enables the freeze.
freezeOnWorldEnter = false
```

- 默认关闭；标题界面按钮控制的是另一个「本次会话」开关，二者互不覆盖；
- 专服没有标题界面，直接改这个文件（改完重启服务器/重新进入世界生效）。

详细说明见 [docs/05-toml-config.md](docs/05-toml-config.md)。

## 构建

需要 JDK 21：

```powershell
$env:JAVA_HOME='<JDK 21 路径>'
.\gradlew.bat build
```

开发运行：`.\gradlew.bat runClient` / `.\gradlew.bat runServer`。

## 文档

- [01 · NPE 测试方块](docs/01-npe-block.md)
- [02 · 世界开始 tick 的时机链](docs/02-world-start-tick-timing.md)
- [03 · /tick freeze 的作用与原理](docs/03-tick-freeze.md)
- [04 · 开始界面开关 + TOML 配置](docs/04-title-toggle-freeze.md)
- [05 · TOML 配置说明](docs/05-toml-config.md)

## 许可

见 [TEMPLATE_LICENSE.txt](TEMPLATE_LICENSE.txt)。
