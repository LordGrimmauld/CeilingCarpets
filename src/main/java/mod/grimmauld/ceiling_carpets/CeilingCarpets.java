package mod.grimmauld.ceiling_carpets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CeilingCarpets.MODID)
public class CeilingCarpets {
	public static final String MODID = "ceiling_carpets";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public CeilingCarpets() {
		MinecraftForge.EVENT_BUS.register(new EventListener());
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(EventListener::onModelBake);
		});
	}
}
