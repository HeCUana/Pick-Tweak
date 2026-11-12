package fi.dy.masa.picktweak.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版物品 ID 字符串解析：支持 {@code minecraft:item} 与
 * {@code minecraft:item[custom_data={...}]} 这类组件写法。
 *
 * <p>客户端与服务端共用，保证 overrides 的结果一致。</p>
 */
public final class ItemStackParser {

	private ItemStackParser() {
	}

	public static ItemStack parse(String text, RegistryAccess registries) {
		if (text == null || text.isBlank()) {
			return ItemStack.EMPTY;
		}
		String trimmed = text.trim();

		// 完整物品 SNBT / JSON（例如 {"id":"minecraft:spawner","count":1,"components":{...}}）。
		// 这是 overrides 里「完全支持 NBT 数据」的写法。
		if (trimmed.startsWith("{")) {
			return parseFullStack(trimmed, registries);
		}

		String idPart = trimmed;
		String componentPart = null;

		int bracket = trimmed.indexOf('[');
		if (bracket >= 0 && trimmed.endsWith("]")) {
			idPart = trimmed.substring(0, bracket).trim();
			componentPart = trimmed.substring(bracket + 1, trimmed.length() - 1).trim();
		}

		Identifier id = Identifier.tryParse(idPart);
		if (id == null) {
			return ItemStack.EMPTY;
		}
		Item item = BuiltInRegistries.ITEM.getValue(id);
		if (item == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = new ItemStack(item);
		if (componentPart != null && !componentPart.isEmpty()) {
			applyComponents(stack, componentPart, registries);
		}
		return stack;
	}

	/** 解析完整的物品 SNBT / JSON 对象（含 count / components）。 */
	private static ItemStack parseFullStack(String text, RegistryAccess registries) {
		if (registries == null) {
			return ItemStack.EMPTY;
		}
		// 先按 JSON 解析（overrides.json 里最自然的写法），再按 SNBT 解析。
		try {
			var ops = RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries);
			var element = JsonParser.parseString(text);
			var result = ItemStack.CODEC.parse(ops, element).result();
			if (result.isPresent()) {
				return result.get();
			}
		} catch (Exception ignored) {
			// 落到 SNBT
		}
		try {
			CompoundTag tag = TagParser.parseCompoundFully(text);
			var ops = RegistryOps.create(NbtOps.INSTANCE, registries);
			var result = ItemStack.CODEC.parse(ops, tag).result();
			if (result.isPresent()) {
				return result.get();
			}
		} catch (Exception ignored) {
			// 无法解析：返回空
		}
		return ItemStack.EMPTY;
	}

	private static void applyComponents(ItemStack stack, String componentPart, RegistryAccess registries) {
		if (registries == null) {
			return;
		}
		// 优先走官方的组件补丁管线。
		try {
			JsonObject patch = new JsonObject();
			for (String part : splitComponents(componentPart)) {
				int eq = part.indexOf('=');
				if (eq <= 0) {
					continue;
				}
				String key = part.substring(0, eq).trim();
				String value = part.substring(eq + 1).trim();
				if (!key.contains(":")) {
					key = "minecraft:" + key;
				}
				patch.add(key, JsonParser.parseString(value));
			}
			RegistryOps<JsonElement> ops = RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries);
			DataComponentPatch parsed = DataComponentPatch.CODEC.parse(ops, patch).result().orElse(null);
			if (parsed != null) {
				stack.applyComponents(parsed);
				return;
			}
		} catch (Exception ignored) {
			// 落到下面的手动处理
		}

		// 兜底：手动处理最常见的 custom_data={...}。
		for (String part : splitComponents(componentPart)) {
			int eq = part.indexOf('=');
			if (eq <= 0) {
				continue;
			}
			String key = part.substring(0, eq).trim();
			String value = part.substring(eq + 1).trim();
			if (!"custom_data".equals(key)) {
				continue;
			}
			try {
				JsonElement element = JsonParser.parseString(value);
				if (element.isJsonObject()) {
					CustomData data = CustomData.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, element).result().orElse(null);
					if (data != null) {
						stack.set(DataComponents.CUSTOM_DATA, data);
					}
				}
			} catch (Exception ignored) {
				// 忽略无法解析的组件
			}
		}
	}

	/** 按顶层逗号切分组件表达式（忽略花括号内的逗号）。 */
	private static List<String> splitComponents(String text) {
		List<String> parts = new ArrayList<>();
		int depth = 0;
		StringBuilder current = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (c == '{' || c == '[' || c == '(') {
				depth++;
			} else if (c == '}' || c == ']' || c == ')') {
				depth--;
			}
			if (c == ',' && depth == 0) {
				parts.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		if (current.length() > 0) {
			parts.add(current.toString());
		}
		return parts;
	}
}