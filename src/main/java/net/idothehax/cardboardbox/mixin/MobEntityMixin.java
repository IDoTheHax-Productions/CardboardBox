package net.idothehax.cardboardbox.mixin;

import net.idothehax.cardboardbox.block.CardboardBoxBlock;
import net.idothehax.cardboardbox.block.entity.CardboardBoxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingPlayerInBox(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity player) {
            World world = player.getWorld();
            BlockPos pos = player.getBlockPos();
            if (world.getBlockState(pos).getBlock() instanceof CardboardBoxBlock) {
                CardboardBoxBlockEntity blockEntity = (CardboardBoxBlockEntity) world.getBlockEntity(pos);
                if (blockEntity != null && blockEntity.getOccupant() == player) {
                    ci.cancel(); // Prevent mob from targeting the player
                }
            }
        }
    }
}