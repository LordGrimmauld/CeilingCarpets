package mod.grimmauld.ceiling_carpets;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class CeilingCarpetBlock extends Block {
	public CeilingCarpetBlock() {
		super(Block.Properties.create(Material.CARPET, MaterialColor.SNOW).notSolid());
	}

	private static VoxelShape flipShape(VoxelShape shape) {
		AtomicReference<VoxelShape> buffer = new AtomicReference<>(VoxelShapes.empty());
		shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer.set(VoxelShapes.or(VoxelShapes.create(minX, 1 - minY, minZ, maxX, 1 - maxY, maxZ), buffer.get())));
		return buffer.get();
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return !worldIn.isAirBlock(pos.up());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return flipShape(getCarpetState(worldIn, pos).getShape(worldIn, pos));
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CeilingCarpetTileEntity();
	}

	@Override
	public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return getCarpetState(worldIn, pos).getMaterialColor(worldIn, pos);
	}

	@Nullable
	private CeilingCarpetTileEntity getTileEntity(IBlockReader world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CeilingCarpetTileEntity)
			return (CeilingCarpetTileEntity) te;
		return null;
	}

	private BlockState getCarpetState(IBlockReader reader, BlockPos pos) {
		CeilingCarpetTileEntity te = getTileEntity(reader, pos);
		if (te != null)
			return te.getCarpetBlock();
		return Blocks.AIR.getDefaultState();
	}

	@OnlyIn(Dist.CLIENT)
	public IBakedModel createModel(IBakedModel original) {
		return new CeilingCarpetModel(original);
	}

	@Override
	public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
		return getCarpetState(world, pos).getSoundType(world, pos, entity);
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld world, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		CeilingCarpetTileEntity te = getTileEntity(world, pos);
		if (te != null) {
			return te.getCarpetBlock().addLandingEffects(world, pos, state2, entity, numberOfParticles / 2);
		}
		return false;
	}

	@Override
	public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
		CeilingCarpetTileEntity te = getTileEntity(world, pos);
		if (te != null) {
			return te.getCarpetBlock().addRunningEffects(world, pos, entity);
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		CeilingCarpetTileEntity te = getTileEntity(world, pos);
		if (te != null) {
			return te.getCarpetBlock().addDestroyEffects(world, pos, manager);
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
		if (target.getType() != RayTraceResult.Type.BLOCK || !(target instanceof BlockRayTraceResult))
			return false;
		BlockPos pos = ((BlockRayTraceResult) target).getPos();
		CeilingCarpetTileEntity te = getTileEntity(world, pos);
		if (te != null) {
			return te.getCarpetBlock().addHitEffects(world, target, manager);
		}
		return false;
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		CeilingCarpetTileEntity te = getTileEntity(world, pos);
		if (te != null) {
			BlockState carpetState = te.getCarpetBlock();
			carpetState.getBlock().getLightValue(carpetState, world, pos);
		}
		return 0;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return getCarpetState(world, pos).getPickBlock(target, world, pos, player);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
		if (!(tileentity instanceof CeilingCarpetTileEntity))
			return Collections.emptyList();
		return ((CeilingCarpetTileEntity) tileentity).getCarpetBlock().getDrops(builder);
	}

	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		return getCarpetState(worldIn, pos).getBlockHardness(worldIn, pos);
	}
}
