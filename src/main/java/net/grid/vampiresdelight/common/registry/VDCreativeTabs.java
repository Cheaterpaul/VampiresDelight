package net.grid.vampiresdelight.common.registry;

import net.grid.vampiresdelight.VampiresDelight;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;

@Mod.EventBusSubscriber(modid = VampiresDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VDCreativeTabs {
    @SubscribeEvent
    public static void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeTabs.TAB_FARMERS_DELIGHT.get())
            addItemsToFDCreativeTab(event);
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(VDItems.ALCHEMICAL_COCKTAIL);
    }

    private static void addItemsToFDCreativeTab(BuildCreativeModeTabContentsEvent event) {
        // Blocks
        //event.accept(VDItems.BREWING_BARREL);
        event.accept(VDItems.GARLIC_CRATE);
        event.accept(VDItems.ORCHID_BAG);
        event.accept(VDItems.SPIRIT_LANTERN);
        event.accept(VDItems.DARK_SPRUCE_CABINET);
        event.accept(VDItems.CURSED_SPRUCE_CABINET);
        event.accept(VDItems.CURSED_FARMLAND);
        event.accept(VDItems.OAK_WINE_SHELF);
        event.accept(VDItems.SPRUCE_WINE_SHELF);
        event.accept(VDItems.BIRCH_WINE_SHELF);
        event.accept(VDItems.JUNGLE_WINE_SHELF);
        event.accept(VDItems.ACACIA_WINE_SHELF);
        event.accept(VDItems.DARK_OAK_WINE_SHELF);
        event.accept(VDItems.MANGROVE_WINE_SHELF);
        event.accept(VDItems.CHERRY_WINE_SHELF);
        event.accept(VDItems.BAMBOO_WINE_SHELF);
        event.accept(VDItems.CRIMSON_WINE_SHELF);
        event.accept(VDItems.WARPED_WINE_SHELF);
        event.accept(VDItems.CURSED_SPRUCE_WINE_SHELF);
        event.accept(VDItems.DARK_SPRUCE_WINE_SHELF);

        // Farming
        event.accept(VDItems.WILD_GARLIC);
        event.accept(VDItems.ORCHID_SEEDS);

        // Foodstuffs
        event.accept(VDItems.GRILLED_GARLIC);
        event.accept(VDItems.BLOOD_SYRUP);
        event.accept(VDItems.ORCHID_TEA);
        event.accept(VDItems.ORCHID_PETALS);
        event.accept(VDItems.SUGARED_BERRIES);
        event.accept(VDItems.HEART_PIECES);
        event.accept(VDItems.HUMAN_EYE);
        event.accept(VDItems.RICE_DOUGH);
        event.accept(VDItems.RICE_BREAD);
        event.accept(VDItems.BLOOD_DOUGH);
        event.accept(VDItems.BLOOD_BAGEL);
        event.accept(VDItems.BLOOD_WINE_BOTTLE);
        event.accept(VDItems.WINE_GLASS);
        event.accept(VDItems.MULLED_WINE_GLASS);
        event.accept(VDItems.BLOOD_PIE);
        event.accept(VDItems.BLOOD_PIE_SLICE);

        // Sweets
        event.accept(VDItems.PURE_SORBET);
        event.accept(VDItems.ORCHID_COOKIE);
        event.accept(VDItems.ORCHID_ECLAIR);
        event.accept(VDItems.ORCHID_ICE_CREAM);
        event.accept(VDItems.TRICOLOR_DANGO);
        event.accept(VDItems.CURSED_CUPCAKE);
        event.accept(VDItems.SNOW_WHITE_ICE_CREAM);

        // Basic Meals
        event.accept(VDItems.EYE_CROISSANT);
        event.accept(VDItems.BAGEL_SANDWICH);
        event.accept(VDItems.FISH_BURGER);
        event.accept(VDItems.HARDTACK);

        // Soups and Stews
        event.accept(VDItems.GARLIC_SOUP);
        event.accept(VDItems.BORSCHT);

        // Feasts
        event.accept(VDItems.WEIRD_JELLY_BLOCK);
        event.accept(VDItems.WEIRD_JELLY);
    }
}
