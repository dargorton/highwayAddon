package org.mozaicclient.meteor.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import org.mozaicclient.meteor.HAddonLoader;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class SwimSpeed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("The horizontal speed multiplier while swimming.")
        .defaultValue(2.0)
        .range(0.1, 50.0)
        .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("The vertical speed multiplier while swimming.")
        .defaultValue(1.0)
        .range(0.1, 10.0)
        .build()
    );

    public SwimSpeed() {
        super(HAddonLoader.CATEGORY, "swim-speed", "Turns the player into a speedboat.");
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || !isActive() || !isSwimming()) return;

        Vec3d velocity = calculateVelocity();
        mc.player.setVelocity(velocity);
    }

    private boolean isSwimming() {
        assert mc.player != null;
        return mc.player.isSwimming() || mc.player.isTouchingWater();
    }

    private Vec3d calculateVelocity() {
        // Retrieve movement input values
        assert mc.player != null;
        double forward = mc.player.input.pressingForward ? 1 : mc.player.input.pressingBack ? -1 : 0;
        double sideways = mc.player.input.pressingRight ? 1 : mc.player.input.pressingLeft ? -1 : 0;
        double upward = mc.player.input.jumping ? 1 : mc.player.input.sneaking ? -1 : 0;

        // No movement input, return current velocity
        if (forward == 0 && sideways == 0 && upward == 0) {
            return mc.player.getVelocity();
        }

        // Calculate direction and apply multipliers
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        double newX = (forward * cos - sideways * sin) * horizontalSpeed.get();
        double newZ = (forward * sin + sideways * cos) * horizontalSpeed.get();
        double newY = upward * verticalSpeed.get();

        if (mc.player.isSprinting()) {
            newX *= 1.5;
            newZ *= 1.5;
        }

        if (mc.player.horizontalCollision && upward > 0) {
            newY *= 1.5;
        }

        return new Vec3d(newX, newY, newZ);
    }

}
