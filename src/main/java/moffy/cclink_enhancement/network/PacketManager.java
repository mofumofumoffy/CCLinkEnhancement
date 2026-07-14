package moffy.cclink_enhancement.network;

import moffy.cclink_enhancement.CCLinkEnhancement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class PacketManager {
    public static void sendToClient(UUID playerUUID, EnhancedHUDOverlayUpdatePacket packet) {
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);

        if (player == null) {
            return;
        }

        if (player.level().isClientSide) {
            EnhancedHUDOverlayUpdatePacket.processPacket(packet);
            return;
        }

        CCLinkEnhancement.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
