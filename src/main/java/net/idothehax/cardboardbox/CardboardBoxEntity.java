package net.idothehax.cardboardbox;

import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;

public class CardboardBoxEntity extends LivingEntity implements JumpingMount {
    private float jumpStrength;
    private boolean jumping;
    protected boolean inAir;

    public CardboardBoxEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.setNoGravity(false);
        this.setHealth(this.getMaxHealth());
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.5);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
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
                        .multiply(0.4); // Movement speed
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

            // Apply friction to horizontal movement
            this.setVelocity(this.getVelocity().multiply(0.9, 1.0, 0.9));
        }
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        System.out.println("tickControlled called with jumpStrength: " + this.jumpStrength);
        Vec2f vec2f = this.getControlledRotation(controllingPlayer);
        this.setRotation(vec2f.y, vec2f.x);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
        if (this.isLogicalSideForUpdatingMovement()) {


            if (this.isOnGround()) {
                this.setInAir(false);
                if (this.jumpStrength > 0.0F && !this.isInAir()) {
                    this.jump(this.jumpStrength, movementInput);
                }

                this.jumpStrength = 0.0F;
            }
        }
    }

    protected Vec2f getControlledRotation(LivingEntity controllingPassenger) {
        return new Vec2f(controllingPassenger.getPitch() * 0.5F, controllingPassenger.getYaw());
    }

    public boolean isInAir() {
        return this.inAir;
    }


    public void setInAir(boolean inAir) {
        this.inAir = inAir;
    }

    protected void jump(float strength, Vec3d movementInput) {
        double jumpVelocity = this.getJumpVelocity(strength);
        Vec3d velocity = this.getVelocity();
        this.setVelocity(velocity.x, jumpVelocity, velocity.z);
        this.velocityDirty = true;

        // Add forward momentum if moving forward
        if (movementInput.z > 0.0) {
            float yawRad = (float) Math.toRadians(this.getYaw());
            float f = (float) Math.sin(yawRad);
            float g = (float) Math.cos(yawRad);
            this.setVelocity(this.getVelocity().add(-0.4F * f * strength, 0.0, 0.4F * g * strength));
        }
    }

    @Override
    public void setJumpStrength(int strength) {
        if (strength < 0) {
            strength = 0;
        } else {
            this.jumping = true; // Mark as jumping
        }

        if (strength >= 90) {
            this.jumpStrength = 1.0F; // Maximum jump strength
        } else {
            this.jumpStrength = 0.4F + 0.4F * (float) strength / 90.0F; // Scale jump strength
        }
    }

    @Override
    public boolean canJump() {
        return true; // Allow jumping at all times (no saddle requirement like horses)
    }

    @Override
    public void startJumping(int height) {
        this.jumping = true; // Mark as jumping
    }

    @Override
    public void stopJumping() {
        this.jumping = false; // Reset jumping state
    }

    @Override
    public Arm getMainArm() {
        return null;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public Box getBoundingBox(EntityPose pose) {
        return new Box(-0.5, 0.0, -0.5, 0.5, 1.0, 0.5); // Adjust to match model size
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
    public Vec3d getPassengerRidingPos(Entity passenger) {
        Vec3d basePos = super.getPassengerRidingPos(passenger);
        return new Vec3d(basePos.x, basePos.y - 1, basePos.z);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return super.writeNbt(nbt);
    }

    // Required by LivingEntity
    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }
}