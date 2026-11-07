package com.cuzzi.reforged.client.render;

import com.cuzzi.reforged.server.magic.RuneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class RuneRenderer extends EntityRenderer<RuneEntity> {

    public RuneRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RuneEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. Posicionamiento base y Orientación 3D
        poseStack.translate(0.0D, 0.05D, 0.0D); // Evita Z-fighting con el piso
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getRuneYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getRunePitch()));

        // 2. Escala Base
        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);

        // 3. Rotación Dinámica (Spin)
        float currentRotation = Mth.lerp(partialTick, entity.previousRotation, entity.currentRotation);
        poseStack.mulPose(Axis.ZP.rotationDegrees(currentRotation));

        // Extracción de color
        int color = entity.getColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(entity)));
        float size = 1.0f;

        // EFECTO 3D EXTRUIDO
        int layers = 12;         // Suficientes capas para cerrar el hueco
        float thickness = 0.06f; // El equivalente a 1 pixel de bloque en Minecraft

        for (int i = 0; i < layers; i++) {
            poseStack.pushPose();

            float depthOffset = (i * (thickness / (layers - 1))) - (thickness / 2);
            poseStack.translate(0, 0, depthOffset);

            Matrix4f matrix4f = poseStack.last().pose();

           float shade = (i == 0 || i == layers - 1) ? 1.0f : 0.5f;

            int rShaded = (int) (r * shade);
            int gShaded = (int) (g * shade);
            int bShaded = (int) (b * shade);
            int alpha = 255;

            vertexConsumer.addVertex(matrix4f, -size, -size, 0.0F).setColor(rShaded, gShaded, bShaded, alpha).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix4f, size, -size, 0.0F).setColor(rShaded, gShaded, bShaded, alpha).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix4f, size, size, 0.0F).setColor(rShaded, gShaded, bShaded, alpha).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix4f, -size, size, 0.0F).setColor(rShaded, gShaded, bShaded, alpha).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);

            poseStack.popPose();
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RuneEntity entity) {
        return ResourceLocation.parse(entity.getTexture());
    }
}