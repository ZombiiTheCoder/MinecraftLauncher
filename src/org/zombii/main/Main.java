package org.zombii.main;

import org.zombii.launcher.FabricLauncher;
import org.zombii.launcher.QuiltLauncher;
import org.zombii.launcher.VanillaLauncher;

public class Main {
    public static configParser config;

    public static configParser init() throws Exception {
        config.loadVmanifest();
        config.GetVersionTypeAndUrl();
        return config;
    }

    public static void main(String[] args) throws Exception {
        config = new configParser();
        config.loadConfig();

        switch (config.config.launcher) {
            case "Fabric":
                FabricLauncher fabric = new FabricLauncher(config);
                fabric.useAuthConfig(false);
                if (fabric.VersionInstalled()) {
                    fabric.Launch();
                } else {
                    fabric.Install();
                    fabric.Launch();
                }
                break;

            case "Quilt":
                QuiltLauncher quilt = new QuiltLauncher(config);
                quilt.useAuthConfig(false);
                if (quilt.VersionInstalled()) {
                    quilt.Launch();
                } else {
                    quilt.Install();
                    quilt.Launch();
                }
                break;

            // case "Forge":
            // ForgeLauncher forge = new ForgeLauncher(config);
            // if (forge.VersionInstalled() ) { forge.Launch(); }
            // else { forge.Install(); forge.Launch(); } break;

            case "Vanilla":
                VanillaLauncher vanilla = new VanillaLauncher(init());
                vanilla.useAuthConfig(false);
                vanilla.AlterMainClass(false);
                vanilla.UseCustomLaunchWrapper(false);
                if (vanilla.VersionInstalled()) {
                    vanilla.Launch();
                } else {
                    vanilla.Install();
                    vanilla.Launch();
                }
                break;
        }
    }

}