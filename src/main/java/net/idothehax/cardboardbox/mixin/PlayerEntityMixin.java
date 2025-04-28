package net.idothehax.cardboardbox.mixin;

import net.idothehax.cardboardbox.block.CardboardBoxBlock;
import net.idothehax.cardboardbox.block.entity.CardboardBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (player.isSneaking() && !world.isClient) {
            BlockPos pos = player.getBlockPos();
            if (world.getBlockState(pos).getBlock() instanceof CardboardBoxBlock) {
                CardboardBoxBlockEntity blockEntity = (CardboardBoxBlockEntity) world.getBlockEntity(pos);
                if (blockEntity != null && blockEntity.getOccupant() == player) {
                    blockEntity.exit(player);
                }
            }
        }
    }
}
