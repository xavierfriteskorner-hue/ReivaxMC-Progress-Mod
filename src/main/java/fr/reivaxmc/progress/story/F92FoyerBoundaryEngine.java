package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

/** Frontière diégétique du Foyer : un arc de Résonance, jamais une ligne permanente. */
public final class F92FoyerBoundaryEngine {
   private static final int TICK_INTERVAL = 8;
   private static final Map<UUID, Boolean> LAST_INSIDE = new ConcurrentHashMap<>();
   private static final Map<UUID, Long> REVEAL_UNTIL = new ConcurrentHashMap<>();

   private F92FoyerBoundaryEngine() {
   }

   public static void onPlayerTick(Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % TICK_INTERVAL != 0) return;
      MinecraftServer server = player.getServer();
      if (server == null) return;
      CampaignSavedData campaign = CampaignSavedData.get(server);
      if (!campaign.foundationPlaced() || !player.serverLevel().dimension().location().toString().equals(campaign.foundationDimension())) {
         LAST_INSIDE.remove(player.getUUID());
         return;
      }

      BlockPos home = campaign.foundationPos();
      double dx = player.getX() - (home.getX() + 0.5);
      double dz = player.getZ() - (home.getZ() + 0.5);
      double distance = Math.sqrt(dx * dx + dz * dz);
      int radius = campaign.territoryRadius();
      boolean inside = distance <= radius;
      Boolean previous = LAST_INSIDE.put(player.getUUID(), inside);
      if (previous != null && previous != inside) crossing(player, inside);

      long now = player.serverLevel().getGameTime();
      boolean fullReveal = REVEAL_UNTIL.getOrDefault(player.getUUID(), 0L) >= now;
      double perception = campaign.hasCivilizationUpgrade("LISIERE_ACCORDEE") ? 24.0 : 12.0;
      if (fullReveal) drawCircle(player.serverLevel(), home, radius, 48);
      else if (Math.abs(distance - radius) <= perception) drawArc(player.serverLevel(), home, radius, Math.atan2(dz, dx));
      else REVEAL_UNTIL.remove(player.getUUID());
   }

   /** Appelé à l'ouverture de la Borne : le contour entier devient brièvement lisible. */
   public static void reveal(Object candidate) {
      if (!(candidate instanceof ServerPlayer player)) return;
      CampaignSavedData campaign = CampaignSavedData.get(player.getServer());
      long duration = campaign.hasCivilizationUpgrade("LISIERE_ACCORDEE") ? 400L : 200L;
      REVEAL_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + duration);
      player.displayClientMessage(Component.literal("§3RÉSONANCE §8• §fLe contour du Foyer devient visible pendant quelques instants."), false);
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 0.75F);
   }

   private static void crossing(ServerPlayer player, boolean entering) {
      String message = entering ? "§3RÉSONANCE §8• §fVous rentrez dans le Foyer." : "§3RÉSONANCE §8• §fVous quittez le Foyer.";
      player.displayClientMessage(Component.literal(message), true);
      F7NarrativeEngine.pushUi(player, "F8_GUIDANCE", entering ? "RÉSONANCE · FOYER RETROUVÉ" : "RÉSONANCE · HORS DU FOYER");
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.PLAYERS, 0.55F, entering ? 1.25F : 0.72F);
      player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.7F, entering ? 1.45F : 0.65F);
      player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.85, 1.15, 0.85, 0.045);
      player.serverLevel().sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.8, player.getZ(), 16, 0.72, 1.0, 0.72, 0.018);
      player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.65, 0.9, 0.65, 0.035);
   }

   private static void drawArc(ServerLevel level, BlockPos home, int radius, double centerAngle) {
      for (int i = -9; i <= 9; i++) point(level, home, radius, centerAngle + i * 0.036, i + 9, false);
   }

   private static void drawCircle(ServerLevel level, BlockPos home, int radius, int points) {
      double rotation = (level.getGameTime() % 240L) / 240.0 * Math.PI * 2.0;
      for (int i = 0; i < points; i++) point(level, home, radius, rotation + Math.PI * 2.0 * i / points, i, true);
   }

   private static void point(ServerLevel level, BlockPos home, int radius, double angle, int index, boolean fullReveal) {
      long time = level.getGameTime();
      boolean fracture = Math.floorMod(index + (int)(time / 8L), 7) == 0;
      double pulse = Math.sin(time * 0.12 + index * 1.7) * 0.12;
      double displacedRadius = radius + pulse + (fracture ? 0.38 : 0.0);
      double x = home.getX() + 0.5 + Math.cos(angle) * displacedRadius;
      double z = home.getZ() + 0.5 + Math.sin(angle) * displacedRadius;
      int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(x), (int)Math.floor(z));
      level.sendParticles(ParticleTypes.END_ROD, x, y + 0.28, z, 1, 0.06, 0.18, 0.06, 0.004);
      level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.95, z, fracture ? 3 : 1, 0.12, 0.46, 0.12, 0.018);
      if (fracture) {
         double sideways = index % 2 == 0 ? 0.24 : -0.24;
         level.sendParticles(ParticleTypes.REVERSE_PORTAL, x + Math.sin(angle) * sideways, y + 1.58, z - Math.cos(angle) * sideways, 3, 0.08, 0.34, 0.08, 0.022);
         level.sendParticles(ParticleTypes.END_ROD, x, y + 2.12, z, fullReveal ? 2 : 1, 0.08, 0.2, 0.08, 0.006);
      }
   }
}
