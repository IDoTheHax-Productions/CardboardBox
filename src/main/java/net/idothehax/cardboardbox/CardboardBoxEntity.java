package net.idothehax.cardboardbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CardboardBoxEntity extends Entity {
    public CardboardBoxEntity(EntityType<? extends Entity> type, World world) {
        super(type, world);
        this.setNoGravity(false);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && !this.hasPassengers()) {
            this.discard(); // Remove entity if no passengers
        }

        // Handle movement
        if (this.hasPassengers() && this.getFirstPassenger() instanceof PlayerEntity player) {
            // Apply movement based on player input
            float forward = player.forwardSpeed;
            float strafe = player.sidewaysSpeed;
            float yaw = player.getYaw();

            if (forward != 0 || strafe != 0) {
                Vec3d movement = new Vec3d(strafe, 0, forward)
                        .rotateY((float) Math.toRadians(-yaw))
                        .multiply(0.2); // Movement speed
                this.setVelocity(movement.x, this.getVelocity().y, movement.z);
            }

            // Handle sneaking to dismount
            if (player.isSneaking()) {
                this.removeAllPassengers();
                return;
            }

            // Apply motion
            this.move(MovementType.SELF, this.getVelocity());

            // Broadcast velocity update to tracking players
            if (this.getWorld().getServer() != null) {
                EntityVelocityUpdateS2CPacket packet = new EntityVelocityUpdateS2CPacket(this);
                for (ServerPlayerEntity serverPlayer : this.getWorld().getServer().getPlayerManager().getPlayerList()) {
                    if (serverPlayer.getWorld().equals(this.getWorld())) {
                        serverPlayer.networkHandler.sendPacket(packet);
                    }
                }
            }

            // Apply friction
            this.setVelocity(this.getVelocity().multiply(0.9));
        }
    }


    @Override
    public boolean collidesWith(Entity other) {
        return other instanceof PlayerEntity && !this.hasPassenger(other);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean shouldDismountUnderwater() {
        return super.shouldDismountUnderwater();
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        passenger.setPosition(this.getX(), this.getY() + 0.5, this.getZ());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return super.writeNbt(nbt);
    }
}