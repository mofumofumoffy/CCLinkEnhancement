package moffy.cclink_enhancement.client.elements;

import com.mojang.blaze3d.vertex.PoseStack;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaTable;
import moffy.cclink_enhancement.lib.HUDEntityRenderer;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class EntityElement extends EnhancedOverlayElement{
    public static final Map<UUID, Map<String, EnhancedOverlayElement>> ENTITIES = new HashMap<>();

    public static final float RANGE = 32f;

    @Nullable private final UUID entityUUID;
    private final float yaw;
    private final float pitch;
    private final float roll;
    private final int x;
    private final int y;
    private final int scale;

    private LivingEntity livingEntityCache = null;

    public EntityElement(IArguments args) throws LuaException {
        this(parseUUID(args.get(1)), new Rotation(args.getTableUnsafe(2)), args.getInt(3), args.getInt(4), args.getInt(5));
    }

    public EntityElement(@Nullable UUID entityUUID, Rotation rotation, int x, int y, int scale){
        this(entityUUID, rotation.yaw(), rotation.pitch(), rotation.roll(), x, y, scale);
    }

    public EntityElement(@Nullable UUID entityUUID, float yaw, float pitch, float roll, int x, int y, int scale) {
        super();
        this.entityUUID = entityUUID;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    protected static UUID parseUUID(Object uuidObj){
        if(uuidObj instanceof String uuidStr){
            try{
                return UUID.fromString(uuidStr);
            } catch (RuntimeException e) {
                return null;
            }
        }

        return null;
    }

    protected boolean isInSameDimension(LivingEntity entity1, LivingEntity entity2){
       try(
               Level level1 = entity1.level();
               Level level2 = entity2.level();
       ){
           return level1.dimension().equals(level2.dimension());
        } catch (IOException e) {
           return false;
       }
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected LivingEntity getEntity(Minecraft mc){
        if(mc.player == null){
            return null;
        }

        if(livingEntityCache != null){
            if(!(isInSameDimension(mc.player, livingEntityCache) && mc.player.distanceTo(livingEntityCache) <= RANGE)){
                livingEntityCache = null;
            }
        } else {
            if(this.entityUUID != null){
                if(mc.level != null) {
                    List<Entity> entityList = mc.level.getEntities((Entity) null, AABB.ofSize(mc.player.position(), RANGE, RANGE, RANGE), entity -> entity.getUUID().equals(this.entityUUID));
                    if (!entityList.isEmpty() && entityList.get(0) instanceof LivingEntity livingEntity) {
                        livingEntityCache = livingEntity;
                    }
                }
            } else {
                livingEntityCache = mc.player;
            }
        }
        return livingEntityCache;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics) {
        LivingEntity target = getEntity(mc);
        if(target != null){
            Quaternionf rotation = new Quaternionf().rotationY(yaw).rotateLocalX(pitch).rotateLocalZ(roll);

            renderEntity(
                    mc,
                    poseStack,
                    guiGraphics,
                    target,
                    rotation,
                    x,
                    y,
                    scale
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderEntity(Minecraft mc, PoseStack poseStack, GuiGraphics guiGraphics, LivingEntity target, Quaternionf rot, int x, int y, int scale){
        poseStack.pushPose();

        EntityRenderer<? super LivingEntity> entityRenderer = mc.getEntityRenderDispatcher().getRenderer(target);

        if(entityRenderer instanceof HUDEntityRenderer hudEntityRenderer){
            hudEntityRenderer.markInHUD();
            InventoryScreen.renderEntityInInventory(
                    guiGraphics,
                    x + scale * 10,
                    y + 2 * scale * 10,
                    scale * 10,
                    rot.rotateYXZ((float) Math.PI, (float) Math.PI, 0),
                    null,
                    target
            );
        }

        poseStack.popPose();
    }

    @Override
    public ElementType getType() {
        return ElementType.ENTITY;
    }

    @Override
    public EntryEncoder<? extends EnhancedOverlayElement> getEncoder() {
        return new EntityEncoder();
    }

    @Override
    public ElementRenderer getRenderer() {
        return new ElementRenderer(ENTITIES);
    }

    public record Rotation(float yaw, float roll, float pitch){
        public Rotation(LuaTable<?, ?> table) throws LuaException{
            this(resolveTable(table, "yaw", "yRot"), resolveTable(table, "roll", "zRot"), resolveTable(table, "pitch", "xRot"));
        }

        private static float resolveTable(LuaTable<?, ?> table, String ...keys) throws LuaException{
            for(String key : keys){
                if(table.containsKey(key)){
                    Optional<Double> value = table.optDouble(key);
                    return value.orElse(0d).floatValue();
                }
            }
            return 0f;
        }
    }

    protected static class EntityEncoder extends EnhancedOverlayElement.EntryEncoder<EntityElement>{

        @Override
        public void encode(EnhancedOverlayElement element, FriendlyByteBuf buf) {
            if(element instanceof EntityElement entityElement){
                buf.writeBoolean(entityElement.entityUUID != null);
                if(entityElement.entityUUID != null){
                    buf.writeUUID(entityElement.entityUUID);
                }
                buf.writeFloat(entityElement.yaw);
                buf.writeFloat(entityElement.pitch);
                buf.writeFloat(entityElement.roll);
                buf.writeInt(entityElement.x);
                buf.writeInt(entityElement.y);
                buf.writeInt(entityElement.scale);
            }
        }

        @Override
        public Entry<EntityElement> decode(ActionType actionType, String elementId, FriendlyByteBuf buf) {
            boolean hasValidUUID = buf.readBoolean();
            UUID entityUUID = null;
            if(hasValidUUID){
                entityUUID = buf.readUUID();
            }

            float yaw = buf.readFloat();
            float pitch = buf.readFloat();
            float roll = buf.readFloat();
            int x = buf.readInt();
            int y = buf.readInt();
            int scale = buf.readInt();
            return new EnhancedOverlayElement.Entry<>(ElementType.ENTITY, actionType, elementId, new EntityElement(entityUUID, yaw, pitch, roll, x, y, scale));
        }
    }
}
