package org.zombii.main;

public class Main {
    public static configParser config;

    public static configParser init() throws Exception {
        config.loadVmanifest();
        config.GetVersionTypeAndUrl();
        return config;
    }

    public static gameLauncher initForge() {
        return new gameLauncher(config);
    }

    public static gameLauncher initFab() {
        return new gameLauncher(config);
    }

    public static void main(String[] args) throws Exception {
        config = new configParser();
        config.loadConfig();
        // System.out.println(config.vManifest.latest.release);
        // System.out.println(config.vManifest.latest.snapshot);

        switch (config.config.launcher) {
            case "Fabric":
                FabricLauncher fabric = new FabricLauncher(config);
                if (fabric.VersionInstalled() ) { fabric.Launch(); }
                else { fabric.Install(); fabric.Launch(); } break;

            case "Quilt":
                QuiltLauncher quilt = new QuiltLauncher(config);
                if (quilt.VersionInstalled() ) { quilt.Launch(); }
                else { quilt.Install(); quilt.Launch(); } break;

//            case "Forge":
//              ForgeLauncher forge = new ForgeLauncher(config);
//              if (forge.VersionInstalled() ) { forge.Launch(); }
//              else { forge.Install(); forge.Launch(); } break;

            case "Vanilla":
                VanillaLauncher vanilla = new VanillaLauncher(init());
                if (vanilla.VersionInstalled() ) { vanilla.Launch(); }
                else { vanilla.Install(); vanilla.Launch(); } break;
        }
    }

}