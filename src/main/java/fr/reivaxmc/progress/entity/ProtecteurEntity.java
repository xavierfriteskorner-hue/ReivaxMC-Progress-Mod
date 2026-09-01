package fr.reivaxmc.progress.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Protecteur : gardien massif (~3,2 blocs), lent et très résistant. Stats V1 du brief. */
public class ProtecteurEntity extends GuardianEntity {
   public ProtecteurEntity(EntityType<? extends ProtecteurEntity> type, Level level) {
      super(type, level);
   }

   @Override
   public String geoName() {
      return "protecteur";
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 100.0)
         .add(Attributes.MOVEMENT_SPEED, 0.19)
         .add(Attributes.ATTACK_DAMAGE, 8.0)
         .add(Attributes.ARMOR, 8.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
         .add(Attributes.FOLLOW_RANGE, 24.0);
   }
}
