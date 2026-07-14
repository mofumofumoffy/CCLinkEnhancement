package moffy.cclink_enhancement.client;

import com.mojang.blaze3d.vertex.PoseStack;
import moffy.cclink_enhancement.CCLinkEnhancement;
import moffy.cclink_enhancement.client.elements.EnhancedOverlayElement;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CCLinkEnhancement.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EnhancedHUDOverlay {


    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            renderHUD(event.getGuiGraphics(), player.getUUID());
        }
    }

    private static void renderHUD(GuiGraphics guiGraphics, UUID playerUUID) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = guiGraphics.pose();

        for(ElementType elementType : ElementType.values()){
            EnhancedOverlayElement.ElementRenderer elementRenderer = EnhancedOverlayElement.RENDERERS.get(elementType);
            if(elementRenderer != null){
                Map<String, EnhancedOverlayElement> elementMap = elementRenderer.renderer().get(playerUUID);
                if(elementMap != null){
                    for(EnhancedOverlayElement element : elementMap.values()){
                        element.render(mc, poseStack, guiGraphics);
                    }
                }
            }
        }
    }
}
