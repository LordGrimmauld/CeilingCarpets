package mod.grimmauld.ceiling_carpets;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CeilingCarpetTileEntity extends TileEntity {
	public static final ModelProperty<BlockState> CARPET_BLOCK = new ModelProperty<>();
	public static final ModelProperty<BlockPos> POSITION = new ModelProperty<>();
	private BlockState carpetBlock = Blocks.AIR.getDefaultState();
	@OnlyIn(value = Dist.CLIENT)
	private IModelData modelData;

	public CeilingCarpetTileEntity() {
		super(RegistryEntries.CEILING_CARPET_TILE_ENTITY);
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this::initDataMap);
	}

	@OnlyIn(value = Dist.CLIENT)
	private void initDataMap() {
		modelData = new ModelDataMap.Builder()
			.withInitial(CARPET_BLOCK, Blocks.AIR.getDefaultState()).withInitial(POSITION, BlockPos.ZERO).build();
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		BlockState before = carpetBlock;
		carpetBlock = NBTUtil.readBlockState(compound.getCompound("CarpetBlock"));
		super.read(state, compound);
		if (!before.equals(carpetBlock))
			requestModelDataUpdate();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("CarpetBlock", NBTUtil.writeBlockState(getCarpetBlock()));
		return super.write(compound);
	}

	public BlockState getCarpetBlock() {
		return carpetBlock;
	}

	public void setCarpetBlock(BlockState carpetBlock) {
		this.carpetBlock = carpetBlock;
		markDirty();
	}

	@OnlyIn(value = Dist.CLIENT)
	@Override
	public IModelData getModelData() {
		modelData.setData(CARPET_BLOCK, carpetBlock);
		modelData.setData(POSITION, pos);
		return modelData;
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		read(state, tag);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(getPos(), 1, write(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(getBlockState(), pkt.getNbtCompound());
	}
}
