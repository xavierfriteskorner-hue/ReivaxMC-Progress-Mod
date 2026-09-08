package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.entity.ProtecteurEntity;
import fr.reivaxmc.progress.entity.VeilleurEntity;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Sanctuaire persistant 0.12.0 : entier dès la Trace, ouvert par concordances. */
public final class C110SanctuaryArchitecture {
   public static final String REFORGED = "F120_SANCTUARY_REFORGED";
   public static final String REGISTRY_OPEN = "F110_REGISTRY_OPEN";
   public static final String GALLERY_OPEN = "F120_GALLERY_OPEN";
   private static final int NO_DROPS = 18;

   private C110SanctuaryArchitecture() {}

   public static boolean isPresent(Object serverObject, int[] origin) {
      if (!(serverObject instanceof MinecraftServer server)) return false;
      return server.overworld().getBlockState(at(origin, 0, 14, -3)).is((Block)ReivaxMCProgress.SANCTUARY_LUMEN.get());
   }

   public static void build(Object serverObject, int[] origin) {
      if (!(serverObject instanceof MinecraftServer server)) return;
      ServerLevel level = server.overworld();
      CampaignSavedData campaign = CampaignSavedData.get(server);
      Block stone = (Block)ReivaxMCProgress.SANCTUARY_STONE.get();
      Block lumen = (Block)ReivaxMCProgress.SANCTUARY_LUMEN.get();
      clear(level, origin, -39, 39, -45, 40, 0, 19);
      clearSuspendedVegetation(level,origin);
      buildNave(level, origin, stone, lumen);
      buildEntrance(level, origin, stone, lumen, campaign);
      buildRegistryWing(level, origin, stone, lumen, campaign.isCompleted(REGISTRY_OPEN));
      buildMatrixWing(level, origin, stone, lumen);
      buildGallery(level, origin, stone, lumen, campaign.isCompleted(GALLERY_OPEN));
      buildBurialAndButtresses(level, origin, stone);
      placeFounderObjects(level, origin);
      ensureSilentPresences(level, origin);
      removeGenerationDrops(level, origin);
      set(level, at(origin, 0, 14, -3), lumen);
      if (!campaign.isCompleted(F94SanctuaryShell.BUILT)) campaign.complete(F94SanctuaryShell.BUILT, 0, 0);
      if (!campaign.isCompleted(REFORGED)) campaign.complete(REFORGED, 0, 0);
   }

   private static void buildNave(ServerLevel level, int[] o, Block stone, Block lumen) {
      for (int x = -17; x <= 17; x++) for (int z = -27; z <= 23; z++) {
         Block floor = ((Math.abs(x) == 5 || x == 0) && z % 4 == 0) ? lumen
            : ((x + z) % 9 == 0 ? Blocks.CHISELED_DEEPSLATE : Blocks.POLISHED_DEEPSLATE);
         set(level, at(o, x, 0, z), floor);
         int roofY = 9 + Math.max(0, 5 - Math.abs(x) / 3);
         set(level, at(o, x, roofY, z), (x == 0 && z % 8 == 0) ? lumen : stone);
      }
      for (int z = -27; z <= 23; z++) for (int y = 1; y <= 9; y++) {
         set(level, at(o, -17, y, z), y == 4 && z % 7 == 0 ? lumen : stone);
         set(level, at(o, 17, y, z), y == 4 && z % 7 == 0 ? lumen : stone);
      }
      for (int z = -24; z <= 20; z += 8) {
         pillar(level, o, -14, z, 8, stone, lumen);
         pillar(level, o, 14, z, 8, stone, lumen);
         beam(level, o, -14, 14, 8, z, stone, lumen);
      }
      for (int z : new int[]{-20, -12, 4, 12}) for (int x : new int[]{-11, 11}) {
         set(level, at(o, x, 1, z), Blocks.POLISHED_BLACKSTONE_STAIRS);
         set(level, at(o, x, 2, z), Blocks.CHAIN);
         set(level, at(o, x, 3, z), Blocks.SOUL_LANTERN);
      }
      frame(level, o, 0, 22, 4, 7, stone, lumen);
      frame(level, o, 0, 7, 3, 6, stone, lumen);
      frame(level, o, 0, -7, 4, 8, stone, lumen);
   }

   private static void buildEntrance(ServerLevel level, int[] o, Block stone, Block lumen,CampaignSavedData campaign) {
      for (int z = 24; z <= 38; z++) {
         int half = 20 - Math.max(0, z - 31) / 2;
         for (int x = -half; x <= half; x++) {
            set(level, at(o, x, 0, z), Math.abs(x) <= 3 ? Blocks.POLISHED_BLACKSTONE_BRICKS : Blocks.POLISHED_DEEPSLATE);
            int h = 5 + Math.max(0, 10 - Math.abs(x) / 2);
            if (Math.abs(x) > 4) for (int y = 1; y <= h; y++) set(level, at(o, x, y, z), stone);
         }
      }
      clear(level, o, -4, 4, 23, 39, 1, 8);
      frame(level, o, 0, 34, 5, 11, stone, lumen);
      for (int z = 35; z <= 42; z++) for (int x = -3; x <= 3; x++)
         set(level, at(o, x, 0, z), Math.abs(x) == 3 ? Blocks.CHISELED_DEEPSLATE : Blocks.POLISHED_BLACKSTONE_BRICKS);
      sealMonolith(level,o,-9,36,stone,lumen,campaign.isCompleted(F90Sanctuary.LEFT));
      sealMonolith(level,o,9,36,stone,lumen,campaign.isCompleted(F90Sanctuary.RIGHT));
   }

   private static void sealMonolith(ServerLevel level,int[] o,int x,int z,Block stone,Block lumen,boolean filled) {
      for (int dx=-2; dx<=2; dx++) for (int dz=-1; dz<=1; dz++) set(level, at(o,x+dx,0,z+dz), Blocks.POLISHED_BLACKSTONE);
      for (int y=1; y<=10; y++) {
         int half = y==1 || y==10 ? 2 : 1;
         for (int dx=-half;dx<=half;dx++) set(level, at(o,x+dx,y,z), Math.abs(dx)==half ? stone : Blocks.CHISELED_POLISHED_BLACKSTONE);
      }
      set(level,at(o,x,4,z+1),filled?(Block)ReivaxMCProgress.ORIGIN_MATRIX.get():(Block)ReivaxMCProgress.SEAL_RECEPTACLE.get());
      set(level, at(o,x-1,4,z+1), Blocks.GILDED_BLACKSTONE);
      set(level, at(o,x+1,4,z+1), Blocks.GILDED_BLACKSTONE);
      set(level, at(o,x,3,z+1), Blocks.GILDED_BLACKSTONE);
      set(level, at(o,x,5,z+1), Blocks.GILDED_BLACKSTONE);
      set(level, at(o,x,11,z), lumen);
   }

   private static void buildRegistryWing(ServerLevel level, int[] o, Block stone, Block lumen, boolean opened) {
      wingShell(level,o,18,36,-27,2,8,stone,lumen);
      if (opened) clear(level,o,17,20,-18,-14,1,5); else door(level,o,18,-16,Blocks.REINFORCED_DEEPSLATE);
      for (int x=22;x<=31;x++) set(level,at(o,x,0,-16),x%2==0?lumen:Blocks.POLISHED_DEEPSLATE);
      for (int x=25;x<=29;x++) for (int z=-18;z<=-14;z++) if (Math.abs(x-27)+Math.abs(z+16)<=3) set(level,at(o,x,1,z),Blocks.POLISHED_BLACKSTONE);
      set(level,at(o,27,2,-16),(Block)ReivaxMCProgress.REGISTRY_CONSOLE.get());
      set(level,at(o,27,3,-16),lumen);
      for (int x=22;x<=32;x+=2) {
         set(level,at(o,x,1,-24),Blocks.CHISELED_BOOKSHELF);
         set(level,at(o,x,2,-24),Blocks.BOOKSHELF);
         set(level,at(o,x,3,-24),Blocks.DARK_OAK_TRAPDOOR);
      }
   }

   private static void buildMatrixWing(ServerLevel level, int[] o, Block stone, Block lumen) {
      wingShell(level,o,-36,-18,-27,2,8,stone,lumen);
      door(level,o,-18,-16,Blocks.REINFORCED_DEEPSLATE);
      for (int x=-31;x<=-22;x++) set(level,at(o,x,0,-16),x%2==0?lumen:Blocks.POLISHED_DEEPSLATE);
      for (int x=-31;x<=-27;x++) for (int z=-18;z<=-14;z++) if (Math.abs(x+29)+Math.abs(z+16)<=3) set(level,at(o,x,1,z),Blocks.CHISELED_DEEPSLATE);
      set(level,at(o,-29,2,-16),(Block)ReivaxMCProgress.ORIGIN_MATRIX.get());
      set(level,at(o,-29,3,-16),lumen);
      for (int z=-20;z<=-12;z+=4) { pillar(level,o,-34,z,6,stone,lumen); pillar(level,o,-20,z,6,stone,lumen); }
   }

   private static void buildGallery(ServerLevel level, int[] o, Block stone, Block lumen, boolean opened) {
      wingShell(level,o,-25,25,-43,-28,7,stone,lumen);
      if (opened) clear(level,o,21,25,-29,-27,1,5); else doorZ(level,o,23,-28,Blocks.REINFORCED_DEEPSLATE);
      for (int x : new int[]{-8,8}) {
         for (int dx=-2;dx<=2;dx++) for (int dz=-2;dz<=2;dz++) if (Math.abs(dx)+Math.abs(dz)<=3) set(level,at(o,x+dx,0,-36+dz),Blocks.POLISHED_BLACKSTONE);
         set(level,at(o,x,1,-36),Blocks.LECTERN); set(level,at(o,x,2,-36),lumen);
      }
      for (int x : new int[]{-19,-7,7,19}) doorZ(level,o,x,-43,Blocks.REINFORCED_DEEPSLATE);
   }

   private static void wingShell(ServerLevel l,int[] o,int minX,int maxX,int minZ,int maxZ,int top,Block stone,Block lumen) {
      int center=(minX+maxX)/2;
      for (int x=minX;x<=maxX;x++) for (int z=minZ;z<=maxZ;z++) {
         set(l,at(o,x,0,z),(x+z)%13==0?lumen:Blocks.POLISHED_DEEPSLATE);
         int roof=Math.max(top-2,top-Math.abs(x-center)/6);
         for(int y=1;y<=top+2;y++)set(l,at(o,x,y,z),(x==minX||x==maxX||z==minZ||z==maxZ)&&y<=roof||y==roof?stone:Blocks.AIR);
         if(x>minX&&x<maxX&&z>minZ&&z<maxZ){set(l,at(o,x,roof+1,z),Blocks.DIRT);set(l,at(o,x,roof+2,z),Blocks.GRASS_BLOCK);}
      }
   }

   private static void buildBurialAndButtresses(ServerLevel l,int[] o,Block stone) {
      for (int side:new int[]{-1,1}) for (int z=-26;z<=22;z+=6) {
         int x=side*18;
         for (int dx=0;dx<=4;dx++) for (int y=0;y<=7-dx;y++) set(l,at(o,x+side*dx,y,z),y==7-dx?Blocks.MOSSY_COBBLESTONE:stone);
      }
      for (int side:new int[]{-1,1}) for (int x=19;x<=38;x++) for (int z=-26;z<=1;z++) {
         int wx=side<0?-x:x;
         if (Math.abs(wx)<=20&&z>=-19&&z<=-13) continue;
         int edge=Math.abs(wx-side*27), y=Math.max(1,7-edge/3-Math.abs(z+12)/10);
         set(l,at(o,wx,y+1,z),Blocks.DIRT); set(l,at(o,wx,y+2,z),Blocks.GRASS_BLOCK);
      }
   }

   private static void placeFounderObjects(ServerLevel l,int[] o) {
      set(l,at(o,-8,2,-18),(Block)ReivaxMCProgress.ORIGIN_RELIQUARY.get());
      set(l,at(o,8,2,-18),Blocks.LECTERN);
      set(l,at(o,0,2,29),(Block)ReivaxMCProgress.ORIGIN_MATRIX.get());
   }

   private static void ensureSilentPresences(ServerLevel l,int[] o) {
      AABB box=box(o,-38,0,-44,38,14,38);
      List<Entity> old=l.getEntities((Entity)null,box,e->e.getTags().contains("reivax_sanctuary_presence"));
      if(!old.isEmpty()) return;
      for(int i=0;i<2;i++) {
         VeilleurEntity v=(VeilleurEntity)ReivaxMCProgress.VEILLEUR.get().create(l); if(v==null) continue;
         int x=i==0?-13:13; v.moveTo(o[0]+x+.5,o[1]+1,o[2]-22.5,i==0?35:325,0);
         v.addTag("reivax_sanctuary_presence"); v.setNoAi(true); v.setInvulnerable(true); v.setSilent(true); v.setPersistenceRequired(); l.addFreshEntity(v);
      }
      ProtecteurEntity p=(ProtecteurEntity)ReivaxMCProgress.PROTECTEUR.get().create(l);
      if(p!=null) { p.moveTo(o[0]-32.5,o[1]+1,o[2]-22.5,90,0); p.addTag("reivax_sanctuary_presence"); p.setNoAi(true); p.setInvulnerable(true); p.setSilent(true); p.setPersistenceRequired(); l.addFreshEntity(p); }
   }

   private static void removeGenerationDrops(ServerLevel l,int[] o) {
      AABB box=box(o,-40,-2,-46,40,21,43);
      for(ItemEntity item:l.getEntitiesOfClass(ItemEntity.class,box)) if(item.tickCount<200) item.discard();
   }
   private static void clearSuspendedVegetation(ServerLevel l,int[] o){for(int x=-40;x<=40;x++)for(int z=-46;z<=42;z++)for(int y=20;y<=31;y++){BlockPos p=at(o,x,y,z);var state=l.getBlockState(p);if(state.is(BlockTags.LEAVES)||state.is(BlockTags.LOGS))set(l,p,Blocks.AIR);}}

   public static void openRegistry(MinecraftServer s,int[] o) {
      clear(s.overworld(),o,17,20,-18,-14,1,5); CampaignSavedData c=CampaignSavedData.get(s);
      if(!c.isCompleted(REGISTRY_OPEN)) c.complete(REGISTRY_OPEN,0,0); effectDoor(s,eastThreshold(s));
   }
   public static void openGallery(MinecraftServer s) {
      int[] o=origin(s); clear(s.overworld(),o,21,25,-29,-27,1,5); CampaignSavedData c=CampaignSavedData.get(s);
      if(!c.isCompleted(GALLERY_OPEN)) c.complete(GALLERY_OPEN,0,0); effectDoor(s,galleryThreshold(s));
   }
   private static void effectDoor(MinecraftServer s,BlockPos p) {
      ServerLevel l=s.overworld(); l.sendParticles(ParticleTypes.END_ROD,p.getX()+.5,p.getY()+2,p.getZ()+.5,90,1.2,2.2,2.2,.05);
      l.sendParticles(ParticleTypes.REVERSE_PORTAL,p.getX()+.5,p.getY()+2,p.getZ()+.5,120,1.5,2.5,2.5,.08);
      l.playSound(null,p,SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),SoundSource.BLOCKS,1.7F,.65F);
   }

   public static boolean isRegistryConsole(MinecraftServer s,BlockPos p) { try{return p.closerThan(registryConsole(s),3);}catch(Throwable ignored){return false;} }
   public static BlockPos registryConsole(MinecraftServer s){return at(origin(s),27,2,-16);}
   public static BlockPos eastThreshold(MinecraftServer s){try{return at(F8SanctuaryEngine.target(s),18,2,-16);}catch(Throwable ignored){return BlockPos.ZERO;}}
   public static BlockPos galleryThreshold(MinecraftServer s){try{return at(F8SanctuaryEngine.target(s),23,2,-28);}catch(Throwable ignored){return BlockPos.ZERO;}}
   public static BlockPos galleryLectern(MinecraftServer s,int side){return at(origin(s),side<0?-8:8,1,-36);}

   public static void pulseMatrix(MinecraftServer s) {
      try {
         int[] o=F8SanctuaryEngine.target(s); ServerLevel l=s.overworld(); BlockPos m=at(o,-29,2,-16),r=at(o,27,2,-16);
         for(int x=-28;x<=26;x+=2) l.sendParticles(ParticleTypes.ELECTRIC_SPARK,o[0]+x+.5,o[1]+2.6,o[2]-15.5,5,.2,.3,.2,.02);
         l.sendParticles(ParticleTypes.SONIC_BOOM,m.getX()+.5,m.getY()+1,m.getZ()+.5,1,0,0,0,0);
         l.sendParticles(ParticleTypes.SCULK_SOUL,r.getX()+.5,r.getY()+1,r.getZ()+.5,90,1.4,1.5,1.4,.04);
         for(ServerPlayer p:s.getPlayerList().getPlayers()) {
            p.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,p.getX(),p.getY()+1,p.getZ(),35,1,1,.8,.06);
            p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.SCULK_SHRIEKER_SHRIEK,SoundSource.AMBIENT,1.5F,.72F);
            p.displayClientMessage(Component.literal("§3MATRICE §8• §fUne ligne de lumière fend le Sanctuaire vers l'ouest. Quelque chose ouvre un œil — puis se tait."),false);
         }
      } catch(Throwable ignored) {}
   }

   private static void pillar(ServerLevel l,int[] o,int x,int z,int h,Block stone,Block lumen){for(int y=1;y<=h;y++)set(l,at(o,x,y,z),y==h/2?lumen:stone);for(int dx:new int[]{-1,1})set(l,at(o,x+dx,1,z),Blocks.CHISELED_DEEPSLATE);}
   private static void beam(ServerLevel l,int[] o,int a,int b,int y,int z,Block stone,Block lumen){for(int x=a;x<=b;x++)set(l,at(o,x,y,z),x==0?lumen:stone);}
   private static void frame(ServerLevel l,int[] o,int x,int z,int half,int h,Block stone,Block lumen){for(int y=1;y<=h;y++){set(l,at(o,x-half,y,z),stone);set(l,at(o,x+half,y,z),stone);}for(int dx=-half;dx<=half;dx++)set(l,at(o,x+dx,h,z),dx==0?lumen:stone);}
   private static void door(ServerLevel l,int[] o,int x,int z,Block b){for(int dx=-1;dx<=1;dx++)for(int dz=-2;dz<=2;dz++)for(int y=1;y<=5;y++)set(l,at(o,x+dx,y,z+dz),b);}
   private static void doorZ(ServerLevel l,int[] o,int x,int z,Block b){for(int dx=-2;dx<=2;dx++)for(int dz=-1;dz<=1;dz++)for(int y=1;y<=5;y++)set(l,at(o,x+dx,y,z+dz),b);}
   private static void clear(ServerLevel l,int[] o,int a,int b,int c,int d,int e,int f){for(int x=a;x<=b;x++)for(int z=c;z<=d;z++)for(int y=e;y<=f;y++)set(l,at(o,x,y,z),Blocks.AIR);}
   private static BlockPos at(int[] o,int x,int y,int z){return new BlockPos(o[0]+x,o[1]+y,o[2]+z);}
   private static int[] origin(MinecraftServer s){try{return F8SanctuaryEngine.target(s);}catch(Exception ignored){return new int[]{0,64,0};}}
   private static AABB box(int[] o,int ax,int ay,int az,int bx,int by,int bz){return new AABB(o[0]+ax,o[1]+ay,o[2]+az,o[0]+bx+1,o[1]+by+1,o[2]+bz+1);}
   private static void set(ServerLevel l,BlockPos p,Block b){l.setBlock(p,b.defaultBlockState(),NO_DROPS);}
}
