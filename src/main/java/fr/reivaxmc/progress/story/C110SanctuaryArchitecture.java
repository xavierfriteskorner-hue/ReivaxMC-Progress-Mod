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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/** Architecture canonique, persistante et décorée du Sanctuaire (0.12.2). */
public final class C110SanctuaryArchitecture {
   public static final String REFORGED = "F122_SANCTUARY_DECORATED";
   public static final String REGISTRY_OPEN = "F110_REGISTRY_OPEN";
   public static final String GALLERY_OPEN = "F120_GALLERY_OPEN";
   private static final int NO_DROPS = 18;

   private C110SanctuaryArchitecture() {}

   public static boolean isPresent(Object serverObject, int[] origin) {
      return serverObject instanceof MinecraftServer server
         && server.overworld().getBlockState(at(origin, 0, 20, -3)).is((Block)ReivaxMCProgress.SANCTUARY_LUMEN.get());
   }

   public static void build(Object serverObject, int[] origin) {
      if (!(serverObject instanceof MinecraftServer server)) return;
      ServerLevel level = server.overworld();
      CampaignSavedData campaign = CampaignSavedData.get(server);
      Block stone = (Block)ReivaxMCProgress.SANCTUARY_STONE.get();
      Block lumen = (Block)ReivaxMCProgress.SANCTUARY_LUMEN.get();
      // Retire les anciennes coques, mais conserve le niveau du sol afin de ne créer aucune fosse.
      clear(level, origin, -39, 39, -45, 40, 1, 26);
      landscape(level, origin, stone);
      nave(level, origin, stone, lumen, campaign);
      entrance(level, origin, stone, lumen, campaign);
      registry(level, origin, stone, lumen, campaign.isCompleted(REGISTRY_OPEN));
      matrix(level, origin, stone, lumen);
      gallery(level, origin, stone, lumen, campaign.isCompleted(GALLERY_OPEN));
      exterior(level, origin, stone, lumen);
      roofscape(level, origin, stone, lumen);
      interior(level, origin, stone, lumen);
      founderObjects(level, origin);
      foundationAltar(level, origin, stone, lumen);
      removeObsoletePresences(level, origin);
      removeDrops(level, origin);
      // Marqueur distinct : tout monde antérieur est redécoré exactement une fois.
      set(level, at(origin, 0, 20, -3), lumen);
      if (!campaign.isCompleted(F94SanctuaryShell.BUILT)) campaign.complete(F94SanctuaryShell.BUILT, 0, 0);
      if (!campaign.isCompleted(REFORGED)) campaign.complete(REFORGED, 0, 0);
   }

   private static void landscape(ServerLevel l, int[] o, Block stone) {
      for (int x=-37;x<=37;x++) for(int z=-44;z<=39;z++) {
         if (footprint(x,z)) {
            set(l,at(o,x,-1,z),Blocks.COBBLED_DEEPSLATE);
            set(l,at(o,x,0,z),((x*19+z*31)&15)==0?Blocks.CHISELED_DEEPSLATE:Blocks.POLISHED_DEEPSLATE);
         } else {
            set(l,at(o,x,-1,z),Blocks.DIRT);
            set(l,at(o,x,0,z),Blocks.GRASS_BLOCK);
         }
      }
      // Terrasses de raccord : elles suivent le relief réel aux quatre bords.
      for(int side:new int[]{-1,1}) for(int z=-40;z<=34;z++) {
         int outside=surface(l,o[0]+side*44,o[2]+z);
         for(int step=0;step<=6;step++) {
            int y=clamp(o[1]+Math.round((outside-o[1])*(step/6F)),o[1]-5,o[1]+7);
            support(l,at(o,side*(38+step),y-o[1],z),stone);
         }
      }
      for(int end:new int[]{-1,1}) for(int x=-34;x<=34;x++) {
         int z=end<0?-44:39, outside=surface(l,o[0]+x,o[2]+z+end*7);
         for(int step=0;step<=6;step++) {
            int y=clamp(o[1]+Math.round((outside-o[1])*(step/6F)),o[1]-5,o[1]+7);
            support(l,at(o,x,y-o[1],z+end*step),stone);
         }
      }
   }

   private static boolean footprint(int x,int z) {
      return Math.abs(x)<=18&&z>=-28&&z<=23 || Math.abs(x)<=26&&z>=-44&&z<=-29
         || Math.abs(x)>=18&&Math.abs(x)<=36&&z>=-28&&z<=3 || Math.abs(x)<=20&&z>=24&&z<=39;
   }

   private static void nave(ServerLevel l,int[] o,Block stone,Block lumen,CampaignSavedData c) {
      for(int z=-28;z<=23;z++) for(int x=-18;x<=18;x++) {
         set(l,at(o,x,0,z),floor(x,z,lumen));
         int roof=roof(x);
         if(Math.abs(x)==18) for(int y=1;y<=roof;y++) set(l,at(o,x,y,z),wall(y,z,stone,lumen));
         set(l,at(o,x,roof,z),x==0&&z%8==0?lumen:(((x+z)&7)==0?Blocks.DEEPSLATE_TILES:stone));
         set(l,at(o,x,roof+1,z),stone);
      }
      endWall(l,o,-28,stone,lumen); endWall(l,o,23,stone,lumen);
      for(int z=-24;z<=20;z+=8){pillar(l,o,-14,z,9,stone,lumen);pillar(l,o,14,z,9,stone,lumen);rib(l,o,z,lumen);}
      for(int z:new int[]{-23,-15,-2,12,19}) for(int x:new int[]{-11,11}) light(l,o,x,z);
      partition(l,o,22,4,7,stone,lumen,c.isCompleted(F8SanctuaryEngine.K_SEAL_INSERTED));
      partition(l,o,7,3,6,stone,lumen,c.isCompleted(F8SanctuaryEngine.K_WATCHERS[2])&&c.isCompleted(F8SanctuaryEngine.K_WATCHERS[3]));
      partition(l,o,-7,4,8,stone,lumen,c.isCompleted(F8SanctuaryEngine.K_FOUNDATION_GUARDS_AWAKENED));
      for(int x=-9;x<=9;x++) for(int z=-25;z<=-10;z++) {double d=Math.hypot(x,z+18);if(d>7.3&&d<9.1)set(l,at(o,x,0,z),lumen);}
      for(int x:new int[]{-8,8}) for(int dx=-2;dx<=2;dx++) for(int dz=-2;dz<=2;dz++)
         if(Math.abs(dx)+Math.abs(dz)<=3)set(l,at(o,x+dx,1,-18+dz),Blocks.POLISHED_BLACKSTONE);
   }

   private static void entrance(ServerLevel l,int[] o,Block stone,Block lumen,CampaignSavedData c) {
      for(int z=24;z<=39;z++) {
         int half=20-Math.max(0,z-30)/2;
         for(int x=-half;x<=half;x++) {
            set(l,at(o,x,0,z),Math.abs(x)<=3?Blocks.POLISHED_BLACKSTONE_BRICKS:Blocks.POLISHED_DEEPSLATE);
            if(Math.abs(x)>=half-1)for(int y=1;y<=Math.max(2,8-(z-24)/3);y++)set(l,at(o,x,y,z),stone);
         }
      }
      // Façade hiérarchisée : portique, tympan et deux contreforts.
      for(int x=-12;x<=12;x++)for(int y=1;y<=12-Math.abs(x)/3;y++)
         if(Math.abs(x)>4||y>7)set(l,at(o,x,y,23),((x+y)&6)==0?Blocks.DEEPSLATE_BRICKS:stone);else set(l,at(o,x,y,23),Blocks.AIR);
      for(int x=-6;x<=6;x++)set(l,at(o,x,9-Math.abs(x)/3,22),x==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);
      buttress(l,o,-16,24,11,stone,lumen);buttress(l,o,16,24,11,stone,lumen);
      for(int z=24;z<=44;z++)for(int x=-3;x<=3;x++)set(l,at(o,x,0,z),(z+x)%5==0?Blocks.CHISELED_DEEPSLATE:Blocks.POLISHED_BLACKSTONE_BRICKS);
      monolith(l,o,-9,36,stone,lumen,c.isCompleted(F90Sanctuary.LEFT));
      monolith(l,o,9,36,stone,lumen,c.isCompleted(F90Sanctuary.RIGHT));
   }

   private static void monolith(ServerLevel l,int[] o,int x,int z,Block stone,Block lumen,boolean filled) {
      for(int dx=-3;dx<=3;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)+Math.abs(dz)<=4)set(l,at(o,x+dx,0,z+dz),Blocks.POLISHED_BLACKSTONE);
      for(int y=1;y<=9;y++){int half=y<=2?2:(y==9?0:1);for(int dx=-half;dx<=half;dx++)set(l,at(o,x+dx,y,z),dx==0&&y%3==0?Blocks.CHISELED_POLISHED_BLACKSTONE:stone);}
      set(l,at(o,x,4,z+1),filled?(Block)ReivaxMCProgress.ORIGIN_MATRIX.get():(Block)ReivaxMCProgress.SEAL_RECEPTACLE.get());
      set(l,at(o,x-1,4,z+1),Blocks.GILDED_BLACKSTONE);set(l,at(o,x+1,4,z+1),Blocks.GILDED_BLACKSTONE);
      set(l,at(o,x,3,z+1),Blocks.GILDED_BLACKSTONE);set(l,at(o,x,5,z+1),Blocks.GILDED_BLACKSTONE);
      set(l,at(o,x,10,z),lumen);set(l,at(o,x,11,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
   }

   private static void registry(ServerLevel l,int[] o,Block stone,Block lumen,boolean opened) {
      wing(l,o,18,36,-28,3,stone,lumen);
      if(opened)clear(l,o,17,20,-18,-14,1,5);else sideDoor(l,o,18,-16);
      for(int x=21;x<=33;x++)set(l,at(o,x,0,-16),x%2==0?lumen:Blocks.POLISHED_DEEPSLATE);
      dais(l,o,27,-16,lumen);set(l,at(o,27,2,-16),(Block)ReivaxMCProgress.REGISTRY_CONSOLE.get());
      for(int x=22;x<=32;x+=2){set(l,at(o,x,1,-24),Blocks.CHISELED_BOOKSHELF);set(l,at(o,x,2,-24),Blocks.BOOKSHELF);set(l,at(o,x,3,-24),Blocks.DARK_OAK_TRAPDOOR);}
      for(int z:new int[]{-22,-10,-2})light(l,o,32,z);
   }

   private static void matrix(ServerLevel l,int[] o,Block stone,Block lumen) {
      wing(l,o,-36,-18,-28,3,stone,lumen);sideDoor(l,o,-18,-16);
      for(int x=-33;x<=-21;x++)set(l,at(o,x,0,-16),x%2==0?lumen:Blocks.POLISHED_DEEPSLATE);
      dais(l,o,-29,-16,lumen);set(l,at(o,-29,2,-16),(Block)ReivaxMCProgress.ORIGIN_MATRIX.get());
      for(int z=-24;z<=-8;z+=4){pillar(l,o,-34,z,6,stone,lumen);pillar(l,o,-20,z,6,stone,lumen);}
   }

   private static void gallery(ServerLevel l,int[] o,Block stone,Block lumen,boolean opened) {
      room(l,o,-26,26,-44,-29,9,stone,lumen);
      if(opened)clear(l,o,21,25,-30,-27,1,5);else rearDoor(l,o,23,-29);
      for(int x:new int[]{-8,8}){dais(l,o,x,-36,lumen);set(l,at(o,x,1,-36),Blocks.LECTERN);}
      for(int x:new int[]{-19,-7,7,19})rearDoor(l,o,x,-44);
      for(int x=-22;x<=22;x+=11)light(l,o,x,-40);
   }

   private static void exterior(ServerLevel l,int[] o,Block stone,Block lumen) {
      for(int side:new int[]{-1,1})for(int z=-26;z<=20;z+=6)buttress(l,o,side*19,z,9,stone,lumen);
      for(int side:new int[]{-1,1})for(int z=-25;z<=0;z+=8)buttress(l,o,side*37,z,7,stone,lumen);
      for(int x=-24;x<=24;x+=8)buttress(l,o,x,-45,8,stone,lumen);
      // Deux jardins encaissés cassent la silhouette sans condamner une future porte.
      for(int side:new int[]{-1,1}){int bx=side*27;for(int x=-5;x<=5;x++)for(int z=7;z<=18;z++){int wx=bx+x;set(l,at(o,wx,0,z),((wx+z)&7)==0?Blocks.MOSSY_COBBLESTONE:Blocks.MOSS_BLOCK);if((x*11+z*5)%17==0)set(l,at(o,wx,1,z),Blocks.AZALEA);}for(int z=10;z<=15;z++)set(l,at(o,bx,0,z),Blocks.WATER);}
   }

   /**
    * Couronnement visible à grande distance : nervure de la nef, tour-lanterne,
    * parapets et petits pylônes. Les volumes restent sous y+24 pour conserver
    * l'emprise protégée et ne touchent à aucune porte narrative.
    */
   private static void roofscape(ServerLevel l,int[] o,Block stone,Block lumen) {
      // Une épine dorsale brisée évite l'effet de grande dalle plate sur la nef.
      for(int z=-27;z<=22;z++) {
         int y=18;
         set(l,at(o,0,y,z),z%7==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
         if(z%8==0) {
            set(l,at(o,-1,y,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
            set(l,at(o,1,y,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
         }
      }

      // Tour-lanterne centrale : un signal ancien, plus fin que la masse du bâtiment.
      for(int y=17;y<=21;y++) for(int x=-3;x<=3;x++) for(int z=-7;z<=1;z++) {
         boolean edge=Math.abs(x)==3||z==-7||z==1;
         if(edge) {
            boolean window=(y==19)&&(x==0||z==-3);
            set(l,at(o,x,y,z),window?lumen:(((x+z+y)&5)==0?Blocks.DEEPSLATE_BRICKS:stone));
         }
      }
      for(int x=-4;x<=4;x++)for(int z=-8;z<=2;z++)if(Math.abs(x)==4||z==-8||z==2)
         set(l,at(o,x,22,z),Blocks.POLISHED_BLACKSTONE_BRICKS);
      for(int x=-2;x<=2;x++)for(int z=-6;z<=0;z++)
         set(l,at(o,x,22,z),(Math.abs(x)==2||z==-6||z==0)?Blocks.POLISHED_BLACKSTONE:lumen);
      set(l,at(o,0,23,-3),Blocks.LIGHTNING_ROD);

      // Les deux ailes gagnent un contour, des nervures et quatre pylônes chacune.
      decorateWingRoof(l,o,18,36,-28,3,stone,lumen);
      decorateWingRoof(l,o,-36,-18,-28,3,stone,lumen);

      // Galerie arrière : trois arches basses scandent le toit au lieu d'une plaque unique.
      for(int z=-43;z<=-30;z++) for(int x=-25;x<=25;x++) {
         if(Math.abs(x)==25 || (Math.abs(x)%9==0 && z%3==0))
            set(l,at(o,x,11,z),Math.abs(x)%9==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      }
      for(int x:new int[]{-18,0,18}) roofPylon(l,o,x,-36,11,stone,lumen);
   }

   private static void decorateWingRoof(ServerLevel l,int[]o,int minX,int maxX,int minZ,int maxZ,Block stone,Block lumen) {
      for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++) {
         boolean rim=x==minX||x==maxX||z==minZ||z==maxZ;
         if(rim)set(l,at(o,x,13,z),((x+z)&7)==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
      }
      int center=(minX+maxX)/2;
      for(int z=minZ+3;z<=maxZ-3;z++)if((z&1)==0)
         set(l,at(o,center,13,z),z%6==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);
      for(int z:new int[]{minZ+3,maxZ-3}) {
         roofPylon(l,o,minX+3,z,13,stone,lumen);
         roofPylon(l,o,maxX-3,z,13,stone,lumen);
      }
   }

   private static void roofPylon(ServerLevel l,int[]o,int x,int z,int base,Block stone,Block lumen) {
      for(int y=base;y<=base+3;y++)set(l,at(o,x,y,z),y==base+2?lumen:stone);
      set(l,at(o,x,base+4,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
   }

   /** Décoration intérieure par espaces, sans bloquer les axes de combat. */
   private static void interior(ServerLevel l,int[]o,Block stone,Block lumen) {
      decorateEntranceHall(l,o,stone,lumen);
      decorateCrossing(l,o,stone,lumen);
      decorateFoundationChamber(l,o,stone,lumen);
      decorateRegistry(l,o,stone,lumen);
      decorateMatrix(l,o,stone,lumen);
      decorateGallery(l,o,stone,lumen);
   }

   private static void decorateEntranceHall(ServerLevel l,int[]o,Block stone,Block lumen) {
      // Alcôves latérales et bancs bas : le milieu reste entièrement libre pour les Veilleurs.
      for(int side:new int[]{-1,1})for(int z:new int[]{10,16,20}) {
         int x=side*17;
         for(int y=2;y<=6;y++)set(l,at(o,x,y,z),y==4?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);
         set(l,at(o,side*15,1,z),Blocks.CHISELED_DEEPSLATE);
         set(l,at(o,side*14,1,z),Blocks.POLISHED_BLACKSTONE_SLAB);
      }
      brazier(l,o,-11,18);brazier(l,o,11,18);
      floorMedallion(l,o,0,14,5,lumen);
   }

   private static void decorateCrossing(ServerLevel l,int[]o,Block stone,Block lumen) {
      floorMedallion(l,o,0,0,6,lumen);
      for(int side:new int[]{-1,1}) {
         shrine(l,o,side*17,1,stone,lumen);
         shrine(l,o,side*17,-5,stone,lumen);
      }
      for(int x:new int[]{-10,10})for(int z:new int[]{-4,4}) {
         set(l,at(o,x,1,z),Blocks.POLISHED_BLACKSTONE);
         set(l,at(o,x,2,z),Blocks.SOUL_LANTERN);
      }
   }

   private static void decorateFoundationChamber(ServerLevel l,int[]o,Block stone,Block lumen) {
      // Fresques abstraites de part et d'autre de l'autel.
      for(int side:new int[]{-1,1})for(int z=-25;z<=-11;z+=3) {
         int x=side*17;
         set(l,at(o,x,3,z),Blocks.GILDED_BLACKSTONE);
         set(l,at(o,x,4,z),z%2==0?lumen:Blocks.CHISELED_POLISHED_BLACKSTONE);
         set(l,at(o,x,5,z),Blocks.CRYING_OBSIDIAN);
      }
      brazier(l,o,-11,-23);brazier(l,o,11,-23);
      for(int side:new int[]{-1,1})for(int z=-24;z<=-12;z+=6) {
         set(l,at(o,side*12,1,z),Blocks.POLISHED_BLACKSTONE_SLAB);
         set(l,at(o,side*13,1,z),Blocks.POLISHED_BLACKSTONE_SLAB);
      }
   }

   private static void decorateRegistry(ServerLevel l,int[]o,Block stone,Block lumen) {
      // Rayonnages contre le mur extérieur, comptoirs de consultation et balises d'archive.
      for(int z=-25;z<=1;z+=4)for(int y=1;y<=4;y++) {
         set(l,at(o,35,y,z),y==4?Blocks.DARK_OAK_TRAPDOOR:(y%2==0?Blocks.CHISELED_BOOKSHELF:Blocks.BOOKSHELF));
      }
      for(int z:new int[]{-22,-10,-2}) {
         for(int x=22;x<=25;x++)set(l,at(o,x,1,z),Blocks.POLISHED_BLACKSTONE_SLAB);
         set(l,at(o,24,2,z),Blocks.SOUL_LANTERN);
      }
      for(int z:new int[]{-24,-8,-2})archiveSigil(l,o,19,z,stone,lumen);
   }

   private static void decorateMatrix(ServerLevel l,int[]o,Block stone,Block lumen) {
      floorMedallion(l,o,-29,-16,6,lumen);
      for(int x:new int[]{-34,-24})for(int z:new int[]{-24,-8}) {
         for(int y=1;y<=3;y++)set(l,at(o,x,y,z),y==2?Blocks.CRYING_OBSIDIAN:Blocks.POLISHED_BLACKSTONE);
         set(l,at(o,x,4,z),Blocks.END_ROD);
      }
      for(int z:new int[]{-24,-8,-2})archiveSigil(l,o,-19,z,stone,lumen);
      for(int z=-22;z<=-10;z+=4) {
         set(l,at(o,-22,1,z),Blocks.SCULK);
         set(l,at(o,-36,1,z),Blocks.SCULK_CATALYST);
      }
   }

   private static void decorateGallery(ServerLevel l,int[]o,Block stone,Block lumen) {
      for(int x:new int[]{-22,-14,0,14,22})for(int z:new int[]{-40,-34}) {
         if((Math.abs(x)==14&&z==-40)||(x==0&&z==-34))continue;
         set(l,at(o,x,1,z),Blocks.CHISELED_DEEPSLATE);
         set(l,at(o,x,2,z),Blocks.POLISHED_BLACKSTONE);
         set(l,at(o,x,3,z),((x+z)&1)==0?Blocks.GILDED_BLACKSTONE:Blocks.CRYING_OBSIDIAN);
      }
      for(int x=-24;x<=24;x+=4)set(l,at(o,x,0,-32),x%8==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);
      for(int side:new int[]{-1,1})for(int z=-42;z<=-31;z+=5)shrine(l,o,side*25,z,stone,lumen);
   }

   /** Socle à trois degrés : la Borne placée à y+4 n'est plus suspendue. */
   private static void foundationAltar(ServerLevel l,int[]o,Block stone,Block lumen) {
      for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++)if(Math.abs(dx)+Math.abs(dz)<=7) {
         // Encoche frontale : le Protecteur et le joueur disposent d'une marche d'approche.
         if(!(dz>=3&&Math.abs(dx)<=1))set(l,at(o,dx,1,-18+dz),(Math.abs(dx)+Math.abs(dz)==7)?lumen:Blocks.POLISHED_BLACKSTONE);
      }
      for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)if(Math.abs(dx)+Math.abs(dz)<=4) {
         if(!(dz>=2&&Math.abs(dx)<=1))set(l,at(o,dx,2,-18+dz),(dx==0||dz==0)?Blocks.GILDED_BLACKSTONE:Blocks.POLISHED_BLACKSTONE_BRICKS);
      }
      for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)
         set(l,at(o,dx,3,-18+dz),(dx==0&&dz==0)?Blocks.CHISELED_POLISHED_BLACKSTONE:lumen);
   }

   private static void brazier(ServerLevel l,int[]o,int x,int z) {
      set(l,at(o,x,1,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
      set(l,at(o,x,2,z),Blocks.SOUL_SOIL);
      set(l,at(o,x,3,z),Blocks.SOUL_FIRE);
   }

   private static void shrine(ServerLevel l,int[]o,int x,int z,Block stone,Block lumen) {
      boolean side=Math.abs(x)>18;
      for(int y=2;y<=6;y++) {
         int px=side?x:x;
         set(l,at(o,px,y,z),y==4?lumen:(y==3||y==5?Blocks.GILDED_BLACKSTONE:stone));
      }
   }

   private static void archiveSigil(ServerLevel l,int[]o,int x,int z,Block stone,Block lumen) {
      set(l,at(o,x,2,z),stone);set(l,at(o,x,3,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
      set(l,at(o,x,4,z),lumen);set(l,at(o,x,5,z),Blocks.CHISELED_POLISHED_BLACKSTONE);
   }

   private static void floorMedallion(ServerLevel l,int[]o,int cx,int cz,int radius,Block lumen) {
      for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
         double d=Math.hypot(x,z);
         if(d>=radius-.7&&d<=radius+.2)set(l,at(o,cx+x,0,cz+z),lumen);
         else if(d<1.2)set(l,at(o,cx+x,0,cz+z),Blocks.CHISELED_POLISHED_BLACKSTONE);
      }
   }

   private static void wing(ServerLevel l,int[] o,int minX,int maxX,int minZ,int maxZ,Block stone,Block lumen){
      room(l,o,minX,maxX,minZ,maxZ,8,stone,lumen);
      int center=(minX+maxX)/2;
      for(int layer=0;layer<4;layer++){int a=minX+layer*(center<0?1:0),b=maxX-layer*(center>0?1:0),y=9+layer;for(int x=a;x<=b;x++)for(int z=minZ+layer;z<=maxZ-layer;z++)if(x==a||x==b||z==minZ+layer||z==maxZ-layer)set(l,at(o,x,y,z),layer==2&&z%7==0?lumen:stone);}
   }

   private static void room(ServerLevel l,int[] o,int minX,int maxX,int minZ,int maxZ,int top,Block stone,Block lumen){
      for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){
         set(l,at(o,x,0,z),floor(x,z,lumen));boolean edge=x==minX||x==maxX||z==minZ||z==maxZ;
         if(edge)for(int y=1;y<=top;y++)set(l,at(o,x,y,z),wall(y,x+z,stone,lumen));
         set(l,at(o,x,top,z),((x*7+z*3)&31)==0?lumen:stone);set(l,at(o,x,top+1,z),Blocks.DEEPSLATE_TILES);
      }
   }

   private static void endWall(ServerLevel l,int[] o,int z,Block stone,Block lumen){for(int x=-18;x<=18;x++)for(int y=1;y<=roof(x);y++)set(l,at(o,x,y,z),wall(y,x+z,stone,lumen));}
   private static void partition(ServerLevel l,int[] o,int z,int half,int height,Block stone,Block lumen,boolean open){for(int x=-17;x<=17;x++)for(int y=1;y<=9;y++){if(Math.abs(x)<=half&&y<=height)set(l,at(o,x,y,z),open?Blocks.AIR:Blocks.REINFORCED_DEEPSLATE);else set(l,at(o,x,y,z),Math.abs(x)==half+1&&y%3==0?lumen:stone);}for(int x=-half-1;x<=half+1;x++)set(l,at(o,x,height+1,z),x==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);}
   private static void sideDoor(ServerLevel l,int[] o,int x,int z){for(int dx=-1;dx<=1;dx++)for(int dz=-2;dz<=2;dz++)for(int y=1;y<=5;y++)set(l,at(o,x+dx,y,z+dz),Blocks.REINFORCED_DEEPSLATE);}
   private static void rearDoor(ServerLevel l,int[] o,int x,int z){for(int dx=-2;dx<=2;dx++)for(int dz=-1;dz<=1;dz++)for(int y=1;y<=5;y++)set(l,at(o,x+dx,y,z+dz),Blocks.REINFORCED_DEEPSLATE);}
   private static void buttress(ServerLevel l,int[] o,int x,int z,int h,Block stone,Block lumen){for(int y=1;y<=h;y++){int r=y<3?2:(y<h-1?1:0);for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++)if(Math.abs(dx)+Math.abs(dz)<=r+1)set(l,at(o,x+dx,y,z+dz),y==h-2&&dx==0&&dz==0?lumen:stone);}set(l,at(o,x,h+1,z),Blocks.CHISELED_POLISHED_BLACKSTONE);}
   private static void pillar(ServerLevel l,int[] o,int x,int z,int h,Block stone,Block lumen){for(int y=1;y<=h;y++)set(l,at(o,x,y,z),y==h/2?lumen:stone);for(int dx:new int[]{-1,1})set(l,at(o,x+dx,1,z),Blocks.CHISELED_DEEPSLATE);}
   private static void rib(ServerLevel l,int[] o,int z,Block lumen){for(int x=-17;x<=17;x++)set(l,at(o,x,roof(x)-1,z),x==0?lumen:Blocks.POLISHED_BLACKSTONE_BRICKS);}
   private static void light(ServerLevel l,int[] o,int x,int z){set(l,at(o,x,7,z),Blocks.CHAIN);set(l,at(o,x,6,z),Blocks.CHAIN);set(l,at(o,x,5,z),Blocks.SOUL_LANTERN);}
   private static void dais(ServerLevel l,int[] o,int x,int z,Block lumen){for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)+Math.abs(dz)<=3)set(l,at(o,x+dx,1,z),(dx==0||dz==0)?lumen:Blocks.POLISHED_BLACKSTONE);}
   private static void founderObjects(ServerLevel l,int[] o){set(l,at(o,-8,2,-18),(Block)ReivaxMCProgress.ORIGIN_RELIQUARY.get());set(l,at(o,8,2,-18),Blocks.LECTERN);}

   private static void removeObsoletePresences(ServerLevel l,int[] o){AABB b=box(o,-40,-2,-46,40,22,44);for(Entity e:l.getEntities((Entity)null,b,q->q.getTags().contains("reivax_sanctuary_presence")))e.discard();}
   private static void removeDrops(ServerLevel l,int[] o){AABB b=box(o,-46,-8,-52,46,25,48);for(ItemEntity e:l.getEntitiesOfClass(ItemEntity.class,b))if(e.tickCount<240)e.discard();}

   public static void openRegistry(MinecraftServer s,int[] o){clear(s.overworld(),o,17,20,-18,-14,1,5);CampaignSavedData c=CampaignSavedData.get(s);if(!c.isCompleted(REGISTRY_OPEN))c.complete(REGISTRY_OPEN,0,0);effectDoor(s,eastThreshold(s));}
   public static void openGallery(MinecraftServer s){int[] o=origin(s);clear(s.overworld(),o,21,25,-30,-27,1,5);CampaignSavedData c=CampaignSavedData.get(s);if(!c.isCompleted(GALLERY_OPEN))c.complete(GALLERY_OPEN,0,0);effectDoor(s,galleryThreshold(s));}
   private static void effectDoor(MinecraftServer s,BlockPos p){ServerLevel l=s.overworld();l.sendParticles(ParticleTypes.END_ROD,p.getX()+.5,p.getY()+2,p.getZ()+.5,90,1.2,2.2,2.2,.05);l.sendParticles(ParticleTypes.REVERSE_PORTAL,p.getX()+.5,p.getY()+2,p.getZ()+.5,120,1.5,2.5,2.5,.08);l.playSound(null,p,SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),SoundSource.BLOCKS,1.7F,.65F);}
   public static boolean isRegistryConsole(MinecraftServer s,BlockPos p){try{return p.closerThan(registryConsole(s),3);}catch(Throwable ignored){return false;}}
   public static BlockPos registryConsole(MinecraftServer s){return at(origin(s),27,2,-16);}
   public static BlockPos eastThreshold(MinecraftServer s){try{return at(F8SanctuaryEngine.target(s),18,2,-16);}catch(Throwable ignored){return BlockPos.ZERO;}}
   public static BlockPos galleryThreshold(MinecraftServer s){try{return at(F8SanctuaryEngine.target(s),23,2,-29);}catch(Throwable ignored){return BlockPos.ZERO;}}
   public static BlockPos galleryLectern(MinecraftServer s,int side){return at(origin(s),side<0?-8:8,1,-36);}

   public static void pulseMatrix(MinecraftServer s){try{int[]o=F8SanctuaryEngine.target(s);ServerLevel l=s.overworld();BlockPos m=at(o,-29,2,-16),r=at(o,27,2,-16);for(int x=-28;x<=26;x+=2)l.sendParticles(ParticleTypes.ELECTRIC_SPARK,o[0]+x+.5,o[1]+2.6,o[2]-15.5,5,.2,.3,.2,.02);l.sendParticles(ParticleTypes.SONIC_BOOM,m.getX()+.5,m.getY()+1,m.getZ()+.5,1,0,0,0,0);l.sendParticles(ParticleTypes.SCULK_SOUL,r.getX()+.5,r.getY()+1,r.getZ()+.5,90,1.4,1.5,1.4,.04);for(ServerPlayer p:s.getPlayerList().getPlayers()){p.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,p.getX(),p.getY()+1,p.getZ(),35,1,1,.8,.06);p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.SCULK_SHRIEKER_SHRIEK,SoundSource.AMBIENT,1.5F,.72F);p.displayClientMessage(Component.literal("§3MATRICE §8• §fUne ligne de lumière fend le Sanctuaire vers l'ouest. Quelque chose ouvre un œil — puis se tait."),false);}}catch(Throwable ignored){}}

   private static Block floor(int x,int z,Block lumen){if((x==0||Math.abs(x)==5)&&z%4==0)return lumen;return((x*13+z*7)&15)==0?Blocks.CHISELED_DEEPSLATE:Blocks.POLISHED_DEEPSLATE;}
   private static Block wall(int y,int seed,Block stone,Block lumen){if(y==4&&Math.floorMod(seed,9)==0)return lumen;if((y==1||y==8)&&Math.floorMod(seed,5)==0)return Blocks.DEEPSLATE_BRICKS;return stone;}
   private static int roof(int x){return 10+Math.max(0,6-Math.abs(x)/3);}
   private static int surface(ServerLevel l,int x,int z){return l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z)-1;}
   private static int clamp(int n,int min,int max){return Math.max(min,Math.min(max,n));}
   private static void support(ServerLevel l,BlockPos top,Block stone){int ground=surface(l,top.getX(),top.getZ()),bottom=Math.max(l.getMinBuildHeight()+1,Math.min(ground,top.getY())-6);for(int y=bottom;y<top.getY();y++)set(l,new BlockPos(top.getX(),y,top.getZ()),stone);set(l,top,((top.getX()*17+top.getZ()*29)&3)==0?Blocks.MOSSY_COBBLESTONE:Blocks.GRASS_BLOCK);for(int y=top.getY()+1;y<=top.getY()+5;y++)set(l,new BlockPos(top.getX(),y,top.getZ()),Blocks.AIR);}
   private static void clear(ServerLevel l,int[]o,int a,int b,int c,int d,int e,int f){for(int x=a;x<=b;x++)for(int z=c;z<=d;z++)for(int y=e;y<=f;y++)set(l,at(o,x,y,z),Blocks.AIR);}
   private static BlockPos at(int[]o,int x,int y,int z){return new BlockPos(o[0]+x,o[1]+y,o[2]+z);}
   private static int[] origin(MinecraftServer s){try{return F8SanctuaryEngine.target(s);}catch(Exception ignored){return new int[]{0,64,0};}}
   private static AABB box(int[]o,int ax,int ay,int az,int bx,int by,int bz){return new AABB(o[0]+ax,o[1]+ay,o[2]+az,o[0]+bx+1,o[1]+by+1,o[2]+bz+1);}
   private static void set(ServerLevel l,BlockPos p,Block b){l.setBlock(p,b.defaultBlockState(),NO_DROPS);}
}
