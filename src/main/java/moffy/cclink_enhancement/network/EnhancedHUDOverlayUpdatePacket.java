package moffy.cclink_enhancement.network;

import com.awesoft.cclink.hudoverlay.packets.HUDOverlayUpdatePacket;
import moffy.cclink_enhancement.client.elements.EnhancedOverlayElement;
import moffy.cclink_enhancement.client.elements.types.ActionType;
import moffy.cclink_enhancement.client.elements.types.ElementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class EnhancedHUDOverlayUpdatePacket {

    private final UUID playerUUID;
    private final List<EnhancedOverlayElement.Entry<? extends EnhancedOverlayElement>> entries;

    public EnhancedHUDOverlayUpdatePacket(UUID playerUUID, List<EnhancedOverlayElement.Entry<?>> entries){
        this.playerUUID = playerUUID;
        this.entries = entries;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.playerUUID);
        buf.writeVarInt(this.entries.size());
        for (EnhancedOverlayElement.Entry<?> e : this.entries) {
            buf.writeEnum(e.type());
            buf.writeEnum(e.actionType());

            boolean hasId = e.actionType() != ActionType.REMOVE_ALL;
            buf.writeBoolean(hasId);
            if (hasId) {
                HUDOverlayUpdatePacket.safeWriteUtf(buf,e.elementID() != null ? e.elementID() : "","elementId");
            }

            if (e.actionType() == ActionType.ADD_OR_UPDATE) {
                EnhancedOverlayElement.EntryEncoder<?> encoder = EnhancedOverlayElement.ENCODERS.get(e.type());
                if(encoder != null){
                    encoder.encode(e.element(), buf);
                }
            }
        }
    }

    public static EnhancedHUDOverlayUpdatePacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        int size = buf.readVarInt();
        List<EnhancedOverlayElement.Entry<?>> entries = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            ElementType type = buf.readEnum(ElementType.class);
            ActionType action = buf.readEnum(ActionType.class);

            boolean hasId = buf.readBoolean();
            String id = hasId ? buf.readUtf() : null;


            if(action == ActionType.ADD_OR_UPDATE){
                EnhancedOverlayElement.EntryEncoder<?> encoder = EnhancedOverlayElement.ENCODERS.get(type);
                if(encoder != null) {
                    entries.add(encoder.decode(action, id, buf));
                }
            } else {
                entries.add(new EnhancedOverlayElement.Entry<>(type, action, id));
            }
        }
        return new EnhancedHUDOverlayUpdatePacket(uuid, entries);
    }

    public static void handle(EnhancedHUDOverlayUpdatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            processPacket(pkt);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void processPacket(EnhancedHUDOverlayUpdatePacket pkt) {
        UUID uuid = pkt.playerUUID;


        for (EnhancedOverlayElement.Entry<?> e : pkt.entries) {
            EnhancedOverlayElement.ElementRenderer renderer = EnhancedOverlayElement.RENDERERS.get(e.type());
            if(renderer != null){
                switch (e.actionType()){
                    case ADD_OR_UPDATE -> renderer.onAddOrUpdate(uuid, e.elementID(), e.element());
                    case REMOVE -> renderer.onRemove(uuid, e.elementID());
                    case REMOVE_ALL -> renderer.onClear(uuid);
                }
            }
        }
    }
}
