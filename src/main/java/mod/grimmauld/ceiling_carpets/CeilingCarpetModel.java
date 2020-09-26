package mod.grimmauld.ceiling_carpets;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static mod.grimmauld.ceiling_carpets.CeilingCarpetTileEntity.CARPET_BLOCK;
import static mod.grimmauld.ceiling_carpets.CeilingCarpetTileEntity.POSITION;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CeilingCarpetModel extends BakedModelWrapper<IBakedModel> {
	public CeilingCarpetModel(IBakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState carpetState = data.getData(CARPET_BLOCK);
		BlockPos position = data.getData(POSITION);
		ClientWorld world = Minecraft.getInstance().world;
		List<BakedQuad> quads = new ArrayList<>();
		if (world == null || position == null || carpetState == null)
			return quads;

		RenderType renderType = MinecraftForgeClient.getRenderLayer();
		if (RenderTypeLookup.canRenderInLayer(carpetState, renderType) && carpetState.getRenderType() == BlockRenderType.MODEL) {
			IBakedModel partialModel = dispatcher.getModelForState(carpetState);
			IModelData modelData = partialModel.getModelData(world, position, carpetState, EmptyModelData.INSTANCE);
			List<BakedQuad> carpetModels = partialModel.getQuads(carpetState, side != null ? side.getOpposite() : null, rand, modelData);
			carpetModels.stream().map(this::flipQuad).forEachOrdered(quads::add);
		}
		return quads;
	}

	private BakedQuad flipQuad(BakedQuad bakedQuad) {
		int[] data = bakedQuad.getVertexData().clone();
		int xZMirror = bakedQuad.getFace().getAxis() == Direction.Axis.X ? 0 : 2;
		for (int i = 0; i < 4; ++i) {
			int j = data.length / 4 * i;
			data[j + 1] = Float.floatToIntBits(1 - Float.intBitsToFloat(data[j + 1]));
			data[j + xZMirror] = Float.floatToIntBits(1 - Float.intBitsToFloat(data[j + xZMirror]));
		}
		return new BakedQuad(data, bakedQuad.getTintIndex(), bakedQuad.getFace().getOpposite(), bakedQuad.func_187508_a(), bakedQuad.shouldApplyDiffuseLighting());
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IModelData data) {
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState carpetState = data.getData(CARPET_BLOCK);
		if (carpetState == null)
			return super.getParticleTexture(data);
		return dispatcher.getModelForState(carpetState).getParticleTexture(data);
	}
}
