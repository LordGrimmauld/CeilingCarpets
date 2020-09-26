package mod.grimmauld.ceiling_carpets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unused")
public class EventListener {
	@OnlyIn(Dist.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
		swapModels(modelRegistry, getAllBlockStateModelLocations(RegistryEntries.CEILING_CARPET_BLOCK), RegistryEntries.CEILING_CARPET_BLOCK::createModel);
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
															 List<ModelResourceLocation> locations, Function<IBakedModel, T> factory) {
		locations.forEach(location -> swapModels(modelRegistry, location, factory));
	}

	@OnlyIn(Dist.CLIENT)
	protected static <T extends IBakedModel> void swapModels(Map<ResourceLocation, IBakedModel> modelRegistry,
															 ModelResourceLocation location, Function<IBakedModel, T> factory) {
		modelRegistry.put(location, factory.apply(modelRegistry.get(location)));
	}

	@OnlyIn(Dist.CLIENT)
	protected static List<ModelResourceLocation> getAllBlockStateModelLocations(Block block) {
		List<ModelResourceLocation> models = new ArrayList<>();
		block.getStateContainer().getValidStates().forEach(state -> {
			ModelResourceLocation rl = getBlockModelLocation(block, BlockModelShapes.getPropertyMapString(state.getValues()));
			if (rl != null)
				models.add(rl);
		});
		return models;
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	protected static ModelResourceLocation getBlockModelLocation(Block block, String suffix) {
		ResourceLocation rl = block.getRegistryName();
		if (rl == null)
			return null;
		return new ModelResourceLocation(rl, suffix);
	}

	@SubscribeEvent
	public void rclickEvent(PlayerInteractEvent.RightClickBlock event) {
		if (event.getFace() != Direction.DOWN)
			return;
		if (event.getUseItem() == Event.Result.DENY)
			return;
		if (event.getEntityLiving().isSneaking())
			return;
		if (!event.getPlayer().isAllowEdit())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty())
			return;
		if (!(stack.getItem() instanceof BlockItem))
			return;
		BlockItem item = (BlockItem) stack.getItem();
		if (!item.isIn(ItemTags.CARPETS) && !item.getBlock().isIn(BlockTags.CARPETS))
			return;

		BlockPos pos = event.getPos().offset(event.getFace(), 1);
		World world = event.getWorld();
		if (!world.getBlockState(pos).isAir(world, pos))
			return;
		BlockState ceilingCarpetState = RegistryEntries.CEILING_CARPET_BLOCK.getDefaultState();
		if (!RegistryEntries.CEILING_CARPET_BLOCK.isValidPosition(ceilingCarpetState, world, pos))
			return;
		world.setBlockState(pos, ceilingCarpetState);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof CeilingCarpetTileEntity) {
			CeilingCarpetTileEntity ceilingCarpetTileEntity = (CeilingCarpetTileEntity) te;
			((CeilingCarpetTileEntity) te).setCarpetBlock(item.getBlock().getDefaultState());
			SoundType soundType = item.getBlock().getSoundType(item.getBlock().getDefaultState(), world, pos, event.getPlayer());
			world.playSound(null, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
			ceilingCarpetTileEntity.requestModelDataUpdate();
			if (!event.getPlayer().isCreative())
				stack.shrink(1);
			event.getPlayer().swingArm(event.getHand());
		}
		event.setCanceled(true);
	}

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event) {
			CeilingCarpets.LOGGER.debug("blocks registering");
			event.getRegistry().register(new CeilingCarpetBlock().setRegistryName("ceiling_carpet"));
		}

		@SubscribeEvent
		public static void registerTEs(final RegistryEvent.Register<TileEntityType<?>> event) {
			CeilingCarpets.LOGGER.debug("TEs registering");
			event.getRegistry().register(TileEntityType.Builder.create(CeilingCarpetTileEntity::new, RegistryEntries.CEILING_CARPET_BLOCK).build(null).setRegistryName("ceiling_carpet"));
		}
	}


}
