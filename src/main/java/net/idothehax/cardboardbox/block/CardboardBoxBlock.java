package net.idothehax.cardboardbox.block;

import net.idothehax.cardboardbox.block.entity.CardboardBoxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CardboardBoxBlock extends Block implements BlockEntityProvider {
    public CardboardBoxBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CardboardBoxBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            CardboardBoxBlockEntity blockEntity = (CardboardBoxBlockEntity) world.getBlockEntity(pos);
            if (blockEntity != null && blockEntity.getOccupant() == null && !player.isSneaking()) {
                blockEntity.setOccupant(player);
                player.setInvisible(true); // Make player invisible to mobs
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

}