package fi.dy.masa.picktweak.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigGuiAllTab;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.picktweak.Reference;
import fi.dy.masa.picktweak.config.MaliOptions;
import fi.dy.masa.picktweak.config.Hotkeys;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Pick Tweak 的配置面板。
 *
 * <p>布局完全照 MaLiLib 自己的配置界面来：顶部一排自动宽度的标签页按钮，
 * 下方是 MaLiLib 的配置列表（自带悬停说明、按键搜索、重置按钮）。</p>
 *
 * <pre>
 *   +------------------------------------------------------------+
 *   |  Pick Tweak Configs - 1.0.0                        [模组切换] |
 *   |  [全部][区块拾取][ID 拾取][工具拾取][背包][创造模式][热键]  |
 *   |  +---------------------------+   |
 *   |  | 选项 / 说明 / 按键         |   |
 *   |  +---------------------------+   |
 *   +------------------------------------------------------------+
 * </pre>
 */
public class GuiConfigs extends GuiConfigsBase implements IConfigGuiAllTab {

	private static ConfigGuiTab tab = ConfigGuiTab.BLOCK_PICKER;

	public GuiConfigs(Screen parent) {
		// 列表从 (10, 50) 开始：上面留给标题(26)和标签页按钮(26..46)
		super(10, 50, Reference.MOD_ID, parent, "picktweak.gui.title.configs", Reference.MOD_VERSION);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.clearOptions();

		int x = 10;
		int y = 26;

		for (ConfigGuiTab configTab : ConfigGuiTab.values()) {
			x += this.createButton(x, y, -1, configTab) + 2;
		}
	}

	private int createButton(int x, int y, int width, ConfigGuiTab configTab) {
		ButtonGeneric button = new ButtonGeneric(x, y, width, 20, configTab.getDisplayName());
		button.setEnabled(GuiConfigs.tab != configTab);
		this.addButton(button, new ButtonListener(configTab, this));

		return button.getWidth();
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		if (GuiConfigs.tab == ConfigGuiTab.ALL && this.useAllTab()) {
			return this.getAllConfigs();
		}

		return ConfigOptionWrapper.createFor(GuiConfigs.tab.getConfigs());
	}

	@Override
	public boolean useAllTab() {
		return true;
	}

	@Override
	public List<ConfigOptionWrapper> getAllConfigs() {
		List<IConfigBase> all = new ArrayList<>(MaliOptions.ALL_CONFIGS);
		return ConfigOptionWrapper.createFor(all);
	}

	@Override
	protected boolean useKeybindSearch() {
		// 按键搜索只在热键页和全部页有意义（和 MaLiLib 自己的做法一致）
		return GuiConfigs.tab == ConfigGuiTab.HOTKEYS || GuiConfigs.tab == ConfigGuiTab.ALL;
	}

	@Override
	protected int getConfigWidth() {
		if (GuiConfigs.tab == ConfigGuiTab.BLOCK_PICKER) {
			return 200;
		}
		return super.getConfigWidth();
	}

	@Override
	protected void onSettingsChanged() {
		super.onSettingsChanged();
		MaliOptions.refresh();
	}

	/** 切换标签页。 */
	private record ButtonListener(ConfigGuiTab tab, GuiConfigs parent) implements IButtonActionListener {
		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			GuiConfigs.tab = this.tab;

			this.parent.reCreateListWidget();
			if (this.parent.getListWidget() != null) {
				this.parent.getListWidget().resetScrollbarPosition();
			}
			this.parent.initGui();
		}
	}

	/** 面板的标签页。 */
	public enum ConfigGuiTab {
		ALL(IConfigGuiAllTab.getTranslationKey()),
		BLOCK_PICKER("picktweak.gui.title.block_picker"),
		ID_PICKER("picktweak.gui.title.id_picker"),
		TOOL_PICKER("picktweak.gui.title.tool_picker"),
		INVENTORY("picktweak.gui.title.inventory"),
		CREATIVE("picktweak.gui.title.creative"),
		HOTKEYS("picktweak.gui.title.hotkeys");

		private final String translationKey;

		ConfigGuiTab(String translationKey) {
			this.translationKey = translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}

		public List<IConfigBase> getConfigs() {
			return switch (this) {
				case ALL -> new ArrayList<>(MaliOptions.ALL_CONFIGS);
				case BLOCK_PICKER -> List.of(
						MaliOptions.BLOCK_PICKER_ENABLED,
						MaliOptions.BLOCK_PICKER_RANGE,
						MaliOptions.BLOCK_PICKER_ENTITY_RANGE,
						MaliOptions.BLOCK_PICKER_CREATIVE_RANGE,
						MaliOptions.BLOCK_PICKER_USE_INTERACTION_RANGE,
						MaliOptions.BLOCK_PICKER_PICK_BLOCKS,
						MaliOptions.BLOCK_PICKER_PICK_ENTITIES,
						MaliOptions.BLOCK_PICKER_PICK_FLUIDS,
						MaliOptions.BLOCK_PICKER_PICK_LIGHT_SOURCES,
						MaliOptions.BLOCK_PICKER_PREFER_ENTITY_HEADS,
						MaliOptions.BLOCK_PICKER_TAKE_OVER_LITEMATICA,
						MaliOptions.BLOCK_PICKER_SHOW_FEEDBACK,
						MaliOptions.BLOCK_PICKER_COPY_NBT,
						MaliOptions.BLOCK_PICKER_SHOW_COPIED_NBT,
						MaliOptions.BLOCK_PICKER_COPY_NBT_CHAT_FEEDBACK,
						MaliOptions.BLOCK_PICKER_COPY_STATE,
						MaliOptions.BLOCK_PICKER_COPY_STATE_CHAT_FEEDBACK,
						MaliOptions.BLOCK_PICKER_AXIOM_MODE,
						MaliOptions.BLOCK_ENTITY_NBT_BLACKLIST,
						MaliOptions.STATE_PROPERTY_BLACKLIST,
						MaliOptions.ENTITY_NBT_BLACKLIST
				);
				case ID_PICKER -> List.of(
						MaliOptions.ID_PICKER_ENABLED,
						MaliOptions.ID_PICKER_RANGE,
						MaliOptions.ID_PICKER_ENTITY_RANGE,
						MaliOptions.ID_PICKER_CREATIVE_RANGE,
						MaliOptions.ID_PICKER_USE_INTERACTION_RANGE,
						MaliOptions.ID_PICKER_INCLUDE_NAMESPACE,
						MaliOptions.ID_PICKER_INCLUDE_STATE_PROPERTIES,
						MaliOptions.ID_PICKER_COPY_TO_CLIPBOARD,
						MaliOptions.ID_PICKER_INCLUDE_NBT,
						MaliOptions.ID_PICKER_PRETTY_NBT,
						MaliOptions.ID_PICKER_CHAT_SHORTCUT,
						MaliOptions.ID_PICKER_ITEM_TO_BLOCK_ID,
						MaliOptions.ID_PICKER_PICK_BLOCKS,
						MaliOptions.ID_PICKER_PICK_ENTITIES,
						MaliOptions.ID_PICKER_PICK_ITEMS,
						MaliOptions.ID_NBT_BLACKLIST,
						MaliOptions.ID_STATE_PROPERTY_BLACKLIST
				);
				case TOOL_PICKER -> List.of(
						MaliOptions.TOOL_PICKER_ENABLED,
						MaliOptions.TOOL_PICKER_RANGE,
						MaliOptions.TOOL_PICKER_CREATIVE_RANGE,
						MaliOptions.TOOL_PICKER_DURABILITY_THRESHOLD,
						MaliOptions.TOOL_PICKER_PICK_FLUID,
						MaliOptions.TOOL_PICKER_BAMBOO_PREFERS_SWORD,
						MaliOptions.TOOL_PICKER_PREFER_SILK_TOUCH,
						MaliOptions.TOOL_PICKER_PREFER_FORTUNE,
						MaliOptions.TOOL_PICKER_PREFER_EFFICIENCY,
						MaliOptions.TOOL_PICKER_ALLOW_INCOMPATIBLE,
						MaliOptions.TOOL_PICKER_GENERATE_IN_CREATIVE,
						MaliOptions.TOOL_SPECS,
						MaliOptions.WEAPON_PREFERENCES
				);
				case INVENTORY -> List.of(
						MaliOptions.SEARCH_CONTAINERS,
						MaliOptions.LOCK_HOTBAR_SLOT_1,
						MaliOptions.LOCK_HOTBAR_SLOT_2,
						MaliOptions.LOCK_HOTBAR_SLOT_3,
						MaliOptions.LOCK_HOTBAR_SLOT_4,
						MaliOptions.LOCK_HOTBAR_SLOT_5,
						MaliOptions.LOCK_HOTBAR_SLOT_6,
						MaliOptions.LOCK_HOTBAR_SLOT_7,
						MaliOptions.LOCK_HOTBAR_SLOT_8,
						MaliOptions.LOCK_HOTBAR_SLOT_9
				);
				case CREATIVE -> List.of(
						MaliOptions.CREATIVE_INJECT_TOOLS,
						MaliOptions.CREATIVE_INJECT_WEAPONS,
						MaliOptions.CREATIVE_INJECT_ARMOR,
						MaliOptions.CREATIVE_TAB
				);
				case HOTKEYS -> hotkeyConfigs();
			};
		}

		/** 热键页：全部快捷方式都在这里改。 */
		private static List<IConfigBase> hotkeyConfigs() {
			List<IConfigBase> list = new ArrayList<>();
			list.add(Hotkeys.PICK_WITH_NBT);
			list.add(Hotkeys.PICK_WITH_STATE);
			list.add(Hotkeys.PICK_ID);
			list.add(Hotkeys.PICK_TOOL);
			list.add(Hotkeys.OPEN_CONFIG);
			list.addAll(Hotkeys.CHAT_SLOT_KEYS);
			return list;
		}
	}
}