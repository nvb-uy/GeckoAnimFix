package elocindev.geckoanimfix.mixin.common;

//#if FABRIC==1
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
//#else
//$$ import net.minecraft.client.Minecraft;
//$$ import net.minecraft.world.entity.LivingEntity;
//#endif

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtils;

@Mixin(value = GeoModel.class, remap = false)
public abstract class GeoModelMixin<T extends GeoAnimatable> implements CoreGeoModel<T> {
    private long lastRenderedInstance = -1;
    @Shadow
    private double animTime;
    @Shadow
    private double lastGameTickTime;
    @Shadow
    abstract boolean crashIfBoneMissing();

    /**
     * @author ElocinDev
     * @reason Fixes Geckolib Shaders issues
     * @since 1.0.0
     */
    @Overwrite
    public final void handleAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        var mc = 
            //#if FABRIC==1
            MinecraftClient
            //#else
            //$$ Minecraft
            //#endif
            .getInstance();

        AnimatableManager<T> animatableManager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);
        Double currentTick = animationState.getData(DataTickets.TICK);

        if (currentTick == null)
            currentTick = animatable instanceof LivingEntity livingEntity ? (double) 
                //#if FABRIC==1
                livingEntity.age 
                //#else
                //$$ livingEntity.tickCount
                //#endif

            : RenderUtils.getCurrentTick();

        if (animatableManager.getFirstTickTime() == -1) animatableManager.startedAt(currentTick + 
            //#if FABRIC==1
            mc.getTickDelta()
            //#else
            //$$ mc.getFrameTime()
            //#endif                    
        );

        double currentFrameTime = currentTick - animatableManager.getFirstTickTime();
        boolean isReRender = !animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime();

        if (isReRender && instanceId == this.lastRenderedInstance) return;

        if (!isReRender && (!mc.isPaused() || animatable.shouldPlayAnimsWhileGamePaused())) {
            if (animatable instanceof LivingEntity) {
                animatableManager.updatedAt(currentFrameTime);
            } else {
                animatableManager.updatedAt(currentFrameTime);
            }

            double lastUpdateTime = animatableManager.getLastUpdateTime();
            this.animTime += lastUpdateTime - this.lastGameTickTime;
            this.lastGameTickTime = lastUpdateTime;
        }

        animationState.animationTick = this.animTime;
        AnimationProcessor<T> processor = getAnimationProcessor();

        processor.preAnimationSetup(animationState.getAnimatable(), this.animTime);

        if (!processor.getRegisteredBones().isEmpty())
            processor.tickAnimation(animatable, this, animatableManager, this.animTime, animationState,
                    crashIfBoneMissing());

        setCustomAnimations(animatable, instanceId, animationState);
    }
}