package moffy.cclink_enhancement.mixin;

import moffy.cclink_enhancement.lib.HUDEntityRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(
            at = @At("HEAD"),
            method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
            cancellable = true
    )
    public void getHUDRenderOffset(AbstractClientPlayer pEntity, float pPartialTicks, CallbackInfoReturnable<Vec3> cir){
        if((PlayerRenderer)((Object)this) instanceof HUDEntityRenderer hudEntityRenderer && hudEntityRenderer.isInHUD()){
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}
