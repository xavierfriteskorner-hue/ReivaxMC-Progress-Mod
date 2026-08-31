package fr.reivaxmc.progress.block;

import fr.reivaxmc.progress.story.F85SanctuaryPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public final class FoundationBeaconBlock extends ShapedBlock {
   public FoundationBeaconBlock(Properties p) {
      super(p, Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0));
   }

   protected InteractionResult useWithoutItem(BlockState var1, Level var2, BlockPos var3, Player var4, BlockHitResult var5) {
      return F85SanctuaryPatch.directBeaconUse(var2, var3, var4) ? InteractionResult.SUCCESS : InteractionResult.PASS;
   }
}
