package net.grid.vampiresdelight.common.item;

import net.grid.vampiresdelight.common.registry.VDAdvancements;
import net.grid.vampiresdelight.common.registry.VDSounds;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Credits to the Create team for mechanic references
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PourableBottleItem extends Item {
    private final Item serving;
    private final Item servingContainer;

    public PourableBottleItem(Properties properties, int servings, Item serving, Item servingContainer) {
        super(properties.defaultDurability(servings).setNoRepair());
        this.serving = serving;
        this.servingContainer = servingContainer;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand mainHand) {
        InteractionHand offHand = mainHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemStack = player.getItemInHand(mainHand);

        if (itemStack.getOrCreateTag().contains("Pouring")) {
            player.startUsingItem(mainHand);
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
        }

        ItemStack bottle = player.getItemInHand(offHand);
        if (bottle.getItem() == servingContainer) {
            ItemStack itemUsed = bottle.copy();
            ItemStack toPour = itemUsed.split(1);
            player.startUsingItem(mainHand);
            itemStack.getOrCreateTag().put("Pouring", toPour.serializeNBT());
            player.setItemInHand(offHand, itemUsed);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }

        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (!(hitResult instanceof BlockHitResult)) return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);

        Vec3 POVHit = hitResult.getLocation();
        AABB aabb = new AABB(POVHit, POVHit).inflate(1f);
        ItemEntity pickUp = null;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, aabb)) {
            if (!itemEntity.isAlive() && itemEntity.position().distanceTo(player.position()) > 3 && !itemEntity.isAlive() &&
                    itemEntity.getItem().getItem() != servingContainer)
                continue;
            pickUp = itemEntity;
            break;
        }
        if (pickUp != null) {

            ItemStack pickedItem = pickUp.getItem().copy();
            ItemStack toPour = pickedItem.split(1);
            player.startUsingItem(mainHand);

            if (!level.isClientSide) {
                itemStack.getOrCreateTag().put("Pouring", toPour.serializeNBT());
                if (pickedItem.isEmpty()) pickUp.discard();
                else pickUp.setItem(pickedItem);
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return itemStack;

        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (compoundTag.contains("Pouring")) {
            if (player instanceof FakePlayer) {
                player.drop(new ItemStack(serving), false, false);
            } else {
                player.getInventory().placeItemBackInInventory(new ItemStack(serving));
            }
            compoundTag.remove("Pouring");
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            if (itemStack.getDamageValue() > 3) itemStack = new ItemStack(servingContainer);
            VDAdvancements.BLOOD_WINE_POURED.trigger((ServerPlayer) player);
        }

        return itemStack;
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int durationTime) {
        if (!(entity instanceof Player player))
            return;

        CompoundTag compoundTag = itemStack.getOrCreateTag();
        if (compoundTag.contains("Pouring")) {
            player.getInventory().placeItemBackInInventory(ItemStack.of(compoundTag.getCompound("Pouring")));
            compoundTag.remove("Pouring");
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return VDSounds.POURING.get();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack item = itemStack.copy();
        item.setDamageValue(item.getDamageValue() + 1);
        if (item.getDamageValue() > 3) return new ItemStack(servingContainer);
        return item;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
