package fr.reivaxmc.progress.client;

import fr.reivaxmc.progress.network.V13Payloads;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

/** Une version à la fois : assez grande pour être lue et comparée en DUO. */
public final class V13WitnessScreen extends Screen {
   private final int index;private final String heading,account,personal,fracture;
   public V13WitnessScreen(int index,String heading,String account,String personal,String fracture){super(Component.literal("Galerie des Témoignages"));this.index=index;this.heading=heading;this.account=account;this.personal=personal;this.fracture=fracture;}
   @Override protected void init(){int w=Math.min(760,width-30),x=(width-w)/2,y=(height-Math.min(430,height-24))/2;addRenderableWidget(Button.builder(Component.literal("CONSERVER CETTE VERSION"),b->{PacketDistributor.sendToServer(new V13Payloads.Action("WITNESS",Integer.toString(index)));onClose();}).bounds(x+w/2-120,y+370,240,24).build());}
   public void renderBackground(GuiGraphics g,int x,int y,float p){g.fill(0,0,width,height,-1442840576);}
   @Override public void render(GuiGraphics g,int mx,int my,float partial){renderBackground(g,mx,my,partial);int w=Math.min(760,width-30),h=Math.min(430,height-24),x=(width-w)/2,y=(height-h)/2;g.fill(x,y,x+w,y+h,-251658223);g.fill(x,y,x+6,y+h,-1463514);g.drawString(font,"GALERIE · TÉMOIGNAGE "+(index+1)+"/3",x+24,y+18,-461589,false);g.drawString(font,heading.toUpperCase(),x+24,y+42,-2309772,false);
      box(g,"VERSION CONSERVÉE",account,x+28,y+76,w-56,-2895929);box(g,"CE QUE VOUS SEUL PERCEVEZ",personal,x+28,y+178,w-56,-724760);box(g,"FRACTURE",fracture,x+28,y+280,w-56,-5725286);super.render(g,mx,my,partial);}
   private void box(GuiGraphics g,String title,String body,int x,int y,int w,int color){g.fill(x,y,x+w,y+82,-1440602325);g.fill(x,y,x+3,y+82,-1463514);g.drawString(font,title,x+14,y+10,-461589,false);int yy=y+30;for(FormattedCharSequence line:font.split(Component.literal(body),w-28)){g.drawString(font,line,x+14,yy,color,false);yy+=14;}}
   @Override public boolean isPauseScreen(){return false;}
}
