package net.idothehax.cardboardbox.client.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.JumpingMount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "tickRiding", at = @At("HEAD"))
    private void onTickRiding(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof JumpingMount mount) {
            if (player.input.jumping && mount.canJump()) {
                mount.startJumping(0);
            } else {
                mount.stopJumping();
            }
        }
    }
}