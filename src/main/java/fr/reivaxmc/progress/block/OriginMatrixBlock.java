package fr.reivaxmc.progress.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class OriginMatrixBlock extends ShapedBlock {
   public OriginMatrixBlock(Properties p) {
      super(p, Block.box(1.0, 0.0, 1.0, 15.0, 5.0, 15.0));
   }
}
