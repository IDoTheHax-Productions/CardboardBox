package net.idothehax.cardboardbox.mixin;

import net.idothehax.cardboardbox.CardboardBoxEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void preventTargetingPlayerInBox(LivingEntity target, CallbackInfo ci) {
        if (target instanceof PlayerEntity player) {
            if (player.getVehicle() instanceof CardboardBoxEntity) {
                ci.cancel(); // Prevent mob from targeting the player
            }
        }
    }
}