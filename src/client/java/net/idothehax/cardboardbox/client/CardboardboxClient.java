package net.idothehax.cardboardbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.idothehax.cardboardbox.Cardboardbox;

public class CardboardboxClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Cardboardbox.CARDBOARD_BOX_ENTITY, CardboardBoxRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(CardboardBoxModel.LAYER_LOCATION, CardboardBoxModel::createBodyLayer);
    }
}
