package net.idothehax.cardboardbox.block.entity;

import net.idothehax.cardboardbox.Cardboardbox;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class CardboardBoxBlockEntity extends BlockEntity {
    private PlayerEntity occupant;

    public CardboardBoxBlockEntity(BlockPos pos, BlockState state) {
        super(Cardboardbox.CARDBOARD_BOX_ENTITY, pos, state);
    }

    public void setOccupant(PlayerEntity player) {
        this.occupant = player;
    }

    public PlayerEntity getOccupant() {
        return occupant;
    }

    public void exit(PlayerEntity player) {
        if (occupant == player) {
            occupant.setInvisible(false); // Remove invisibility
            occupant = null;
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
    }
}