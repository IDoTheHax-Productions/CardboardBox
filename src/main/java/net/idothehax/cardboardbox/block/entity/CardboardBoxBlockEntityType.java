package net.idothehax.cardboardbox.block.entity;

import com.sun.jna.platform.win32.COM.util.Factory;
import net.minecraft.block.entity.BlockEntityType;

public class CardboardBoxBlockEntityType extends BlockEntityType<CardboardBoxBlockEntity> {
    public CardboardBoxBlockEntityType(Factory<? extends CardboardBoxBlockEntity> factory) {
        super(factory, null, null);
    }
}