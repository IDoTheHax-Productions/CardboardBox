package net.idothehax.cardboardbox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.idothehax.cardboardbox.block.CardboardBoxBlock;
import net.idothehax.cardboardbox.block.entity.CardboardBoxBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cardboardbox implements ModInitializer {
    public static String MOD_ID = "cardboardbox";
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);


    // Items
    public static final Item PAPER_FIBERS = new Item(new Item.Settings().food(new FoodComponent.Builder()
            .alwaysEdible()
            .nutrition(1)
            .saturationModifier(0.2F)
            .build()));

    // Blocks
    public static final Block CARDBOARD_BOX = new CardboardBoxBlock(AbstractBlock.Settings.copy(Blocks.OAK_WOOD).strength(0.5f));

    // Block entity
    public static final BlockEntityType<CardboardBoxBlockEntity> CARDBOARD_BOX_ENTITY = registerBlockEntityTypes(
            "cardboard_box_entity",
            BlockEntityType.Builder.create(CardboardBoxBlockEntity::new, CARDBOARD_BOX).build()
    );

    @Override
    public void onInitialize() {
        //ModDataTrackers.register();

        registerFuels();
        registerItems();
        registerBlocks();
    }

    public static void registerFuels() {
        FuelRegistry.INSTANCE.add(PAPER_FIBERS, 240);
    }

    public static void registerItems() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cardboard_box"), new BlockItem(CARDBOARD_BOX, new Item.Settings()));
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "paper_fiber"), PAPER_FIBERS);
    }

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "cardboard_box"), CARDBOARD_BOX);
    }

    public static <T extends BlockEntityType<?>> T registerBlockEntityTypes(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, path), blockEntityType);
    }
}
