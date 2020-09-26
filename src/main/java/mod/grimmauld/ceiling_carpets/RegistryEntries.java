package mod.grimmauld.ceiling_carpets;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class RegistryEntries {
	@ObjectHolder(CeilingCarpets.MODID + ":ceiling_carpet")
	public static CeilingCarpetBlock CEILING_CARPET_BLOCK;

	@ObjectHolder(CeilingCarpets.MODID + ":ceiling_carpet")
	public static TileEntityType<CeilingCarpetTileEntity> CEILING_CARPET_TILE_ENTITY;
}
