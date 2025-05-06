package net.idothehax.cardboardbox;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.Collections;

public class CardboardBoxEntity extends LivingEntity implements JumpingMount {
    private float jumpStrength;
    protected boolean jumping;
    private Vec3d movementInput; // Store movement input
    protected boolean inAir;
    private static final TrackedData<Boolean> JUMPING = DataTracker.registerData(CardboardBoxEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public CardboardBoxEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        this.setNoGravity(false);
        this.setHealth(this.getMaxHealth());
        this.movementInput = Vec3d.ZERO; // Initialize movementInput
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.4)
                .add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.7)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(JUMPING, false);  // Add the tracked data with initial value false
    }

    @Override
    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        if (this.hasPassengers()) {
            float friction = 0.91F * slipperiness;

            float moveForward = (float)movementInput.z;
            float moveStrafing = (float)movementInput.x;

            // Increased speed
            float speed = 0.4F;

            Vec3d movement = new Vec3d(
                    moveStrafing * speed,
                    movementInput.y,
                    moveForward * speed
            );

            return movement.multiply(friction, 1.0, friction);
        }

        return super.applyMovementInput(movementInput, slipperiness);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient && !this.hasPassengers()) {
            this.discard();
            return;
        }

        if (this.hasPassengers() && this.getFirstPassenger() instanceof PlayerEntity player) {
            // Basic movement control
            float forward = player.forwardSpeed;
            float strafe = player.sidewaysSpeed;
            float yaw = player.getYaw();

            Vec3d currentVelocity = this.getVelocity();

            // Only apply movement if there's input
            if (forward != 0 || strafe != 0) {
                Vec3d movement = new Vec3d(strafe, 0, forward)
                        .rotateY((float) Math.toRadians(-yaw))
                        .multiply(0.4);

                // IMPORTANT: Only update x and z components, preserve y velocity
                this.setVelocity(
                        movement.x,
                        currentVelocity.y,  // Preserve vertical velocity
                        movement.z
                );
            } else {
                // When not moving, preserve vertical velocity only
                this.setVelocity(
                        currentVelocity.x * 0.91,
                        currentVelocity.y,
                        currentVelocity.z * 0.91
                );
            }

            // Apply gravity only if in air
            if (!this.isOnGround()) {
                this.setVelocity(this.getVelocity().add(0, -0.08, 0));
            }

            // Apply motion
            this.move(MovementType.SELF, this.getVelocity());

            // Apply air resistance to vertical movement only when in air
            Vec3d velocity = this.getVelocity();
            if (!this.isOnGround()) {
                this.setVelocity(
                        velocity.x,
                        velocity.y * 0.98, // Air resistance on vertical movement
                        velocity.z
                );
            }

            if (!this.getWorld().isClient) {
                this.velocityDirty = true;
            }

            // Update ground state
            if (this.isOnGround()) {
                this.inAir = false;
            }
        }
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);

        // Rotation
        Vec2f vec2f = this.getControlledRotation(controllingPlayer);
        this.setRotation(vec2f.y, vec2f.x);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();

        this.movementInput = movementInput;

        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.isOnGround()) {
                this.setInAir(false);
                if (this.jumping && !this.isInAir()) {
                    this.jump();
                }
            }
        }
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
    }

    @Override
    protected float getJumpVelocity(float strength) {
        return super.getJumpVelocity(strength);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        super.fall(heightDifference, onGround, state, landedPosition);
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

    @Override
    public void jump() {
        if (this.isOnGround()) {
            double jumpForce = 0.5; // Adjust this value for jump height
            this.setVelocity(this.getVelocity().add(0, jumpForce, 0));
            this.jumping = true;
            this.velocityDirty = true;
        }
    }

    public void setJumpStrength(int strength) {
        if (strength < 0) {
            strength = 0;
        } else {
            this.jumping = true;
        }

        if (strength >= 90) {
            this.jumpStrength = 1.0F;
        } else {
            this.jumpStrength = 0.4F + 0.4F * (float)strength / 90.0F;
        }
    }

    @Override
    public void startJumping(int height) {
        if (this.isOnGround()) {
            this.jumping = true;
            this.getDataTracker().set(JUMPING, true);

            Vec3d currentVelocity = this.getVelocity();

            // Apply jump velocity while preserving horizontal momentum
            this.setVelocity(
                    currentVelocity.x,
                    0.5,
                    currentVelocity.z
            );

            this.velocityDirty = true;
            this.inAir = true;
        }
    }

    @Override
    public void stopJumping() {
        this.jumping = false;
        this.getDataTracker().set(JUMPING, false);
    }

    @Override
    public boolean canJump() {
        return this.isOnGround();
    }

    @Override
    public void setJumping(boolean jumping) {
        this.getDataTracker().set(JUMPING, jumping);
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

    @Override
    public void setVelocity(Vec3d velocity) {
        super.setVelocity(velocity);
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x, y, z);
    }

    @Override
    public void setVelocityClient(double x, double y, double z) {
        super.setVelocityClient(x, y, z);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 1;
    }

    @Override
    protected void updateLimbs(float v) {
        // This helps with animation timing if you add any
        super.updateLimbs(v);
        if (this.getDataTracker().get(JUMPING)) {
            this.jump();
        }
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}