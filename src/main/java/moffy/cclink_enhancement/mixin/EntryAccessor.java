package moffy.cclink_enhancement.mixin;

import com.awesoft.cclink.hudoverlay.packets.HUDOverlayUpdatePacket;
import com.awesoft.cclink.upgrades.luaFunctions.OverlayUpgradeFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = OverlayUpgradeFunctions.class, remap = false)
public interface EntryAccessor {
    @Accessor("entries")
    List<HUDOverlayUpdatePacket.Entry> getEntry();
}
