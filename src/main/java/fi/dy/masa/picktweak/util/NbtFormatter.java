package fi.dy.masa.picktweak.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * NBT 的过滤与格式化输出。
 *
 * <ul>
 *   <li>黑名单按键"路径"逐条剔除（大小写不敏感）。</li>
 *   <li>支持单行与缩进美化两种格式。</li>
 * </ul>
 *
 * <p>黑名单按路径匹配，而不是按名字到处删：写 {@code id} 只删顶层的 id，
 * 嵌套的物品 id（{@code Items[0].id}）不受影响；要删嵌套的写完整路径，
 * 比如 {@code Items.id}。否则箱子里的东西会被连 id 一起删掉。</p>
 */
public final class NbtFormatter {

	private NbtFormatter() {
	}

	/** 按黑名单过滤一份 NBT（返回新的副本）。 */
	public static CompoundTag filter(CompoundTag source, Collection<String> blacklist) {
		if (source == null) {
			return null;
		}
		if (blacklist == null || blacklist.isEmpty()) {
			return source.copy();
		}
		return (CompoundTag) filterTag(source, blacklist, "");
	}

	private static Tag filterTag(Tag tag, Collection<String> blacklist, String path) {
		if (tag instanceof CompoundTag compound) {
			CompoundTag copy = new CompoundTag();
			for (String key : compound.keySet()) {
				String childPath = path.isEmpty() ? key : path + "." + key;
				if (isBlacklisted(childPath, blacklist)) {
					continue;
				}
				copy.put(key, filterTag(compound.get(key), blacklist, childPath));
			}
			return copy;
		}
		if (tag instanceof ListTag list) {
			ListTag result = new ListTag();
			int index = 0;
			for (Tag element : list) {
				String childPath = path + "[" + index + "]";
				Tag filtered = filterTag(element, blacklist, childPath);
				if (filtered != null) {
					// 用 add 而不是 addAndUnwrap：后者会把复合标签拆散
					result.add(filtered);
				}
				index++;
			}
			return result;
		}
		return tag;
	}

	/**
	 * 判断某个路径是否在黑名单里。
	 *
	 * <p>列表下标会被忽略，所以 {@code Items[0].id} 和 {@code Items[1].id}
	 * 都按 {@code Items.id} 匹配。</p>
	 */
	private static boolean isBlacklisted(String path, Collection<String> blacklist) {
		String normalized = normalizePath(path);
		for (String entry : blacklist) {
			if (entry == null || entry.isEmpty()) {
				continue;
			}
			if (normalized.equalsIgnoreCase(entry)) {
				return true;
			}
		}
		return false;
	}

	/** 把路径里的列表下标去掉，便于按路径匹配。 */
	private static String normalizePath(String path) {
		StringBuilder builder = new StringBuilder(path.length());
		int depth = 0;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '[') {
				depth++;
				continue;
			}
			if (c == ']') {
				depth--;
				continue;
			}
			if (depth == 0) {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	/**
	 * 生成可以直接粘进命令的 NBT 文本（过滤黑名单 + 去掉 id）。
	 *
	 * @param source 原始 NBT
	 * @param blacklist 要剔除的键（按路径）
	 * @param pretty 是否缩进美化
	 * @return SNBT 文本；没有可复制的内容时返回 {@code null}
	 */
	public static String copyText(CompoundTag source, Collection<String> blacklist, boolean pretty) {
		CompoundTag filtered = filter(source, blacklist);
		if (filtered == null || filtered.isEmpty()) {
			return null;
		}
		String text = format(filtered, pretty);
		return text.isEmpty() ? null : text;
	}

	/** 单行输出。 */
	public static String compact(Tag tag) {
		if (tag == null) {
			return "";
		}
		return new SnbtPrinterTagVisitor("", 0, new ArrayList<>()).visit(tag);
	}

	/** 缩进美化输出。 */
	public static String pretty(Tag tag) {
		if (tag == null) {
			return "";
		}
		return new SnbtPrinterTagVisitor().visit(tag);
	}

	/** 按配置选择格式。 */
	public static String format(Tag tag, boolean pretty) {
		return pretty ? pretty(tag) : compact(tag);
	}
}
