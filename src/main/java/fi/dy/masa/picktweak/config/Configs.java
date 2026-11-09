package fi.dy.masa.picktweak.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.picktweak.Reference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pick Tweak 的全部配置。
 *
 * <p>配置项本身是 MaLiLib 的 {@code ConfigXxx}（面板里直接可编辑、可用 MaLiLib 搜索），
 * 同时这里维护一份运行时快照，业务代码读快照，避免到处写 {@code getBooleanValue()}。</p>
 */
public final class Configs {

	// ==================== 区块拾取 ====================

	public static final ConfigBoolean BLOCK_PICKER_ENABLED = bool("blockPickerEnabled", true, "Whether the block picker is enabled");
	public static final ConfigDouble BLOCK_PICKER_RANGE = dbl("blockPickerRange", 6.0D, 1.0D, 256.0D, "Block pick range in survival mode");
	public static final ConfigDouble BLOCK_PICKER_ENTITY_RANGE = dbl("blockPickerEntityRange", 6.0D, 1.0D, 256.0D, "Entity pick range in survival mode");
	public static final ConfigDouble BLOCK_PICKER_CREATIVE_RANGE = dbl("blockPickerCreativeRange", 100.0D, 1.0D, 256.0D, "Block pick range in creative mode");
	public static final ConfigBoolean BLOCK_PICKER_USE_INTERACTION_RANGE = bool("blockPickerUseInteractionRange", false, "Use the player's own interaction range instead of the values above");
	public static final ConfigBoolean BLOCK_PICKER_PICK_BLOCKS = bool("blockPickerPickBlocks", true, "Allow picking blocks");
	public static final ConfigBoolean BLOCK_PICKER_PICK_ENTITIES = bool("blockPickerPickEntities", true, "Allow picking entities");
	public static final ConfigBoolean BLOCK_PICKER_PICK_FLUIDS = bool("blockPickerPickFluids", true, "Pick fluids as the matching bucket");
	public static final ConfigBoolean BLOCK_PICKER_PICK_LIGHT_SOURCES = bool("blockPickerPickLightSources", true, "Allow picking the sun and the moon");
	public static final ConfigBoolean BLOCK_PICKER_PREFER_ENTITY_HEADS = bool("blockPickerPreferEntityHeads", true, "Prefer mob heads (zombie -> zombie head) over the vanilla spawn egg");
	public static final ConfigBoolean BLOCK_PICKER_TAKE_OVER_LITEMATICA = bool("blockPickerTakeOverLitematica", true, "Take over Litematica's pick block hotkeys");
	public static final ConfigBoolean BLOCK_PICKER_SHOW_FEEDBACK = bool("blockPickerShowFeedback", false, "Show a chat message about what was picked");
	public static final ConfigBoolean BLOCK_PICKER_COPY_NBT = bool("blockPickerCopyNbt", true, "Ctrl + middle click copies the NBT of the target to the clipboard");
	public static final ConfigBoolean BLOCK_PICKER_SHOW_COPIED_NBT = bool("blockPickerShowCopiedNbt", true, "Print the copied NBT in the chat box as well");
	public static final ConfigStringList BLOCK_ENTITY_NBT_BLACKLIST = strList("blockEntityNbtBlacklist", ImmutableList.of("id"), "Block entity NBT keys to hide (paths, e.g. id or Items.id)");
	public static final ConfigStringList STATE_PROPERTY_BLACKLIST = strList("statePropertyBlacklist", ImmutableList.of("waterlogged"), "Block state properties to hide");
	public static final ConfigStringList ENTITY_NBT_BLACKLIST = strList("entityNbtBlacklist", ImmutableList.of("UUID", "Motion", "Rotation", "Pos", "FallDistance", "fall_distance"), "Entity NBT keys to hide (paths, e.g. UUID or equipment.head.id)");

	// ==================== ID 拾取 ====================

	public static final ConfigBoolean ID_PICKER_ENABLED = bool("idPickerEnabled", true, "Whether the ID picker is enabled");
	public static final ConfigDouble ID_PICKER_RANGE = dbl("idPickerRange", 6.0D, 1.0D, 256.0D, "ID pick range in survival mode");
	public static final ConfigDouble ID_PICKER_ENTITY_RANGE = dbl("idPickerEntityRange", 6.0D, 1.0D, 256.0D, "Entity ID pick range in survival mode");
	public static final ConfigDouble ID_PICKER_CREATIVE_RANGE = dbl("idPickerCreativeRange", 12.0D, 1.0D, 256.0D, "ID pick range in creative mode");
	public static final ConfigBoolean ID_PICKER_USE_INTERACTION_RANGE = bool("idPickerUseInteractionRange", false, "Use the player's own interaction range instead of the values above");
	public static final ConfigBoolean ID_PICKER_INCLUDE_NAMESPACE = bool("idPickerIncludeNamespace", true, "Include the namespace in the output (minecraft:stone)");
	public static final ConfigBoolean ID_PICKER_INCLUDE_STATE_PROPERTIES = bool("idPickerIncludeStateProperties", true, "Include the block state properties in the output");
	public static final ConfigBoolean ID_PICKER_COPY_TO_CLIPBOARD = bool("idPickerCopyToClipboard", true, "Copy the output to the clipboard as well");
	public static final ConfigBoolean ID_PICKER_INCLUDE_NBT = bool("idPickerIncludeNbt", false, "Output NBT instead of just the ID (can be toggled with Ctrl)");
	public static final ConfigBoolean ID_PICKER_PRETTY_NBT = bool("idPickerPrettyNbt", false, "Pretty print the NBT output");
	public static final ConfigBoolean ID_PICKER_CHAT_SHORTCUT = bool("idPickerChatShortcut", true, "Ctrl + number key in the chat box writes the hotbar item ID");
	public static final ConfigBoolean ID_PICKER_ITEM_TO_BLOCK_ID = bool("idPickerItemToBlockId", true, "Convert item IDs into the matching block IDs");
	public static final ConfigBoolean ID_PICKER_PICK_BLOCKS = bool("idPickerPickBlocks", true, "Allow ID picking for blocks");
	public static final ConfigBoolean ID_PICKER_PICK_ENTITIES = bool("idPickerPickEntities", true, "Allow ID picking for entities");
	public static final ConfigBoolean ID_PICKER_PICK_ITEMS = bool("idPickerPickItems", true, "Allow ID picking for items");
	public static final ConfigStringList ID_NBT_BLACKLIST = strList("idNbtBlacklist", ImmutableList.of("UUID", "Motion", "Rotation", "Pos", "FallDistance"), "NBT keys to hide");
	public static final ConfigStringList ID_STATE_PROPERTY_BLACKLIST = strList("idStatePropertyBlacklist", ImmutableList.of("waterlogged"), "Block state properties to hide");

	// ==================== 工具拾取 ====================

	public static final ConfigBoolean TOOL_PICKER_ENABLED = bool("toolPickerEnabled", true, "Whether the tool picker is enabled");
	public static final ConfigDouble TOOL_PICKER_RANGE = dbl("toolPickerRange", 6.0D, 1.0D, 256.0D, "Tool pick range in survival mode");
	public static final ConfigDouble TOOL_PICKER_CREATIVE_RANGE = dbl("toolPickerCreativeRange", 100.0D, 1.0D, 256.0D, "Tool pick range in creative mode");
	public static final ConfigInteger TOOL_PICKER_DURABILITY_THRESHOLD = integer("toolPickerDurabilityThreshold", 10, 0, 1000, "Tools with less remaining durability are skipped");
	public static final ConfigBoolean TOOL_PICKER_PICK_FLUID = bool("toolPickerPickFluid", true, "Use a bucket for fluids and powder snow");
	public static final ConfigBoolean TOOL_PICKER_BAMBOO_PREFERS_SWORD = bool("toolPickerBambooPrefersSword", true, "Prefer a sword for bamboo");
	public static final ConfigBoolean TOOL_PICKER_PREFER_SILK_TOUCH = bool("toolPickerPreferSilkTouch", false, "Prefer silk touch");
	public static final ConfigBoolean TOOL_PICKER_PREFER_FORTUNE = bool("toolPickerPreferFortune", true, "Prefer fortune");
	public static final ConfigBoolean TOOL_PICKER_PREFER_EFFICIENCY = bool("toolPickerPreferEfficiency", true, "Prefer efficiency");
	public static final ConfigBoolean TOOL_PICKER_ALLOW_INCOMPATIBLE = bool("toolPickerAllowIncompatible", false, "Allow incompatible enchantments (generated up to level 255)");
	public static final ConfigBoolean TOOL_PICKER_GENERATE_IN_CREATIVE = bool("toolPickerGenerateInCreative", true, "Generate a fully enchanted tool in creative mode");
	public static final ConfigStringList TOOL_SPECS = strList("toolSpecs", ImmutableList.of(
			"pickaxe|minecraft:diamond_pickaxe|minecraft:efficiency=5;minecraft:silk_touch=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:fortune=5",
			"axe|minecraft:diamond_axe|minecraft:efficiency=5;minecraft:silk_touch=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:fortune=5",
			"shovel|minecraft:diamond_shovel|minecraft:efficiency=5;minecraft:silk_touch=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:fortune=5",
			"hoe|minecraft:diamond_hoe|minecraft:efficiency=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:fortune=5",
			"sword|minecraft:diamond_sword|minecraft:sharpness=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:looting=5;minecraft:fire_aspect=5",
			"shears|minecraft:shears|minecraft:unbreaking=5;minecraft:mending=5",
			"bucket|minecraft:bucket|minecraft:unbreaking=5;minecraft:mending=5",
			"fishing_rod|minecraft:fishing_rod|minecraft:unbreaking=5;minecraft:mending=5;minecraft:luck_of_the_sea=5",
			"brush|minecraft:brush|minecraft:unbreaking=5;minecraft:mending=5",
			"bow|minecraft:bow|minecraft:power=5;minecraft:punch=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:infinity=1",
			"crossbow|minecraft:crossbow|minecraft:power=5;minecraft:punch=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:quick_charge=5",
			"trident|minecraft:trident|minecraft:sharpness=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:riptide=5;minecraft:impaling=5",
			"mace|minecraft:mace|minecraft:smite=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:breach=5"
	), "Tool and weapon presets: kind|itemId|enchantment=level;enchantment=level");
	public static final ConfigStringList WEAPON_PREFERENCES = strList("weaponPreferences", ImmutableList.of(),
			"Preferred weapon per entity type: entityId=bow|trident|sword|axe|mace");

	// ==================== 背包 ====================

	public static final ConfigBoolean SEARCH_CONTAINERS = bool("searchContainers", true, "Search shulker boxes and bundles for tools");
	public static final ConfigBoolean KEEP_ORIGINAL_SLOT = bool("keepOriginalSlot", false, "Return to the original hotbar slot after switching");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_1 = bool("lockHotbarSlot1", false, "Lock hotbar slot 1");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_2 = bool("lockHotbarSlot2", false, "Lock hotbar slot 2");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_3 = bool("lockHotbarSlot3", false, "Lock hotbar slot 3");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_4 = bool("lockHotbarSlot4", false, "Lock hotbar slot 4");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_5 = bool("lockHotbarSlot5", false, "Lock hotbar slot 5");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_6 = bool("lockHotbarSlot6", false, "Lock hotbar slot 6");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_7 = bool("lockHotbarSlot7", false, "Lock hotbar slot 7");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_8 = bool("lockHotbarSlot8", false, "Lock hotbar slot 8");
	public static final ConfigBoolean LOCK_HOTBAR_SLOT_9 = bool("lockHotbarSlot9", false, "Lock hotbar slot 9");

	// ==================== 创造模式 ====================

	public static final ConfigBoolean CREATIVE_INJECT_TOOLS = bool("creativeInjectTools", true, "Inject the configured tools into the creative inventory");
	public static final ConfigBoolean CREATIVE_INJECT_WEAPONS = bool("creativeInjectWeapons", true, "Inject the configured weapons into the creative inventory");
	public static final fi.dy.masa.malilib.config.options.ConfigString CREATIVE_TAB = str("creativeTab", "minecraft:op_blocks", "The creative inventory tab to inject the items into");

	/** 面板里显示的全部配置项。 */
	public static final List<IConfigBase> ALL_CONFIGS = List.of(
			BLOCK_PICKER_ENABLED, BLOCK_PICKER_RANGE, BLOCK_PICKER_ENTITY_RANGE, BLOCK_PICKER_CREATIVE_RANGE,
			BLOCK_PICKER_USE_INTERACTION_RANGE, BLOCK_PICKER_PICK_BLOCKS, BLOCK_PICKER_PICK_ENTITIES,
			BLOCK_PICKER_PICK_FLUIDS, BLOCK_PICKER_PICK_LIGHT_SOURCES, BLOCK_PICKER_PREFER_ENTITY_HEADS,
			BLOCK_PICKER_TAKE_OVER_LITEMATICA, BLOCK_PICKER_SHOW_FEEDBACK,
			BLOCK_PICKER_COPY_NBT, BLOCK_PICKER_SHOW_COPIED_NBT,
			BLOCK_ENTITY_NBT_BLACKLIST, STATE_PROPERTY_BLACKLIST, ENTITY_NBT_BLACKLIST,

			ID_PICKER_ENABLED, ID_PICKER_RANGE, ID_PICKER_ENTITY_RANGE, ID_PICKER_CREATIVE_RANGE,
			ID_PICKER_USE_INTERACTION_RANGE, ID_PICKER_INCLUDE_NAMESPACE, ID_PICKER_INCLUDE_STATE_PROPERTIES,
			ID_PICKER_COPY_TO_CLIPBOARD, ID_PICKER_INCLUDE_NBT, ID_PICKER_PRETTY_NBT, ID_PICKER_CHAT_SHORTCUT,
			ID_PICKER_ITEM_TO_BLOCK_ID, ID_PICKER_PICK_BLOCKS, ID_PICKER_PICK_ENTITIES, ID_PICKER_PICK_ITEMS,
			ID_NBT_BLACKLIST, ID_STATE_PROPERTY_BLACKLIST,

			TOOL_PICKER_ENABLED, TOOL_PICKER_RANGE, TOOL_PICKER_CREATIVE_RANGE, TOOL_PICKER_DURABILITY_THRESHOLD,
			TOOL_PICKER_PICK_FLUID, TOOL_PICKER_BAMBOO_PREFERS_SWORD, TOOL_PICKER_PREFER_SILK_TOUCH,
			TOOL_PICKER_PREFER_FORTUNE, TOOL_PICKER_PREFER_EFFICIENCY, TOOL_PICKER_ALLOW_INCOMPATIBLE,
			TOOL_PICKER_GENERATE_IN_CREATIVE, TOOL_SPECS, WEAPON_PREFERENCES,

			SEARCH_CONTAINERS, KEEP_ORIGINAL_SLOT,
			LOCK_HOTBAR_SLOT_1, LOCK_HOTBAR_SLOT_2, LOCK_HOTBAR_SLOT_3, LOCK_HOTBAR_SLOT_4, LOCK_HOTBAR_SLOT_5,
			LOCK_HOTBAR_SLOT_6, LOCK_HOTBAR_SLOT_7, LOCK_HOTBAR_SLOT_8, LOCK_HOTBAR_SLOT_9,

			CREATIVE_INJECT_TOOLS, CREATIVE_INJECT_WEAPONS, CREATIVE_TAB
	);

	// ==================== 运行时快照 ====================

	public static final BlockPicker blockPicker = new BlockPicker();
	public static final IdPicker idPicker = new IdPicker();
	public static final ToolPicker toolPicker = new ToolPicker();
	public static final Inventory inventory = new Inventory();
	public static final Creative creative = new Creative();

	public static final class BlockPicker {
		public boolean enabled;
		public double range;
		public double entityRange;
		public double creativeRange;
		public boolean useInteractionRange;
		public boolean pickBlocks;
		public boolean pickEntities;
		public boolean pickFluids;
		public boolean pickLightSources;
		public boolean preferEntityHeads;
		public boolean takeOverLitematica;
		public boolean showPickFeedback;
		public boolean copyNbt;
		public boolean showCopiedNbt;
		public List<String> blockEntityNbtBlacklist = new ArrayList<>();
		public List<String> statePropertyBlacklist = new ArrayList<>();
		public List<String> entityNbtBlacklist = new ArrayList<>();
	}

	public static final class IdPicker {
		public boolean enabled;
		public double range;
		public double entityRange;
		public double creativeRange;
		public boolean useInteractionRange;
		public boolean includeNamespace;
		public boolean includeStateProperties;
		public boolean copyToClipboard;
		public boolean includeNbt;
		public boolean prettyNbt;
		public boolean chatShortcut;
		public boolean itemToBlockId;
		public boolean pickBlocks;
		public boolean pickEntities;
		public boolean pickItems;
		public List<String> nbtBlacklist = new ArrayList<>();
		public List<String> statePropertyBlacklist = new ArrayList<>();
	}

	public static final class ToolPicker {
		public boolean enabled;
		public double range;
		public double creativeRange;
		public int durabilityThreshold;
		public boolean pickFluid;
		public boolean bambooPrefersSword;
		public boolean preferSilkTouch;
		public boolean preferFortune;
		public boolean preferEfficiency;
		public boolean allowIncompatible;
		public boolean generateInCreative;
		public Map<String, String> weaponPreference = new LinkedHashMap<>();
		public Map<String, ToolSpec> tools = new LinkedHashMap<>();
		public Map<String, ToolSpec> weapons = new LinkedHashMap<>();
	}

	/** 单件工具 / 武器的预设。 */
	public static final class ToolSpec {
		public String item = "minecraft:diamond_pickaxe";
		public boolean enabled = true;
		public LinkedHashMap<String, Integer> enchantments = new LinkedHashMap<>();

		public ToolSpec() {
		}

		public ToolSpec(String item) {
			this.item = item;
		}
	}

	public static final class Inventory {
		public boolean searchContainers;
		public boolean keepOriginalSlot;
		public boolean[] lockedSlots = new boolean[9];
	}

	public static final class Creative {
		public boolean injectTools;
		public boolean injectWeapons;
		public String injectTab = "minecraft:op_blocks";
	}

	private Configs() {
	}

	/** 把 MaLiLib 的配置值同步到运行时快照。 */
	public static void refresh() {
		BlockPicker bp = blockPicker;
		bp.enabled = BLOCK_PICKER_ENABLED.getBooleanValue();
		bp.range = BLOCK_PICKER_RANGE.getDoubleValue();
		bp.entityRange = BLOCK_PICKER_ENTITY_RANGE.getDoubleValue();
		bp.creativeRange = BLOCK_PICKER_CREATIVE_RANGE.getDoubleValue();
		bp.useInteractionRange = BLOCK_PICKER_USE_INTERACTION_RANGE.getBooleanValue();
		bp.pickBlocks = BLOCK_PICKER_PICK_BLOCKS.getBooleanValue();
		bp.pickEntities = BLOCK_PICKER_PICK_ENTITIES.getBooleanValue();
		bp.pickFluids = BLOCK_PICKER_PICK_FLUIDS.getBooleanValue();
		bp.pickLightSources = BLOCK_PICKER_PICK_LIGHT_SOURCES.getBooleanValue();
		bp.preferEntityHeads = BLOCK_PICKER_PREFER_ENTITY_HEADS.getBooleanValue();
		bp.takeOverLitematica = BLOCK_PICKER_TAKE_OVER_LITEMATICA.getBooleanValue();
		bp.showPickFeedback = BLOCK_PICKER_SHOW_FEEDBACK.getBooleanValue();
		bp.copyNbt = BLOCK_PICKER_COPY_NBT.getBooleanValue();
		bp.showCopiedNbt = BLOCK_PICKER_SHOW_COPIED_NBT.getBooleanValue();
		bp.blockEntityNbtBlacklist = new ArrayList<>(BLOCK_ENTITY_NBT_BLACKLIST.getStrings());
		bp.statePropertyBlacklist = new ArrayList<>(STATE_PROPERTY_BLACKLIST.getStrings());
		bp.entityNbtBlacklist = new ArrayList<>(ENTITY_NBT_BLACKLIST.getStrings());

		IdPicker ip = idPicker;
		ip.enabled = ID_PICKER_ENABLED.getBooleanValue();
		ip.range = ID_PICKER_RANGE.getDoubleValue();
		ip.entityRange = ID_PICKER_ENTITY_RANGE.getDoubleValue();
		ip.creativeRange = ID_PICKER_CREATIVE_RANGE.getDoubleValue();
		ip.useInteractionRange = ID_PICKER_USE_INTERACTION_RANGE.getBooleanValue();
		ip.includeNamespace = ID_PICKER_INCLUDE_NAMESPACE.getBooleanValue();
		ip.includeStateProperties = ID_PICKER_INCLUDE_STATE_PROPERTIES.getBooleanValue();
		ip.copyToClipboard = ID_PICKER_COPY_TO_CLIPBOARD.getBooleanValue();
		ip.includeNbt = ID_PICKER_INCLUDE_NBT.getBooleanValue();
		ip.prettyNbt = ID_PICKER_PRETTY_NBT.getBooleanValue();
		ip.chatShortcut = ID_PICKER_CHAT_SHORTCUT.getBooleanValue();
		ip.itemToBlockId = ID_PICKER_ITEM_TO_BLOCK_ID.getBooleanValue();
		ip.pickBlocks = ID_PICKER_PICK_BLOCKS.getBooleanValue();
		ip.pickEntities = ID_PICKER_PICK_ENTITIES.getBooleanValue();
		ip.pickItems = ID_PICKER_PICK_ITEMS.getBooleanValue();
		ip.nbtBlacklist = new ArrayList<>(ID_NBT_BLACKLIST.getStrings());
		ip.statePropertyBlacklist = new ArrayList<>(ID_STATE_PROPERTY_BLACKLIST.getStrings());

		ToolPicker tp = toolPicker;
		tp.enabled = TOOL_PICKER_ENABLED.getBooleanValue();
		tp.range = TOOL_PICKER_RANGE.getDoubleValue();
		tp.creativeRange = TOOL_PICKER_CREATIVE_RANGE.getDoubleValue();
		tp.durabilityThreshold = TOOL_PICKER_DURABILITY_THRESHOLD.getIntegerValue();
		tp.pickFluid = TOOL_PICKER_PICK_FLUID.getBooleanValue();
		tp.bambooPrefersSword = TOOL_PICKER_BAMBOO_PREFERS_SWORD.getBooleanValue();
		tp.preferSilkTouch = TOOL_PICKER_PREFER_SILK_TOUCH.getBooleanValue();
		tp.preferFortune = TOOL_PICKER_PREFER_FORTUNE.getBooleanValue();
		tp.preferEfficiency = TOOL_PICKER_PREFER_EFFICIENCY.getBooleanValue();
		tp.allowIncompatible = TOOL_PICKER_ALLOW_INCOMPATIBLE.getBooleanValue();
		tp.generateInCreative = TOOL_PICKER_GENERATE_IN_CREATIVE.getBooleanValue();
		tp.weaponPreference = parseWeaponPreferences();
		tp.tools = new LinkedHashMap<>();
		tp.weapons = new LinkedHashMap<>();
		parseToolSpecs();

		inventory.searchContainers = SEARCH_CONTAINERS.getBooleanValue();
		inventory.keepOriginalSlot = KEEP_ORIGINAL_SLOT.getBooleanValue();
		inventory.lockedSlots = new boolean[]{
				LOCK_HOTBAR_SLOT_1.getBooleanValue(), LOCK_HOTBAR_SLOT_2.getBooleanValue(),
				LOCK_HOTBAR_SLOT_3.getBooleanValue(), LOCK_HOTBAR_SLOT_4.getBooleanValue(),
				LOCK_HOTBAR_SLOT_5.getBooleanValue(), LOCK_HOTBAR_SLOT_6.getBooleanValue(),
				LOCK_HOTBAR_SLOT_7.getBooleanValue(), LOCK_HOTBAR_SLOT_8.getBooleanValue(),
				LOCK_HOTBAR_SLOT_9.getBooleanValue()
		};

		creative.injectTools = CREATIVE_INJECT_TOOLS.getBooleanValue();
		creative.injectWeapons = CREATIVE_INJECT_WEAPONS.getBooleanValue();
		creative.injectTab = CREATIVE_TAB.getStringValue();
	}

	private static Map<String, String> parseWeaponPreferences() {
		Map<String, String> map = new LinkedHashMap<>();
		for (String entry : WEAPON_PREFERENCES.getStrings()) {
			int idx = entry.indexOf('=');
			if (idx > 0) {
				map.put(entry.substring(0, idx).trim().toLowerCase(Locale.ROOT), entry.substring(idx + 1).trim().toLowerCase(Locale.ROOT));
			}
		}
		return map;
	}

	/** 解析工具预设：kind|itemId|enchant=level;enchant=level */
	private static void parseToolSpecs() {
		for (String entry : TOOL_SPECS.getStrings()) {
			String[] parts = entry.split("\\|");
			if (parts.length < 2) {
				continue;
			}
			String kind = parts[0].trim().toLowerCase(Locale.ROOT);
			ToolSpec spec = new ToolSpec(parts[1].trim());
			if (parts.length > 2 && !parts[2].isBlank()) {
				for (String enchant : parts[2].split(";")) {
					int idx = enchant.indexOf('=');
					if (idx <= 0) {
						continue;
					}
					try {
						spec.enchantments.put(enchant.substring(0, idx).trim(), Integer.parseInt(enchant.substring(idx + 1).trim()));
					} catch (NumberFormatException ignored) {
						// 忽略无法解析的等级
					}
				}
			}
			if (isWeaponKind(kind)) {
				toolPicker.weapons.put(kind, spec);
			} else {
				toolPicker.tools.put(kind, spec);
			}
		}
	}

	private static boolean isWeaponKind(String kind) {
		return switch (kind) {
			case "bow", "crossbow", "trident", "mace" -> true;
			default -> false;
		};
	}

	// ==================== 构造辅助 ====================

	private static ConfigBoolean bool(String name, boolean def, String comment) {
		ConfigBoolean option = new ConfigBoolean(name, def, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static ConfigDouble dbl(String name, double def, double min, double max, String comment) {
		ConfigDouble option = new ConfigDouble(name, def, min, max, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static ConfigInteger integer(String name, int def, int min, int max, String comment) {
		ConfigInteger option = new ConfigInteger(name, def, min, max, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static fi.dy.masa.malilib.config.options.ConfigString str(String name, String def, String comment) {
		fi.dy.masa.malilib.config.options.ConfigString option = new fi.dy.masa.malilib.config.options.ConfigString(name, def, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static ConfigStringList strList(String name, ImmutableList<String> def, String comment) {
		ConfigStringList option = new ConfigStringList(name, def, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}
}
