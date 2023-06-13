package net.entityoutliner.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.entityoutliner.EntityOutliner;
import net.entityoutliner.ui.EntitySelector;
import net.entityoutliner.ui.ColorWidget.Color;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (EntityOutliner.outliningEntities
                && vertexConsumers instanceof OutlineVertexConsumerProvider
                && EntitySelector.outlinedEntityTypes.containsKey(entity.getType())) {

            Color color = EntitySelector.outlinedEntityTypes.get(entity.getType());
            OutlineVertexConsumerProvider outlineVertexConsumers = (OutlineVertexConsumerProvider) vertexConsumers;
            outlineVertexConsumers.setColor(color.red, color.green, color.blue, 255);

            if (entity.getType() == EntityType.PLAYER) {
                PlayerEntity player = (PlayerEntity) entity;
                AbstractTeam team = player.getScoreboardTeam();
                if (team != null && team.getColor().getColorValue() != null) {
                    int hexColor = team.getColor().getColorValue();
                    int blue = hexColor % 256;
                    int green = (hexColor / 256) % 256;
                    int red = (hexColor / 65536) % 256;
                    outlineVertexConsumers.setColor(red, green, blue, 255);
                }
            }
        }
    }
}