package fr.reivaxmc.progress.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class MemorialPlaqueBlock extends ShapedBlock {
   public MemorialPlaqueBlock(Properties p) {
      super(p, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0));
   }
}
