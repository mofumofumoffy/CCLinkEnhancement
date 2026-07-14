package moffy.cclink_enhancement;


import com.mojang.logging.LogUtils;
import moffy.cclink_enhancement.network.EnhancedHUDOverlayUpdatePacket;
import moffy.cclink_enhancement.network.PacketIDs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

@Mod(CCLinkEnhancement.MODID)
public class CCLinkEnhancement {
    public static final String MODID = "cclink_enhancement";

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            getResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public CCLinkEnhancement(FMLJavaModLoadingContext context){
        IEventBus bus = context.getModEventBus();
        Registry.ITEMS.register(bus);

        CHANNEL.messageBuilder(EnhancedHUDOverlayUpdatePacket.class, PacketIDs.ENHANCED_OVERLAY_UPDATE)
                .encoder(EnhancedHUDOverlayUpdatePacket::encode)
                .decoder(EnhancedHUDOverlayUpdatePacket::decode)
                .consumerMainThread(EnhancedHUDOverlayUpdatePacket::handle)
                .add();

        bus.addListener(EventHandler::addItemToTab);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ()->()->{
            bus.addListener(EventHandler::registerReloadListeners);
        });
    }

    public static ResourceLocation getResource(String path){
        return ResourceLocation.fromNamespaceAndPath(CCLinkEnhancement.MODID, path);
    }
}
