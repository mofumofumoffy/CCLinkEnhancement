package moffy.cclink_enhancement.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import moffy.cclink_enhancement.lib.HUDEntityRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin implements HUDEntityRenderer {
    @Unique
    private final ThreadLocal<Boolean> IS_IN_HUD = ThreadLocal.withInitial(()->false);

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V")
    )
    public <T extends LivingEntity> void setupHUDRotation(LivingEntityRenderer<T, EntityModel<T>> instance, T pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, Operation<Void> original){
        if(!IS_IN_HUD.get()){
            original.call(instance, pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
        }
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    )
    public <T extends Entity> void setupHUDAnimation(EntityModel<T> instance, T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, Operation<Void> original){
        if(IS_IN_HUD.get()){
            instance.riding = false;
            instance.attackTime = 0f;
            if(instance instanceof HumanoidModel<?> humanoidModel){
                humanoidModel.crouching = false;
                humanoidModel.swimAmount = 0f;
                original.call(humanoidModel, pEntity, 0f, 0f, 0f, 0f, 0f);
                humanoidModel.head.xRot = 0f;
            } else {
                original.call(instance, pEntity, 0f, 0f, 0f, 0f, 0f);
            }

            IS_IN_HUD.set(false);
        } else {
            original.call(instance, pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        }
    }

    @Override
    public void markInHUD() {
        IS_IN_HUD.set(true);
    }

    @Override
    public boolean isInHUD() {
        return IS_IN_HUD.get();
    }
}
