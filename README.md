# Pick Tweak

masa 风格的拾取增强（Minecraft 26.3 / Fabric）：远距离拾取方块与实体、Ctrl 附加 NBT 并复制到剪贴板、
Alt 附加状态属性、ID 一键复制、智能选工具与武器，配置面板由 MaLiLib 提供。

## 依赖

- Minecraft 26.3 · Fabric Loader ≥ 0.20.1 · Fabric API 0.165.2+26.3
- MaLiLib 0.30.2（客户端，提供配置面板与热键绑定）
- Mod Menu 21.0.0（可选，在模组条目上显示「配置」按钮）· Litematica（可选，接管其拾取）

## 安装

服务端与客户端都放入 `picktweak-1.0.0-beta.3.jar`；客户端再装 MaLiLib 与 Mod Menu。
服务端组件负责权威拾取（校验距离、计算 NBT 与状态属性、发放物品），客户端负责界面与热键。

## 快捷键

| 按键 | 作用 |
|---|---|
| 中键 | 拾取方块 / 实体（可远距离） |
| Ctrl + 中键 | 额外附加完整 NBT，并把目标的 NBT 复制到剪贴板 |
| Alt + 中键 | 额外附加方块状态属性（朝向、台阶朝向……） |
| V | 复制目标 ID 到聊天栏与剪贴板（Ctrl + V 输出 NBT） |
| G | 拾取最合适的工具 / 武器 |
| K | 打开配置面板 |
| Ctrl + 数字键（聊天框内） | 把该快捷栏物品的 ID 写进输入框 |

## 命令

| 命令 | 作用 |
|---|---|
| `/pbp reload` | 重新载入自定义映射（客户端与服务端一起） |
| `/pbp give tool <类型>` | 领取一件带满附魔的工具 |
| `/pbp give weapon <类型>` | 领取一件带满附魔的武器 |

## 配置

| 文件 | 位置 | 内容 |
|---|---|---|
| `picktweak.json` | 客户端 | 全部设置，在 MaLiLib 面板（K 键）里编辑 |
| `picktweak-server.json` | 服务端 | 最大拾取距离、NBT / 状态开关、生存模式是否发放、太阳与月亮对应的物品 |
| `picktweak-overrides.json` | 两端 | 自定义映射：`方块 / 实体 -> 物品` |

## 构建

```bash
./gradlew build     # 产物：build/libs/picktweak-1.0.0-beta.3.jar
./gradlew smoke     # 冒烟测试（不启动游戏）
```

## 许可

MIT
