import json, os, collections

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "picktweak", "lang")
BASE = os.path.normpath(BASE)
os.makedirs(BASE, exist_ok=True)

T = collections.OrderedDict()


def add(key, zh_cn, zh_tw, en_us, fr_fr, ru_ru):
    T[key] = {"zh_cn": zh_cn, "zh_tw": zh_tw, "en_us": en_us, "fr_fr": fr_fr, "ru_ru": ru_ru}


def add5(key, cn, tw, en, fr, ru):
    add(key, cn, tw, en, fr, ru)


# ---------------- 面板 ----------------
add("picktweak.config.title", "Pick Tweak 设置", "Pick Tweak 設定", "Pick Tweak Settings",
    "Paramètres de Pick Tweak", "Настройки Pick Tweak")
add("picktweak.config.tab.blockPicker", "区块拾取", "區塊拾取", "Block Picker", "Sélecteur de blocs", "Выбор блоков")
add("picktweak.config.tab.idPicker", "ID 拾取", "ID 拾取", "ID Picker", "Sélecteur d'ID", "Выбор ID")
add("picktweak.config.tab.toolPicker", "工具拾取", "工具拾取", "Tool Picker", "Sélecteur d'outils", "Выбор инструментов")
add("picktweak.config.tab.inventory", "背包", "背包", "Inventory", "Inventaire", "Инвентарь")
add("picktweak.config.tab.creative", "创造模式", "創造模式", "Creative", "Mode créatif", "Творческий режим")
add("picktweak.config.tab.hotkeys", "热键", "熱鍵", "Hotkeys", "Raccourcis", "Горячие клавиши")

# ---------------- 区块拾取 ----------------
add("picktweak.config.blockPickerEnabled", "启用区块拾取", "啟用區塊拾取", "Enable the block picker",
    "Activer le sélecteur de blocs", "Включить выбор блоков")
add("picktweak.config.blockPickerRange", "拾取范围（生存）", "拾取範圍（生存）", "Pick range (survival)",
    "Portée de sélection (survie)", "Дальность выбора (выживание)")
add("picktweak.config.blockPickerEntityRange", "实体拾取范围（生存）", "實體拾取範圍（生存）", "Entity pick range (survival)",
    "Portée de sélection des entités (survie)", "Дальность выбора сущностей (выживание)")
add("picktweak.config.blockPickerCreativeRange", "拾取范围（创造）", "拾取範圍（創造）", "Pick range (creative)",
    "Portée de sélection (créatif)", "Дальность выбора (творческий)")
add("picktweak.config.blockPickerUseInteractionRange", "使用交互距离", "使用互動距離", "Use the interaction range",
    "Utiliser la portée d'interaction", "Использовать дальность взаимодействия")
add("picktweak.config.blockPickerPickBlocks", "拾取方块", "拾取方塊", "Pick blocks", "Choisir les blocs", "Выбирать блоки")
add("picktweak.config.blockPickerPickEntities", "拾取实体", "拾取實體", "Pick entities", "Choisir les entités", "Выбирать сущности")
add("picktweak.config.blockPickerPickFluids", "流体拾取成桶", "流體拾取成桶", "Pick fluids as buckets", "Choisir les fluides comme des seaux",
    "Выбирать жидкости как ведра")
add("picktweak.config.blockPickerPickLightSources", "拾取太阳 / 月亮", "拾取太陽 / 月亮", "Pick the sun and the moon",
    "Choisir le soleil et la lune", "Выбирать солнце и луну")
add("picktweak.config.blockPickerPreferEntityHeads", "实体优先给头颅", "實體優先給頭顱", "Prefer mob heads",
    "Préférer les têtes d'entités", "Предпочитать головы сущностей")
add("picktweak.config.blockPickerTakeOverLitematica", "接管 Litematica 拾取", "接管 Litematica 拾取", "Take over Litematica's pick block",
    "Prendre en charge le pick block de Litematica", "Перехватывать pick block Litematica")
add("picktweak.config.blockPickerShowFeedback", "显示拾取提示", "顯示拾取提示", "Show pick feedback",
    "Afficher un retour de sélection", "Показывать сообщение о выборе")
add("picktweak.config.blockPickerCopyNbt", "Ctrl + 中键复制 NBT", "Ctrl + 中鍵複製 NBT", "Ctrl + middle click copies the NBT",
    "Ctrl + clic du milieu copie le NBT", "Ctrl + средняя кнопка копирует NBT")
add("picktweak.config.blockPickerShowCopiedNbt", "在聊天栏显示复制的 NBT", "在聊天欄顯示複製的 NBT", "Show the copied NBT in the chat box",
    "Afficher le NBT copié dans le chat", "Показывать скопированный NBT в чате")
add("picktweak.config.blockPickerAxiomMode", "与 Axiom 共存方式", "與 Axiom 共存方式", "How to coexist with Axiom",
    "Coexistence avec Axiom", "Совместная работа с Axiom")
add("picktweak.config.blockEntityNbtBlacklist", "方块实体 NBT 黑名单", "方塊實體 NBT 黑名單", "Block entity NBT blacklist",
    "Liste noire des NBT de bloc", "Чёрный список NBT блоков")
add("picktweak.config.statePropertyBlacklist", "方块状态属性黑名单", "方塊狀態屬性黑名單", "Block state property blacklist",
    "Liste noire des propriétés d'état", "Чёрный список свойств состояния блока")
add("picktweak.config.entityNbtBlacklist", "实体 NBT 黑名单", "實體 NBT 黑名單", "Entity NBT blacklist",
    "Liste noire des NBT d'entité", "Чёрный список NBT сущностей")

# ---------------- ID 拾取 ----------------
add("picktweak.config.idPickerEnabled", "启用 ID 拾取", "啟用 ID 拾取", "Enable the ID picker",
    "Activer le sélecteur d'ID", "Включить выбор ID")
add("picktweak.config.idPickerRange", "拾取范围（生存）", "拾取範圍（生存）", "Pick range (survival)",
    "Portée de sélection (survie)", "Дальность выбора (выживание)")
add("picktweak.config.idPickerEntityRange", "实体拾取范围（生存）", "實體拾取範圍（生存）", "Entity pick range (survival)",
    "Portée de sélection des entités (survie)", "Дальность выбора сущностей (выживание)")
add("picktweak.config.idPickerCreativeRange", "拾取范围（创造）", "拾取範圍（創造）", "Pick range (creative)",
    "Portée de sélection (créatif)", "Дальность выбора (творческий)")
add("picktweak.config.idPickerUseInteractionRange", "使用交互距离", "使用互動距離", "Use the interaction range",
    "Utiliser la portée d'interaction", "Использовать дальность взаимодействия")
add("picktweak.config.idPickerIncludeNamespace", "输出命名空间", "輸出命名空間", "Include the namespace",
    "Inclure l'espace de noms", "Включать пространство имён")
add("picktweak.config.idPickerIncludeStateProperties", "输出状态属性", "輸出狀態屬性", "Include the state properties",
    "Inclure les propriétés d'état", "Включать свойства состояния")
add("picktweak.config.idPickerCopyToClipboard", "复制到剪贴板", "複製到剪貼簿", "Copy to the clipboard",
    "Copier dans le presse-papiers", "Копировать в буфер обмена")
add("picktweak.config.idPickerIncludeNbt", "输出 NBT", "輸出 NBT", "Output NBT", "Sortir le NBT", "Выводить NBT")
add("picktweak.config.idPickerPrettyNbt", "美化 NBT 输出", "美化 NBT 輸出", "Pretty print the NBT",
    "NBT formaté", "Форматированный NBT")
add("picktweak.config.idPickerChatShortcut", "聊天框内 Ctrl + 数字键", "聊天框內 Ctrl + 數字鍵", "Ctrl + number key in chat",
    "Ctrl + chiffre dans le chat", "Ctrl + цифра в чате")
add("picktweak.config.idPickerItemToBlockId", "物品转方块 ID", "物品轉方塊 ID", "Convert item IDs to block IDs",
    "Convertir les ID d'objet en ID de bloc", "Преобразовать ID предметов в ID блоков")
add("picktweak.config.idPickerPickBlocks", "拾取方块 ID", "拾取方塊 ID", "Pick block IDs", "Choisir les ID de bloc",
    "Выбирать ID блоков")
add("picktweak.config.idPickerPickEntities", "拾取实体 ID", "拾取實體 ID", "Pick entity IDs", "Choisir les ID d'entité",
    "Выбирать ID сущностей")
add("picktweak.config.idPickerPickItems", "拾取物品 ID", "拾取物品 ID", "Pick item IDs", "Choisir les ID d'objet",
    "Выбирать ID предметов")
add("picktweak.config.idNbtBlacklist", "NBT 黑名单", "NBT 黑名單", "NBT blacklist", "Liste noire NBT", "Чёрный список NBT")
add("picktweak.config.idStatePropertyBlacklist", "状态属性黑名单", "狀態屬性黑名單", "State property blacklist",
    "Liste noire des propriétés d'état", "Чёрный список свойств состояния")

# ---------------- 工具拾取 ----------------
add("picktweak.config.toolPickerEnabled", "启用工具拾取", "啟用工具拾取", "Enable the tool picker",
    "Activer le sélecteur d'outils", "Включить выбор инструментов")
add("picktweak.config.toolPickerRange", "拾取范围（生存）", "拾取範圍（生存）", "Pick range (survival)",
    "Portée de sélection (survie)", "Дальность выбора (выживание)")
add("picktweak.config.toolPickerCreativeRange", "拾取范围（创造）", "拾取範圍（創造）", "Pick range (creative)",
    "Portée de sélection (créatif)", "Дальность выбора (творческий)")
add("picktweak.config.toolPickerDurabilityThreshold", "最低剩余耐久", "最低剩餘耐久", "Minimum remaining durability",
    "Durabilité restante minimale", "Минимальная остаточная прочность")
add("picktweak.config.toolPickerPickFluid", "流体用桶", "流體用桶", "Use a bucket for fluids", "Choisir un seau pour les fluides",
    "Использовать ведро для жидкостей")
add("picktweak.config.toolPickerBambooPrefersSword", "竹子优先用剑", "竹子優先用劍", "Prefer a sword for bamboo",
    "Préférer l'épée pour le bambou", "Предпочитать меч для бамбука")
add("picktweak.config.toolPickerPreferSilkTouch", "优先精准采集", "優先精準採集", "Prefer silk touch",
    "Préférer toucher de soie", "Предпочитать «Тонкое касание»")
add("picktweak.config.toolPickerPreferFortune", "优先时运", "優先時運", "Prefer fortune", "Préférer fortune",
    "Предпочитать «Удачу»")
add("picktweak.config.toolPickerPreferEfficiency", "优先效率", "優先效率", "Prefer efficiency", "Préférer efficacité",
    "Предпочитать «Эффективность»")
add("picktweak.config.toolPickerAllowIncompatible", "允许不兼容附魔", "允許不相容附魔", "Allow incompatible enchantments",
    "Autoriser les enchantements incompatibles", "Разрешить несовместимые чары")
add("picktweak.config.toolPickerGenerateInCreative", "创造模式生成工具", "創造模式生成工具", "Generate tools in creative mode",
    "Générer des outils en mode créatif", "Создавать инструменты в творческом режиме")
add("picktweak.config.toolSpecs", "工具预设", "工具預設", "Tool presets", "Préréglages d'outils", "Пресеты инструментов")
add("picktweak.config.weaponPreferences", "武器偏好", "武器偏好", "Weapon preferences", "Préférences d'arme", "Предпочтения оружия")

# ---------------- 背包 ----------------
add("picktweak.config.searchContainers", "搜索潜影盒 / 收纳袋", "搜尋潛影盒 / 收納袋", "Search shulker boxes and bundles",
    "Chercher dans les boîtes de shulkers et les sacs", "Искать в шалкерах и связках")
add("picktweak.config.keepOriginalSlot", "保持在原槽位", "保持在原槽位", "Keep the original slot", "Garder la case d'origine",
    "Оставаться в исходном слоте")
for _i in range(1, 10):
    add(f"picktweak.config.lockHotbarSlot{_i}", f"锁定快捷栏 {_i}", f"鎖定快捷欄 {_i}", f"Lock hotbar slot {_i}",
        f"Verrouiller la case {_i}", f"Закрепить слот {_i}")

# ---------------- 创造模式 ----------------
add("picktweak.config.creativeInjectTools", "注入工具到创造栏", "注入工具到創造欄", "Inject tools into the creative tab",
    "Injecter les outils dans l'onglet créatif", "Добавлять инструменты в творческую вкладку")
add("picktweak.config.creativeInjectWeapons", "注入武器到创造栏", "注入武器到創造欄", "Inject weapons into the creative tab",
    "Injecter les armes dans l'onglet créatif", "Добавлять оружие в творческую вкладку")
add("picktweak.config.creativeTab", "注入到哪个标签页", "注入到哪個標籤頁", "The tab to inject into",
    "L'onglet d'injection", "Вкладка для добавления")

# ---------------- 热键 ----------------
add("picktweak.config.hotkeyPickId", "拾取目标 ID", "拾取目標 ID", "Pick the target ID", "Récupérer l'ID de la cible",
    "Взять ID цели")
add("picktweak.config.hotkeyPickTool", "拾取工具 / 武器", "拾取工具 / 武器", "Pick the tool or weapon",
    "Récupérer l'outil ou l'arme", "Взять инструмент или оружие")
add("picktweak.config.hotkeyOpenConfig", "打开配置界面", "打開設定介面", "Open the config GUI",
    "Ouvrir la fenêtre de configuration", "Открыть окно настроек")
add("picktweak.config.hotkeyPickWithNbt", "拾取时附加 NBT（并复制到剪贴板）", "拾取時附加 NBT（並複製到剪貼簿）",
    "Pick with NBT (and copy it to the clipboard)",
    "Sélectionner avec le NBT (et le copier dans le presse-papiers)",
    "Выбрать с NBT (и скопировать в буфер обмена)")
add("picktweak.config.hotkeyPickWithState", "拾取时附加方块状态属性", "拾取時附加方塊狀態屬性",
    "Pick with the block state properties",
    "Sélectionner avec les propriétés d'état du bloc",
    "Выбрать со свойствами состояния блока")
for _i in range(1, 10):
    add(f"picktweak.config.hotkeyChatSlot{_i}", f"聊天框内写第 {_i} 格物品 ID", f"聊天框內寫第 {_i} 格物品 ID",
        f"Write the ID of hotbar slot {_i} in the chat box",
        f"Écrire l'ID de la case {_i} dans le chat",
        f"Записать ID ячейки {_i} в чат")
add("picktweak.config.hotkeyChatSlot0", "聊天框内写第 10 格物品 ID", "聊天框內寫第 10 格物品 ID",
    "Write the ID of hotbar slot 10 in the chat box",
    "Écrire l'ID de la case 10 dans le chat",
    "Записать ID ячейки 10 в чат")
add("picktweak.config.axiomMode.yield", "让位（Axiom 的功能优先）", "讓位（Axiom 的功能優先）",
    "Yield (Axiom's features come first)", "Céder (les fonctions d'Axiom d'abord)",
    "Уступить (сначала функции Axiom)")
add("picktweak.config.axiomMode.override", "覆盖（Pick Tweak 优先）", "覆蓋（Pick Tweak 優先）",
    "Override (Pick Tweak comes first)", "Écraser (Pick Tweak d'abord)", "Перекрыть (сначала Pick Tweak)")
add("key.picktweak.id", "拾取 ID", "拾取 ID", "Pick ID", "Récupérer l'ID", "Взять ID")
add("key.picktweak.tool", "拾取工具", "拾取工具", "Pick Tool", "Récupérer l'outil", "Взять инструмент")

add("picktweak.gui.title.configs", "Pick Tweak 设置 - %s", "Pick Tweak 設定 - %s", "Pick Tweak Configs - %s",
    "Configs de Pick Tweak - %s", "Настройки Pick Tweak - %s")
add("picktweak.gui.title.block_picker", "区块拾取", "區塊拾取", "Block Picker", "Sélecteur de blocs", "Выбор блоков")
add("picktweak.gui.title.id_picker", "ID 拾取", "ID 拾取", "ID Picker", "Sélecteur d'ID", "Выбор ID")
add("picktweak.gui.title.tool_picker", "工具拾取", "工具拾取", "Tool Picker", "Sélecteur d'outils", "Выбор инструментов")
add("picktweak.gui.title.inventory", "背包", "背包", "Inventory", "Inventaire", "Инвентарь")
add("picktweak.gui.title.creative", "创造模式", "創造模式", "Creative", "Mode créatif", "Творческий режим")
add("picktweak.gui.title.hotkeys", "热键", "熱鍵", "Hotkeys", "Raccourcis", "Горячие клавиши")
# ---------------- 消息 ----------------
add("picktweak.message.no_tool", "该方块没有对应的工具。", "該方塊沒有對應的工具。", "No matching tool for this block.",
    "Aucun outil adapté à ce bloc.", "Нет подходящего инструмента для этого блока.")
add("picktweak.message.no_tool_in_inventory", "背包里没有可用的工具。", "背包裡沒有可用的工具。", "No usable tool in your inventory.",
    "Aucun outil utilisable dans l'inventaire.", "В инвентаре нет подходящего инструмента.")
add("picktweak.message.no_weapon_in_inventory", "背包里没有可用的武器。", "背包裡沒有可用的武器。", "No usable weapon in your inventory.",
    "Aucune arme utilisable dans l'inventaire.", "В инвентаре нет подходящего оружия.")
add("picktweak.message.picked", "PBP：已拾取 %s", "PBP：已拾取 %s", "PBP: picked %s", "PBP : %s récupéré", "PBP: получено %s")
add("picktweak.message.generated", "已生成 %s", "已生成 %s", "Generated %s", "%s généré", "Создано: %s")
add("picktweak.message.generate_failed", "生成失败，请检查配置。", "生成失敗，請檢查設定。", "Generation failed, check your config.",
    "Échec de la génération, vérifiez la configuration.", "Не удалось создать, проверьте настройки.")
add("picktweak.message.server_mod_missing", "服务端未安装 Pick Tweak，已改用客户端本地拾取。",
    "伺服器未安裝 Pick Tweak，已改用用戶端本地拾取。",
    "The server has no Pick Tweak server component; falling back to the client-side pick.",
    "Le serveur ne contient pas le composant serveur de Pick Tweak ; retour à la sélection côté client.",
    "На сервере нет серверной части Pick Tweak — возврат к выбору на клиенте.")
add("picktweak.message.give_denied", "服务端禁止了工具发放请求。", "伺服器禁止了工具發放請求。", "The server denied the tool request.",
    "Le serveur a refusé la demande d'outil.", "Сервер отклонил запрос на выдачу инструмента.")
add("picktweak.message.nbt_copied", "已把 %s 的 NBT 复制到剪贴板。", "已把 %s 的 NBT 複製到剪貼簿。", "Copied the NBT of %s to the clipboard.",
    "NBT de %s copié dans le presse-papiers.", "NBT сущности %s скопирован в буфер обмена.")
add("picktweak.message.no_nbt", "%s 没有可复制的 NBT。", "%s 沒有可複製的 NBT。", "%s has no NBT to copy.",
    "%s n'a aucun NBT à copier.", "У %s нет NBT для копирования.")

# ---------------- 命令 ----------------
add("picktweak.command.reload_success", "已重新载入自定义映射。", "已重新載入自訂映射。", "Custom mappings reloaded.",
    "Mappings personnalisés rechargés.", "Пользовательские сопоставления перезагружены.")
add("picktweak.command.not_in_world", "当前不在世界中。", "目前不在世界中。", "You are not in a world.", "Vous n'êtes pas dans un monde.",
    "Вы не в мире.")
add("picktweak.command.unknown_type", "未知的类型：%s", "未知的類型：%s", "Unknown type: %s", "Type inconnu : %s", "Неизвестный тип: %s")
add("picktweak.command.need_creative", "该命令仅在创造模式可用。", "該命令僅在創造模式可用。", "This command only works in creative mode.",
    "Cette commande ne fonctionne qu'en mode créatif.", "Эта команда работает только в творческом режиме.")
add("picktweak.command.generate_failed", "生成 %s 失败，请检查配置。", "生成 %s 失敗，請檢查設定。", "Failed to generate %s, check your config.",
    "Échec de la génération de %s, vérifiez la configuration.", "Не удалось создать %s, проверьте настройки.")
add("picktweak.command.given", "已给予 %s", "已給予 %s", "Gave you %s", "Vous avez reçu %s", "Вы получили %s")

LANGS = ["zh_cn", "zh_tw", "en_us", "fr_fr", "ru_ru"]

for lang in LANGS:
    data = collections.OrderedDict()
    for key, values in T.items():
        data[key] = values[lang]
    path = os.path.join(BASE, f"{lang}.json")
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(f"wrote {path} ({len(data)} keys)")
