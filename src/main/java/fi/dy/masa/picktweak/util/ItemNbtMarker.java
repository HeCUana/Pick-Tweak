package fi.dy.masa.picktweak.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * 给「带 NBT 的拾取物品」加一条紫色的 +NBT 副标题（lore）。
 *
 * <p>Ctrl + 中键拾取时物品会带上实体数据 / 方块实体 NBT，这里在物品名称下方
 * 追加一行亮紫色的 {@code +NBT}，方便一眼区分「带数据的物品」。已经带过标记的物品
 * 不会重复追加。</p>
 */
public final class ItemNbtMarker {

	public static final String MARKER_TEXT = "+NBT";

	/** 亮紫色、非斜体（与原版 lore 默认的深紫斜体区分开）。 */
	private static final Style MARKER_STYLE = Style.EMPTY
			.withColor(ChatFormatting.LIGHT_PURPLE)
			.withItalic(false);

	private ItemNbtMarker() {
	}

	/** 给物品加 +NBT 副标题；没有 NBT 标记需求时由调用方决定是否调用。 */
	public static void markNbt(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);

		for (Component line : lore.lines()) {
			if (MARKER_TEXT.equals(line.getString())) {
				return;
			}
		}

		stack.set(DataComponents.LORE, lore.withLineAdded(Component.literal(MARKER_TEXT).withStyle(MARKER_STYLE)));
	}

	/** 物品是否已带 +NBT 副标题。 */
	public static boolean hasMarker(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		ItemLore lore = stack.get(DataComponents.LORE);

		if (lore == null) {
			return false;
		}

		for (Component line : lore.lines()) {
			if (MARKER_TEXT.equals(line.getString())) {
				return true;
			}
		}

		return false;
	}
}
