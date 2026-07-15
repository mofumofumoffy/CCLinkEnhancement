package moffy.cclink_enhancement.client.elements;

import com.awesoft.cclink.hudoverlay.packets.HUDOverlayUpdatePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScalableItemElement extends EnhancedOverlayElement{
    public static final Map<UUID, Map<String, EnhancedOverlayElement>> SCALABLE_ITEMS = new HashMap<>();

    private final String itemResource;
    private final String nbt;
    private final int x;
    private final int y;
    private final int scale;

    private ItemStack stack = ItemStack.EMPTY;

    public ScalableItemElement(IArguments args) throws LuaException {
        this(args.getString(1), args.getInt(2), args.getInt(3), args.getInt(4));
    }

    public ScalableItemElement(String itemResourceWithNBT, int x, int y, int scale){
        this(parseItemId(itemResourceWithNBT)[0], parseItemId(itemResourceWithNBT)[1], x, y, scale);
    }

    public ScalableItemElement(String itemResource, String nbt, int x, int y, int scale){
        super();
        this.itemResource = itemResource;
        this.nbt = nbt;
        this.x = x;
        this.y = y;
        this.scale = scale;

        this.stack = createStack();
    }

    protected static String[] parseItemId(String itemResourceWithNBT){
        String resource = "";
        String nbt = "";

        if(itemResourceWithNBT.contains("{")){
            resource = itemResourceWithNBT.substring(0, itemResourceWithNBT.indexOf("{"));
            nbt = itemResourceWithNBT.substring(itemResourceWithNBT.indexOf("{"));
        } else {
            resource = itemResourceWithNBT;
        }

        return new String[]{resource, nbt};
    }


    protected ItemStack createStack(){
        ResourceLocation itemId = ResourceLocation.tryParse(this.itemResource);
        if(itemId != null){
             Item item = BuiltInRegistries.ITEM.get(itemId);

             ItemStack stack = new ItemStack(item);
             try{
                 stack.setTag(TagParser.parseTag(this.nbt));
                 return stack;
             } catch (CommandSyntaxException e) {
                 return stack;
             }
        }
        return ItemStack.EMPTY;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, 1.0F);

        int scaledX = this.x / this.scale;
        int scaledY = this.y / this.scale;

        guiGraphics.renderFakeItem(this.stack,  scaledX, scaledY);
        poseStack.popPose();
    }

    @Override
    public ElementType getType() {
        return ElementType.SCALABLE_ITEM;
    }

    @Override
    public EntryEncoder<? extends EnhancedOverlayElement> getEncoder() {
        return new ScalableItemEncoder();
    }

    @Override
    public ElementRenderer getRenderer() {
        return new ElementRenderer(SCALABLE_ITEMS);
    }

    public static class ScalableItemEncoder extends EnhancedOverlayElement.EntryEncoder<ScalableItemElement>{
        public void encode(EnhancedOverlayElement element, FriendlyByteBuf buf) {
            if(element instanceof ScalableItemElement scalableItemElement){
                HUDOverlayUpdatePacket.safeWriteUtf(buf, scalableItemElement.itemResource, "itemResource");
                HUDOverlayUpdatePacket.safeWriteUtf(buf, scalableItemElement.nbt, "NBT");
                buf.writeInt(scalableItemElement.x);
                buf.writeInt(scalableItemElement.y);
                buf.writeInt(scalableItemElement.scale);
            }
        }

        public EnhancedOverlayElement.Entry<ScalableItemElement> decode(ActionType actionType, String elementId, FriendlyByteBuf buf){
            String itemResource = buf.readUtf();
            String nbt = buf.readUtf();
            int x = buf.readInt();
            int y = buf.readInt();
            int scale = buf.readInt();
            return new Entry<>(ElementType.SCALABLE_ITEM, actionType, elementId, new ScalableItemElement(itemResource, nbt, x, y, scale));
        }
    }
}
