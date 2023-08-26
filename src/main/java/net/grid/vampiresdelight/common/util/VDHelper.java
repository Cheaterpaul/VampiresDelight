package net.grid.vampiresdelight.common.util;

import de.teamlapen.vampirism.items.GarlicBreadItem;
import de.teamlapen.vampirism.items.VampirismItemBloodFoodItem;
import net.grid.vampiresdelight.common.item.HunterConsumableItem;
import net.grid.vampiresdelight.common.item.OrchidTeaItem;
import net.grid.vampiresdelight.common.item.VampireConsumableItem;
import net.minecraft.world.item.ItemStack;

public class VDHelper {
    public static boolean isVampireFood(ItemStack stack) {
        return !stack.isEmpty() &&
                stack.getItem() instanceof VampireConsumableItem ||
                stack.getItem() instanceof VampirismItemBloodFoodItem ||
                stack.getItem() instanceof OrchidTeaItem;
    }
    public static boolean isHunterFood(ItemStack stack) {
        return !stack.isEmpty() &&
                stack.getItem() instanceof HunterConsumableItem ||
                stack.getItem() instanceof GarlicBreadItem;
    }
}