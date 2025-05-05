package net.idothehax.cardboardbox;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cardboardbox implements ModInitializer {
    public static String MOD_ID = "cardboardbox";
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);


    // Items
    public static final Item CARDBOARD_BOX_ITEM = new CardboardBoxItem(new Item.Settings().maxCount(1));
    public static final Item CARDBOARD = new Item(new Item.Settings().food(new FoodComponent.Builder()
            .alwaysEdible()
            .nutrition(1)
            .saturationModifier(0.2F)
            .build()));

    // Entity
    public static final EntityType<CardboardBoxEntity> CARDBOARD_BOX_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "cardboard_box"),
                    EntityType.Builder.create(CardboardBoxEntity::new, SpawnGroup.MISC)
                    .dimensions(1.0f, 1.0f)
                    .maxTrackingRange(10)
                    .build()
    );


    @Override
    public void onInitialize() {

        registerFuels();
        registerItems();

        FabricDefaultAttributeRegistry.register(CARDBOARD_BOX_ENTITY, CardboardBoxEntity.createAttributes());

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
            content.add(CARDBOARD_BOX_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            content.add(CARDBOARD);
        });

    }

    public static void registerFuels() {
        FuelRegistry.INSTANCE.add(CARDBOARD, 240);
    }

    public static void registerItems() {
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cardboard_box"), CARDBOARD_BOX_ITEM);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cardboard"), CARDBOARD);
    }

}
