package moffy.cclink_enhancement;

import com.awesoft.cclink.item.UpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Registry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, CCLinkEnhancement.MODID);

    public static final RegistryObject<Item> ENHANCED_OVERLAY_UPGRADE = ITEMS.register("enhanced_overlay_upgrade", () -> new UpgradeItem(new Item.Properties().stacksTo(1),0));
}
