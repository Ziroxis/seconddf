package com.yuanno.seconddf.mixins;

import com.yuanno.seconddf.data.allow.AllowCapability;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.pixelatedw.mineminenomi.blocks.HardenedSnowBlock;
import xyz.pixelatedw.mineminenomi.data.entity.devilfruit.DevilFruitCapability;
import xyz.pixelatedw.mineminenomi.init.ModAbilities;

@Mixin(value = HardenedSnowBlock.class, priority = 990)
public class HardenedSnowBlockMixin {


    /**
     * @author Beosti
     * @reason We are continuing the ballin'
     */
    @Overwrite
    public VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos pos, ISelectionContext context)
    {
        Entity entity = context.getEntity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (DevilFruitCapability.get(player).hasDevilFruit(ModAbilities.YUKI_YUKI_NO_MI) || AllowCapability.get(player).getSecondFruit().equals("yuki_yuki")) {
                return VoxelShapes.empty();
            }
        }

        return VoxelShapes.block();
    }
}
