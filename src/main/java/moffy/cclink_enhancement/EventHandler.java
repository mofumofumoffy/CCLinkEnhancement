package moffy.cclink_enhancement;

import com.awesoft.cclink.registration.TabInit;
import moffy.cclink_enhancement.client.atlas.HudAtlas;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

public class EventHandler {
    public static void addItemToTab(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey() == TabInit.CCLINK_TAB_UPGRADES.getKey()){
            event.accept(Registry.ENHANCED_OVERLAY_UPGRADE.get());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event){
        HudAtlas.init(event, Minecraft.getInstance().textureManager);
    }
}
