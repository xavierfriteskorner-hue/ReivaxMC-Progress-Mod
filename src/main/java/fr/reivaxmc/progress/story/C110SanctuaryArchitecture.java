package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Grand Sanctuaire 0.11.0. Le bâtiment entier existe dès l'apparition de la Trace ;
 * seules les séparations intérieures changent quand l'histoire progresse.
 */
public final class C110SanctuaryArchitecture {
   private static final String REGISTRY_OPEN = "F110_REGISTRY_OPEN";

   private C110SanctuaryArchitecture() {
   }

   public static boolean isPresent(Object serverObject, int[] origin) {
      if (!(serverObject instanceof MinecraftServer server)) return false;
      BlockPos marker = at(origin, 34, 10, -28);
      return server.overworld().getBlockState(marker).is((Block)ReivaxMCProgress.SANCTUARY_LUMEN.get());
   }

   public static void build(Object serverObject, int[] origin) {
      if (!(serverObject instanceof MinecraftServer server)) return;
      ServerLevel level = server.overworld();
      Block stone = (Block)ReivaxMCProgress.SANCTUARY_STONE.get();
      Block lumen = (Block)ReivaxMCProgress.SANCTUARY_LUMEN.get();
      Block seal = (Block)ReivaxMCProgress.SEAL_RECEPTACLE.get();
      CampaignSavedData campaign = CampaignSavedData.get(server);

      buildBuriedNave(level, origin, stone, lumen);
      buildFacade(level, origin, stone, lumen, seal);
      buildWing(level, origin, -1, stone, lumen, Blocks.REINFORCED_DEEPSLATE, false);
      buildWing(level, origin, 1, stone, lumen, Blocks.REINFORCED_DEEPSLATE,
         campaign.isCompleted(REGISTRY_OPEN));
      buildFutureGalleries(level, origin, stone, lumen);

      // Les trois objets fondateurs restent à leur place canonique.
      set(level, at(origin, -8, 2, -18), (Block)ReivaxMCProgress.ORIGIN_RELIQUARY.get());
      set(level, at(origin, 8, 2, -18), Blocks.LECTERN);
      set(level, at(origin, 0, 2, 29), (Block)ReivaxMCProgress.ORIGIN_MATRIX.get());
      set(level, at(origin, 34, 10, -28), lumen);

      if (!campaign.isCompleted(F94SanctuaryShell.BUILT)) campaign.complete(F94SanctuaryShell.BUILT, 0, 0);
   }

   private static void buildBuriedNave(ServerLevel level, int[] o, Block stone, Block lumen) {
      room(level, o, -18, 18, -28, 24, 0, 13, stone, Blocks.POLISHED_DEEPSLATE, lumen);
      // Nef centrale plus haute, transept et arcs qui évitent l'effet de simple boîte.
      for (int z = -24; z <= 20; z += 8) arch(level, o, -13, 13, z, 11, stone, lumen);
      for (int z = -27; z <= 23; z++) {
         if ((z + 27) % 6 == 0) {
            set(level, at(o, -17, 4, z), lumen);
            set(level, at(o, 17, 4, z), lumen);
         }
      }
      for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
         if (x * x + z * z >= 18 && x * x + z * z <= 30)
            set(level, at(o, x, 0, z), Blocks.CUT_COPPER);
      }
      // Axe visuel vers la Matrice, visible mais inaccessible derrière sa paroi.
      for (int x = -3; x <= 3; x++) for (int y = 1; y <= 7; y++) {
         boolean slit = x == 0 && y >= 2 && y <= 4;
         set(level, at(o, x, y, -28), slit ? Blocks.IRON_BARS : stone);
      }
   }

   private static void buildFacade(ServerLevel level, int[] o, Block stone, Block lumen, Block seal) {
      // Avant-corps monumental semi-enfoui.
      room(level, o, -22, 22, 24, 38, 0, 16, stone, Blocks.POLISHED_BLACKSTONE_BRICKS, lumen);
      clear(level, o, -4, 4, 23, 39, 1, 8);
      arch(level, o, -9, 9, 32, 13, stone, lumen);
      for (int x = -16; x <= 16; x++) {
         int h = 4 + Math.max(0, 9 - Math.abs(x) / 2);
         set(level, at(o, x, h, 38), stone);
      }
      // Deux réceptacles verticaux : un de chaque côté, jamais dans le sol.
      sealMonolith(level, o, -8, 29, stone, lumen, seal);
      sealMonolith(level, o, 8, 29, stone, lumen, seal);
   }

   private static void sealMonolith(ServerLevel level, int[] o, int x, int z, Block stone, Block lumen, Block seal) {
      for (int dx = -2; dx <= 2; dx++) for (int y = 1; y <= 11; y++) {
         boolean edge = Math.abs(dx) == 2 || y == 1 || y == 11;
         set(level, at(o, x + dx, y, z), edge ? stone : Blocks.CHISELED_DEEPSLATE);
      }
      for (int y : new int[]{3, 6, 9}) {
         set(level, at(o, x, y, z + 1), seal);
         set(level, at(o, x - 1, y, z + 1), Blocks.GILDED_BLACKSTONE);
         set(level, at(o, x + 1, y, z + 1), Blocks.GILDED_BLACKSTONE);
      }
      set(level, at(o, x, 12, z), lumen);
   }

   private static void buildWing(ServerLevel level, int[] o, int side, Block stone, Block lumen, Block locked, boolean opened) {
      int minX = side < 0 ? -36 : 19;
      int maxX = side < 0 ? -19 : 36;
      room(level, o, minX, maxX, -27, 4, 0, 10, stone, Blocks.POLISHED_DEEPSLATE, lumen);
      int thresholdX = side < 0 ? -18 : 18;
      if (opened && side > 0) clear(level, o, thresholdX, thresholdX, -18, -14, 1, 5);
      else for (int z = -18; z <= -14; z++) for (int y = 1; y <= 5; y++) set(level, at(o, thresholdX, y, z), locked);

      if (side < 0) {
         set(level, at(o, -29, 2, -16), (Block)ReivaxMCProgress.ORIGIN_MATRIX.get());
         set(level, at(o, -27, 3, -16), Blocks.IRON_BARS);
         set(level, at(o, -29, 5, -16), lumen);
      } else {
         set(level, at(o, 27, 2, -16), (Block)ReivaxMCProgress.REGISTRY_CONSOLE.get());
         set(level, at(o, 27, 5, -16), lumen);
         for (int x = 23; x <= 33; x += 2) {
            set(level, at(o, x, 1, -24), Blocks.CHISELED_BOOKSHELF);
            set(level, at(o, x, 2, -24), Blocks.BOOKSHELF);
         }
      }
   }

   private static void buildFutureGalleries(ServerLevel level, int[] o, Block stone, Block lumen) {
      room(level, o, -25, 25, -43, -29, 0, 8, stone, Blocks.POLISHED_DEEPSLATE, lumen);
      for (int x : new int[]{-19, -7, 7, 19}) {
         for (int y = 1; y <= 5; y++) for (int z = -29; z <= -28; z++)
            set(level, at(o, x, y, z), Blocks.REINFORCED_DEEPSLATE);
      }
   }

   public static void openRegistry(MinecraftServer server, int[] origin) {
      ServerLevel level = server.overworld();
      clear(level, origin, 18, 18, -18, -14, 1, 5);
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!campaign.isCompleted(REGISTRY_OPEN)) campaign.complete(REGISTRY_OPEN, 0, 0);
      BlockPos p = eastThreshold(server);
      level.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 2.0, p.getZ() + 0.5, 90, 1.2, 2.2, 2.2, 0.05);
      level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX() + 0.5, p.getY() + 2.0, p.getZ() + 0.5, 120, 1.5, 2.5, 2.5, 0.08);
      level.playSound(null, p, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.7F, 0.65F);
   }

   public static boolean isRegistryConsole(MinecraftServer server, BlockPos pos) {
      try {
         int[] o = F8SanctuaryEngine.target(server);
         return pos.closerThan(at(o, 27, 2, -16), 3.0);
      } catch (Throwable ignored) {
         return false;
      }
   }

   public static BlockPos eastThreshold(MinecraftServer server) {
      try {
         int[] o = F8SanctuaryEngine.target(server);
         return at(o, 18, 2, -16);
      } catch (Throwable ignored) {
         return BlockPos.ZERO;
      }
   }

   public static void pulseMatrix(MinecraftServer server) {
      try {
         int[] o = F8SanctuaryEngine.target(server);
         BlockPos p = at(o, -29, 2, -16);
         ServerLevel level = server.overworld();
         level.sendParticles(ParticleTypes.SONIC_BOOM, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 1, 0, 0, 0, 0);
         level.sendParticles(ParticleTypes.SCULK_SOUL, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 70, 1.2, 1.5, 1.2, 0.04);
         level.playSound(null, p, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 1.0F, 0.75F);
         for (ServerPlayer player : server.getPlayerList().getPlayers())
            player.displayClientMessage(Component.literal("§3MATRICE §8• §fUn symbole s'allume derrière la paroi occidentale. Puis plus rien."), false);
      } catch (Throwable ignored) {
      }
   }

   private static void room(ServerLevel level, int[] o, int minX, int maxX, int minZ, int maxZ,
                            int floorY, int ceilingY, Block wall, Block floor, Block lumen) {
      for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
         set(level, at(o, x, floorY, z), ((x + z) % 11 == 0) ? lumen : floor);
         for (int y = floorY + 1; y <= ceilingY; y++) {
            boolean shell = x == minX || x == maxX || z == minZ || z == maxZ || y == ceilingY;
            set(level, at(o, x, y, z), shell ? wall : Blocks.AIR);
         }
      }
   }

   private static void arch(ServerLevel level, int[] o, int minX, int maxX, int z, int height, Block stone, Block lumen) {
      for (int x = minX; x <= maxX; x++) {
         int rise = height - Math.max(0, Math.abs(x) - (maxX - minX) / 3) / 2;
         set(level, at(o, x, rise, z), (x % 7 == 0) ? lumen : stone);
      }
      for (int y = 1; y <= height; y++) {
         set(level, at(o, minX, y, z), stone);
         set(level, at(o, maxX, y, z), stone);
      }
   }

   private static void clear(ServerLevel level, int[] o, int minX, int maxX, int minZ, int maxZ, int minY, int maxY) {
      for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) for (int y = minY; y <= maxY; y++)
         set(level, at(o, x, y, z), Blocks.AIR);
   }

   private static BlockPos at(int[] o, int x, int y, int z) {
      return new BlockPos(o[0] + x, o[1] + y, o[2] + z);
   }

   private static void set(ServerLevel level, BlockPos pos, Block block) {
      level.setBlock(pos, block.defaultBlockState(), 3);
   }
}
