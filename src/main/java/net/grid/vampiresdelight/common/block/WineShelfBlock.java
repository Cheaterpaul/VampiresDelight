package net.grid.vampiresdelight.common.block;

import de.teamlapen.lib.lib.util.UtilLib;
import net.grid.vampiresdelight.VampiresDelight;
import net.grid.vampiresdelight.common.block.entity.WineShelfBlockEntity;
import net.grid.vampiresdelight.common.registry.VDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class WineShelfBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_UPPER_SUPPORT = BooleanProperty.create("has_upper_support");
    public static final BooleanProperty WINE_SHELF_SLOT_0_OCCUPIED = BooleanProperty.create("slot_0_occupied");
    public static final BooleanProperty WINE_SHELF_SLOT_1_OCCUPIED = BooleanProperty.create("slot_1_occupied");
    public static final BooleanProperty WINE_SHELF_SLOT_2_OCCUPIED = BooleanProperty.create("slot_2_occupied");
    public static final BooleanProperty WINE_SHELF_SLOT_3_OCCUPIED = BooleanProperty.create("slot_3_occupied");
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(WINE_SHELF_SLOT_0_OCCUPIED, WINE_SHELF_SLOT_1_OCCUPIED, WINE_SHELF_SLOT_2_OCCUPIED, WINE_SHELF_SLOT_3_OCCUPIED);

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        VoxelShape shape;

        switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST -> shape = UtilLib.rotateShape(makeShape(state.getValue(HAS_UPPER_SUPPORT), state), UtilLib.RotationAmount.NINETY);
            case SOUTH -> shape = UtilLib.rotateShape(makeShape(state.getValue(HAS_UPPER_SUPPORT), state), UtilLib.RotationAmount.HUNDRED_EIGHTY);
            case WEST -> shape = UtilLib.rotateShape(makeShape(state.getValue(HAS_UPPER_SUPPORT), state), UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);
            default -> shape = makeShape(state.getValue(HAS_UPPER_SUPPORT), state);
        }

        return shape;
    }

    public WineShelfBlock(Properties properties) {
        super(properties.strength(1.5F));

        BlockState blockstate = this.stateDefinition.any()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
                .setValue(HAS_UPPER_SUPPORT, false);

        for (BooleanProperty booleanproperty : SLOT_OCCUPIED_PROPERTIES) {
            blockstate = blockstate.setValue(booleanproperty, Boolean.FALSE);
        }

        this.registerDefaultState(blockstate);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter world = context.getLevel();
        BlockPos posAbove = context.getClickedPos().above();
        return this.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HAS_UPPER_SUPPORT, world.getBlockState(posAbove).getBlock() instanceof WineShelfBlock);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos blockPos, @NotNull BlockPos facingPos) {
        return super.updateShape(state.setValue(HAS_UPPER_SUPPORT, level.getBlockState(blockPos.above()).getBlock() instanceof WineShelfBlock), facing, facingState, level, blockPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HorizontalDirectionalBlock.FACING, HAS_UPPER_SUPPORT);
        SLOT_OCCUPIED_PROPERTIES.forEach(builder::add);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level pLevel, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        BlockEntity blockEntity = pLevel.getBlockEntity(pos);
        if (blockEntity instanceof WineShelfBlockEntity wineshelfblockentity) {
            Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(hitResult, state.getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                int i = getHitSlot(optional.get());
                if (state.getValue(SLOT_OCCUPIED_PROPERTIES.get(i))) {
                    removeBottle(pLevel, pos, player, wineshelfblockentity, i);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                } else {
                    ItemStack itemstack = player.getItemInHand(hand);
                    if (itemstack.is(VDItems.BLOOD_WINE_BOTTLE.get())) {
                        addBottle(pLevel, pos, player, wineshelfblockentity, itemstack, i);
                        return InteractionResult.sidedSuccess(pLevel.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult hitResult, Direction blocFace) {
        Direction direction = hitResult.getDirection();
        if (blocFace != direction) {
            return Optional.empty();
        } else {
            BlockPos blockpos = hitResult.getBlockPos().relative(direction);
            Vec3 vec3 = hitResult.getLocation().subtract(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();

            return switch (direction) {
                case NORTH -> Optional.of(new Vec2((float) (1.0D - d0), (float) d1));
                case SOUTH -> Optional.of(new Vec2((float) d0, (float) d1));
                case WEST -> Optional.of(new Vec2((float) d2, (float) d1));
                case EAST -> Optional.of(new Vec2((float) (1.0D - d2), (float) d1));
                case DOWN, UP -> Optional.empty();
                default -> throw new IncompatibleClassChangeError();
            };
        }
    }

    private static int getHitSlot(Vec2 hitPos) {
        int i = hitPos.y >= 0.5F ? 0 : 1;
        int j = getSection(hitPos.x);
        return j + i * 2;
    }

    private static int getSection(float pX) {
        float f = 0.5F;
        if (pX < f) {
            return 0;
        } else {
            return 1;
        }
    }

    private static void addBottle(Level level, BlockPos pos, Player player, WineShelfBlockEntity blockEntity, ItemStack bottleStack, int slot) {
        if (!level.isClientSide) {
            player.awardStat(Stats.ITEM_USED.get(bottleStack.getItem()));
            //SoundEvent soundevent = pBookStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            blockEntity.setItem(slot, bottleStack.split(1));
            //pLevel.playSound((Player)null, pPos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                bottleStack.grow(1);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    private static void removeBottle(Level level, BlockPos pos, Player player, WineShelfBlockEntity blockEntity, int slot) {
        if (!level.isClientSide) {
            ItemStack itemstack = blockEntity.removeItem(slot, 1);
            //SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
            //level.playSound((Player)null, pos, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(itemstack)) {
                player.drop(itemstack, false);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WineShelfBlockEntity(pos, state);
    }

    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof WineShelfBlockEntity wineshelfblockentity) {
                if (!wineshelfblockentity.isEmpty()) {
                    for(int i = 0; i < 4; ++i) {
                        ItemStack itemstack = wineshelfblockentity.getItem(i);
                        if (!itemstack.isEmpty()) {
                            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                        }
                    }

                    wineshelfblockentity.clearContent();
                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }

            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    public static @NotNull VoxelShape makeShape(boolean has_support, BlockState state) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0, 0.5, 0, 1, 0.625, 1));
        shape = Shapes.or(shape, Shapes.box(0, 0, 0, 1, 0.125, 1));
        shape = Shapes.or(shape, Shapes.box(0.4375, 0.125, 0.875, 0.5625, 0.5, 1));

        if (has_support) {
            shape = Shapes.or(shape, Shapes.box(0.4375, 0.625, 0.875, 0.5625, 1, 1));
        }

        return shape;
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        if (level.isClientSide()) {
            return 0;
        } else {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof WineShelfBlockEntity wineshelfblockentity) {
                return wineshelfblockentity.getLastInteractedSlot() + 1;
            } else {
                return 0;
            }
        }
    }

    public static Iterable<Item> getAllShelves() {
        return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> ForgeRegistries.BLOCKS.getKey(block) != null && VampiresDelight.MODID.equals(Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block)).getNamespace()) && block instanceof WineShelfBlock).map(Block::asItem).collect(Collectors.toList());
    }
}
