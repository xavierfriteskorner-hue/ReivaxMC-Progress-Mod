package fr.reivaxmc.progress.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/** Cœur indestructible d'une Pierre de l'Écho multibloc. */
public final class EchoStoneBlock extends ShapedBlock {
   public EchoStoneBlock(Properties properties) {
      super(properties, Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0));
   }
}
