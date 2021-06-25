package net.entityoutliner.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.entityoutliner.EntityOutliner;
import net.entityoutliner.ui.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;



@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow @Final private EntityType<?> type;

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void outlineEntities(CallbackInfoReturnable<Boolean> ci) {
        if (EntityOutliner.outliningEntities && EntitySelector.outlinedEntityTypes != null) {
            if (EntitySelector.outlinedEntityTypes.containsKey(this.type)) {
                ci.setReturnValue(true);
            } 
        }
    }
}