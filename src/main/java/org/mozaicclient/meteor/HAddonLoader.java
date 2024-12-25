package org.mozaicclient.meteor;

import org.mozaicclient.meteor.modules.SpeedSpoofer;
import org.mozaicclient.meteor.modules.StrictEFly;
import org.mozaicclient.meteor.modules.SwimSpeed;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class HAddonLoader extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("hAddon");
    public static final HudGroup HUD_GROUP = new HudGroup("hAddon");

    @Override
    public void onInitialize() {
        LOG.info("Initializing hAddon");

        // Modules
        Modules.get().add(new SwimSpeed());
        Modules.get().add(new StrictEFly());
        Modules.get().add(new SpeedSpoofer());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "org.mozaicclient.meteor";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("dargorton", "hAddon");
    }
}
