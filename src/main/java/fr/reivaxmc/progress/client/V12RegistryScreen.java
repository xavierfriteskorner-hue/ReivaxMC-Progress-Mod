package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.V12Payloads;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/** Registre lisible : les choix importants occupent de vraies cartes plein écran. */
public final class V12RegistryScreen extends Screen {
   private final String stage,perception,census,testimonies,versions,memory; private int tab; private Button acknowledge,restore,keep,blank;
   public V12RegistryScreen(String stage,String perception,String census,String testimonies,String versions,String memory){super(Component.literal("Registre des Absents"));this.stage=stage;this.perception=perception;this.census=census;this.testimonies=testimonies;this.versions=versions;this.memory=memory;this.tab="MEMORY_COUNCIL".equals(stage)?3:0;}
   @Override protected void init(){int w=Math.min(780,width-28),x=(width-w)/2,y=(height-Math.min(450,height-20))/2;
      acknowledge=addRenderableWidget(Button.builder(Component.literal("JE L'AI VUE"),b->send("ACK","")).bounds(x+w/2-90,y+350,180,24).build());
      restore=addRenderableWidget(Button.builder(Component.literal("RESTITUER LES NOMS"),b->send("VOTE","RESTORE_NAMES")).bounds(x+35,y+330,210,24).build());
      keep=addRenderableWidget(Button.builder(Component.literal("CONSERVER LES DEUX TRACES"),b->send("VOTE","KEEP_BOTH")).bounds(x+w/2-115,y+330,230,24).build());
      blank=addRenderableWidget(Button.builder(Component.literal("LAISSER LES BLANCS"),b->send("VOTE","LEAVE_BLANKS")).bounds(x+w-245,y+330,210,24).build()); updateButtons();}
   private void send(String a,String v){PacketDistributor.sendToServer(new V12Payloads.Action(a,v));onClose();}
   private void updateButtons(){boolean ack=tab==0&&"EIGHTH_LINE".equals(stage),vote=tab==3&&"MEMORY_COUNCIL".equals(stage);acknowledge.visible=ack;restore.visible=keep.visible=blank.visible=vote;}
   public void renderBackground(GuiGraphics g,int x,int y,float p){g.fill(0,0,width,height,-1392508928);}
   @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);int w=Math.min(780,width-28),h=Math.min(450,height-20),x=(width-w)/2,y=(height-h)/2;
      g.fill(x,y,x+w,y+h,-251658223);g.fill(x,y,x+6,y+h,-1463514);g.drawString(font,"REGISTRE DES ABSENTS",x+22,y+18,-461589,false);g.drawString(font,"Une archive ne dit pas toujours la vérité. Elle dit ce qui a survécu.",x+22,y+38,-4144960,false);
      String[] tabs={"RECENSEMENTS","TÉMOIGNAGES","VERSIONS","MÉMOIRE"};int cw=(w-52)/4;
      for(int i=0;i<4;i++){int tx=x+20+i*(cw+4);g.fill(tx,y+60,tx+cw,y+88,i==tab?-13025210:-1440602325);if(i==tab)g.fill(tx,y+85,tx+cw,y+88,-1463514);g.drawCenteredString(font,tabs[i],tx+cw/2,y+70,i==tab?-461589:-2895929);}
      String body=switch(tab){case 0->census;case 1->testimonies;case 2->versions;default->memory;};int by=y+112;
      if(tab==0&&!perception.isBlank()){g.fill(x+28,by,x+w-28,by+72,-1440602325);g.drawString(font,"LA HUITIÈME LIGNE",x+44,by+14,-1463514,false);paragraph(g,perception,x+44,by+35,w-88,-724760);by+=92;}
      paragraph(g,body,x+34,by,w-68,-2895929);super.render(g,mx,my,partial);}
   private void paragraph(GuiGraphics g,String s,int x,int y,int w,int color){for(FormattedCharSequence line:font.split(Component.literal(s),w)){g.drawString(font,line,x,y,color,false);y+=15;}}
   @Override public boolean mouseClicked(double mx,double my,int button){int w=Math.min(780,width-28),h=Math.min(450,height-20),x=(width-w)/2,y=(height-h)/2,cw=(w-52)/4;for(int i=0;i<4;i++){int tx=x+20+i*(cw+4);if(mx>=tx&&mx<tx+cw&&my>=y+60&&my<y+88){tab=i;updateButtons();return true;}}return super.mouseClicked(mx,my,button);}
   @Override public boolean isPauseScreen(){return false;}
}
