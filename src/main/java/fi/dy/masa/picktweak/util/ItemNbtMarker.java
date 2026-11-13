package fi.dy.masa.picktweak.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * 给「带数据的拾取物品」加紫色副标题（lore）。
 *
 * <p>Ctrl + 中键拾取的物品会带实体数据 / 方块实体 NBT，显示 {@code +NBT}；
 * Alt + 中键拾取的物品会带方块状态，显示 {@code +State}。两条标记都是亮紫色、非斜体，
 * 已存在时不会重复追加。</p>
 */
public final class ItemNbtMarker {

	public static final String NBT_MARKER_TEXT = "+NBT";

	/** 亮紫色、非斜体（与原版 lore 默认的深紫斜体区分开）。 */
	private static final Style MARKER_STYLE = Style.EMPTY
			.withColor(ChatFormatting.LIGHT_PURPLE)
			.withItalic(false);

	private ItemNbtMarker() {
	}

	/** 给物品加 +NBT 副标题。 */
	public static void markNbt(ItemStack stack) {
		addMarker(stack, Component.literal(NBT_MARKER_TEXT));
	}

	/** Adds the +State (block state) subtitle. */
	public static void markState(ItemStack stack) {
		addMarker(stack, Component.literal("+State"));
	}

	/** 物品是否已带 +NBT 副标题。 */
	public static boolean hasMarker(ItemStack stack) {
		return hasMarker(stack, Component.literal(NBT_MARKER_TEXT));
	}

	/** Whether the item already has the +State subtitle. */
	public static boolean hasStateMarker(ItemStack stack) {
		return hasMarker(stack, Component.literal("+State"));
	}

	private static void addMarker(ItemStack stack, MutableComponent marker) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		Component styledMarker = marker.withStyle(MARKER_STYLE);
		ItemLore lore = stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY);

		if (hasLine(lore, styledMarker)) {
			return;
		}

		stack.set(DataComponents.LORE, lore.withLineAdded(styledMarker));
	}

	private static boolean hasMarker(ItemStack stack, MutableComponent marker) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		ItemLore lore = stack.get(DataComponents.LORE);

		return lore != null && hasLine(lore, marker.withStyle(MARKER_STYLE));
	}

	private static boolean hasLine(ItemLore lore, Component marker) {
		for (Component line : lore.lines()) {
			if (line.equals(marker) || line.getString().equals(marker.getString())) {
				return true;
			}
		}

		return false;
	}
}