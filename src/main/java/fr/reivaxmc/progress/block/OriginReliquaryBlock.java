package fr.reivaxmc.progress.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class OriginReliquaryBlock extends ShapedBlock {
   public OriginReliquaryBlock(Properties p) {
      super(p, Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0));
   }
}
