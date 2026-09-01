package fr.reivaxmc.progress.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base commune des gardiens du Sanctuaire (Veilleur, Protecteur) rendus par GeckoLib.
 * Corps = créature custom (modèle .geo.json + texture + emissive), remplace le piglin_brute retexturé.
 */
public abstract class GuardianEntity extends Monster implements GeoEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private final RawAnimation idle;
   private final RawAnimation walk;

   protected GuardianEntity(EntityType<? extends GuardianEntity> type, Level level) {
      super(type, level);
      this.idle = RawAnimation.begin().thenLoop("animation." + this.geoName() + ".idle");
      this.walk = RawAnimation.begin().thenLoop("animation." + this.geoName() + ".walk");
      this.setPersistenceRequired();
   }

   /** Nom de base des ressources : "veilleur" ou "protecteur". */
   public abstract String geoName();

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 14.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
   }

   @Override
   public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "main", 5, state -> {
         if (state.isMoving()) {
            return state.setAndContinue(this.walk);
         }

         return state.setAndContinue(this.idle);
      }));
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
