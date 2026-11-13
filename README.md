# Pick Tweak

当前版本：**1.0.0-beta.7**（Minecraft 26.3 / Fabric）。

masa 风格的拾取增强：远距离拾取方块与实体、Ctrl 把目标数据写进物品（实体数据 / 方块实体 NBT）并复制 NBT、
Alt 把方块状态写进物品（`block_state` 组件）并复制状态文本、ID 一键复制、智能选工具与武器。
另支持火方块 → 打火石、刷怪笼带 NBT、落下方块 → 方块物品，以及指向太阳 / 月亮时循环光源等级。
拾取由服务端权威处理，客户端可选安装 MaLiLib 获得配置面板与改键。

## 依赖

- Minecraft 26.3 · Fabric Loader ≥ 0.20.1 · Fabric API 0.165.2+26.3
- **MaLiLib 0.30.2（客户端推荐，非必需）**：装了才有配置面板（K 键）与热键改键；不装也能正常运行，
  使用默认配置与默认的 V / G 按键。**服务端完全不需要 MaLiLib。**
- Mod Menu 21.0.0（可选，在模组条目上显示「配置」按钮；面板本身由 MaLiLib 提供）
- Litematica（可选，可接管其拾取快捷键）· Axiom（可选，共存模式）

## 安装

服务端与客户端都放入 `picktweak-1.0.0-beta.7.jar`；客户端可选装 MaLiLib 与 Mod Menu。

服务端组件负责权威拾取（校验距离、计算 NBT 与状态属性、发放物品），客户端负责输入、界面与本地回退。
只装客户端、服务端没装模组时，拾取会退回客户端本地计算（创造模式可本地生成物品；生存模式只在背包里已有同类物品时切换）。

## 快捷键

**装了 MaLiLib 时，所有快捷方式都能在面板（K 键）的「热键」页里改**，下表是默认值；
不装 MaLiLib 时 V / G 使用默认按键。中键本身是原版的「拾取」按键，在原版控制项里改。

| 按键 | 作用 |
|---|---|
| 中键 | 拾取方块 / 实体（可远距离）；生物默认给头颅，其余给刷怪蛋或原版拾取物，展示框 / 画作给本体物品 |
| Ctrl + 中键 | 把目标数据写进物品并复制 NBT 到剪贴板：方块 → 方块实体 NBT；实体 → 刷怪蛋 / 盔甲架 / 展示框 + `entity_data`、画作 + `painting_variant`（放下即还原状态） |
| Alt + 中键 | 把方块状态写进物品的 `block_state` 组件（放下可还原朝向 / 点亮等），并把状态文本（如 `facing=north,half=top`）复制到剪贴板（`blockPickerCopyState`，默认开） |
| Ctrl + Alt + 中键 | 同时附加方块实体 NBT 与方块状态 |

> Ctrl / Alt 复制完成后的聊天提示**默认关闭**，可在面板「区块拾取」页用 `blockPickerCopyNbtChatFeedback` / `blockPickerCopyStateChatFeedback` 打开；`showCopiedNbt` 的美化 NBT 输出同样受 Ctrl 提示开关控制。
| V | 复制目标 ID 到聊天栏与剪贴板（按住 Ctrl 切换为 NBT 输出） |
| G | 拾取最合适的工具 / 武器 |
| K | 打开配置面板（需要 MaLiLib） |
| Ctrl + 数字键（聊天框内） | 把该快捷栏物品的 ID 写进输入框（1…0 十个槽位都能单独改键） |

**一些拾取细节：**

- 火（普通火 / 灵魂火）→ 打火石；下落中的方块 → 落地后的方块物品（Ctrl 时附带方块状态）。
- 刷怪笼（普通 / 试炼）→ 刷怪笼物品，并附带上刷怪笼的方块实体 NBT（生物类型等，受服务端 `allowIncludeData` 约束）。
- 指向太阳 / 月亮（需要服务端安装本模组）：默认发放光源方块（`sunItem` / `moonItem` 默认都是 `minecraft:light`）；
  若主手或副手正拿着光源方块，每次拾取循环光照等级 0–15（15 → 0），并在聊天栏提示当前等级。
- 流体拾取成桶默认**关闭**（客户端 `blockPickerPickFluids` / 服务端 `pickFluids` 默认均为 `false`），需要时在配置里打开。
- 玩家 → 带皮肤数据的玩家头颅；`picktweak-overrides.json` 支持 ID、`ID[组件=JSON]` 与完整物品 JSON / SNBT，可完全自定义 NBT（两端与客户端回退都生效）。

## 与 Axiom 共存

Axiom 也会往中键拾取里插自己的逻辑（编辑器拾取、builder 工具、`INFINITE_REACH` 能力）。
Pick Tweak 不会和它抢同一个注入点，而是在「Axiom 明确要接管这次中键」时主动让位。
面板上的「区块拾取 → 与 Axiom 共存方式」可以选：

- **让位**（默认）：Axiom 的 builder 工具、编辑器和无限距离优先，其余我们自己接管
- **覆盖**：除编辑器模式外一律我们自己接管

没装 Axiom 时这一项不起作用。

## 命令

| 命令 | 作用 |
|---|---|
| `/pbp reload` | 重新载入自定义映射：服务端装了本模组时由服务端重载（**默认要求 OP**，`requireOpForReload`）；服务端未安装时客户端本地重载 |
| `/pbp give tool <类型>` | 请求服务端发放一件带满附魔的工具（**默认要求 OP**，`requireOpForGive`；且 `allowClientGive = true`） |
| `/pbp give weapon <类型>` | 同上，武器 |

工具类型：`pickaxe`、`axe`、`shovel`、`hoe`、`sword`、`shears`、`bucket`、`fishing_rod`、`brush`；
武器类型：`sword`、`axe`、`bow`、`crossbow`、`trident`、`mace`。

服务端没装模组时，创造模式会在客户端本地生成对应物品，生存模式只提示（不会凭空发物品）；
服务端已安装但请求被拒绝（`allowClientGive` 关闭或非 OP）时只提示，不会本地生成。

## 配置

| 文件 | 位置 | 内容 |
|---|---|---|
| `picktweak.json` | 客户端 | 全部客户端设置（含流体 / 光源开关），在 MaLiLib 面板（K 键）里编辑；没装 MaLiLib 时使用默认值 |
| `picktweak-server.json` | 服务端 | 最大拾取距离、NBT / 状态开关、是否允许客户端发物品、生存模式是否发放、限流、附魔等级上限、流体拾取（默认关）、太阳 / 月亮物品（默认光源方块）、工具 / 武器物品表 |
| `picktweak-overrides.json` | 两端（各自读取） | 自定义映射：`方块 / 实体 -> 物品`；值支持 ID、`ID[组件=JSON]`、完整物品 JSON / SNBT（含 NBT），缺失时自动写入模板 |

旧版本的 `pickblockpro-server.json` / `pickblockpro-overrides.json` 会在首次启动时自动迁移读取，并写出新的文件名。

## 服务端安全默认值

服务端默认按「防刷物品」配置，专用服务器可按需放开：

- `allowClientGive = false`：默认拒绝客户端发来的工具 / 武器发放请求
- `giveInSurvival = false`：生存模式不凭空发放新物品，只在背包里已有同类物品时切换过去
- **GiveTool 与 reload 默认需要 OP**：`requireOpForGive` / `requireOpForReload` 默认 `true`，非 OP 的客户端请求会被拒绝
- **每玩家限流 `rateLimitPerSecond`**：默认 `20`（可配范围 1~200），请求超过速率上限会被静默丢弃，抑制刷物品 / 刷包
- 最大拾取距离（`maxPickDistance` / `maxEntityPickDistance`，默认 100，夹取范围 1~512）与附魔等级上限
  （`maxEnchantLevel`，默认 255）均可配置

单人游戏与信任的服务器确认后再打开 `allowClientGive` / `giveInSurvival`。

## 构建

```bash
./gradlew build     # 产物：build/libs/picktweak-1.0.0-beta.7.jar
./gradlew smoke     # 冒烟测试（不启动游戏）
```

## 许可

MIT