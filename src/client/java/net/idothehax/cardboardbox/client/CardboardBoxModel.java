package net.idothehax.cardboardbox.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class CardboardBoxModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final EntityModelLayer LAYER_LOCATION = new EntityModelLayer(Identifier.of("cardboardbox", "cardboard_box"), "main");
	private final ModelPart group;
	private final ModelPart bone;

	public CardboardBoxModel(ModelPart root) {
		this.group = root.getChild("group");
		this.bone = root.getChild("bone");
	}

	public static TexturedModelData createBodyLayer() {
		ModelData meshdefinition = new ModelData();
		ModelPartData partdefinition = meshdefinition.getRoot();

		ModelPartData group = partdefinition.addChild("group", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, -8.0F));

		ModelPartData bone = partdefinition.addChild("bone", ModelPartBuilder.create().uv(0, 26).cuboid(4.0F, -32.0F, -7.0F, 2.0F, 32.0F, 28.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-20.0F, -32.0F, -5.0F, 24.0F, 2.0F, 24.0F, new Dilation(0.0F))
		.uv(60, 26).cuboid(-22.0F, -32.0F, -7.0F, 2.0F, 32.0F, 28.0F, new Dilation(0.0F))
		.uv(112, 86).cuboid(-20.0F, -32.0F, -7.0F, 24.0F, 32.0F, 2.0F, new Dilation(0.0F))
		.uv(120, 0).cuboid(-20.0F, -32.0F, 19.0F, 24.0F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(8.0F, 24.0F, -8.0F));

		ModelPartData cube_r1 = bone.addChild("cube_r1", ModelPartBuilder.create().uv(120, 48).cuboid(-20.0F, -14.0F, 0.0F, 28.0F, 14.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, 0.0F, 21.0F, -0.3927F, 0.0F, 0.0F));

		ModelPartData cube_r2 = bone.addChild("cube_r2", ModelPartBuilder.create().uv(120, 34).cuboid(-20.0F, -14.0F, 0.0F, 28.0F, 14.0F, 0.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, 0.0F, -7.0F, 0.3927F, 0.0F, 0.0F));

		ModelPartData cube_r3 = bone.addChild("cube_r3", ModelPartBuilder.create().uv(56, 86).cuboid(0.0F, -14.0F, -7.0F, 0.0F, 14.0F, 28.0F, new Dilation(0.0F)), ModelTransform.of(-22.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

		ModelPartData cube_r4 = bone.addChild("cube_r4", ModelPartBuilder.create().uv(0, 86).cuboid(0.0F, -14.0F, -7.0F, 0.0F, 14.0F, 28.0F, new Dilation(0.0F)), ModelTransform.of(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

		return TexturedModelData.of(meshdefinition, 256, 256);
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
		group.render(matrices, vertices, light, overlay, color);
		bone.render(matrices, vertices, light, overlay, color);
	}
}