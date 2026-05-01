package com.troblecodings.tcutility.items;

import com.troblecodings.tcutility.blocks.TCBigDoor;
import com.troblecodings.tcutility.blocks.TCBigDoor.BigDoorThird;
import com.troblecodings.tcutility.init.TCTabs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Setzt beim Klicken den dreistoeckigen Cluster eines {@link TCBigDoor}:
 * Lower bekommt das Default-OPEN/POWERED, Middle und Upper spiegeln den
 * State und tragen ihre eigenen {@link BigDoorThird}-Wert. Hinge-Seite
 * orientiert sich nach Standard-Vanilla-Door-Heuristik (Nachbarn auf
 * der Seite des Players + Hit-Position des Klicks).
 */
public class TCBigDoorItem extends Item {

    private final TCBigDoor door;

    public TCBigDoorItem(final Block block) {
        super(new Item.Properties().group(TCTabs.DOORS));
        this.door = (TCBigDoor) block;
        this.door.setItem(this);
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext ctx) {
        if (ctx.getFace() != Direction.UP) {
            return ActionResultType.FAIL;
        }
        final World world = ctx.getWorld();
        final BlockPos lower = ctx.getPos().offset(ctx.getFace());
        final BlockPos middle = lower.up();
        final BlockPos upper = middle.up();

        if (!isReplaceable(world, lower) || !isReplaceable(world, middle)
                || !isReplaceable(world, upper)) {
            return ActionResultType.FAIL;
        }
        // Stehen wir auf einem soliden Untergrund? Tuer braucht etwas zum
        // "draufstehen".
        if (!world.getBlockState(lower.down()).isSolid()) {
            return ActionResultType.FAIL;
        }

        final PlayerEntity player = ctx.getPlayer();
        final Direction facing = (player != null
                ? player.getHorizontalFacing()
                : Direction.NORTH);
        final DoorHingeSide hinge = computeHinge(world, lower, facing, ctx.getHitVec());

        final BlockState defaultState = door.getDefaultState()
                .with(TCBigDoor.FACING, facing)
                .with(TCBigDoor.HINGE, hinge);
        world.setBlockState(lower,
                defaultState.with(TCBigDoor.THIRD, BigDoorThird.LOWER), 11);
        world.setBlockState(middle,
                defaultState.with(TCBigDoor.THIRD, BigDoorThird.MIDDLE), 11);
        world.setBlockState(upper,
                defaultState.with(TCBigDoor.THIRD, BigDoorThird.UPPER), 11);

        world.playSound(player, lower, SoundEvents.BLOCK_WOODEN_DOOR_OPEN,
                SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (player == null || !player.abilities.isCreativeMode) {
            final ItemStack stack = ctx.getItem();
            stack.shrink(1);
        }
        return ActionResultType.SUCCESS;
    }

    private static boolean isReplaceable(final World world, final BlockPos pos) {
        final BlockState st = world.getBlockState(pos);
        return st.getMaterial().isReplaceable();
    }

    /**
     * Vereinfachte Vanilla-Heuristik: bevorzuge LEFT, kippe auf RIGHT, wenn
     * der Klick rechts der Mittellinie war oder der rechte Nachbar bereits
     * eine Tuer ist.
     */
    private static DoorHingeSide computeHinge(final World world, final BlockPos lower,
            final Direction facing, final Vec3d hit) {
        final Direction left = facing.rotateYCCW();
        final BlockPos leftNeighbor = lower.offset(left);
        final BlockPos rightNeighbor = lower.offset(left.getOpposite());

        final boolean rightIsDoor = world.getBlockState(rightNeighbor)
                .getBlock() instanceof TCBigDoor;
        final boolean leftIsDoor = world.getBlockState(leftNeighbor)
                .getBlock() instanceof TCBigDoor;
        if (rightIsDoor && !leftIsDoor) {
            return DoorHingeSide.LEFT;
        }
        if (leftIsDoor && !rightIsDoor) {
            return DoorHingeSide.RIGHT;
        }

        // Fallback: relative Hit-Position auf der dem Spieler zugewandten Seite
        final double dx = hit.x - lower.getX();
        final double dz = hit.z - lower.getZ();
        switch (facing) {
            case NORTH:
                return dx < 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case SOUTH:
                return dx > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case WEST:
                return dz > 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            case EAST:
            default:
                return dz < 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        }
    }

    /** Used by TCBlocks to know we're a placement-overriding item. */
    @SuppressWarnings("unused")
    private void unused(final BlockItemUseContext ignored) {
    }
}
