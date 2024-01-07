package elocindev.geckoanimfix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//#if FABRIC==1
import net.fabricmc.api.ModInitializer;
//#else
//$$ import net.minecraftforge.fml.common.Mod;
//#endif

//#if FORGE==1
//$$ @Mod(CommonInitializer.MODID)
//$$ public class CommonInitializer {
//#else
public class CommonInitializer implements ModInitializer {
//#endif
    public static final String MODID = "geckoanimfix";
    public static final Logger LOGGER = LoggerFactory.getLogger("geckoanimfix");
    public static final String VERSION = "1.0.0";

    //#if FABRIC==1
    @Override
    public void onInitialize() {
        LOGGER.info("Gecko Anim Fix Initialized");
    }
    //#endif
}