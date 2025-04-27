package net.idothehax.cardboardbox;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class CardboardBoxItem extends Item {
    public CardboardBoxItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            // Toggle hiding state
            boolean isHiding = user.getDataTracker().get(PlayerEntityAccessor.getHidingInBox());
            user.getDataTracker().set(PlayerEntityAccessor.getHidingInBox(), !isHiding);
            user.setInvisible(!isHiding); // Visual invisibility for other players
        }
        return TypedActionResult.success(stack);
    }
}