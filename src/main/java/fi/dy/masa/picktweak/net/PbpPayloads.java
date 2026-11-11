package fi.dy.masa.picktweak.net;

import fi.dy.masa.picktweak.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * PBP 的客户端 -> 服务端数据包。
 *
 * <p>服务端安装了本模组时，客户端的拾取请求会走这里，由服务端权威地计算并发放物品；
 * 服务端没装时客户端会自动回退到原版拾取流程。</p>
 *
 * <p>通道 ID 使用 {@code picktweak} 命名空间，注册集中在 {@link PbpServerNetworking}。
 * record 的字段与构造器由客户端直接 new，保持稳定。</p>
 */
public final class PbpPayloads {

	public static final Identifier PICK_BLOCK_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "pick_block");
	public static final Identifier PICK_ENTITY_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "pick_entity");
	public static final Identifier GIVE_TOOL_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "give_tool");
	public static final Identifier PICK_CELESTIAL_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "pick_celestial");
	public static final Identifier RELOAD_ID = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "reload");

	/** 拾取方块。 */
	public record PickBlock(BlockPos pos, boolean includeData, boolean includeState) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<PickBlock> TYPE = new CustomPacketPayload.Type<>(PICK_BLOCK_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, PickBlock> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, PickBlock::pos,
				ByteBufCodecs.BOOL, PickBlock::includeData,
				ByteBufCodecs.BOOL, PickBlock::includeState,
				PickBlock::new);

		@Override
		public CustomPacketPayload.Type<PickBlock> type() {
			return TYPE;
		}
	}

	/** 拾取实体。 */
	public record PickEntity(int entityId, boolean includeData) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<PickEntity> TYPE = new CustomPacketPayload.Type<>(PICK_ENTITY_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, PickEntity> CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, PickEntity::entityId,
				ByteBufCodecs.BOOL, PickEntity::includeData,
				PickEntity::new);

		@Override
		public CustomPacketPayload.Type<PickEntity> type() {
			return TYPE;
		}
	}

	/** 请求服务端发放一件带附魔的工具 / 武器（请求内容为 JSON 字符串）。 */
	public record GiveTool(String request) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<GiveTool> TYPE = new CustomPacketPayload.Type<>(GIVE_TOOL_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, GiveTool> CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, GiveTool::request,
				GiveTool::new);

		@Override
		public CustomPacketPayload.Type<GiveTool> type() {
			return TYPE;
		}
	}

	/** 拾取太阳 / 月亮。 */
	public record PickCelestial(String kind) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<PickCelestial> TYPE = new CustomPacketPayload.Type<>(PICK_CELESTIAL_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, PickCelestial> CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, PickCelestial::kind,
				PickCelestial::new);

		@Override
		public CustomPacketPayload.Type<PickCelestial> type() {
			return TYPE;
		}
	}

	/** 请求服务端重新载入自定义映射。 */
	public record Reload() implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Reload> TYPE = new CustomPacketPayload.Type<>(RELOAD_ID);
		public static final StreamCodec<RegistryFriendlyByteBuf, Reload> CODEC = StreamCodec.unit(new Reload());

		@Override
		public CustomPacketPayload.Type<Reload> type() {
			return TYPE;
		}
	}

	private PbpPayloads() {
	}
}
