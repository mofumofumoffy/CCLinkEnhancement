package moffy.cclink_enhancement.mixin;

import com.awesoft.cclink.item.linkCore.LinkAPI;
import moffy.cclink_enhancement.CCLinkEnhancement;
import moffy.cclink_enhancement.upgrades.luafunctions.EnhancedOverlayUpgradeFunctions;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = LinkAPI.class, remap = false)
public abstract class LinkAPIMixin {
    @Shadow
    public abstract ServerPlayer getPlayer();

    @Shadow
    public abstract boolean hasUpgradeNonLua(String upgradeid);

    @Inject(
            at = @At("RETURN"),
            method = "getUpgradeFunctions"
    )
    public void addEnhancedUpgradeFunctions(CallbackInfoReturnable<Map<String, Object>> cir){
        Map<String, Object> functions = cir.getReturnValue();
        if(functions != null){
            if(hasUpgradeNonLua(CCLinkEnhancement.getResource("enhanced_overlay_upgrade").toString())){
                EnhancedOverlayUpgradeFunctions funcs = new EnhancedOverlayUpgradeFunctions(getPlayer(), false);
                functions.put("enhanced_overlay", funcs.getFunctions());
            }
        }
    }
}
