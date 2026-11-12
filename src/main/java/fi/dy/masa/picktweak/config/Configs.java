package fi.dy.masa.picktweak.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pick Tweak 的运行时配置快照（<b>不依赖 MaLiLib</b>）。
 *
 * <p>配置项本身在 {@link MaliOptions}（客户端配置面板），这里保存业务代码读取的普通字段，
 * 同时定义全部默认值。这样两边的边界很清楚：</p>
 *
 * <ul>
 *   <li>服务端：只读本类的默认值 / 自己的 {@code PbpServerConfig}，永远不加载 MaLiLib；</li>
 *   <li>装了 MaLiLib 的客户端：{@link MaliOptions#refresh()} 把面板里的值同步进本类；</li>
 *   <li>没装 MaLiLib 的客户端：{@link #installDefaults()} 用默认值工作。</li>
 * </ul>
 */
public final class Configs {

	// ==================== 默认值（MaLiLib 配置项的默认值也取这里，避免两处维护） ====================

	// 区块拾取
	public static final boolean DEFAULT_BLOCK_PICKER_ENABLED = true;
	public static final double DEFAULT_BLOCK_PICKER_RANGE = 6.0D;
	public static final double DEFAULT_BLOCK_PICKER_ENTITY_RANGE = 6.0D;
	public static final double DEFAULT_BLOCK_PICKER_CREATIVE_RANGE = 100.0D;
	public static final boolean DEFAULT_BLOCK_PICKER_USE_INTERACTION_RANGE = false;
	public static final boolean DEFAULT_BLOCK_PICKER_PICK_BLOCKS = true;
	public static final boolean DEFAULT_BLOCK_PICKER_PICK_ENTITIES = true;
	public static final boolean DEFAULT_BLOCK_PICKER_PICK_FLUIDS = true;
	public static final boolean DEFAULT_BLOCK_PICKER_PICK_LIGHT_SOURCES = true;
	public static final boolean DEFAULT_BLOCK_PICKER_PREFER_ENTITY_HEADS = true;
	public static final boolean DEFAULT_BLOCK_PICKER_TAKE_OVER_LITEMATICA = true;
	public static final boolean DEFAULT_BLOCK_PICKER_SHOW_FEEDBACK = false;
	public static final boolean DEFAULT_BLOCK_PICKER_COPY_NBT = true;
	public static final boolean DEFAULT_BLOCK_PICKER_SHOW_COPIED_NBT = true;
	public static final boolean DEFAULT_BLOCK_PICKER_COPY_STATE = true;
	public static final List<String> DEFAULT_BLOCK_ENTITY_NBT_BLACKLIST = List.of("id");
	public static final List<String> DEFAULT_STATE_PROPERTY_BLACKLIST = List.of("waterlogged");
	public static final List<String> DEFAULT_ENTITY_NBT_BLACKLIST =
			List.of("UUID", "Motion", "Rotation", "Pos", "FallDistance", "fall_distance");

	// ID 拾取
	public static final boolean DEFAULT_ID_PICKER_ENABLED = true;
	public static final double DEFAULT_ID_PICKER_RANGE = 6.0D;
	public static final double DEFAULT_ID_PICKER_ENTITY_RANGE = 6.0D;
	public static final double DEFAULT_ID_PICKER_CREATIVE_RANGE = 12.0D;
	public static final boolean DEFAULT_ID_PICKER_USE_INTERACTION_RANGE = false;
	public static final boolean DEFAULT_ID_PICKER_INCLUDE_NAMESPACE = true;
	public static final boolean DEFAULT_ID_PICKER_INCLUDE_STATE_PROPERTIES = true;
	public static final boolean DEFAULT_ID_PICKER_COPY_TO_CLIPBOARD = true;
	public static final boolean DEFAULT_ID_PICKER_INCLUDE_NBT = false;
	public static final boolean DEFAULT_ID_PICKER_PRETTY_NBT = false;
	public static final boolean DEFAULT_ID_PICKER_CHAT_SHORTCUT = true;
	public static final boolean DEFAULT_ID_PICKER_ITEM_TO_BLOCK_ID = true;
	public static final boolean DEFAULT_ID_PICKER_PICK_BLOCKS = true;
	public static final boolean DEFAULT_ID_PICKER_PICK_ENTITIES = true;
	public static final boolean DEFAULT_ID_PICKER_PICK_ITEMS = true;
	public static final List<String> DEFAULT_ID_NBT_BLACKLIST =
			List.of("UUID", "Motion", "Rotation", "Pos", "FallDistance");
	public static final List<String> DEFAULT_ID_STATE_PROPERTY_BLACKLIST = List.of("waterlogged");

	// 工具拾取
	public static final boolean DEFAULT_TOOL_PICKER_ENABLED = true;
	public static final double DEFAULT_TOOL_PICKER_RANGE = 6.0D;
	public static final double DEFAULT_TOOL_PICKER_CREATIVE_RANGE = 100.0D;
	public static final int DEFAULT_TOOL_PICKER_DURABILITY_THRESHOLD = 10;
	public static final boolean DEFAULT_TOOL_PICKER_PICK_FLUID = true;
	public static final boolean DEFAULT_TOOL_PICKER_BAMBOO_PREFERS_SWORD = true;
	public static final boolean DEFAULT_TOOL_PICKER_PREFER_SILK_TOUCH = false;
	public static final boolean DEFAULT_TOOL_PICKER_PREFER_FORTUNE = true;
	public static final boolean DEFAULT_TOOL_PICKER_PREFER_EFFICIENCY = true;
	public static final boolean DEFAULT_TOOL_PICKER_ALLOW_INCOMPATIBLE = false;
	public static final boolean DEFAULT_TOOL_PICKER_GENERATE_IN_CREATIVE = true;

	public static final List<String> DEFAULT_TOOL_SPECS = List.of(
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
			"mace|minecraft:mace|minecraft:smite=5;minecraft:unbreaking=5;minecraft:mending=5;minecraft:breach=5");

	public static final List<String> DEFAULT_WEAPON_PREFERENCES = List.of();

	// 背包
	public static final boolean DEFAULT_SEARCH_CONTAINERS = true;

	// 创造模式
	public static final boolean DEFAULT_CREATIVE_INJECT_TOOLS = true;
	public static final boolean DEFAULT_CREATIVE_INJECT_WEAPONS = true;
	public static final String DEFAULT_CREATIVE_TAB = "minecraft:op_blocks";

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
		public boolean copyState;
		public AxiomMode axiomMode = AxiomMode.YIELD;
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
		public boolean[] lockedSlots = new boolean[9];
	}

	public static final class Creative {
		public boolean injectTools;
		public boolean injectWeapons;
		public String injectTab = "minecraft:op_blocks";
	}

	/**
	 * 装了 Axiom 时，中键拾取归谁管。
	 *
	 * <p>这里刻意不实现 MaLiLib 的 {@code IConfigOptionListEntry}：本类要保持无 MaLiLib 依赖。
	 * 面板里的适配在 {@link MaliOptions.AxiomModeEntry}。</p>
	 */
	public enum AxiomMode {
		/** 让位：Axiom 的 builder 工具、编辑器和无限距离优先，其余我们自己接管。 */
		YIELD("yield"),
		/** 覆盖：除编辑器模式外一律我们自己接管。 */
		OVERRIDE("override");

		private final String configString;

		AxiomMode(String configString) {
			this.configString = configString;
		}

		public String getStringValue() {
			return this.configString;
		}

		/** 配置项的翻译键（和面板里显示的一致）。 */
		public String getTranslationKey() {
			return "picktweak.config.axiomMode." + this.configString;
		}

		/** 在枚举里循环（面板的下拉框用）。 */
		public AxiomMode cycle(boolean forward) {
			AxiomMode[] values = values();
			int index = this.ordinal() + (forward ? 1 : -1);
			return values[Math.floorMod(index, values.length)];
		}

		/** 按配置字符串解析，认不出来时返回当前值。 */
		public AxiomMode fromString(String value) {
			if (value != null) {
				for (AxiomMode mode : values()) {
					if (mode.configString.equalsIgnoreCase(value)) {
						return mode;
					}
				}
			}
			return this;
		}

		/** 按配置字符串解析，认不出来时返回 {@link #YIELD}。 */
		public static AxiomMode byName(String value) {
			if (value != null) {
				for (AxiomMode mode : values()) {
					if (mode.configString.equalsIgnoreCase(value)) {
						return mode;
					}
				}
			}
			return YIELD;
		}
	}

	private Configs() {
	}

	/** 用默认值填充运行时快照（没装 MaLiLib 的客户端 / 服务端共用）。 */
	public static void installDefaults() {
		BlockPicker bp = blockPicker;
		bp.enabled = DEFAULT_BLOCK_PICKER_ENABLED;
		bp.range = DEFAULT_BLOCK_PICKER_RANGE;
		bp.entityRange = DEFAULT_BLOCK_PICKER_ENTITY_RANGE;
		bp.creativeRange = DEFAULT_BLOCK_PICKER_CREATIVE_RANGE;
		bp.useInteractionRange = DEFAULT_BLOCK_PICKER_USE_INTERACTION_RANGE;
		bp.pickBlocks = DEFAULT_BLOCK_PICKER_PICK_BLOCKS;
		bp.pickEntities = DEFAULT_BLOCK_PICKER_PICK_ENTITIES;
		bp.pickFluids = DEFAULT_BLOCK_PICKER_PICK_FLUIDS;
		bp.pickLightSources = DEFAULT_BLOCK_PICKER_PICK_LIGHT_SOURCES;
		bp.preferEntityHeads = DEFAULT_BLOCK_PICKER_PREFER_ENTITY_HEADS;
		bp.takeOverLitematica = DEFAULT_BLOCK_PICKER_TAKE_OVER_LITEMATICA;
		bp.showPickFeedback = DEFAULT_BLOCK_PICKER_SHOW_FEEDBACK;
		bp.copyNbt = DEFAULT_BLOCK_PICKER_COPY_NBT;
		bp.showCopiedNbt = DEFAULT_BLOCK_PICKER_SHOW_COPIED_NBT;
		bp.copyState = DEFAULT_BLOCK_PICKER_COPY_STATE;
		bp.axiomMode = AxiomMode.YIELD;
		bp.blockEntityNbtBlacklist = new ArrayList<>(DEFAULT_BLOCK_ENTITY_NBT_BLACKLIST);
		bp.statePropertyBlacklist = new ArrayList<>(DEFAULT_STATE_PROPERTY_BLACKLIST);
		bp.entityNbtBlacklist = new ArrayList<>(DEFAULT_ENTITY_NBT_BLACKLIST);

		IdPicker ip = idPicker;
		ip.enabled = DEFAULT_ID_PICKER_ENABLED;
		ip.range = DEFAULT_ID_PICKER_RANGE;
		ip.entityRange = DEFAULT_ID_PICKER_ENTITY_RANGE;
		ip.creativeRange = DEFAULT_ID_PICKER_CREATIVE_RANGE;
		ip.useInteractionRange = DEFAULT_ID_PICKER_USE_INTERACTION_RANGE;
		ip.includeNamespace = DEFAULT_ID_PICKER_INCLUDE_NAMESPACE;
		ip.includeStateProperties = DEFAULT_ID_PICKER_INCLUDE_STATE_PROPERTIES;
		ip.copyToClipboard = DEFAULT_ID_PICKER_COPY_TO_CLIPBOARD;
		ip.includeNbt = DEFAULT_ID_PICKER_INCLUDE_NBT;
		ip.prettyNbt = DEFAULT_ID_PICKER_PRETTY_NBT;
		ip.chatShortcut = DEFAULT_ID_PICKER_CHAT_SHORTCUT;
		ip.itemToBlockId = DEFAULT_ID_PICKER_ITEM_TO_BLOCK_ID;
		ip.pickBlocks = DEFAULT_ID_PICKER_PICK_BLOCKS;
		ip.pickEntities = DEFAULT_ID_PICKER_PICK_ENTITIES;
		ip.pickItems = DEFAULT_ID_PICKER_PICK_ITEMS;
		ip.nbtBlacklist = new ArrayList<>(DEFAULT_ID_NBT_BLACKLIST);
		ip.statePropertyBlacklist = new ArrayList<>(DEFAULT_ID_STATE_PROPERTY_BLACKLIST);

		ToolPicker tp = toolPicker;
		tp.enabled = DEFAULT_TOOL_PICKER_ENABLED;
		tp.range = DEFAULT_TOOL_PICKER_RANGE;
		tp.creativeRange = DEFAULT_TOOL_PICKER_CREATIVE_RANGE;
		tp.durabilityThreshold = DEFAULT_TOOL_PICKER_DURABILITY_THRESHOLD;
		tp.pickFluid = DEFAULT_TOOL_PICKER_PICK_FLUID;
		tp.bambooPrefersSword = DEFAULT_TOOL_PICKER_BAMBOO_PREFERS_SWORD;
		tp.preferSilkTouch = DEFAULT_TOOL_PICKER_PREFER_SILK_TOUCH;
		tp.preferFortune = DEFAULT_TOOL_PICKER_PREFER_FORTUNE;
		tp.preferEfficiency = DEFAULT_TOOL_PICKER_PREFER_EFFICIENCY;
		tp.allowIncompatible = DEFAULT_TOOL_PICKER_ALLOW_INCOMPATIBLE;
		tp.generateInCreative = DEFAULT_TOOL_PICKER_GENERATE_IN_CREATIVE;
		tp.weaponPreference = parseWeaponPreferences(DEFAULT_WEAPON_PREFERENCES);
		applyToolSpecs(DEFAULT_TOOL_SPECS);

		inventory.searchContainers = DEFAULT_SEARCH_CONTAINERS;
		inventory.lockedSlots = new boolean[9];

		creative.injectTools = DEFAULT_CREATIVE_INJECT_TOOLS;
		creative.injectWeapons = DEFAULT_CREATIVE_INJECT_WEAPONS;
		creative.injectTab = DEFAULT_CREATIVE_TAB;
	}

	/**
	 * 解析工具预设：{@code kind|itemId|enchant=level;enchant=level}。
	 *
	 * <p>剑 / 斧既是工具也是武器，所以会同时放进 {@link ToolPicker#tools} 与
	 * {@link ToolPicker#weapons}（两份独立的 spec，互不影响）。</p>
	 */
	public static void applyToolSpecs(List<String> entries) {
		ToolPicker tp = toolPicker;
		tp.tools = new LinkedHashMap<>();
		tp.weapons = new LinkedHashMap<>();

		for (String entry : entries) {
			String[] parts = entry.split("\\|");
			if (parts.length < 2) {
				continue;
			}
			String kind = parts[0].trim().toLowerCase(Locale.ROOT);
			if (kind.isEmpty()) {
				continue;
			}
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
			if (isToolKind(kind)) {
				tp.tools.put(kind, spec);
			}
			if (isWeaponKind(kind)) {
				tp.weapons.put(kind, copySpec(spec));
			}
		}
	}

	/** 解析武器偏好：{@code entityId=bow|trident|sword|axe|mace}。 */
	public static Map<String, String> parseWeaponPreferences(List<String> entries) {
		Map<String, String> map = new LinkedHashMap<>();
		for (String entry : entries) {
			int idx = entry.indexOf('=');
			if (idx > 0) {
				map.put(entry.substring(0, idx).trim().toLowerCase(Locale.ROOT),
						entry.substring(idx + 1).trim().toLowerCase(Locale.ROOT));
			}
		}
		return map;
	}

	/** 工具种类（和 {@code ToolPicker.TOOL_KINDS} 对应）。 */
	private static boolean isToolKind(String kind) {
		return switch (kind) {
			case "pickaxe", "axe", "shovel", "hoe", "sword", "shears", "bucket", "fishing_rod", "brush" -> true;
			default -> false;
		};
	}

	/** 武器种类（和 {@code ToolPicker.WEAPON_KINDS} 对应；剑 / 斧与工具重叠）。 */
	private static boolean isWeaponKind(String kind) {
		return switch (kind) {
			case "sword", "axe", "bow", "crossbow", "trident", "mace" -> true;
			default -> false;
		};
	}

	private static ToolSpec copySpec(ToolSpec spec) {
		ToolSpec copy = new ToolSpec(spec.item);
		copy.enabled = spec.enabled;
		copy.enchantments = new LinkedHashMap<>(spec.enchantments);
		return copy;
	}
}
