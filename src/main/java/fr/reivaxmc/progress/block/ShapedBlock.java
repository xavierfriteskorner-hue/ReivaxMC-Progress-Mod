package fr.reivaxmc.progress.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapedBlock extends Block {
   private final VoxelShape shape;

   public ShapedBlock(Properties p, VoxelShape s) {
      super(p);
      this.shape = s;
   }

   protected VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return this.shape;
   }

   protected VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) {
      return this.shape;
   }

   protected boolean propagatesSkylightDown(BlockState s, BlockGetter l, BlockPos p) {
      return true;
   }
}
