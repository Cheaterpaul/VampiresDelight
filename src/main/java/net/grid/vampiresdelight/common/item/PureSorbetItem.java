package net.grid.vampiresdelight.common.item;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import de.teamlapen.vampirism.util.Helper;
import net.grid.vampiresdelight.common.registry.VDEffects;
import net.grid.vampiresdelight.common.utility.VDEntityUtils;
import net.grid.vampiresdelight.common.utility.VDHelper;
import net.grid.vampiresdelight.common.utility.VDTextUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.item.PopsicleItem;

import java.util.List;
import java.util.Objects;

public class PureSorbetItem extends PopsicleItem {

    /**
    * Pure Sorbet is supposed to give the same saturation to both vampires and humans, that's why it has its own class
     */

    public PureSorbetItem(Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity consumer) {
        if (!level.isClientSide) {
            this.affectConsumer(stack, level, consumer);
        }

        FoodProperties foodProperties = stack.getFoodProperties(consumer);
        assert foodProperties != null;

        if (consumer instanceof Player player) {
            // Don't shrink stack before retrieving food
            VampirePlayer.getOpt(player).ifPresent(v -> v.drinkBlood(foodProperties.getNutrition(), foodProperties.getSaturationModifier(), new DrinkBloodContext(stack)));
        }
        if (consumer instanceof IVampire) {
            ((IVampire) consumer).drinkBlood(foodProperties.getNutrition(), foodProperties.getSaturationModifier(), new DrinkBloodContext(stack));
        } else if (!Helper.isVampire(consumer))
            consumer.eat(level, stack);

        if (consumer instanceof Player player && !player.isCreative() || !(consumer instanceof Player)) {
            stack.shrink(1);
        }

        level.playSound(null, consumer.getX(), consumer.getY(), consumer.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);

        VDEntityUtils.addFoodEffects(foodProperties, level, consumer);

        if (!stack.isEdible()) {
            Player player = consumer instanceof Player ? (Player) consumer : null;
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer) player, stack);
            }
        }

        ItemStack containerStack = stack.getCraftingRemainingItem();

        if (stack.isEmpty()) {
            return containerStack;
        } else {
            if (consumer instanceof Player player && !((Player) consumer).getAbilities().instabuild) {
                if (!player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
            return stack;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        Player player = VampirismMod.proxy.getClientPlayer();
        assert player != null;

        if (Configuration.FOOD_EFFECT_TOOLTIP.get()) {
            VDTextUtils.addFoodEffectTooltip(Objects.requireNonNull(stack.getFoodProperties(player)), tooltip, 1.0F);
        }
    }
}
