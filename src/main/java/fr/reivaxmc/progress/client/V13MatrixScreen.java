package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.V13Payloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/** Première interface jouable de la Matrice : dépôt sans consommation et lecture personnelle. */
public final class V13MatrixScreen extends Screen {
   private final String stage,heading,body,personal,policy;private final boolean canAnalyze,canAcknowledge;
   public V13MatrixScreen(String stage,String heading,String body,String personal,String policy,boolean canAnalyze,boolean canAcknowledge){super(Component.literal("Matrice des Origines"));this.stage=stage;this.heading=heading;this.body=body;this.personal=personal;this.policy=policy;this.canAnalyze=canAnalyze;this.canAcknowledge=canAcknowledge;}
   @Override protected void init(){int w=Math.min(800,width-28),x=(width-w)/2,y=(height-Math.min(460,height-20))/2;if(canAnalyze)addRenderableWidget(Button.builder(Component.literal("ANALYSER SANS CÉDER LES FRAGMENTS"),b->{PacketDistributor.sendToServer(new V13Payloads.Action("ANALYZE",""));onClose();}).bounds(x+w/2-160,y+396,320,24).build());if(canAcknowledge)addRenderableWidget(Button.builder(Component.literal("CONFRONTER MA VERSION"),b->{PacketDistributor.sendToServer(new V13Payloads.Action("ACK_REVELATION",""));onClose();}).bounds(x+w/2-130,y+396,260,24).build());}
   public void renderBackground(GuiGraphics g,int x,int y,float p){g.fill(0,0,width,height,-1493172224);}
   @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);int w=Math.min(800,width-28),h=Math.min(460,height-20),x=(width-w)/2,y=(height-h)/2;g.fill(x,y,x+w,y+h,-267776499);g.fill(x,y,x+7,y+h,-11690122);g.drawCenteredString(font,"MATRICE DES ORIGINES",width/2,y+18,-2309772);g.drawCenteredString(font,heading.toUpperCase(),width/2,y+40,-461589);
      box(g,"LECTURE COMMUNE",body,x+30,y+72,w-60,112,-2895929);box(g,"TRACE PERSONNELLE",personal,x+30,y+198,w-60,82,-724760);box(g,"POLITIQUE DE MÉMOIRE",policy,x+30,y+294,w-60,74,-3094084);g.drawString(font,"ÉTAT · "+stage,x+30,y+h-30,-7369852,false);super.render(g,mx,my,partial);}
   private void box(GuiGraphics g,String title,String body,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+h,-15656939);g.fill(x,y,x+3,y+h,-11690122);g.drawString(font,title,x+14,y+11,-461589,false);int yy=y+32;for(FormattedCharSequence line:font.split(Component.literal(body),w-28)){g.drawString(font,line,x+14,yy,color,false);yy+=14;if(yy>y+h-12)break;}}
   @Override public boolean isPauseScreen(){return false;}
}
