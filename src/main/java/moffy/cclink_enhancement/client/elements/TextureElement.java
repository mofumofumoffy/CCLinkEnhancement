package moffy.cclink_enhancement.client.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import moffy.cclink_enhancement.client.atlas.HudAtlas;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TextureElement extends EnhancedOverlayElement{
    public static final Map<UUID, Map<String, EnhancedOverlayElement>> TEXTURES = new HashMap<>();

    @Nullable
    private final ResourceLocation textureLoc;

    private final int uWidth, vHeight;
    private final int x, y;
    private final int scale;

    TextureAtlasSprite spriteCache = null;

    public TextureElement(IArguments args) throws LuaException {
        this(ResourceLocation.tryParse(args.getString(1)), args.getInt(2), args.getInt(3), args.getInt(4), args.getInt(5), args.getInt(6));
    }

    public TextureElement(@Nullable  ResourceLocation textureLoc, int uWidth, int vHeight, int x, int y, int scale) {
        super();
        this.textureLoc = textureLoc;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    protected TextureAtlasSprite getSprite(){
        if(textureLoc != null && spriteCache == null){
            spriteCache = HudAtlas.INSTANCE.get(textureLoc.withPrefix("hud/"));
        }
        return spriteCache;
    }


    @Override
    public void render(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics) {
        if(this.textureLoc != null){
            TextureAtlasSprite sprite = getSprite();

            poseStack.pushPose();

            poseStack.scale(this.scale, this.scale, 1.0F);

            int scaledX = this.x / this.scale;
            int scaledY = this.y / this.scale;

            int scaledUWidth = this.uWidth * this.scale;
            int scaledVHeight = this.vHeight * this.scale;

            guiGraphics.enableScissor(this.x, this.y, this.x + scaledUWidth, this.y + scaledVHeight);

            try(SpriteContents contents = sprite.contents()){
                guiGraphics.blit(
                        scaledX,
                        scaledY,
                        0,
                        contents.width(),
                        contents.height(),
                        sprite
                );

            }

            guiGraphics.disableScissor();

            poseStack.popPose();
        }
    }

    @Override
    public ElementType getType() {
        return ElementType.TEXTURE;
    }

    @Override
    public EntryEncoder<? extends EnhancedOverlayElement> getEncoder() {
        return new TextureEncoder();
    }

    @Override
    public ElementRenderer getRenderer() {
        return new ElementRenderer(TEXTURES);
    }

    public static class TextureEncoder extends EnhancedOverlayElement.EntryEncoder<TextureElement>{

        @Override
        public void encode(EnhancedOverlayElement element, FriendlyByteBuf buf) {
            if(element instanceof TextureElement textureElement){
                buf.writeBoolean(textureElement.textureLoc != null);
                if(textureElement.textureLoc != null){
                    buf.writeResourceLocation(textureElement.textureLoc);
                }
                buf.writeInt(textureElement.uWidth);
                buf.writeInt(textureElement.vHeight);
                buf.writeInt(textureElement.x);
                buf.writeInt(textureElement.y);
                buf.writeInt(textureElement.scale);
            }
        }

        @Override
        public Entry<TextureElement> decode(ActionType actionType, String elementId, FriendlyByteBuf buf) {
            boolean hasLoc = buf.readBoolean();
            ResourceLocation textureLoc = null;
            if(hasLoc){
                textureLoc = buf.readResourceLocation();
            }
            int uWidth = buf.readInt();
            int vHeight = buf.readInt();
            int x = buf.readInt();
            int y = buf.readInt();
            int scale = buf.readInt();
            return new EnhancedOverlayElement.Entry<>(ElementType.TEXTURE, actionType, elementId, new TextureElement(textureLoc, uWidth, vHeight, x, y, scale));
        }
    }
}
