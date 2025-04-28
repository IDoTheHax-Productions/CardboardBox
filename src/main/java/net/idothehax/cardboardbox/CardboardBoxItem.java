package net.idothehax.cardboardbox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CardboardBoxItem extends Item {
    public CardboardBoxItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            CardboardBoxEntity box = new CardboardBoxEntity(Cardboardbox.CARDBOARD_BOX_ENTITY, world);
            Vec3d pos = user.getPos().add(0, 0.5, 0);
            box.setPosition(pos);
            world.spawnEntity(box);
            user.startRiding(box);
            if (!user.isCreative()) {
                stack.decrement(1);
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}