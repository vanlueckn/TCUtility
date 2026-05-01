package com.troblecodings.tcutility.blocks;

import javax.annotation.Nullable;

import com.troblecodings.tcutility.utils.BlockCreateInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Drei-Block-tall Door-Variante. Im Gegensatz zum 1.14-Vanilla-DoorBlock
 * (zwei Blocks: LOWER/UPPER) trackt {@link #THIRD} drei Stockwerke
 * (LOWER/MIDDLE/UPPER); Open- und Power-State liegen am untersten Block,
 * die anderen zwei spiegeln den State.
 */
public class TCBigDoor extends Block {

    public enum BigDoorThird implements IStringSerializable {
        LOWER("lower"), MIDDLE("middle"), UPPER("upper");

        private final String name;

        BigDoorThird(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<BigDoorThird> THIRD = EnumProperty.create("third",
            BigDoorThird.class);

    // 3/16-dicke Hitbox an einer der vier Block-Seiten, abhaengig von
    // FACING + Open + Hinge. In 0..16-Skala.
    private static final VoxelShape SOUTH_AABB = Block.makeCuboidShape(0, 0, 0, 16, 16, 3);
    private static final VoxelShape NORTH_AABB = Block.makeCuboidShape(0, 0, 13, 16, 16, 16);
    private static final VoxelShape WEST_AABB = Block.makeCuboidShape(13, 0, 0, 16, 16, 16);
    private static final VoxelShape EAST_AABB = Block.makeCuboidShape(0, 0, 0, 3, 16, 16);

    private Item item;

    public TCBigDoor(final BlockCreateInfo blockInfo) {
        super(blockInfo.toProperties());
        this.setDefaultState(this.stateContainer.getBaseState()
                .with(FACING, Direction.NORTH)
                .with(OPEN, Boolean.FALSE)
                .with(HINGE, DoorHingeSide.LEFT)
                .with(POWERED, Boolean.FALSE)
                .with(THIRD, BigDoorThird.LOWER));
    }

    public void setItem(final Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader world,
            final BlockPos pos, final ISelectionContext context) {
        Direction facing = state.get(FACING);
        // Wenn die Tuer offen ist, schwingt sie um 90 Grad in Richtung der
        // Hinge -- das simulieren wir, indem wir die Hitbox auf die Seite
        // versetzen, an der die Hinge sitzt.
        if (state.get(OPEN)) {
            final Direction rotated = state.get(HINGE) == DoorHingeSide.RIGHT
                    ? facing.rotateY()
                    : facing.rotateYCCW();
            facing = rotated;
        }
        switch (facing) {
            case NORTH:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
            default:
                return EAST_AABB;
        }
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit) {
        if (state.getMaterial() == Material.IRON) {
            return false;
        }
        final BlockPos lowerPos = lowerPosOf(state, pos);
        final BlockState lowerState = world.getBlockState(lowerPos);
        if (!(lowerState.getBlock() instanceof TCBigDoor)) {
            return false;
        }
        final boolean newOpen = !lowerState.get(OPEN);
        propagateState(world, lowerPos, lowerState, OPEN, newOpen);
        world.playEvent(player, newOpen ? openSoundEvent(state) : closeSoundEvent(state),
                pos, 0);
        return true;
    }

    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos,
            final Block fromBlock, final BlockPos fromPos, final boolean isMoving) {
        // Das untere Glied behandelt Redstone; die anderen beiden delegieren
        // an den Lower-Block (so wie das Vanilla-DoorBlock fuer UPPER macht).
        if (state.get(THIRD) != BigDoorThird.LOWER) {
            final BlockPos lower = lowerPosOf(state, pos);
            final BlockState lowerState = world.getBlockState(lower);
            if (lowerState.getBlock() == this) {
                lowerState.neighborChanged(world, lower, fromBlock, fromPos, isMoving);
            }
            return;
        }
        final boolean powered = world.isBlockPowered(pos)
                || world.isBlockPowered(pos.up()) || world.isBlockPowered(pos.up(2));
        if (powered != state.get(POWERED) || powered != state.get(OPEN)) {
            BlockState s = state.with(POWERED, powered);
            if (powered != state.get(OPEN)) {
                s = s.with(OPEN, powered);
                world.playEvent(null, powered ? openSoundEvent(s) : closeSoundEvent(s), pos, 0);
            }
            propagateState(world, pos, s, POWERED, powered);
            propagateState(world, pos, s, OPEN, s.get(OPEN));
        }
    }

    @Override
    public void onBlockHarvested(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player) {
        if (player.abilities.isCreativeMode) {
            removeAllParts(world, pos, state);
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void onReplaced(final BlockState state, final World world, final BlockPos pos,
            final BlockState newState, final boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            // Wenn der Block via Explosion / setBlockState aus der Welt
            // verschwindet (nicht via onBlockHarvested), trotzdem die
            // anderen zwei Glieder nachreissen.
            removeAllParts(world, pos, state);
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    /**
     * Position des LOWER-Blocks unseres 3er-Clusters relativ zu {@code pos}.
     */
    public static BlockPos lowerPosOf(final BlockState state, final BlockPos pos) {
        switch (state.get(THIRD)) {
            case UPPER:
                return pos.down(2);
            case MIDDLE:
                return pos.down();
            case LOWER:
            default:
                return pos;
        }
    }

    private void propagateState(final World world, final BlockPos lowerPos,
            final BlockState lowerState, final net.minecraft.state.IProperty<?> prop,
            final Object value) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final BlockState newLower = lowerState.with((net.minecraft.state.IProperty) prop, (Comparable) value);
        world.setBlockState(lowerPos, newLower, 10);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final BlockState newMiddle = world.getBlockState(lowerPos.up()).with(
                (net.minecraft.state.IProperty) prop, (Comparable) value);
        world.setBlockState(lowerPos.up(), newMiddle, 10);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final BlockState newUpper = world.getBlockState(lowerPos.up(2)).with(
                (net.minecraft.state.IProperty) prop, (Comparable) value);
        world.setBlockState(lowerPos.up(2), newUpper, 10);
    }

    private void removeAllParts(final World world, final BlockPos pos, final BlockState state) {
        final BlockPos lower = lowerPosOf(state, pos);
        for (int dy = 0; dy < 3; dy++) {
            final BlockPos p = lower.up(dy);
            if (p.equals(pos)) {
                continue; // wird vom regulaeren Break-Pfad selbst entfernt
            }
            if (world.getBlockState(p).getBlock() instanceof TCBigDoor) {
                world.setBlockState(p, net.minecraft.block.Blocks.AIR.getDefaultState(), 35);
            }
        }
    }

    private static int openSoundEvent(final BlockState state) {
        return state.getMaterial() == Material.IRON ? 1005 : 1006;
    }

    private static int closeSoundEvent(final BlockState state) {
        return state.getMaterial() == Material.IRON ? 1011 : 1012;
    }

    @Nullable
    public SoundEvent getOpenSound() {
        return SoundEvents.BLOCK_WOODEN_DOOR_OPEN;
    }

    @Nullable
    public SoundEvent getCloseSound() {
        return SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        // Das LOWER-Block-Placement uebernimmt der TCBigDoorItem; hier
        // returnen wir den default als sanity-fallback.
        return this.getDefaultState().with(FACING,
                context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return mirror == Mirror.NONE
                ? state
                : state.rotate(mirror.toRotation(state.get(FACING)))
                        .with(HINGE,
                                state.get(HINGE) == DoorHingeSide.LEFT ? DoorHingeSide.RIGHT
                                        : DoorHingeSide.LEFT);
    }

    @Override
    public boolean canSpawnInBlock() {
        return false;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HINGE, POWERED, THIRD);
    }

    /** Erlaubt Mobs/Iron-Golems das Tor nicht zu durchbrechen, wenn closed. */
    public boolean isOpen(final BlockState state) {
        return state.get(OPEN);
    }

}
