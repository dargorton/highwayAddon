package org.mozaicclient.meteor.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.mozaicclient.meteor.HAddonLoader;

import java.util.Objects;

public class SpeedSpoofer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speedMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed-multiplier")
        .description("The multiplier for the player's speed.")
        .defaultValue(2.0)
        .min(1.0)
        .build()
    );

    private Vec3d lastPos;

    public SpeedSpoofer() {
        super(HAddonLoader.CATEGORY, "speed-spoofer", "Allows the player to move faster than normal while spoofing their speed to the server.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (lastPos == null) {
            lastPos = mc.player.getPos();
            return;
        }

        Vec3d newPos = mc.player.getPos();
        Vec3d spoofedPos = lastPos.add(mc.player.getVelocity().multiply(1 / speedMultiplier.get()));

        mc.player.setPos(spoofedPos.x, spoofedPos.y, spoofedPos.z);
        Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(spoofedPos.x, spoofedPos.y, spoofedPos.z, mc.player.isOnGround()));

        mc.player.setPos(newPos.x, newPos.y, newPos.z);
        lastPos = newPos;
    }

    @Override
    public void onDeactivate() {
        lastPos = null;
    }
}
