package fi.dy.masa.picktweak.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.picktweak.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * 全部 MaLiLib 配置项（配置面板 / {@code picktweak.json}）。
 *
 * <p><b>这个类是唯一依赖 MaLiLib 的配置类</b>：服务端和没装 MaLiLib 的客户端都不会加载它。
 * 默认值统一放在 {@link Configs} 里，这里只负责把它们声明成 MaLiLib 的配置项，
 * 并在 {@link #refresh()} 时把值同步进 {@link Configs} 的运行时快照。</p>
 */
public final class MaliOptions {

	// ==================== 区块拾取 ====================

	public static final ConfigBoolean BLOCK_PICKER_ENABLED = bool("blockPickerEnabled",
			Configs.DEFAULT_BLOCK_PICKER_ENABLED, "Whether the block picker is enabled");
	public static final ConfigDouble BLOCK_PICKER_RANGE = dbl("blockPickerRange",
			Configs.DEFAULT_BLOCK_PICKER_RANGE, 1.0D, 256.0D, "Block pick range in survival mode");
	public static final ConfigDouble BLOCK_PICKER_ENTITY_RANGE = dbl("blockPickerEntityRange",
			Configs.DEFAULT_BLOCK_PICKER_ENTITY_RANGE, 1.0D, 256.0D, "Entity pick range in survival mode");
	public static final ConfigDouble BLOCK_PICKER_CREATIVE_RANGE = dbl("blockPickerCreativeRange",
			Configs.DEFAULT_BLOCK_PICKER_CREATIVE_RANGE, 1.0D, 256.0D, "Block pick range in creative mode");
	public static final ConfigBoolean BLOCK_PICKER_USE_INTERACTION_RANGE = bool("blockPickerUseInteractionRange",
			Configs.DEFAULT_BLOCK_PICKER_USE_INTERACTION_RANGE, "Use the player's own interaction range instead of the values above");
	public static final ConfigBoolean BLOCK_PICKER_PICK_BLOCKS = bool("blockPickerPickBlocks",
			Configs.DEFAULT_BLOCK_PICKER_PICK_BLOCKS, "Allow picking blocks");
	public static final ConfigBoolean BLOCK_PICKER_PICK_ENTITIES = bool("blockPickerPickEntities",
			Configs.DEFAULT_BLOCK_PICKER_PICK_ENTITIES, "Allow picking entities");
	public static final ConfigBoolean BLOCK_PICKER_PICK_FLUIDS = bool("blockPickerPickFluids",
			Configs.DEFAULT_BLOCK_PICKER_PICK_FLUIDS, "Pick fluids as the matching bucket");
	public static final ConfigBoolean BLOCK_PICKER_PICK_LIGHT_SOURCES = bool("blockPickerPickLightSources",
			Configs.DEFAULT_BLOCK_PICKER_PICK_LIGHT_SOURCES, "Allow picking the sun and the moon");
	public static final ConfigBoolean BLOCK_PICKER_PREFER_ENTITY_HEADS = bool("blockPickerPreferEntityHeads",
			Configs.DEFAULT_BLOCK_PICKER_PREFER_ENTITY_HEADS, "Prefer mob heads (zombie -> zombie head) over the vanilla spawn egg");
	public static final ConfigBoolean BLOCK_PICKER_TAKE_OVER_LITEMATICA = bool("blockPickerTakeOverLitematica",
			Configs.DEFAULT_BLOCK_PICKER_TAKE_OVER_LITEMATICA, "Take over Litematica's pick block hotkeys");
	public static final ConfigBoolean BLOCK_PICKER_SHOW_FEEDBACK = bool("blockPickerShowFeedback",
			Configs.DEFAULT_BLOCK_PICKER_SHOW_FEEDBACK, "Show a chat message about what was picked");
	public static final ConfigBoolean BLOCK_PICKER_COPY_NBT = bool("blockPickerCopyNbt",
			Configs.DEFAULT_BLOCK_PICKER_COPY_NBT, "Ctrl + middle click copies the NBT of the target to the clipboard");
	public static final ConfigBoolean BLOCK_PICKER_SHOW_COPIED_NBT = bool("blockPickerShowCopiedNbt",
			Configs.DEFAULT_BLOCK_PICKER_SHOW_COPIED_NBT, "Print the copied NBT in the chat box as well");
	public static final ConfigBoolean BLOCK_PICKER_COPY_STATE = bool("blockPickerCopyState",
			Configs.DEFAULT_BLOCK_PICKER_COPY_STATE, "Alt + middle click copies the block state to the clipboard as well");
	public static final ConfigOptionList BLOCK_PICKER_AXIOM_MODE = optionList("blockPickerAxiomMode",
			AxiomModeEntry.YIELD, "How to behave when Axiom is installed");
	public static final ConfigStringList BLOCK_ENTITY_NBT_BLACKLIST = strList("blockEntityNbtBlacklist",
			Configs.DEFAULT_BLOCK_ENTITY_NBT_BLACKLIST, "Block entity NBT keys to hide (paths, e.g. id or Items.id)");
	public static final ConfigStringList STATE_PROPERTY_BLACKLIST = strList("statePropertyBlacklist",
			Configs.DEFAULT_STATE_PROPERTY_BLACKLIST, "Block state properties to hide");
	public static final ConfigStringList ENTITY_NBT_BLACKLIST = strList("entityNbtBlacklist",
			Configs.DEFAULT_ENTITY_NBT_BLACKLIST, "Entity NBT keys to hide (paths, e.g. UUID or equipment.head.id)");

	// ==================== ID 拾取 ====================

	public static final ConfigBoolean ID_PICKER_ENABLED = bool("idPickerEnabled",
			Configs.DEFAULT_ID_PICKER_ENABLED, "Whether the ID picker is enabled");
	public static final ConfigDouble ID_PICKER_RANGE = dbl("idPickerRange",
			Configs.DEFAULT_ID_PICKER_RANGE, 1.0D, 256.0D, "ID pick range in survival mode");
	public static final ConfigDouble ID_PICKER_ENTITY_RANGE = dbl("idPickerEntityRange",
			Configs.DEFAULT_ID_PICKER_ENTITY_RANGE, 1.0D, 256.0D, "Entity ID pick range in survival mode");
	public static final ConfigDouble ID_PICKER_CREATIVE_RANGE = dbl("idPickerCreativeRange",
			Configs.DEFAULT_ID_PICKER_CREATIVE_RANGE, 1.0D, 256.0D, "ID pick range in creative mode");
	public static final ConfigBoolean ID_PICKER_USE_INTERACTION_RANGE = bool("idPickerUseInteractionRange",
			Configs.DEFAULT_ID_PICKER_USE_INTERACTION_RANGE, "Use the player's own interaction range instead of the values above");
	public static final ConfigBoolean ID_PICKER_INCLUDE_NAMESPACE = bool("idPickerIncludeNamespace",
			Configs.DEFAULT_ID_PICKER_INCLUDE_NAMESPACE, "Include the namespace in the output (minecraft:stone)");
	public static final ConfigBoolean ID_PICKER_INCLUDE_STATE_PROPERTIES = bool("idPickerIncludeStateProperties",
			Configs.DEFAULT_ID_PICKER_INCLUDE_STATE_PROPERTIES, "Include the block state properties in the output");
	public static final ConfigBoolean ID_PICKER_COPY_TO_CLIPBOARD = bool("idPickerCopyToClipboard",
			Configs.DEFAULT_ID_PICKER_COPY_TO_CLIPBOARD, "Copy the output to the clipboard as well");
	public static final ConfigBoolean ID_PICKER_INCLUDE_NBT = bool("idPickerIncludeNbt",
			Configs.DEFAULT_ID_PICKER_INCLUDE_NBT, "Output NBT instead of just the ID (toggled with the NBT modifier key)");
	public static final ConfigBoolean ID_PICKER_PRETTY_NBT = bool("idPickerPrettyNbt",
			Configs.DEFAULT_ID_PICKER_PRETTY_NBT, "Pretty print the NBT output");
	public static final ConfigBoolean ID_PICKER_CHAT_SHORTCUT = bool("idPickerChatShortcut",
			Configs.DEFAULT_ID_PICKER_CHAT_SHORTCUT, "Write the hotbar item IDs in the chat box with the chat slot hotkeys");
	public static final ConfigBoolean ID_PICKER_ITEM_TO_BLOCK_ID = bool("idPickerItemToBlockId",
			Configs.DEFAULT_ID_PICKER_ITEM_TO_BLOCK_ID, "Convert item IDs into the matching block IDs");
	public static final ConfigBoolean ID_PICKER_PICK_BLOCKS = bool("idPickerPickBlocks",
			Configs.DEFAULT_ID_PICKER_PICK_BLOCKS, "Allow ID picking for blocks");
	public static final ConfigBoolean ID_PICKER_PICK_ENTITIES = bool("idPickerPickEntities",
			Configs.DEFAULT_ID_PICKER_PICK_ENTITIES, "Allow ID picking for entities");
	public static final ConfigBoolean ID_PICKER_PICK_ITEMS = bool("idPickerPickItems",
			Configs.DEFAULT_ID_PICKER_PICK_ITEMS, "Allow ID picking for items");
	public static final ConfigStringList ID_NBT_BLACKLIST = strList("idNbtBlacklist",
			Configs.DEFAULT_ID_NBT_BLACKLIST, "NBT keys to hide");
	public static final ConfigStringList ID_STATE_PROPERTY_BLACKLIST = strList("idStatePropertyBlacklist",
			Configs.DEFAULT_ID_STATE_PROPERTY_BLACKLIST, "Block state properties to hide");

	// ==================== 工具拾取 ====================

	public static final ConfigBoolean TOOL_PICKER_ENABLED = bool("toolPickerEnabled",
			Configs.DEFAULT_TOOL_PICKER_ENABLED, "Whether the tool picker is enabled");
	public static final ConfigDouble TOOL_PICKER_RANGE = dbl("toolPickerRange",
			Configs.DEFAULT_TOOL_PICKER_RANGE, 1.0D, 256.0D, "Tool pick range in survival mode");
	public static final ConfigDouble TOOL_PICKER_CREATIVE_RANGE = dbl("toolPickerCreativeRange",
			Configs.DEFAULT_TOOL_PICKER_CREATIVE_RANGE, 1.0D, 256.0D, "Tool pick range in creative mode");
	public static final ConfigInteger TOOL_PICKER_DURABILITY_THRESHOLD = integer("toolPickerDurabilityThreshold",
			Configs.DEFAULT_TOOL_PICKER_DURABILITY_THRESHOLD, 0, 1000, "Tools with less remaining durability are skipped");
	public static final ConfigBoolean TOOL_PICKER_PICK_FLUID = bool("toolPickerPickFluid",
			Configs.DEFAULT_TOOL_PICKER_PICK_FLUID, "Use a bucket for fluids and powder snow");
	public static final ConfigBoolean TOOL_PICKER_BAMBOO_PREFERS_SWORD = bool("toolPickerBambooPrefersSword",
			Configs.DEFAULT_TOOL_PICKER_BAMBOO_PREFERS_SWORD, "Prefer a sword for bamboo");
	public static final ConfigBoolean TOOL_PICKER_PREFER_SILK_TOUCH = bool("toolPickerPreferSilkTouch",
			Configs.DEFAULT_TOOL_PICKER_PREFER_SILK_TOUCH, "Prefer silk touch");
	public static final ConfigBoolean TOOL_PICKER_PREFER_FORTUNE = bool("toolPickerPreferFortune",
			Configs.DEFAULT_TOOL_PICKER_PREFER_FORTUNE, "Prefer fortune");
	public static final ConfigBoolean TOOL_PICKER_PREFER_EFFICIENCY = bool("toolPickerPreferEfficiency",
			Configs.DEFAULT_TOOL_PICKER_PREFER_EFFICIENCY, "Prefer efficiency");
	public static final ConfigBoolean TOOL_PICKER_ALLOW_INCOMPATIBLE = bool("toolPickerAllowIncompatible",
			Configs.DEFAULT_TOOL_PICKER_ALLOW_INCOMPATIBLE, "Allow incompatible enchantments (generated up to level 255)");
	public static final ConfigBoolean TOOL_PICKER_GENERATE_IN_CREATIVE = bool("toolPickerGenerateInCreative",
			Configs.DEFAULT_TOOL_PICKER_GENERATE_IN_CREATIVE, "Generate a fully enchanted tool in creative mode");
	public static final ConfigStringList TOOL_SPECS = strList("toolSpecs",
			Configs.DEFAULT_TOOL_SPECS,
			"Tool and weapon presets: kind|itemId|enchantment=level;enchantment=level");
	public static final ConfigStringList WEAPON_PREFERENCES = strList("weaponPreferences",
			Configs.DEFAULT_WEAPON_PREFERENCES,
			"Preferred weapon per entity type: entityId=bow|trident|sword|axe|mace");

	// ==================== 背包 ====================

	public static final ConfigBoolean SEARCH_CONTAINERS = bool("searchContainers",
			Configs.DEFAULT_SEARCH_CONTAINERS, "Search shulker boxes and bundles for tools");
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

	public static final ConfigBoolean CREATIVE_INJECT_TOOLS = bool("creativeInjectTools",
			Configs.DEFAULT_CREATIVE_INJECT_TOOLS, "Inject the configured tools into the creative inventory");
	public static final ConfigBoolean CREATIVE_INJECT_WEAPONS = bool("creativeInjectWeapons",
			Configs.DEFAULT_CREATIVE_INJECT_WEAPONS, "Inject the configured weapons into the creative inventory");
	public static final ConfigString CREATIVE_TAB = str("creativeTab",
			Configs.DEFAULT_CREATIVE_TAB, "The creative inventory tab to inject the items into");

	/** 面板里显示的全部配置项。 */
	public static final List<IConfigBase> ALL_CONFIGS = List.of(
			BLOCK_PICKER_ENABLED, BLOCK_PICKER_RANGE, BLOCK_PICKER_ENTITY_RANGE, BLOCK_PICKER_CREATIVE_RANGE,
			BLOCK_PICKER_USE_INTERACTION_RANGE, BLOCK_PICKER_PICK_BLOCKS, BLOCK_PICKER_PICK_ENTITIES,
			BLOCK_PICKER_PICK_FLUIDS, BLOCK_PICKER_PICK_LIGHT_SOURCES, BLOCK_PICKER_PREFER_ENTITY_HEADS,
			BLOCK_PICKER_TAKE_OVER_LITEMATICA, BLOCK_PICKER_SHOW_FEEDBACK,
			BLOCK_PICKER_COPY_NBT, BLOCK_PICKER_SHOW_COPIED_NBT, BLOCK_PICKER_COPY_STATE, BLOCK_PICKER_AXIOM_MODE,
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

			SEARCH_CONTAINERS,
			LOCK_HOTBAR_SLOT_1, LOCK_HOTBAR_SLOT_2, LOCK_HOTBAR_SLOT_3, LOCK_HOTBAR_SLOT_4, LOCK_HOTBAR_SLOT_5,
			LOCK_HOTBAR_SLOT_6, LOCK_HOTBAR_SLOT_7, LOCK_HOTBAR_SLOT_8, LOCK_HOTBAR_SLOT_9,

			CREATIVE_INJECT_TOOLS, CREATIVE_INJECT_WEAPONS, CREATIVE_TAB
	);

	private MaliOptions() {
	}

	/** 把 MaLiLib 的配置值同步到 {@link Configs} 的运行时快照。 */
	public static void refresh() {
		Configs.BlockPicker bp = Configs.blockPicker;
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
		bp.copyState = BLOCK_PICKER_COPY_STATE.getBooleanValue();
		bp.axiomMode = readAxiomMode();
		bp.blockEntityNbtBlacklist = new ArrayList<>(BLOCK_ENTITY_NBT_BLACKLIST.getStrings());
		bp.statePropertyBlacklist = new ArrayList<>(STATE_PROPERTY_BLACKLIST.getStrings());
		bp.entityNbtBlacklist = new ArrayList<>(ENTITY_NBT_BLACKLIST.getStrings());

		Configs.IdPicker ip = Configs.idPicker;
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

		Configs.ToolPicker tp = Configs.toolPicker;
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
		tp.weaponPreference = Configs.parseWeaponPreferences(WEAPON_PREFERENCES.getStrings());
		Configs.applyToolSpecs(TOOL_SPECS.getStrings());

		Configs.inventory.searchContainers = SEARCH_CONTAINERS.getBooleanValue();
		Configs.inventory.lockedSlots = new boolean[]{
				LOCK_HOTBAR_SLOT_1.getBooleanValue(), LOCK_HOTBAR_SLOT_2.getBooleanValue(),
				LOCK_HOTBAR_SLOT_3.getBooleanValue(), LOCK_HOTBAR_SLOT_4.getBooleanValue(),
				LOCK_HOTBAR_SLOT_5.getBooleanValue(), LOCK_HOTBAR_SLOT_6.getBooleanValue(),
				LOCK_HOTBAR_SLOT_7.getBooleanValue(), LOCK_HOTBAR_SLOT_8.getBooleanValue(),
				LOCK_HOTBAR_SLOT_9.getBooleanValue()
		};

		Configs.creative.injectTools = CREATIVE_INJECT_TOOLS.getBooleanValue();
		Configs.creative.injectWeapons = CREATIVE_INJECT_WEAPONS.getBooleanValue();
		Configs.creative.injectTab = CREATIVE_TAB.getStringValue();
	}

	private static Configs.AxiomMode readAxiomMode() {
		IConfigOptionListEntry value = BLOCK_PICKER_AXIOM_MODE.getOptionListValue();
		return value instanceof AxiomModeEntry entry ? entry.toMode() : Configs.AxiomMode.YIELD;
	}

	/** 面板里的 Axiom 模式下拉项：只是 {@link Configs.AxiomMode} 的 MaLiLib 适配层。 */
	public enum AxiomModeEntry implements IConfigOptionListEntry {
		YIELD(Configs.AxiomMode.YIELD),
		OVERRIDE(Configs.AxiomMode.OVERRIDE);

		private final Configs.AxiomMode mode;

		AxiomModeEntry(Configs.AxiomMode mode) {
			this.mode = mode;
		}

		public Configs.AxiomMode toMode() {
			return this.mode;
		}

		@Override
		public String getStringValue() {
			return this.mode.getStringValue();
		}

		@Override
		public String getDisplayName() {
			return StringUtils.translate(this.mode.getTranslationKey());
		}

		@Override
		public AxiomModeEntry cycle(boolean forward) {
			return values()[Math.floorMod(this.ordinal() + (forward ? 1 : -1), values().length)];
		}

		@Override
		public AxiomModeEntry fromString(String value) {
			if (value != null) {
				for (AxiomModeEntry entry : values()) {
					if (entry.mode.getStringValue().equalsIgnoreCase(value)) {
						return entry;
					}
				}
			}
			return this;
		}
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

	private static ConfigString str(String name, String def, String comment) {
		ConfigString option = new ConfigString(name, def, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static ConfigStringList strList(String name, List<String> def, String comment) {
		ConfigStringList option = new ConfigStringList(name, ImmutableList.copyOf(def), comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}

	private static ConfigOptionList optionList(String name, IConfigOptionListEntry def, String comment) {
		ConfigOptionList option = new ConfigOptionList(name, def, comment);
		option.translatedName(Reference.CONFIG_KEY_PREFIX + name);
		return option;
	}
}
