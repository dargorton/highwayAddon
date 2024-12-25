package org.mozaicclient.meteor.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;
import org.mozaicclient.meteor.HAddonLoader;

public class StrictEFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAutoRocket = settings.createGroup("AutoRocket");

    // General Settings
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("The speed multiplier for Elytra flight.")
        .defaultValue(1.5)
        .range(0.1, 10.0)
        .build()
    );

    private final Setting<Double> glideFactor = sgGeneral.add(new DoubleSetting.Builder()
        .name("glide-factor")
        .description("How much the player glides down naturally.")
        .defaultValue(0.03)
        .range(0.01, 0.1)
        .build()
    );

    // AutoRocket Settings
    private final Setting<Boolean> strictAutoRocket = sgAutoRocket.add(new BoolSetting.Builder()
        .name("strict-autorocket")
        .description("Automatically places rockets in the offhand and uses them.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoRocket = sgAutoRocket.add(new BoolSetting.Builder()
        .name("autorocket")
        .description("Silently swaps rockets and uses them.")
        .defaultValue(false)
        .build()
    );

    public StrictEFly() {
        super(HAddonLoader.CATEGORY, "strict-elytrafly", "Allows smooth Elytra flight while bypassing some anticheats.");
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null || !mc.player.isFallFlying() || !isActive()) return;

        Vec3d direction = getMovementDirection();
        Vec3d velocity = calculateVelocity(direction);

        // Apply velocity adjustments
        mc.player.setVelocity(velocity);

        // Handle AutoRocket logic
        if (shouldUseRocket()) {
            useRocket();
        }

        // Send position packets to bypass anti-cheats
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
            mc.player.getX(),
            mc.player.getY(),
            mc.player.getZ(),
            mc.player.isOnGround()
        ));
    }

    private Vec3d getMovementDirection() {
        assert mc.player != null;
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double forward = mc.player.input.pressingForward ? 1 : 0;
        double sideways = mc.player.input.pressingRight ? 1 : mc.player.input.pressingLeft ? -1 : 0;

        double x = -Math.sin(yaw) * forward + Math.cos(yaw) * sideways;
        double z = Math.cos(yaw) * forward + Math.sin(yaw) * sideways;

        return new Vec3d(x, 0, z).normalize();
    }

    private Vec3d calculateVelocity(Vec3d direction) {
        double currentSpeed = speed.get();
        double glide = glideFactor.get();

        // Adjust Y velocity for gliding
        assert mc.player != null;
        double newY = mc.player.getVelocity().y - glide;

        // Apply speed multiplier
        double newX = direction.x * currentSpeed;
        double newZ = direction.z * currentSpeed;

        return new Vec3d(newX, newY, newZ);
    }

    private boolean shouldUseRocket() {
        // Check if player is falling or not gaining enough altitude
        assert mc.player != null;
        return mc.player.getVelocity().y < -0.1 || mc.player.getY() < 64;
    }

    private void useRocket() {
     if (autoRocket.get()) {
            // Silently swap to rockets and use them
            int rocketSlot = findRocketSlot();
            if (rocketSlot != -1) {
                assert mc.player != null;
                mc.player.getInventory().selectedSlot = rocketSlot;
                assert mc.interactionManager != null;
                mc.interactionManager.interactItem(mc.player, mc.player.preferredHand);
            }
        }
    }

    private int findRocketSlot() {
        for (int i = 0; i < 36; i++) {
            assert mc.player != null;
            if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onActivate() {
        if (mc.player != null) {
            mc.player.setSprinting(true); // Start with a boost
        }
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.setVelocity(Vec3d.ZERO); // Stop movement on disable
        }
    }
}
