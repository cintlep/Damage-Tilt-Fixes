package cintlex.dtf.mixin;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class directionalfixes {
    @Shadow @Final
    private MinecraftClient client;
    private static DamageSource nondirectionaldamage = null;
    private static float randomleftright = 0.0f;
    @Redirect(
            method = "tiltViewWhenHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getDamageTiltYaw()F"
            )
    )
    private float restoredrandomleftright(LivingEntity entity) {
        if (this.client.player != null && entity == this.client.player && this.client.player.hurtTime > 0) {
            DamageSource damage = this.client.player.getRecentDamageSource();
            if (damage != null) {
                if (!directionaldamage(damage)) {
                    if (nondirectionaldamage != damage) {
                        randomleftright = this.client.player.getRandom().nextBoolean() ? 0.0f : 180.0f;
                        nondirectionaldamage = damage;
                    }
                    return randomleftright;
                } else if (explosiondamage(damage)) {
                    return explosiontilt(damage);
                }
            }
        }
        return entity.getDamageTiltYaw();
    }

    private boolean directionaldamage(DamageSource source) {
        return source.getAttacker() != null ||
                source.getSource() != null ||
                source.getPosition() != null ||
                source.isOf(DamageTypes.PLAYER_ATTACK) ||
                source.isOf(DamageTypes.MOB_ATTACK) ||
                source.isOf(DamageTypes.ARROW) ||
                source.isOf(DamageTypes.TRIDENT) ||
                source.isOf(DamageTypes.FIREBALL) ||
                source.isOf(DamageTypes.WITHER_SKULL) ||
                source.isOf(DamageTypes.THROWN) ||
                source.isOf(DamageTypes.INDIRECT_MAGIC) ||
                source.isOf(DamageTypes.EXPLOSION) ||
                source.isOf(DamageTypes.PLAYER_EXPLOSION) ||
                source.isOf(DamageTypes.WIND_CHARGE);
    }

    private boolean explosiondamage(DamageSource source) {
        return source.isOf(DamageTypes.EXPLOSION) ||
                source.isOf(DamageTypes.PLAYER_EXPLOSION);
    }

    private float explosiontilt(DamageSource explosionsource) {
        if (explosionsource.getPosition() != null) {
            double dx = explosionsource.getPosition().x - this.client.player.getX();
            double dz = explosionsource.getPosition().z - this.client.player.getZ();
            double worldAngle = Math.atan2(dz, dx) * 180.0 / Math.PI;
            double playerYaw = this.client.player.getYaw();
            double relativeAngle = worldAngle - playerYaw;
            while (relativeAngle < 0) relativeAngle += 360;
            while (relativeAngle >= 360) relativeAngle -= 360;
            if (relativeAngle < 45 || relativeAngle >= 315) {
                return 0.0f;
            } else if (relativeAngle < 135) {
                return 90.0f;
            } else if (relativeAngle < 225) {
                return 180.0f;
            } else {
                return 270.0f;
            }
        }
        return this.client.player.getDamageTiltYaw();
    }
}