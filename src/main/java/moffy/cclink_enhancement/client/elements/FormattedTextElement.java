package moffy.cclink_enhancement.client.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTable;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FormattedTextElement extends EnhancedOverlayElement{
    public static final Map<UUID, Map<String, EnhancedOverlayElement>> FORMATTED_TEXTS = new HashMap<>();

    private final String text;
    private final int x;
    private final int y;
    private final int color;
    private final int scale;

    private TextFormat formats = null;

    @Nullable
    private ResourceLocation fontName = null;

    public FormattedTextElement(IArguments args) throws LuaException{
        this(args.getString(1), args.getInt(2), args.getInt(3), args.getInt(4), args.getInt(5), args.get(6) == null ? null : new TextFormat(args.getTableUnsafe(6)));
    }

    public FormattedTextElement(String text, int x, int y, int color, int scale, @Nullable TextFormat formats) {
        super();
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.scale = scale;

        this.formats = formats;

        if(formats != null && formats.fontName != null){
            this.fontName = ResourceLocation.tryParse(formats.fontName());
        }
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics) {
        MutableComponent component = Component.literal(this.text);

        if(this.fontName != null){
            component = component.withStyle(style -> style.withFont(this.fontName));
        }

        if(this.formats != null){
            component = component.withStyle(style ->
                    style.withColor(this.color)
                            .withObfuscated(formats.obfuscated())
                            .withBold(formats.bold())
                            .withStrikethrough(formats.strikethrough)
                            .withUnderlined(formats.underlined)
                            .withItalic(formats.italic)
            );
        }

        poseStack.pushPose();

        poseStack.scale(this.scale, this.scale, 1.0F);

        int scaledX = this.x / this.scale;
        int scaledY = this.y / this.scale;


        guiGraphics.drawString(mc.font, component, scaledX, scaledY, this.color, false);

        poseStack.popPose();
    }

    @Override
    public ElementType getType() {
        return ElementType.FORMATTED_TEXT;
    }

    @Override
    public EntryEncoder<? extends EnhancedOverlayElement> getEncoder() {
        return new FormattedTextEncoder();
    }

    @Override
    public ElementRenderer getRenderer() {
        return new ElementRenderer(FORMATTED_TEXTS);
    }

    public record TextFormat(boolean obfuscated, boolean bold, boolean strikethrough, boolean underlined, boolean italic, String fontName){
        public TextFormat(LuaTable<?, ?> table) throws LuaException{
            this(
                    table.optBoolean("obfuscated").orElse(false),
                    table.optBoolean("bold").orElse(false),
                    table.optBoolean("strikethrough").orElse(false),
                    table.optBoolean("underlined").orElse(false),
                    table.optBoolean("italic").orElse(false),
                    table.optString("fontName").orElse(null)
            );
        }
    }

    protected static class FormattedTextEncoder extends EnhancedOverlayElement.EntryEncoder<FormattedTextElement>{
        @Override
        public void encode(EnhancedOverlayElement element, FriendlyByteBuf buf) {
            if(element instanceof FormattedTextElement formattedTextElement){
                buf.writeUtf(formattedTextElement.text);
                buf.writeInt(formattedTextElement.x);
                buf.writeInt(formattedTextElement.y);
                buf.writeInt(formattedTextElement.color);
                buf.writeInt(formattedTextElement.scale);

                buf.writeBoolean(formattedTextElement.formats.obfuscated);
                buf.writeBoolean(formattedTextElement.formats.bold);
                buf.writeBoolean(formattedTextElement.formats.strikethrough);
                buf.writeBoolean(formattedTextElement.formats.underlined);
                buf.writeBoolean(formattedTextElement.formats.italic);

                buf.writeBoolean(formattedTextElement.fontName != null);
                if(formattedTextElement.fontName != null){
                    buf.writeResourceLocation(formattedTextElement.fontName);
                }
            }
        }

        @Override
        public Entry<FormattedTextElement> decode(ActionType actionType, String elementId, FriendlyByteBuf buf) {
            String text = buf.readUtf();
            int x = buf.readInt();
            int y = buf.readInt();
            int color = buf.readInt();
            int scale = buf.readInt();

            boolean obfuscated = buf.readBoolean();
            boolean bold = buf.readBoolean();
            boolean strikethrough = buf.readBoolean();
            boolean underlined = buf.readBoolean();
            boolean italic = buf.readBoolean();

            String fontName = null;
            boolean hasFont = buf.readBoolean();
            if(hasFont){
                fontName = buf.readUtf();
            }
            return new Entry<>(ElementType.FORMATTED_TEXT, actionType, elementId, new FormattedTextElement(text, x, y, color, scale, new TextFormat(obfuscated, bold, strikethrough, underlined, italic, fontName)));

        }
    }
}
