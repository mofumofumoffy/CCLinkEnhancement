package moffy.cclink_enhancement.client.atlas;

import moffy.cclink_enhancement.CCLinkEnhancement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

public final class HudAtlas extends TextureAtlasHolder {
    public static final ResourceLocation ATLAS_LOCATION = CCLinkEnhancement.getResource("textures/atlas/hud.png");
    public static final ResourceLocation ATLAS_INFO_LOCATION = CCLinkEnhancement.getResource("hud");

    public static HudAtlas INSTANCE;

    public static void init(RegisterClientReloadListenersEvent event, TextureManager textureManager){
        INSTANCE = new HudAtlas(textureManager);
        event.registerReloadListener(INSTANCE);
    }

    public HudAtlas(TextureManager pTextureManager) {
        super(pTextureManager, ATLAS_LOCATION, ATLAS_INFO_LOCATION);
    }

    public TextureAtlasSprite get(ResourceLocation spriteId) {
        return getSprite(spriteId);
    }
}
