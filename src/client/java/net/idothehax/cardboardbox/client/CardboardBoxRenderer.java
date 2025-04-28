package net.idothehax.cardboardbox.client;

import net.idothehax.cardboardbox.CardboardBoxEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CardboardBoxRenderer extends EntityRenderer<CardboardBoxEntity> {
	private static final Identifier TEXTURE = Identifier.of("cardboardbox", "textures/entity/cardboard_box.png");
	private final CardboardBoxModel model;

	public CardboardBoxRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
		this.model = new CardboardBoxModel(ctx.getPart(CardboardBoxModel.LAYER_LOCATION));
	}

	@Override
	public void render(CardboardBoxEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		// Adjust for Blockbench model scale and player position
		matrices.translate(0.0, 1.0, 0.0);
		matrices.scale(1.0F, 1.0F, 1.0F); // Adjust scale if needed
		this.model.render(matrices, vertexConsumers.getBuffer(model.getLayer(this.getTexture(entity))), light, 0);
		matrices.pop();
	}

	@Override
	public Identifier getTexture(CardboardBoxEntity entity) {
		return TEXTURE;
	}
}