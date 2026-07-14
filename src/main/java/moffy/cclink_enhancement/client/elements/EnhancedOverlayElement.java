package moffy.cclink_enhancement.client.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public abstract class EnhancedOverlayElement {
    public static final Map<ElementType, EntryEncoder<? extends EnhancedOverlayElement>> ENCODERS = new HashMap<>();
    public static final Map<ElementType, ElementRenderer> RENDERERS = new HashMap<>();

    public EnhancedOverlayElement(){
        if(!ENCODERS.containsKey(getType())){
            getEncoder().register(getType());
        }
        if(!RENDERERS.containsKey(getType())){
            getRenderer().register(getType());
        }
    }

    public abstract void render(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics);

    public abstract ElementType getType();

    public abstract EntryEncoder<? extends EnhancedOverlayElement> getEncoder();
    public abstract ElementRenderer getRenderer();

    public record Entry<T extends EnhancedOverlayElement>(ElementType type, ActionType actionType, String elementID, T element) {

            public Entry(ElementType type, ActionType actionType){
                this(type, actionType, null);
            }

            public Entry(ElementType type, ActionType actionType, String elementID){
                this(type, actionType, elementID, null);
            }

            public Entry(ElementType type, ActionType actionType, String elementID, T element) {
                this.type = type;
                this.actionType = actionType;
                this.elementID = actionType != ActionType.REMOVE_ALL ? elementID : null;
                this.element = actionType == ActionType.ADD_OR_UPDATE ? element : null;
            }
    }

   public record ElementRenderer(Map<UUID, Map<String, EnhancedOverlayElement>> renderer){
       public void onAddOrUpdate(UUID playerUUID, String elementId, EnhancedOverlayElement element){
            renderer.computeIfAbsent(playerUUID, k ->new LinkedHashMap<>()).put(elementId, element);
       }

       public void onRemove(UUID playerUUID, String elementId){
           Map<String, EnhancedOverlayElement> elements = renderer.get(playerUUID);
           if (elements != null) {
               elements.remove(elementId);
               if (elements.isEmpty()) {
                   renderer.remove(playerUUID);
               }
           }
       }
       public void onClear(UUID playerUUID){
           renderer.remove(playerUUID);
       }

       public void register(ElementType elementType){
            RENDERERS.put(elementType, this);
       }
   }

   public static abstract class EntryEncoder<T extends EnhancedOverlayElement>{
        public abstract void encode(EnhancedOverlayElement element, FriendlyByteBuf buf);
        public abstract Entry<T> decode(ActionType actionType, String elementId, FriendlyByteBuf buf);

        public void register(ElementType elementType){
            ENCODERS.put(elementType, this);
        }
   }
}
