package fi.dy.masa.picktweak.inv;

import fi.dy.masa.picktweak.config.Configs;
import fi.dy.masa.picktweak.config.Configs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.Optional;

/**
 * 背包管理：选槽位、锁定槽位、与服务器同步的交换、容器搜索。
 */
public final class InventoryManager {

	private InventoryManager() {
	}

	/** 快捷栏槽位是否被锁定。 */
	public static boolean isLocked(int hotbarSlot) {
		if (hotbarSlot < 0 || hotbarSlot > 8) {
			return false;
		}
		boolean[] locked = Configs.inventory.lockedSlots;
		return locked != null && hotbarSlot < locked.length && locked[hotbarSlot];
	}

	/** 切换到指定快捷栏槽位，并通知服务端。 */
	public static void selectSlot(int slot) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || slot < 0 || slot > 8) {
			return;
		}
		player.getInventory().setSelectedSlot(slot);
		if (mc.getConnection() != null) {
			mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
		}
	}

	/** 找一个未被锁定且为空的快捷栏槽位。 */
	public static int findFreeHotbarSlot() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return -1;
		}
		Inventory inventory = player.getInventory();
		for (int i = 0; i < 9; i++) {
			if (!isLocked(i) && inventory.getItem(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 把物品交给玩家（仅创造模式 / 客户端回退路径使用）。
	 *
	 * <p>正常情况下拾取由服务端处理，这里只服务于「服务端没装 PBP」的回退路径。</p>
	 */
	public static boolean give(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return false;
		}
		Inventory inventory = player.getInventory();

		int exact = inventory.findSlotMatchingItem(stack);
		if (exact >= 0) {
			selectOrSwap(exact);
			return true;
		}
		if (player.hasInfiniteMaterials()) {
			return giveCreative(stack);
		}
		return false;
	}

	/** 创造模式下把物品放入快捷栏。 */
	public static boolean giveCreative(ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.gameMode == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		int target = player.getInventory().getSelectedSlot();
		if (isLocked(target)) {
			target = findFreeHotbarSlot();
		}
		if (target < 0) {
			target = player.getInventory().getSelectedSlot();
		}
		ItemStack copy = stack.copy();
		copy.setCount(1);
		inventory.setItem(target, copy);
		mc.gameMode.handleCreativeModeItemAdd(copy, target);
		selectSlot(target);
		return true;
	}

	/** 找到背包中同类（物品相同、组件不同）的槽位。 */
	public static int findSameItemSlot(ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return -1;
		}
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack candidate = inventory.getItem(i);
			if (!candidate.isEmpty() && candidate.getItem() == stack.getItem()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 找出容器里装的物品（潜影盒 / 收纳袋）。
	 *
	 * @return 容器内符合谓词的物品（没有则返回 {@link ItemStack#EMPTY}）
	 */
	public static ItemStack findInContainer(ItemStack container, java.util.function.Predicate<ItemStack> filter) {
		ItemContainerContents shulker = container.get(DataComponents.CONTAINER);
		if (shulker != null) {
			Optional<ItemStack> found = shulker.nonEmptyItemCopyStream().filter(filter).findFirst();
			if (found.isPresent()) {
				return found.get();
			}
		}
		BundleContents bundle = container.get(DataComponents.BUNDLE_CONTENTS);
		if (bundle != null) {
			Optional<ItemStack> found = bundle.itemCopies().filter(filter).findFirst();
			if (found.isPresent()) {
				return found.get();
			}
		}
		return ItemStack.EMPTY;
	}

	/** 该物品是否是能装东西的容器。 */
	public static boolean isContainer(ItemStack stack) {
		return stack.get(DataComponents.CONTAINER) != null || stack.get(DataComponents.BUNDLE_CONTENTS) != null;
	}

	/** 选中某个槽位；若在背包里则先交换到快捷栏。 */
	public static void selectOrSwap(int inventoryIndex) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}
		Inventory inventory = player.getInventory();
		if (inventoryIndex >= 0 && inventoryIndex < 9) {
			if (!isLocked(inventoryIndex)) {
				selectSlot(inventoryIndex);
			}
			return;
		}
		if (!swapIntoHotbar(inventoryIndex)) {
			return;
		}
		int hotbarSlot = player.getInventory().getSelectedSlot();
		if (!isLocked(hotbarSlot)) {
			selectSlot(hotbarSlot);
		}
	}

	/**
	 * 把背包中的某个槽位与当前快捷栏槽位交换，全程通过容器点击包与服务器同步。
	 *
	 * @return 是否交换成功
	 */
	public static boolean swapIntoHotbar(int inventoryIndex) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.gameMode == null) {
			return false;
		}
		Inventory inventory = player.getInventory();
		if (inventoryIndex < 0 || inventoryIndex >= inventory.getContainerSize()) {
			return false;
		}
		if (inventory.getItem(inventoryIndex).isEmpty()) {
			return false;
		}
		int current = inventory.getSelectedSlot();
		if (isLocked(current)) {
			return false;
		}
		AbstractContainerMenu menu = player.containerMenu;
		int sourceSlot = findMenuSlot(menu, inventory, inventoryIndex);
		int targetSlot = findMenuSlot(menu, inventory, current);
		if (sourceSlot < 0 || targetSlot < 0) {
			return false;
		}

		int originalSelected = current;
		// 1. 拿起源槽位的物品
		if (!click(menu, sourceSlot)) {
			return false;
		}
		// 2. 放到快捷栏（快捷栏有物品时两者交换，光标上会拿到原来的物品）
		if (!click(menu, targetSlot)) {
			return false;
		}
		// 3. 把光标上的物品（原来的快捷栏物品）放回源槽位
		if (!player.containerMenu.getCarried().isEmpty()) {
			click(menu, sourceSlot);
		}

		if (Configs.inventory.keepOriginalSlot && !isLocked(originalSelected)) {
			selectSlot(originalSelected);
		}
		return true;
	}

	/** 在容器的槽位上模拟一次左键点击（拾取 / 放下）。 */
	private static boolean click(AbstractContainerMenu menu, int menuSlot) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.gameMode == null) {
			return false;
		}
		try {
			mc.gameMode.handleContainerInput(menu.containerId, menuSlot, 0, ContainerInput.PICKUP, player);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/** 找到背包槽位在当前菜单中对应的槽位下标。 */
	private static int findMenuSlot(AbstractContainerMenu menu, Inventory inventory, int inventoryIndex) {
		for (Slot slot : menu.slots) {
			if (slot.container == inventory && slot.getContainerSlot() == inventoryIndex) {
				return slot.index;
			}
		}
		return -1;
	}

	/**
	 * 搜索背包中的潜影盒 / 收纳袋，看里面有没有目标物品。
	 *
	 * @return 目标物品所在容器的槽位，没有则返回 -1
	 */
	public static int findInContainers(ItemStack target) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || target == null || target.isEmpty()) {
			return -1;
		}
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack candidate = inventory.getItem(i);
			if (candidate.isEmpty()) {
				continue;
			}
			if (contains(candidate, target)) {
				return i;
			}
		}
		return -1;
	}
	private static boolean contains(ItemStack container, ItemStack target) {
		ItemContainerContents shulker = container.get(DataComponents.CONTAINER);
		if (shulker != null) {
			Optional<ItemStack> found = shulker.nonEmptyItemCopyStream().filter(stack -> ItemStack.isSameItemSameComponents(stack, target)).findFirst();
			if (found.isPresent()) {
				return true;
			}
		}
		BundleContents bundle = container.get(DataComponents.BUNDLE_CONTENTS);
		if (bundle != null) {
			Optional<ItemStack> found = bundle.itemCopies().filter(stack -> ItemStack.isSameItemSameComponents(stack, target)).findFirst();
			if (found.isPresent()) {
				return true;
			}
		}
		return false;
	}
}
