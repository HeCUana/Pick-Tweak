package fi.dy.masa.picktweak.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * NBT 的过滤与格式化输出。
 *
 * <ul>
 *   <li>黑名单按键名逐条剔除（大小写不敏感）。</li>
 *   <li>支持单行与缩进美化两种格式。</li>
 * </ul>
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
		CompoundTag copy = new CompoundTag();
		for (String key : source.keySet()) {
			if (isBlacklisted(key, blacklist)) {
				continue;
			}
			Tag value = source.get(key);
			copy.put(key, filterTag(value, blacklist));
		}
		return copy;
	}

	private static Tag filterTag(Tag tag, Collection<String> blacklist) {
		if (tag instanceof CompoundTag compound) {
			return filter(compound, blacklist);
		}
		if (tag instanceof ListTag list) {
			ListTag result = new ListTag();
			for (Tag element : list) {
				Tag filtered = filterTag(element, blacklist);
				if (filtered != null) {
					// 用 add 而不是 addAndUnwrap：后者会把复合标签拆散
					result.add(filtered);
				}
			}
			return result;
		}
		return tag;
	}

	private static boolean isBlacklisted(String key, Collection<String> blacklist) {
		String lower = key.toLowerCase(Locale.ROOT);
		for (String entry : blacklist) {
			if (entry == null) {
				continue;
			}
			String candidate = entry.toLowerCase(Locale.ROOT);
			if (lower.equals(candidate) || lower.endsWith("." + candidate)) {
				return true;
			}
		}
		return false;
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
