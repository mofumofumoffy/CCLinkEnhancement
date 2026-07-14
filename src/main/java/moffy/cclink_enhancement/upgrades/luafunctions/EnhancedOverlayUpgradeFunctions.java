package moffy.cclink_enhancement.upgrades.luafunctions;

import com.awesoft.cclink.upgrades.luaFunctions.OverlayUpgradeFunctions;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaFunction;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import moffy.cclink_enhancement.Registry;
import moffy.cclink_enhancement.client.elements.*;
import moffy.cclink_enhancement.network.EnhancedHUDOverlayUpdatePacket;
import moffy.cclink_enhancement.network.PacketManager;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnhancedOverlayUpgradeFunctions extends OverlayUpgradeFunctions {
    public EnhancedOverlayUpgradeFunctions(Entity entity, boolean isIntegrated) {
        super(entity, isIntegrated);
        this.UPGRADE = Registry.ENHANCED_OVERLAY_UPGRADE.get();
    }

    protected List<EnhancedOverlayElement.Entry<?>> entries = new ArrayList<>();

    protected ILuaFunction createSendFn(ILuaFunction original){
        return (args) -> {
            MethodResult result = original.call(args);
            Object[] resultObjects = result.getResult();
            if (resultObjects != null && resultObjects[0] instanceof Boolean packetResult){
                if(packetResult){
                    EnhancedHUDOverlayUpdatePacket packet = new EnhancedHUDOverlayUpdatePacket(getPlayer().getUUID(), entries);

                    PacketManager.sendToClient(getPlayer().getUUID(), packet);

                    return MethodResult.of(true);
                }else{
                    return result;
                }
            }

            return MethodResult.of(false, "Something went wrong!");
        };
    }

    protected ILuaFunction createClearAllFn(ILuaFunction original){
        return (args) -> {
            MethodResult result = original.call(args);
            Object[] resultObjects = result.getResult();
            if (resultObjects != null && resultObjects[0] instanceof Boolean packetResult){
                if(packetResult){

                    //clear all enhanced-overlay items
                    for(ElementType type : ElementType.values()){
                        entries.add(new EnhancedOverlayElement.Entry<>(type, ActionType.REMOVE_ALL));
                    }

                    return MethodResult.of(true);
                }else{
                    return result;
                }
            }

            return MethodResult.of(false, "Something went wrong!");
        };
    }

    protected void registerElement(Map<String, Object> functions, ElementType type, EnhancedElementConstructor constructor){
        ILuaFunction addOrUpdate = args -> {
            entries.add(new EnhancedOverlayElement.Entry<>(type, ActionType.ADD_OR_UPDATE, args.getString(0), constructor.apply(args)));
            return MethodResult.of(true);
        };

        ILuaFunction remove = args -> {
            entries.add(new EnhancedOverlayElement.Entry<>(type, ActionType.REMOVE, args.getString(0)));
            return MethodResult.of(true);
        };

        ILuaFunction clear = args -> {
            entries.add(new EnhancedOverlayElement.Entry<>(type, ActionType.REMOVE_ALL));
            return MethodResult.of(true);
        };

        functions.put("addOrUpdate"+type.getName()+"Element", addOrUpdate);
        functions.put("remove"+type.getName()+"Element", remove);
        functions.put("clear"+type.getName()+"Element", clear);
    }

    @Override
    public Map<String, Object> getFunctions() {
        Map<String, Object> functions = super.getFunctions();

        registerElement(functions, ElementType.SCALABLE_ITEM, ScalableItemElement::new);
        registerElement(functions, ElementType.FORMATTED_TEXT, FormattedTextElement::new);
        registerElement(functions, ElementType.ENTITY, EntityElement::new);
        registerElement(functions, ElementType.TEXTURE, TextureElement::new);

        ILuaFunction clearAllFn = (ILuaFunction) functions.get("clearAll");
        ILuaFunction sendFn = (ILuaFunction) functions.get("send");
        functions.put("clearAll", createClearAllFn(clearAllFn));
        functions.put("send", createSendFn(sendFn));
        return functions;
    }

    @FunctionalInterface
    public interface EnhancedElementConstructor{
        EnhancedOverlayElement apply(IArguments args) throws LuaException;
    }
}
