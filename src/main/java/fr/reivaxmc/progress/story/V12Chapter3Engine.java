package fr.reivaxmc.progress.story;

import fr.reivaxmc.progress.ReivaxMCProgress;
import fr.reivaxmc.progress.entity.VeilleurEntity;
import fr.reivaxmc.progress.network.ProgressNetworking;
import fr.reivaxmc.progress.network.V12Payloads;
import fr.reivaxmc.progress.progression.CampaignSavedData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;

/** Chapitre III — Les Noms retirés. Piste partagée, perceptions personnelles. */
public final class V12Chapter3Engine {
   private static final String REWARD="CH3_NAMES_COMPLETE", OPTIONAL="CH3_HOUSE_OPTIONAL", PROCESSION_TAG="reivax_ch3_procession";
   private V12Chapter3Engine(){}

   public static void onLogin(PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){offerIfReady(p);restore(p);}}
   public static void onPlayerTick(Post e){
      if(!(e.getEntity() instanceof ServerPlayer p)||p.tickCount%10!=0)return; MinecraftServer s=p.getServer();if(s==null)return;
      offerIfReady(p);applyCivilizationBenefits(p);V12TrailData d=V12TrailData.get(s);V12TrailData.Snapshot q=d.snapshot();if(q.stage().equals(V12TrailRules.LOCKED)||q.stage().equals(V12TrailRules.OFFERED)||q.stage().equals(V12TrailRules.COMPLETE))return;
      ensureSites(p.serverLevel(),CampaignSavedData.get(s),d);
      if(q.stage().equals(V12TrailRules.PROCESSION_LOCATE)&&p.blockPosition().closerThan(q.procession(),12)){if(d.beginProcession(p.serverLevel().getGameTime())){spawnProcession(p.serverLevel(),q.procession());scene(s,"LA PROCESSION","Six silhouettes marchent vers une arche qui n'ouvre sur rien. Aucune ne porte de visage.");}}
      q=d.snapshot();
      if(q.stage().equals(V12TrailRules.PROCESSION_OBSERVE))tickProcession(p,d,q);
      if(q.stage().equals(V12TrailRules.HOUSE_LOCATE)&&p.blockPosition().closerThan(q.house(),12)){if(d.enterHouse(p.serverLevel().getGameTime()))scene(s,"LA MAISON NUMÉROTÉE","Quelqu'un a retiré le nom de cette maison. Il n'a pas réussi à retirer la manière dont on y vivait.");}
      q=d.snapshot();
      if(q.stage().equals(V12TrailRules.RETURN_REGISTRY)&&p.blockPosition().closerThan(C110SanctuaryArchitecture.registryConsole(s),7))p.displayClientMessage(Component.literal("§6REGISTRE §8• §fCliquez la console : quatre traces attendent d'être inscrites."),true);
      if((q.stage().equals(V12TrailRules.EIGHTH_LINE)||q.stage().equals(V12TrailRules.MEMORY_COUNCIL))&&p.blockPosition().closerThan(C110SanctuaryArchitecture.registryConsole(s),7))p.displayClientMessage(Component.literal("§6REGISTRE §8• §fCliquez la console pour lire ce qui n'apparaît qu'à vous."),true);
   }

   public static boolean onRightClickBlock(RightClickBlock e){
      if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel))return false;MinecraftServer s=p.getServer();if(s==null)return false;
      V12TrailData d=V12TrailData.get(s);V12TrailData.Snapshot q=d.snapshot();BlockPos pos=e.getPos();
      if((q.stage().equals(V12TrailRules.HOUSE_INSPECT)||q.stage().equals(V12TrailRules.RETURN_REGISTRY))&&!q.optionalFound()&&pos.closerThan(q.house().offset(0,4,-6),2.2)){
         consume(e);d.optional();CampaignSavedData c=CampaignSavedData.get(s);if(c.complete(OPTIONAL,25,8)){c.addTimeline(ProgressNetworking.day(s),p.getGameProfile().getName(),"La marque sous l'enduit","Une cinquième trace, volontairement recouverte, a été rendue visible.");}
         p.displayClientMessage(Component.literal("§6TRACE FACULTATIVE §8• §fSous l'enduit : « On ne commence pas par effacer les noms. On commence par apprendre aux autres à ne plus les demander. » §8• §a+25 Âge · +8 Civilisation"),false);return true;
      }
      if(C110SanctuaryArchitecture.isRegistryConsole(s,pos)&&!q.stage().equals(V12TrailRules.LOCKED)&&!q.stage().equals(V12TrailRules.OFFERED)){
         consume(e);
         if(q.stage().equals(V12TrailRules.RETURN_REGISTRY)&&d.returnRegistry(p.serverLevel().getGameTime())){C110SanctuaryArchitecture.openGallery(s);scene(s,"LES DEUX APPELS","La Galerie s'ouvre. Deux pupitres attendent deux gestes — ou la patience d'une seule personne.");q=d.snapshot();}
         openRegistry(p);return true;
      }
      if(q.stage().equals(V12TrailRules.HOUSE_INSPECT))for(int i=0;i<4;i++)if(pos.closerThan(tracePos(q.house(),i),2.4)){
         consume(e);if(d.inspect(i,p.serverLevel().getGameTime())){String[] lines={"Un couvert supplémentaire. Aucun siège ne lui fait face.","La hauteur d'un enfant, gravée plusieurs fois puis rayée.","Une cache vide dont la poussière dessine encore deux mains.","Deux lits. Un seul numéro sur la porte."};p.displayClientMessage(Component.literal("§6TRACE "+d.snapshot().traceCount()+"/4 §8• §f"+lines[i]),false);if(d.snapshot().stage().equals(V12TrailRules.RETURN_REGISTRY))scene(s,"CE QUI RESTE","Quatre preuves sans nom. Le Registre ne pourra plus prétendre qu'il n'y avait personne.");}return true;
      }
      if(q.stage().equals(V12TrailRules.CROSS_CALL))for(int side:new int[]{-1,1})if(pos.closerThan(C110SanctuaryArchitecture.galleryLectern(s,side),2.6)){
         consume(e);d.activateLectern(side<0,p.getUUID().toString(),p.serverLevel().getGameTime());p.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,45,.7,1,.7,.04);p.serverLevel().playSound(null,pos,SoundEvents.AMETHYST_BLOCK_RESONATE,SoundSource.BLOCKS,1.2F,side<0?.8F:1.2F);
         if(d.snapshot().stage().equals(V12TrailRules.MEMORY_COUNCIL)){scene(s,"CONCORDANCE CROISÉE","Les deux appels se rencontrent. Le Registre ne demande plus ce qui s'est passé. Il demande ce que vous allez en faire.");openRegistry(p);}else p.displayClientMessage(Component.literal("§6CONCORDANCE §8• §fUn appel est actif. Faites répondre l'autre avant que l'écho ne retombe."),false);return true;
      }
      return false;
   }

   public static void follow(ServerPlayer p){
      MinecraftServer s=p.getServer();if(s==null)return;CampaignSavedData c=CampaignSavedData.get(s);V12TrailData d=V12TrailData.get(s);
      if(!c.foundationPlaced()||!p.blockPosition().closerThan(c.foundationPos(),13)){p.displayClientMessage(Component.literal("§6PISTES §8• §fRevenez près de la Borne pour inscrire cette Piste."),false);return;}
      Map<String,String> perceptions=new LinkedHashMap<>();List<ServerPlayer> players=s.getPlayerList().getPlayers();
      for(ServerPlayer each:players){String seen=players.size()>1?players.stream().filter(o->o!=each).findFirst().map(o->o.getGameProfile().getName()).orElse(each.getGameProfile().getName()):each.getGameProfile().getName();perceptions.put(each.getUUID().toString(),"La huitième ligne porte un nom : « "+seen+" ». Quand vous relevez les yeux, l'encre a disparu.");}
      if(!d.accept(p.getGameProfile().getName(),p.serverLevel().getGameTime(),perceptions)){ProgressNetworking.openFoyerPanel(p);return;}
      c.stage("CH3_EIGHTH_LINE");c.addTimeline(ProgressNetworking.day(s),p.getGameProfile().getName(),V12TrailRules.title(),"Le Foyer a accepté de lire une ligne que le Registre ne montre pas de la même manière à tous.");
      F92JournalData journal=F92JournalData.get(s);for(ServerPlayer each:players)journal.addPersonal(each.getUUID().toString(),"Une ligne du Registre portait un nom. Je ne sais pas si les autres ont lu le même.");
      scene(s,"LA HUITIÈME LIGNE","Le Registre vient d'ajouter une ligne. Elle n'apparaîtra pas de la même manière sur vos deux écrans.");ProgressNetworking.syncAll(s,c);
   }

   public static void acknowledge(ServerPlayer p){MinecraftServer s=p.getServer();if(s==null)return;V12TrailData d=V12TrailData.get(s);if(d.acknowledge(p.getUUID().toString(),p.serverLevel().getGameTime())){if(d.snapshot().stage().equals(V12TrailRules.PROCESSION_LOCATE)){ensureSites(p.serverLevel(),CampaignSavedData.get(s),d);scene(s,"VOUS N'AVEZ PAS VU LA MÊME CHOSE","Le Registre referme sa page. La Boussole s'oriente vers une marche silencieuse.");}else p.displayClientMessage(Component.literal("§6REGISTRE §8• §fVotre lecture est reconnue. Le Foyer attend l'autre regard."),false);}}
   public static void vote(ServerPlayer p,String choice){MinecraftServer s=p.getServer();if(s==null)return;V12TrailData d=V12TrailData.get(s);if(!d.vote(p.getUUID().toString(),choice,p.serverLevel().getGameTime()))return;V12TrailData.Snapshot q=d.snapshot();if(q.stage().equals(V12TrailRules.COMPLETE))complete(s,p,q);else{p.displayClientMessage(Component.literal("§6CONSEIL DE MÉMOIRE §8• §fVotre choix est inscrit. Une décision commune exige encore l'autre voix."),false);for(ServerPlayer each:s.getPlayerList().getPlayers())openRegistry(each);}}

   public static void openRegistry(ServerPlayer p){MinecraftServer s=p.getServer();if(s==null)return;V12TrailData.Snapshot q=V12TrailData.get(s).snapshot();String perception=q.perceptions().getOrDefault(p.getUUID().toString(),"");String census="Recensement I — huit présences ; sept au-delà de la lisière, une déjà à l'intérieur.\nRecensement II — six marcheurs ; "+q.vanished()+" effacés sous le seuil.";String testimony="La maison numérotée conserve "+q.traceCount()+" trace(s) sur 4. Les objets se souviennent parfois mieux que les institutions.";String versions="Version officielle : la place était vide.\nVersion vécue : quelqu'un y mangeait, grandissait, cachait des choses et dormait.";String memory=q.stage().equals(V12TrailRules.MEMORY_COUNCIL)?"Choisissez ensemble : restituer les noms, conserver les deux traces, ou laisser les blancs visibles. En DUO, aucun choix ne gagne contre l'autre.":q.policy().isBlank()?"La page attend encore que les faits deviennent une décision.":"Décision du Foyer : "+policyLabel(q.policy())+".";PacketDistributor.sendToPlayer(p,new V12Payloads.OpenRegistry(q.stage(),perception,census,testimony,versions,memory),new CustomPacketPayload[0]);}

   private static void offerIfReady(ServerPlayer p){MinecraftServer s=p.getServer();if(s==null)return;C110TrailData.Snapshot previous=C110TrailData.get(s).snapshot();if(!previous.completed()||p.serverLevel().getGameTime()-previous.stageTick()<1200)return;V12TrailData d=V12TrailData.get(s);if(d.offer(p.serverLevel().getGameTime())){CampaignSavedData c=CampaignSavedData.get(s);c.stage("CH3_OFFERED");p.displayClientMessage(Component.literal("§6NOUVELLE PISTE §8• §fLes Noms retirés §8— §7elle vous attend dans la Borne, sans urgence."),false);ProgressNetworking.syncAll(s,c);}}
   private static void restore(ServerPlayer p){MinecraftServer s=p.getServer();if(s==null)return;V12TrailData.Snapshot q=V12TrailData.get(s).snapshot();if(!q.stage().equals(V12TrailRules.LOCKED)&&!q.stage().equals(V12TrailRules.OFFERED)&&!q.stage().equals(V12TrailRules.COMPLETE))p.displayClientMessage(Component.literal("§6PISTE SUIVIE §8• §f"+V12TrailRules.title()+" §8— §7"+V12TrailRules.objective(q.stage(),q.traceCount(),q.vanished())),false);}
   private static void ensureSites(ServerLevel l,CampaignSavedData c,V12TrailData d){if(!c.foundationPlaced()||d.snapshot().sitesBuilt())return;BlockPos f=c.foundationPos();BlockPos p=surface(l,f.offset(228,0,-146)),h=surface(l,f.offset(-316,0,-205));buildProcessionSite(l,p);buildHouse(l,h);d.sites(p,h);}
   private static BlockPos surface(ServerLevel l,BlockPos p){return new BlockPos(p.getX(),l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ()),p.getZ());}
   private static void buildProcessionSite(ServerLevel l,BlockPos p){Block stone=(Block)ReivaxMCProgress.SANCTUARY_STONE.get();for(int z=-18;z<=8;z++)for(int x=-2;x<=2;x++)l.setBlock(p.offset(x,-1,z),(Math.abs(x)==2?Blocks.TUFF_BRICKS:Blocks.POLISHED_TUFF).defaultBlockState(),18);for(int side:new int[]{-3,3})for(int y=0;y<=7;y++)l.setBlock(p.offset(side,y,-18),stone.defaultBlockState(),18);for(int x=-3;x<=3;x++)l.setBlock(p.offset(x,7,-18),stone.defaultBlockState(),18);l.setBlock(p.offset(0,6,-18),((Block)ReivaxMCProgress.ECHO_STONE.get()).defaultBlockState(),18);}
   private static void buildHouse(ServerLevel l,BlockPos p){for(int x=-7;x<=7;x++)for(int z=-6;z<=6;z++){l.setBlock(p.offset(x,0,z),Blocks.MUD_BRICKS.defaultBlockState(),18);for(int y=1;y<=6;y++){boolean shell=Math.abs(x)==7||Math.abs(z)==6||y==6;l.setBlock(p.offset(x,y,z),(shell?Blocks.DEEPSLATE_BRICKS:Blocks.AIR).defaultBlockState(),18);}}for(int y=1;y<=4;y++)for(int x=-1;x<=1;x++)l.setBlock(p.offset(x,y,6),Blocks.AIR.defaultBlockState(),18);l.setBlock(tracePos(p,0),Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState(),18);l.setBlock(tracePos(p,1),Blocks.CHISELED_BOOKSHELF.defaultBlockState(),18);l.setBlock(tracePos(p,2),Blocks.BARREL.defaultBlockState(),18);l.setBlock(tracePos(p,3),Blocks.RED_BED.defaultBlockState(),18);l.setBlock(p.offset(0,4,-6),((Block)ReivaxMCProgress.ECHO_STONE.get()).defaultBlockState(),18);}
   private static BlockPos tracePos(BlockPos h,int i){return switch(i){case 0->h.offset(-4,1,-2);case 1->h.offset(5,2,-3);case 2->h.offset(-5,1,3);default->h.offset(4,1,2);};}
   private static void spawnProcession(ServerLevel l,BlockPos p){clearProcession(l);for(int i=0;i<6;i++){VeilleurEntity v=(VeilleurEntity)ReivaxMCProgress.VEILLEUR.get().create(l);if(v==null)continue;v.moveTo(p.getX()+.5,p.getY(),p.getZ()+6-i*4+.5,180,0);v.addTag(PROCESSION_TAG);v.addTag(PROCESSION_TAG+"_"+i);v.setNoAi(true);v.setInvulnerable(true);v.setSilent(true);v.setPersistenceRequired();l.addFreshEntity(v);}}
   private static void tickProcession(ServerPlayer p,V12TrailData d,V12TrailData.Snapshot q){if(!p.blockPosition().closerThan(q.procession(),42))return;long elapsed=p.serverLevel().getGameTime()-q.stageTick();int count=(int)Math.min(6,elapsed/55);if(count>d.snapshot().vanished()){for(Entity e:p.serverLevel().getEntities((Entity)null,new AABB(q.procession()).inflate(48),x->x.getTags().contains(PROCESSION_TAG+"_"+(count-1)))){p.serverLevel().sendParticles(ParticleTypes.SCULK_SOUL,e.getX(),e.getY()+1,e.getZ(),55,.7,1.2,.7,.03);e.discard();}d.vanish(count,p.serverLevel().getGameTime());if(count>=6){scene(p.getServer(),"LE SEUIL COMPTE À REBOURS","La dernière silhouette disparaît. Aucune n'a crié. La Boussole refuse pourtant d'appeler cela un départ.");}}}
   private static void clearProcession(ServerLevel l){for(Entity e:l.getEntities((Entity)null,new AABB(-30000000,-64,-30000000,30000000,400,30000000),x->x.getTags().contains(PROCESSION_TAG)))e.discard();}
   private static void complete(MinecraftServer s,ServerPlayer p,V12TrailData.Snapshot q){CampaignSavedData c=CampaignSavedData.get(s);if(c.complete(REWARD,180,40)){c.addTimeline(ProgressNetworking.day(s),p.getGameProfile().getName(),V12TrailRules.title(),"Le Foyer a choisi de "+policyLabel(q.policy()).toLowerCase()+".");F92JournalData.get(s).addShared(ProgressNetworking.day(s),p.getGameProfile().getName(),"Nous avons décidé de "+policyLabel(q.policy()).toLowerCase()+". Le Registre ne pourra plus prétendre que cette décision était neutre.");}c.stage("CH3_COMPLETE");dReward(s);scene(s,"LES NOMS RETIRÉS","La mémoire n'est pas la vérité. C'est ce que les vivants acceptent encore de porter. Votre décision est désormais irréversible.");ProgressNetworking.syncAll(s,c);}
   private static void applyCivilizationBenefits(ServerPlayer p){if(p.tickCount%200!=0)return;MinecraftServer s=p.getServer();if(s==null)return;CampaignSavedData c=CampaignSavedData.get(s);if(!c.foundationPlaced()||!p.blockPosition().closerThan(c.foundationPos(),c.territoryRadius()))return;
      if(c.hasCivilizationUpgrade("VEILLE_LISIERE")){List<Monster> threats=p.serverLevel().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(32),Entity::isAlive);if(!threats.isEmpty())threats.get(0).addEffect(new MobEffectInstance(MobEffects.GLOWING,220,0));}
      if(c.hasCivilizationUpgrade("TABLE_COMMUNE")&&s.getPlayerList().getPlayers().stream().filter(o->o.blockPosition().closerThan(c.foundationPos(),16)).count()>1&&p.getFoodData().getFoodLevel()<16){p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,80,0));p.getFoodData().eat(1,0.2F);}}
   private static void dReward(MinecraftServer s){V12TrailData d=V12TrailData.get(s);if(d.reward())for(ServerPlayer p:s.getPlayerList().getPlayers())p.displayClientMessage(Component.literal("§bRÉCOMPENSE §8• §f180 points d'Âge · 40 points de Civilisation · nouvelles décisions disponibles à la Borne."),false);}
   private static String policyLabel(String p){return switch(p){case"RESTORE_NAMES"->"Restituer les noms";case"KEEP_BOTH"->"Conserver les deux traces";default->"Laisser les blancs visibles";};}
   private static void scene(MinecraftServer s,String title,String text){if(s==null)return;CampaignSavedData c=CampaignSavedData.get(s);ProgressNetworking.broadcast(s,c,"CH3_"+title.hashCode(),"PISTE",title,text,0);}
   private static void consume(RightClickBlock e){e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);}

   public static BlockPos currentTarget(MinecraftServer s){V12TrailData.Snapshot q=V12TrailData.get(s).snapshot();return switch(q.stage()){case V12TrailRules.PROCESSION_LOCATE,V12TrailRules.PROCESSION_OBSERVE->q.procession();case V12TrailRules.HOUSE_LOCATE,V12TrailRules.HOUSE_INSPECT->q.house();default->C110SanctuaryArchitecture.registryConsole(s);};}
   public static String packet(V12TrailData.Snapshot q){return q.stage()+"~"+V12TrailRules.title()+"~"+V12TrailRules.objective(q.stage(),q.traceCount(),q.vanished())+"~"+q.traceCount()+"~"+q.vanished()+"~"+(q.stage().equals(V12TrailRules.COMPLETE)?"1":"0");}

   public static int devCommand(ServerPlayer p,String action){MinecraftServer s=p.getServer();if(s==null)return 0;V12TrailData d=V12TrailData.get(s);switch(action){case"start"->{d.reset();d.offer(p.serverLevel().getGameTime());ProgressNetworking.openFoyerPanel(p);p.displayClientMessage(Component.literal("§6CHAPITRE III DEV §8• §aPiste prête dans la Borne."),false);}case"next"->devNext(p,d);case"status"->status(p,d.snapshot());case"reset"->{clearProcession(p.serverLevel());d.reset();p.displayClientMessage(Component.literal("§6CHAPITRE III DEV §8• §aRéinitialisé."),false);}default->status(p,d.snapshot());}return 1;}
   private static void devNext(ServerPlayer p,V12TrailData d){MinecraftServer s=p.getServer();V12TrailData.Snapshot q=d.snapshot();if(q.stage().equals(V12TrailRules.OFFERED))follow(p);else if(q.stage().equals(V12TrailRules.EIGHTH_LINE)){d.acknowledge(p.getUUID().toString(),p.serverLevel().getGameTime());if(d.snapshot().stage().equals(V12TrailRules.EIGHTH_LINE))for(String id:q.participants())d.acknowledge(id,p.serverLevel().getGameTime());ensureSites(p.serverLevel(),CampaignSavedData.get(s),d);}else if(q.stage().equals(V12TrailRules.PROCESSION_LOCATE)){ensureSites(p.serverLevel(),CampaignSavedData.get(s),d);q=d.snapshot();p.teleportTo(p.serverLevel(),q.procession().getX()+5,q.procession().getY()+1,q.procession().getZ()+8,180,0);}else if(q.stage().equals(V12TrailRules.PROCESSION_OBSERVE)){d.vanish(6,p.serverLevel().getGameTime());clearProcession(p.serverLevel());}else if(q.stage().equals(V12TrailRules.HOUSE_LOCATE)){p.teleportTo(p.serverLevel(),q.house().getX(),q.house().getY()+1,q.house().getZ()+9,180,0);}else if(q.stage().equals(V12TrailRules.HOUSE_INSPECT)){for(int i=0;i<4;i++)d.inspect(i,p.serverLevel().getGameTime());}else if(q.stage().equals(V12TrailRules.RETURN_REGISTRY)){BlockPos r=C110SanctuaryArchitecture.registryConsole(s);p.teleportTo(p.serverLevel(),r.getX()-3,r.getY(),r.getZ()+.5,270,0);}else if(q.stage().equals(V12TrailRules.CROSS_CALL)){d.activateLectern(true,p.getUUID().toString(),p.serverLevel().getGameTime());d.activateLectern(false,q.participants().size()>1?q.participants().stream().filter(x->!x.equals(p.getUUID().toString())).findFirst().orElse(p.getUUID().toString()):p.getUUID().toString(),p.serverLevel().getGameTime()+1);}else if(q.stage().equals(V12TrailRules.MEMORY_COUNCIL)){for(String id:q.participants())d.vote(id,"KEEP_BOTH",p.serverLevel().getGameTime());complete(s,p,d.snapshot());}status(p,d.snapshot());}
   private static void status(ServerPlayer p,V12TrailData.Snapshot q){p.displayClientMessage(Component.literal("§6CHAPITRE III §8• §f"+q.stage()+" §8• §fTraces "+q.traceCount()+"/4 §8• §fProcession "+q.vanished()+"/6 §8• §fLectures "+q.acknowledgements().size()+"/"+q.participants().size()),false);}
}
