# Pick Tweak — 功能文档

> 双端拾取增强：服务端权威校验与发放，客户端负责输入、界面与纯客户端功能。
> 适用版本：Minecraft 26.3 / Fabric（1.0.4）。

---

## 1. 概述

Pick Tweak（模组 id `picktweak`）扩展了 Minecraft 原版的中键拾取与工具选择能力：

- **拿得更准**：能在原版交互距离之外拾取方块与实体；按住 Ctrl / Alt 时把实体数据、方块实体 NBT、方块状态写进物品，放下即可还原。
- **拿得更全**：能识别画作、物品框、下落方块、刷怪笼、火方块、玩家头颅与生物头颅，以及太阳 / 月亮（可循环光源等级）。
- **给得更准**：自动挑出当前目标的最优工具 / 武器，按附魔收益和耐久打分，而不是「有没有对应工具」。
- **看得更清**：一键复制方块 / 实体 / 物品的 ID、NBT 与方块状态文本，可直接粘到命令或文档里。
- **双端安全**：物品由服务端权威计算与发放，距离、NBT、发放与限流全部由服务端裁定。

---

## 2. 双端架构与权威拾取

### 2.1 分工

| 端 | 职责 |
|---|---|
| 客户端 | 按键与热键、远距离射线、ID / NBT / 状态文本与剪贴板、配置面板、创造物品栏注入 |
| 服务端 | 校验拾取距离与各项开关、解析自定义映射、计算方块 / 实体结果、应用附魔并发放物品、处理 `/pbp give` 与 reload 请求 |

服务端**不需要** MaLiLib；客户端即使不装 MaLiLib 也能正常拾取（使用默认配置与默认 V / G 按键）。

### 2.2 拾取流程（服务端装有本模组时）

1. 客户端按下中键，按客户端配置的范围自己做一次射线；原版 `hitResult` 在交互距离内时直接复用，否则做远距离射线。
2. 命中方块 / 实体后，客户端发送拾取请求（方块：位置 + 是否附带数据 / 状态；实体：实体 id + 是否附带数据）。
3. 服务端先过每玩家的令牌桶限流（`rateLimitPerSecond`），超速请求被静默丢弃。
4. 再校验：目标所在区块已加载、距离不超过 `maxPickDistance` / `maxEntityPickDistance`、`allowIncludeData` / `allowStateProperties` 是否允许附带数据、天体请求是否允许（`allowCelestial`）。
5. 服务端计算结果：先查 `picktweak-overrides.json` 自定义映射（支持完整物品 JSON / SNBT 与 NBT），再按内置规则生成物品——火 → 打火石、刷怪笼 → 带方块实体 NBT、流体 → 桶（需开启 `pickFluids`）、下落方块 → 方块物品、头颅 / 刷怪蛋 / 原版拾取物等；按住 Ctrl 时写入方块实体 NBT（方块）或 `minecraft:entity_data` / `painting_variant`（实体），按住 Alt 时把方块状态写入 `minecraft:block_state` 组件。
6. 服务端把物品交给玩家：背包里已有完全相同的物品则直接切换；否则按 `giveInSurvival` 与创造模式决定是否真正发放（发放时同步手持槽位与背包）。
7. 客户端按 `showPickFeedback` 决定是否在聊天栏提示。

### 2.3 服务端未安装时

- 中键拾取（方块 / 实体）**完全使用原版行为**：扩展拾取需要服务端权威计算，客户端不再本地构造物品，因此不会出现「客户端假物品」。
- Ctrl / Alt 仍可用：仅把目标 NBT / 方块状态复制到剪贴板，不产生任何物品。
- **太阳 / 月亮拾取（含光源等级循环）、`/pbp give`、`/pbp reload` 需要服务端**；未安装时命令提示不可用。
- ID 拾取（V）与工具 / 武器拾取（G，从背包里选择）仍是纯客户端功能；创造模式的工具生成按原版创造模式发放。
- 服务端未安装时首次拾取提示一次「服务端未安装」（30 秒节流）。

### 2.4 距离的最终裁定

客户端配置的拾取范围决定「这次射线能看多远」；服务端另有上限，超出服务端上限的请求会被**静默忽略**，
不会发放任何物品。因此专用服务器可以放心把客户端范围配置得很大。

服务端距离上限加载时会被夹取到 1~512 格；非法值回落并记录日志。

---

## 3. 区块拾取（Block Picker）

中键拾取，接管原版行为。

### 3.1 拾取目标

| 目标 | 普通拾取（不按修饰键） |
|---|---|
| 普通方块 | 对应物品 |
| 火（普通火 / 灵魂火） | 打火石（与原版 1.16+ 行为一致） |
| 流体方块（水 / 岩浆 / 粉雪） | 对应材质的桶（而非流体方块本身）；**默认关闭**（客户端 `blockPickerPickFluids` 与服务端 `pickFluids` 均为 `false`，需在配置开启） |
| 玩家 | 带皮肤数据的玩家头颅 |
| 生物 | 常见生物默认给头颅（僵尸 → 僵尸头、骷髅 → 骷髅头等，`preferEntityHeads` 默认开）；其余生物给对应刷怪蛋，没有刷怪蛋的退回原版拾取物 |
| 刷怪笼（普通 / 试炼） | 刷怪笼物品，并附带上刷怪笼的方块实体 NBT（生物类型 / SpawnPotentials 等，受服务端 `allowIncludeData` 约束） |
| 物品展示框 / 画作 | 展示框 / 画作本体物品 |
| 下落中的方块 | 落地后会变成的方块物品（而不是空气）；Ctrl 时附带方块状态 |
| 太阳 / 月亮 | 光源方块（`sunItem` / `moonItem` 默认均为 `minecraft:light`），需要服务端且 `allowCelestial = true`；若主手 / 副手正持光源方块，则循环光照等级 0..15（15 → 0）并在聊天栏提示等级 |

### 3.2 修饰键增强

| 按键 | 效果 |
|---|---|
| **Ctrl + 中键** | 把目标数据写进物品（带 NBT 的物品名称下方显示紫色 `+NBT` 副标题），复制 NBT 到剪贴板需开启 `blockPickerCopyNbt`（默认关闭）：方块 → 方块实体 NBT；生物 → 对应刷怪蛋 + `minecraft:entity_data`（生命值 / 装备 / AI 状态等，放下即生成同样状态的生物）；盔甲架 → 盔甲架物品 + `entity_data`（姿势 / 装备）；展示框 → 展示框物品 + `entity_data`（框内物品 / 旋转 / 隐形 / 固定）；画作 → 画作物品 + `painting_variant`（确切变种）；玩家 → 带皮肤玩家头颅；无刷怪蛋的实体退回其拾取物品并尽量附带 `entity_data` |
| **Alt + 中键** | 把方块状态写进物品的 `minecraft:block_state` 组件（楼梯朝向、营火点燃、台阶类型等，放下可还原），物品名称下方显示紫色 `+State` 副标题；同时把状态文本（如 `facing=north,half=top`）复制到剪贴板 |
| **Ctrl + Alt + 中键** | 同时附加方块实体 NBT 与方块状态 |

- 刷怪笼即使不按 Ctrl 也会尽量带上方块实体 NBT（受服务端 `allowIncludeData` 约束）；下落的方块仅在 Ctrl 时附带方块状态。
- `entity_data` 中会剔除坐标 / 速度 / UUID / 朝向等「放置时才决定」的字段，放下实体不会瞬移到旧位置。
- 剪贴板里是单行 SNBT（已去掉黑名单键），可直接粘进 `/data` 或 `/summon`；开启 `showCopiedNbt` 时聊天栏同时打印缩进版。
- `blockPickerCopyNbt`（默认关闭）控制 Ctrl 拾取时是否把 NBT 写入剪贴板；关闭时物品仍带 NBT（并显示紫色 `+NBT` 副标题），只是不写剪贴板。
- 带方块状态的物品会显示紫色 `+State` 副标题；Ctrl+Alt 同时拾取时两条副标题都会出现。
- `blockPickerCopyState`（默认开）控制 Alt 是否把状态文本复制到剪贴板；关闭后物品仍带 `block_state`，只是不复制文本。
- 修饰键本身是可改键的 MaLiLib 热键（默认 `LEFT_CONTROL + 中键`、`LEFT_ALT + 中键`），清空绑定即可关闭对应功能。

### 3.3 拾取范围

- 方块拾取范围与实体拾取范围分别可配（生存模式）。
- 可选择使用「玩家交互距离」而非固定值。
- 创造模式可单独使用更大的范围。
- 服务端另有 `maxPickDistance` / `maxEntityPickDistance` 上限（默认 100，可配范围 1~512，见 10.3）。

### 3.4 反馈

- `showPickFeedback`：拾取后显示拾取目标（以及 +NBT / +状态）。
- `showCopiedNbt`：Ctrl 复制 NBT 时在聊天栏打印缩进版（仅在 `blockPickerCopyNbtChatFeedback` 开启时生效）。
- `blockPickerCopyNbtChatFeedback` / `blockPickerCopyStateChatFeedback`（默认均关闭）：控制 Ctrl / Alt 复制时的聊天提示（`nbt_copied` / `no_nbt` / 美化 NBT、`state_copied` / `no_block_state`）；关闭时只静默复制到剪贴板。
- Alt 复制状态成功时提示 `state_copied`（方块名 + 状态文本）；目标没有状态属性时提示 `no_block_state`。
- 太阳 / 月亮的等级循环：每次成功循环提示 `light_level`（当前光照等级）。
- 服务端未安装时首次中键拾取提示一次（30 秒内不重复），之后走原版拾取。

---

## 4. ID 拾取（ID Picker）

把目标的标识信息输出到聊天栏，并可一键复制。

### 4.1 输出内容

| 目标 | 默认输出 | 按住 Ctrl（NBT 修饰键）时 |
|---|---|---|
| 方块 | 方块 ID（可带命名空间）+ 方块状态属性 | 方块实体的完整 NBT |
| 实体 | 实体 ID（可带命名空间） | 实体的完整 NBT |
| 掉落物 | 物品 ID（需要开启 `idPickerPickItems`） | — |
| 天空中的太阳 / 月亮 | `minecraft:sun` / `minecraft:moon` | — |
| 快捷栏物品（聊天框内） | 物品 ID（可按配置转换成方块 ID） | — |

NBT 输出由配置项 `includeNbt` 与 NBT 修饰键异或决定：默认 `includeNbt = false`，即按住 Ctrl 输出 NBT；
把 `includeNbt` 设为 true 后则是按住 Ctrl 切回 ID。

### 4.2 可配置项

- **是否带命名空间**：输出 `minecraft:stone` 还是只输出 `stone`
- **是否附带状态属性**：把 `facing=north`、`half=top` 这类属性一并列出
- **是否复制到剪贴板**：复制的同时仍会显示在聊天栏
- **NBT 标签黑名单**：逐条屏蔽不想显示的 NBT 键（如实体里的 UUID、坐标、朝向）
- **属性黑名单**：逐条屏蔽不想显示的方块状态属性（如 `waterlogged`）
- **NBT 美化输出**：以缩进格式而非单行输出
- **物品转方块 ID**：方块物品（BlockItem）直接转换成对应方块 ID，桶等非方块物品走内置映射

### 4.3 聊天框内快捷输出

聊天输入框打开时按 **Ctrl + 数字键（1-9 或 0）**，把对应快捷栏槽位物品的 ID 直接写入输入框。
十个槽位都是独立热键，可在面板「热键」页逐个改键。

---

## 5. 工具拾取（Tool Picker）

自动为当前目标选择或生成最合适的工具 / 武器（默认热键 G）。

### 5.1 选工具

看向一个方块时按 G：

1. 按方块判定所需工具种类：流体 → 桶；可刷方块 → 刷子；可剪方块 → 剪刀；竹子类 → 剑（可关闭）；其余按镐 / 斧 / 锹 / 锄的挖掘标签匹配。
2. 在背包中找出该种类的物品（开启 `searchContainers` 时会翻找潜影盒 / 收纳袋）。
3. 按综合评分挑出最优的一件并切换槽位；剩余耐久低于阈值的工具会被跳过。
4. 背包里一件都没有时提示（生存模式不会凭空生成物品）。

**评分维度**：挖掘速度、是否正确掉落、耐久上限、精准采集 / 时运 / 效率（按配置偏好加权）、耐久 / 经验修补。

### 5.2 选武器

看向一个生物时按 G，自动挑出最优武器：

- 以武器的**基础攻击伤害**为主分，计入锋利、亡灵杀手、节肢杀手、穿刺、火焰附加、抢夺等附魔收益。
- 支持**武器偏好表**：为某些实体指定优先武器（`bow` / `trident` / `sword` / `axe` / `mace`），默认剑。
- 背包里没有可用武器时提示。

### 5.3 生存 / 创造模式差异

| | 生存模式 | 创造模式 |
|---|---|---|
| 工具 / 武器 | 从背包里**选**已有的最佳工具并切换 | 通过服务端请求**发放**一件带满配置附魔的工具 / 武器 |
| 背包里没有 | 提示无可用工具 | 直接发放 / 生成 |
| 耐久不足 | 跳过并提示 | 不受影响 |
| 服务端拒绝发放 | — | 聊天栏提示被拒绝（服务端 `allowClientGive = false` 或非 OP） |

---

## 6. 附魔与配置

### 6.1 工具 / 武器预设

工具与武器的预设写在客户端配置 `toolSpecs` 字符串列表里，每行格式：

```
种类|物品ID|附魔=等级;附魔=等级
```

- 支持的工具种类：`pickaxe`、`axe`、`shovel`、`hoe`、`sword`、`shears`、`bucket`、`fishing_rod`、`brush`
- 支持的武器种类：`sword`、`axe`、`bow`、`crossbow`、`trident`、`mace`
- 武器偏好表 `weaponPreferences` 每行格式：`实体ID=bow|trident|sword|axe|mace`

服务端也可在 `picktweak-server.json` 里分别配置工具 / 武器发放时使用的物品（`toolItems` / `weaponItems`）。

### 6.2 附魔冲突自动处理

默认会按固定的优先级组剔除互相冲突的附魔，保证结果稳定可预期：

- 亡灵杀手 > 节肢杀手 > 锋利
- 精准采集 / 时运：按 `preferSilkTouch` / `preferFortune` 配置决定，都未开启时保留精准采集
- 无限 > 经验修补
- 多重射击 > 穿透
- 激流 > 引雷 > 忠诚

### 6.3 附魔等级

- 服务端发放时，附魔等级受 `maxEnchantLevel` 限制（默认 255）。
- 只接受注册表中真实存在的附魔；客户端随意构造的非法附魔会被忽略。

### 6.4 不兼容附魔模式

开启 `allowIncompatible` 后不再剔除冲突附魔，按配置的等级原样生成（上限 255）。

---

## 7. 创造模式物品栏注入

把按**互斥附魔方案**生成的满附魔装备**始终注入「管理员用品」标签页**（`minecraft:op_blocks`），方便随时取用；`creativeTab` 配置用于额外追加一个标签页（与管理员用品相同时自动去重）。

- 开关：武器 `creativeInjectWeapons`、工具 `creativeInjectTools`、盔甲与其他 `creativeInjectArmor`（均默认开）。
- **武器**：剑（锋利 / 亡灵杀手 / 节肢杀手，各含带火焰附加与刷怪场无火焰两版）、斧、矛（含突进 III）、重锤（致密 / 破甲 / 亡灵杀手 / 节肢杀手 + 风爆 III）、弓（经验修补 / 无限）、弩（多重射击 / 穿透）、三叉戟（忠诚 + 引雷 / 激流）。
- **工具**：镐 / 斧 / 锹 / 锄的时运 III / 精准采集 I 两套，剪刀（+效率 V）、钓鱼竿、打火石。
- **盔甲与其他**：头盔 / 胸甲 / 护腿 / 靴子的四种保护各一套（靴子另分深海探索者 III / 冰霜行者 II），盾牌、鞘翅。
- 基础物品优先沿用客户端 `toolSpecs` 预设；没有预设的种类使用内置最佳物品（矛 → 钻石矛，盔甲 → 下界合金）。
- 该注入只影响客户端看到的创造物品栏列表。

---

## 8. 背包管理

模组自己管理物品的放置与切换，而不是完全交给原版。

### 8.1 选槽位逻辑

1. 物品**已在快捷栏** → 直接切到那个槽位（锁定的槽位不会被选中）。
2. 物品**在背包里**（开启 `searchContainers` 时还会翻找潜影盒 / 收纳袋）→ 通过容器点击包与当前快捷栏槽位交换。
3. 服务端发放新物品时优先塞进当前手持槽位，被锁定则找空槽位。

### 8.2 锁定槽位

快捷栏 1-9 可逐个锁定（面板「背包」页），锁定后模组不会占用或切换这些槽位。

### 8.3 其他

- **搜索容器**（`searchContainers`）：翻找潜影盒、收纳袋等容器，找到目标工具时切换到该容器所在槽位。
- 所有背包改动都通过与服务器同步的容器点击完成，不会因客户端预测而回弹。

---

## 9. 命令

命令都是客户端命令（`/pbp`），服务端权限与限流在服务端校验。

| 命令 | 作用 |
|---|---|
| `/pbp reload` | 重新载入自定义映射：由服务端重载（**`requireOpForReload` 默认要求 OP**，拒绝时聊天栏提示）；服务端未安装时提示需要服务端 |
| `/pbp give tool <类型>` | 请求服务端发放一件带满配置附魔的工具（**服务端 `requireOpForGive` 默认要求 OP**，且 `allowClientGive = true`） |
| `/pbp give weapon <类型>` | 同上，武器 |

- 工具类型：`pickaxe`、`axe`、`shovel`、`hoe`、`sword`、`shears`、`bucket`、`fishing_rod`、`brush`
- 武器类型：`sword`、`axe`、`bow`、`crossbow`、`trident`、`mace`
- 服务端未安装时：提示需要服务端，不会本地生成物品。
- 服务端已安装但请求被拒绝（`allowClientGive` 关闭或非 OP）时：聊天栏提示被拒绝，不会本地生成。
- 未知类型会报错并给出可用类型补全。

---

## 10. 配置

### 10.1 配置文件

| 文件 | 位置 | 内容 |
|---|---|---|
| `picktweak.json` | 客户端 | 全部客户端设置与热键（含 `blockPickerPickFluids` 流体开关，默认关），在 MaLiLib 面板（K 键）里编辑；没装 MaLiLib 时使用默认值，不写文件 |
| `picktweak-server.json` | 服务端 | 拾取距离上限、NBT / 状态开关、发放与限流、附魔等级上限、`pickFluids`（默认关）、太阳 / 月亮物品（默认光源方块）、工具 / 武器物品表 |
| `picktweak-overrides.json` | 两端（各自读取） | 自定义映射：`方块 / 实体 -> 物品`；值支持 ID、`ID[组件=JSON]`、完整物品 JSON / SNBT（含 NBT），缺失时自动写入默认模板 |

所有文件都在游戏目录的 `config/` 下。服务端配置与映射缺失时会自动生成默认文件。

### 10.2 客户端面板

- 需要 **MaLiLib**：装了才有面板（默认 K）与热键改键；没装也能用默认配置与默认 V / G 按键。
- 面板分组：**区块拾取**、**ID 拾取**、**工具拾取**、**背包**、**创造模式**、**热键**（以及「全部」页）。
- 「区块拾取」页可配置 `blockPickerCopyNbt`（Ctrl 是否把 NBT 写入剪贴板，默认关）、`blockPickerCopyState`（Alt 是否复制状态文本，默认开）、`blockPickerCopyNbtChatFeedback` / `blockPickerCopyStateChatFeedback`（复制时的聊天提示，默认关）、`preferEntityHeads`（普通拾取是否优先头颅，默认开）、`blockPickerPickFluids`（流体拾取成桶，默认关）、NBT / 状态黑名单等。
- 面板内可直接搜索、改键、改值，保存后立即生效。

### 10.3 服务端配置项（`picktweak-server.json`）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `maxPickDistance` | `100.0` | 服务端允许的最大方块拾取距离，夹取范围 1~512，超出静默忽略 |
| `maxEntityPickDistance` | `100.0` | 服务端允许的最大实体拾取距离，夹取范围 1~512 |
| `allowIncludeData` | `true` | 是否允许拾取时附带方块实体 / 实体 NBT |
| `allowStateProperties` | `true` | 是否允许拾取时附带方块状态属性 |
| `allowClientGive` | `false` | 是否允许客户端请求发放工具 / 武器 |
| `giveInSurvival` | `false` | 生存模式是否真正发放新物品（关闭时只在背包已有同类物品时切换） |
| `requireOpForGive` | `true` | 发放工具 / 武器是否要求 OP（26.3 moderator 权限，等价旧版等级 ≥ 2） |
| `requireOpForReload` | `true` | 客户端 reload 请求是否要求 OP（同上） |
| `allowCelestial` | `true` | 是否允许拾取太阳 / 月亮 |
| `rateLimitPerSecond` | `20` | 每玩家每秒允许的拾取 / 发放 / 天体请求数，夹取范围 1~200，超出即丢弃 |
| `preferEntityHeads` | `true` | 拾取生物时优先给头颅而不是刷怪蛋 |
| `pickFluids` | `false` | 是否把流体 / 粉雪拾取成装满的桶（默认关闭） |
| `sunItem` | `minecraft:light` | 拾取太阳时发放的物品（默认光源方块；手持光源方块时改为循环等级） |
| `moonItem` | `minecraft:light` | 拾取月亮时发放的物品（默认光源方块；手持光源方块时改为循环等级） |
| `maxEnchantLevel` | `255` | 服务端发放工具 / 武器时允许的最高附魔等级，夹取范围 1~255 |
| `toolItems` / `weaponItems` | 钻石工具 / 对应武器表 | 各类工具 / 武器发放时使用的物品；非法 ID 会回落默认并记录日志 |

### 10.4 权限与安全

- **服务端权威**：距离、NBT / 状态开关、发放策略都以服务端配置为准，客户端配置无法绕过。
- **OP 校验**：`requireOpForGive` / `requireOpForReload` 默认开启（26.3 的 moderator 权限，等价旧版 OP 等级 ≥ 2）；非 OP 请求被拒绝并提示。
- **限流**：每玩家令牌桶限流 `rateLimitPerSecond`（默认 20，可配 1~200），超速请求直接丢弃。
- **发放上限**：附魔等级受 `maxEnchantLevel` 限制（默认 255），只接受注册表中真实存在的附魔。
- **默认拒绝**：`allowClientGive`、`giveInSurvival`、`requireOpForGive`、`requireOpForReload` 默认即安全值；专用服务器确认信任后再按需放开。
- **映射文件两端各自读取**：专用服务器上服务端只认服务端自己的 `picktweak-overrides.json`。

### 10.5 自定义映射（`picktweak-overrides.json`）

两端（客户端 / 服务端）各自读取同格式的映射文件，优先级高于所有内置拾取规则。
文件缺失时会自动写入模板（铁块 → 铁锭、猪 → 猪排），修改后用 `/pbp reload` 重载（服务端需 OP）。

```json
{
  "blocks": {
    "minecraft:iron_block": "minecraft:iron_ingot",
    "minecraft:chest": "minecraft:chest[minecraft:custom_name={\"text\":\"Demo\"}]"
  },
  "entities": {
    "minecraft:pig": "minecraft:porkchop",
    "minecraft:zombie": "minecraft:zombie_spawn_egg"
  }
}
```

映射值支持三种写法（完全支持 NBT 数据）：

| 写法 | 示例 |
|---|---|
| 纯物品 ID | `minecraft:iron_ingot` |
| ID + 组件 | `minecraft:stone[minecraft:custom_name={"text":"Demo"}]`（多个组件用顶层逗号分隔） |
| 完整物品 JSON / SNBT | `{"id":"minecraft:spawner","count":1,"components":{...}}`（JSON），或对应 SNBT（`{id:"minecraft:spawner",...}`） |

- 完整写法按 `ItemStack.CODEC` 解析，因此 `count`、`components`（组件 / NBT）都能完整还原；非法值退回内置拾取规则。
- `blocks` / `entities` 的键分别为方块 ID / 实体 ID（含命名空间，如 `minecraft:chest`）。

### 10.6 已知限制

- 服务端未安装本模组时：中键拾取走原版行为，太阳 / 月亮拾取（含光源等级循环）、`/pbp give`、`/pbp reload` 不可用，
  生存模式不会凭空产生物品。
- 没有 MaLiLib 时无法在游戏内改键或改配置，只能使用默认值。
- 流体拾取成桶默认关闭，需要时在客户端 `blockPickerPickFluids` 与服务端 `pickFluids` 一起开启。

---

## 11. 兼容性

| 模组 | 关系 | 作用 |
|---|---|---|
| **MaLiLib** | 客户端推荐（非必需） | 提供配置面板与热键改键；不装则用默认配置 + 原版 V / G 按键。**服务端不需要** |
| **Mod Menu** | 可选 | 在模组条目上提供「配置」按钮；面板本身仍由 MaLiLib 提供 |
| **Litematica** | 可选 | 可接管其自带的 Pick Block 快捷键，避免两个模组的拾取行为打架 |
| **Axiom** | 可选 | 中键拾取共存：默认「让位」，可改为「覆盖」；编辑器模式下始终让位 |
| **Fabric API** | 必需 | 网络、命令、创造物品栏事件等基础 API |

---

## 12. 多语言

内置 5 种语言：简体中文（`zh_cn`）、繁体中文（`zh_tw`）、英文（`en_us`）、法文（`fr_fr`）、俄文（`ru_ru`）。
5 个语言文件的键集合保持一致。

---

## 13. 适用场景速查

| 你想做的事 | 用什么 |
|---|---|
| 快速拿到一个楼梯方块 | 中键拾取（带朝向信息）；Alt + 中键还会把朝向写进物品并复制状态文本 |
| 复制一个箱子的内容 | Ctrl + 中键拾取箱子 |
| 复制某个生物的完整 NBT | Ctrl + 中键拾取生物（会得到带 `entity_data` 的刷怪蛋） |
| 复制一个穿戴装备的盔甲架 | Ctrl + 中键拾取（盔甲架物品 + `entity_data`） |
| 把画作的确切变种取下来 | Ctrl + 中键拾取画作（`painting_variant` 组件） |
| 把营火点燃 / 楼梯朝向状态带进物品 | Alt + 中键拾取（写入 `block_state`，放置即还原） |
| 拿打火石（对着火） | 中键拾取火方块（普通火 / 灵魂火 → 打火石） |
| 循环光源方块的光照等级 | 手持光源方块指向太阳 / 月亮按中键（0..15 循环，聊天栏提示等级） |
| 带生物类型的刷怪笼 | 中键拾取刷怪笼（自动附带方块实体 NBT） |
| 用 overrides 塞入复杂 NBT 物品 | 在 `picktweak-overrides.json` 里写完整物品 JSON / SNBT |
| 拿一个箱子里的潜影盒内容 | 中键拾取潜影盒 → Ctrl 复制 NBT |
| 查某个方块的状态值 | ID 拾取 |
| 复制物品 ID 到命令框 | 聊天框内 Ctrl + 数字键 |
| 找一把挖黑曜石最快的镐 | 按 G 看向黑曜石（生存模式自动切换） |
| 造一把满附魔的效率 V 镐 | 创造模式 + 工具拾取，或 `/pbp give tool pickaxe` |
| 对付一群骷髅 | 看向骷髅按 G，让模组挑亡灵杀手武器 |
| 超出交互距离拾取 | 服务端安装本模组后直接中键（远距离） |

---

*本文档描述 1.0.4 的功能行为，命令、配置项与默认值以实际生成的配置文件为准。*