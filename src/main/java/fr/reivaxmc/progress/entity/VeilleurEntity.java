package fr.reivaxmc.progress.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Veilleur : sentinelle fine et verticale (~2,4 blocs). Stats V1 du brief. */
public class VeilleurEntity extends GuardianEntity {
   public VeilleurEntity(EntityType<? extends VeilleurEntity> type, Level level) {
      super(type, level);
   }

   @Override
   public String geoName() {
      return "veilleur";
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.MOVEMENT_SPEED, 0.26)
         .add(Attributes.ATTACK_DAMAGE, 5.0)
         .add(Attributes.ARMOR, 4.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.35)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }
}
