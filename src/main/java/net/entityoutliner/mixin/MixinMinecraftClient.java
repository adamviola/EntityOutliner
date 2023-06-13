package net.entityoutliner.mixin;

import net.entityoutliner.EntityOutliner;
import net.entityoutliner.ui.EntitySelector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void outlineEntities(Entity entity, CallbackInfoReturnable<Boolean> ci) {
        if (EntityOutliner.outliningEntities && EntitySelector.outlinedEntityTypes != null) {
            if (EntitySelector.outlinedEntityTypes.containsKey(entity.getType())) {
                ci.setReturnValue(true);
            } 
        }
    }
}