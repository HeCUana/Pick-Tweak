package fi.dy.masa.picktweak.pick;

import com.mojang.logging.LogUtils;
import fi.dy.masa.picktweak.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 统一的「实体 → 拾取物品」构建器（服务端与客户端回退共用）。
 *
 * <p>普通拾取沿用 masa 风格：生物给头颅（可配置）、物品展示框 / 画作给本体物品。</p>
 *
 * <p>按住 Ctrl 拾取时，把实体的完整数据（生命值、装备、AI 状态、姿势……）写进
 * {@code minecraft:entity_data} 组件，物品本身选择「放下后能还原实体」的那一个：</p>
 *
 * <ul>
 *   <li>有刷怪蛋的生物 → 对应刷怪蛋 + entity_data（放下即生成带数据的生物）</li>
 *   <li>盔甲架 → 盔甲架物品 + entity_data（保留姿势与装备）</li>
 *   <li>物品展示框 → 展示框物品 + entity_data（保留框内物品、旋转、是否隐形 / 固定）</li>
 *   <li>画作 → 画作物品 + {@code painting_variant} 组件（保留确切变种）</li>
 *   <li>玩家 → 玩家头颅（带皮肤）</li>
 * </ul>
 *
 * <p>数据里会剔除位置 / 速度 / UUID / 朝向这类「放下时由放置动作决定」的字段，
 * 避免生成出来的实体瞬移到旧坐标或和原实体抢 UUID。</p>
 */
public final class EntityPickBuilder {

	private static final Logger LOGGER = LogUtils.getLogger();

	/** 放下实体时由放置动作决定、必须剔除的字段。 */
	private static final List<String> VOLATILE_KEYS = List.of(
			"id", "UUID", "Pos", "Motion", "Rotation", "FallDistance",
			"PortalCooldown", "OnGround", "Air", "fire", "HurtTime", "HurtByTimestamp");

	/** 物品展示框只需要保留这些字段（别的字段由放置动作决定）。 */
	private static final List<String> FRAME_KEYS = List.of(
			"Item", "ItemRotation", "ItemDropChance", "Invisible", "Fixed");

	/** 常见生物对应的头颅物品（不按 Ctrl 时使用）。 */
	private static final Map<String, Item> HEADS = Map.of(
			"minecraft:zombie", Items.ZOMBIE_HEAD,
			"minecraft:skeleton", Items.SKELETON_SKULL,
			"minecraft:creeper", Items.CREEPER_HEAD,
			"minecraft:piglin", Items.PIGLIN_HEAD,
			"minecraft:dragon", Items.DRAGON_HEAD
	);

	private EntityPickBuilder() {
	}

	/**
	 * 构建拾取物品。
	 *
	 * @param includeData        是否按住 Ctrl（把实体数据写进物品）
	 * @param preferEntityHeads  不按 Ctrl 时是否优先给生物头颅
	 */
	public static ItemStack build(Level level, Entity entity, boolean includeData, boolean preferEntityHeads) {
		if (entity == null) {
			return ItemStack.EMPTY;
		}

		if (includeData) {
			ItemStack stack = buildWithData(level, entity);
			if (!stack.isEmpty()) {
				return stack;
			}
		}

		return buildPlain(level, entity, preferEntityHeads);
	}

	/** 普通拾取（不按 Ctrl）。 */
	private static ItemStack buildPlain(Level level, Entity entity, boolean preferEntityHeads) {
		if (entity instanceof Player player) {
			return playerHead(player);
		}
		if (entity instanceof ItemFrame frame) {
			return new ItemStack(frame instanceof GlowItemFrame ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME);
		}
		if (entity instanceof Painting) {
			ItemStack picked = entity.getPickResult();
			return picked == null || picked.isEmpty() ? new ItemStack(Items.PAINTING) : picked.copy();
		}
		if (preferEntityHeads) {
			Item head = headFor(entity);
			if (head != null) {
				return new ItemStack(head);
			}
		}
		Item egg = spawnEggItem(entity.getType());
		if (egg != null) {
			return new ItemStack(egg);
		}
		ItemStack picked = entity.getPickResult();
		return picked == null ? ItemStack.EMPTY : picked.copy();
	}

	/** 按住 Ctrl 的拾取：物品 + 实体数据。 */
	private static ItemStack buildWithData(Level level, Entity entity) {
		if (entity instanceof Player player) {
			// 玩家没有能还原成玩家的物品，仍然给带皮肤的玩家头颅
			return playerHead(player);
		}

		CompoundTag tag = entityTag(level, entity);
		if (tag == null || tag.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if (entity instanceof ItemFrame frame) {
			Item item = frame instanceof GlowItemFrame ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME;
			ItemStack stack = new ItemStack(item);
			stack.set(DataComponents.ENTITY_DATA, typed(entity.getType(), frameTag(tag)));
			return stack;
		}

		if (entity instanceof Painting painting) {
			ItemStack stack = new ItemStack(Items.PAINTING);
			// 26.3 的画作物品通过 painting_variant 组件决定放下后的画作
			stack.set(DataComponents.PAINTING_VARIANT, painting.getVariant());
			return stack;
		}

		CompoundTag sanitized = sanitize(tag);

		if (entity instanceof ArmorStand) {
			ItemStack stack = new ItemStack(Items.ARMOR_STAND);
			stack.set(DataComponents.ENTITY_DATA, typed(EntityTypes.ARMOR_STAND, sanitized));
			return stack;
		}

		Item egg = spawnEggItem(entity.getType());
		if (egg != null) {
			ItemStack stack = new ItemStack(egg);
			stack.set(DataComponents.ENTITY_DATA, typed(entity.getType(), sanitized));
			return stack;
		}

		// 没有刷怪蛋的实体：退回到它自己的拾取物品，能带上数据就带上
		ItemStack picked = entity.getPickResult();
		if (picked == null || picked.isEmpty()) {
			return ItemStack.EMPTY;
		}
		picked = picked.copy();
		picked.set(DataComponents.ENTITY_DATA, typed(entity.getType(), sanitized));
		return picked;
	}

	/** 生物对应的头颅物品；没有则 null。 */
	private static Item headFor(Entity entity) {
		if (entity.getType() == null) {
			return null;
		}
		net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return id == null ? null : HEADS.get(id.toString());
	}

	/** 实体类型对应的刷怪蛋物品；没有则 null。测试也直接用它。 */
	public static Item spawnEggItem(EntityType<?> type) {
		if (type == null) {
			return null;
		}
		try {
			Optional<net.minecraft.core.Holder<Item>> egg = SpawnEggItem.byId(type);
			return egg.map(net.minecraft.core.Holder::value).orElse(null);
		} catch (Throwable t) {
			// 物品默认组件还没绑定（例如裸 JVM 冒烟测试）时，退回到「没有刷怪蛋」
			LOGGER.debug("[{}] 查询刷怪蛋失败", Reference.MOD_NAME, t);
			return null;
		}
	}

	/** 玩家头颅（带皮肤）；拿不到 profile 时给普通头颅。 */
	private static ItemStack playerHead(Player player) {
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		try {
			stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
		} catch (Exception ignored) {
			// 皮肤拿不到时给一个普通头颅
		}
		return stack;
	}

	/** 读取实体 NBT（不含 id）。 */
	public static CompoundTag entityTag(Level level, Entity entity) {
		try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(collector, level.registryAccess());
			entity.saveWithoutId(output);
			CompoundTag tag = output.buildResult();
			tag.remove("id");
			return tag;
		} catch (Exception e) {
			LOGGER.debug("[{}] 读取实体 NBT 失败", Reference.MOD_NAME, e);
			return null;
		}
	}

	/** 剔除「由放置动作决定」的字段（位置 / 速度 / UUID / 朝向……）。 */
	public static CompoundTag sanitize(CompoundTag tag) {
		CompoundTag result = tag.copy();
		for (String key : VOLATILE_KEYS) {
			result.remove(key);
		}
		return result;
	}

	/** 物品展示框只保留展示相关的字段。 */
	public static CompoundTag frameTag(CompoundTag tag) {
		CompoundTag result = new CompoundTag();
		for (String key : FRAME_KEYS) {
			Tag value = tag.get(key);
			if (value != null) {
				result.put(key, value.copy());
			}
		}
		return result;
	}

	/** 构造 entity_data 组件（TypedEntityData 内部会处理 id）。 */
	private static TypedEntityData<EntityType<?>> typed(EntityType<?> type, CompoundTag tag) {
		return TypedEntityData.<EntityType<?>>of(type, tag);
	}
}
