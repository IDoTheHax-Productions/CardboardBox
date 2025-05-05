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
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.5)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(EntityAttributes.GENERIC_GRAVITY);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(JUMPING, false);  // Add the tracked data with initial value false
    }

    @Override
    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        // Only apply movement input if we have a passenger
        if (this.hasPassengers()) {
            // Get the friction factor
            float friction = 0.91F * slipperiness;

            // Calculate movement forces
            float moveForward = (float)movementInput.z;
            float moveStrafing = (float)movementInput.x;
            float moveVertical = (float)movementInput.y;

            // Calculate movement speed
            float speed = 1.5F; // Adjust this value to control movement speed

            // Apply movement based on input
            Vec3d movement = new Vec3d(
                    moveStrafing * speed,
                    moveVertical,
                    moveForward * speed
            );

            // Apply friction
            movement = movement.multiply(friction, 1.0, friction);

            return movement;
        }

        // If no passengers, use default movement
        return super.applyMovementInput(movementInput, slipperiness);
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
                Vec3d playerMovement = new Vec3d(strafe, 0, forward)
                        .rotateY((float) Math.toRadians(-yaw))
                        .multiply(0.1); // Movement speed

                // Combine player movement with existing velocity
                Vec3d currentVelocity = this.getVelocity();
                Vec3d combinedVelocity = new Vec3d(
                        playerMovement.x,
                        currentVelocity.y, // Keep the Y velocity from jumping
                        playerMovement.z
                );
                this.setVelocity(combinedVelocity);
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

    protected void jump(float strength, Vec3d movementInput) {
        double d = (double)this.getJumpVelocity(strength);
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x, d, vec3d.z);
        this.setInAir(true);
        this.velocityDirty = true;
        if (movementInput.z > (double)0.0F) {
            float f = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F));
            float g = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F));
            this.setVelocity(this.getVelocity().add((double)(-0.4F * f * strength), (double)0.0F, (double)(0.4F * g * strength)));
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
        if (!this.isInAir() && this.isOnGround()) {
            // Use a fixed jump velocity instead of relying on jumpStrength
            double jumpVelocity = 0.5; // You can adjust this value
            Vec3d currentVelocity = this.getVelocity();

            // Apply upward velocity
            this.setVelocity(currentVelocity.x, jumpVelocity, currentVelocity.z);

            // Apply forward momentum if moving forward
            if (this.movementInput.z > 0.0F) {
                float yawRadians = this.getYaw() * ((float)Math.PI / 180F);
                this.setVelocity(this.getVelocity().add(
                        -0.2F * MathHelper.sin(yawRadians),
                        0.0,
                        0.2F * MathHelper.cos(yawRadians)
                ));
            }

            this.setInAir(true);
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
    public void setJumping(boolean jumping) {
        this.getDataTracker().set(JUMPING, jumping);
        this.jumping = jumping;
    }

    @Override
    public boolean canJump() {
        return true; // Allow jumping at all times (no saddle requirement like horses)
    }

    @Override
    public void startJumping(int height) {
        this.setJumping(true);
    }

    @Override
    public void stopJumping() {
        this.setJumping(false);
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
}