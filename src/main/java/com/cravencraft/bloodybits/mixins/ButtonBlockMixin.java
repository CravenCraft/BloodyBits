package com.cravencraft.bloodybits.mixins;

import com.cravencraft.bloodybits.BloodyBitsMod;
import com.cravencraft.bloodybits.entity.custom.BloodSprayEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonBlock.class)
public abstract class ButtonBlockMixin extends FaceAttachedHorizontalDirectionalBlock {

    @Shadow @Final public static BooleanProperty POWERED;

    @Shadow @Final private boolean arrowsCanPress;

    @Shadow protected abstract void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos);

    @Shadow protected abstract void playSound(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos, boolean pHitByArrow);

    @Shadow @Final private int ticksToStayPressed;

    public ButtonBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    @Inject(method = "entityInside", at = @At("HEAD"), remap = false, cancellable = true)
    public void stopBloodSpatter(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity, CallbackInfo ci) {
        if (pEntity instanceof BloodSprayEntity bloodSprayEntity) {
            BloodyBitsMod.LOGGER.info("BLOOD SPRAY HIT BUTTON.");
            ci.cancel();
        }
    }



    // TODO: This combined with the above method work with fixing the button press issue.
    @Inject(method = "checkPressed", at = @At("HEAD"), remap = false)
    public void bloodSpatterCannotPressButton(BlockState pState, Level pLevel, BlockPos pPos, CallbackInfo ci) {
        AbstractArrow abstractarrow = this.arrowsCanPress ? pLevel.getEntitiesOfClass(AbstractArrow.class, pState.getShape(pLevel, pPos).bounds().move(pPos)).stream().findFirst().orElse((AbstractArrow)null) : null;
//        boolean flag = abstractarrow != null;
        boolean flag = !(abstractarrow instanceof BloodSprayEntity || abstractarrow == null);
        boolean flag1 = pState.getValue(POWERED);
        if (flag != flag1) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
            this.updateNeighbours(pState, pLevel, pPos);
            this.playSound((Player)null, pLevel, pPos, flag);
            pLevel.gameEvent(abstractarrow, flag ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pPos);
        }

        if (flag) {
            pLevel.scheduleTick(new BlockPos(pPos), this, this.ticksToStayPressed);
        }
    }
}
