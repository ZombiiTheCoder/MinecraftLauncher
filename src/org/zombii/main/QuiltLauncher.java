package org.zombii.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.zombii.utils.FileUtils;
import org.zombii.utils.HttpUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class QuiltLauncher {
    private final configParser config;
    private final File AssetsDir;
    private final File AssetIndexes;
    private final File LoggingDir;
    private final File LibrariesDir;
    private final File NativesDir;
    public File VersionDir, GameDir;
    private final String SimpleName;
    private File GameJar;
    private File GameManifest;
    private String AssetId;
    private JsonParser json = new JsonParser();
    private JsonObject manifest;
    private File LoggingConfig;
    private File BaseManifest;
    private JsonObject QuiltObj;

    public QuiltLauncher(configParser config) {
        this.config = config;
        SimpleName = config.config.launcher + "_" + config.config.version;
        VersionDir = new File("versions/" + SimpleName);
        GameDir = new File(VersionDir + "/.minecraft");
        GameJar = new File(VersionDir + "/" + SimpleName + ".jar");
        GameManifest = new File(VersionDir + "/" + SimpleName + ".json");
        BaseManifest = new File(VersionDir + "/" + config.config.version + ".json");
        AssetsDir = new File(GameDir + "/assets");
        AssetIndexes = new File(AssetsDir + "/indexes");
        LoggingDir = new File(GameDir + "/assets/log_configs");
        NativesDir = new File(VersionDir + "/natives");
        LibrariesDir = new File(VersionDir + "/libs");
    }

    public void CreateBaseDirs() {
        VersionDir.mkdirs();
        GameDir.mkdirs();
        AssetsDir.mkdirs();
        LoggingDir.mkdirs();
        NativesDir.mkdirs();
        LibrariesDir.mkdirs();
        new File(VersionDir + "/nativesJars").mkdirs();
        AssetIndexes.mkdirs();
    }

    public boolean VersionInstalled() {
        return new File("versions/" + config.config.launcher + "_" + config.config.version).exists();
    }

    public void Install() throws Exception {
        String apiUrl = "https://meta.quiltmc.org/v3/versions/loader/";
        String v = "";
        try {
            v = json.parse(HttpUtils.read(apiUrl + config.config.version)).getAsJsonArray().get(0).getAsJsonObject()
                    .get("loader").getAsJsonObject().get("version").getAsString();
        } catch (Exception ignore) {
            System.out.println(config.config.launcher + " Version " + config.config.version + " does not exist.");
            return;
        }
        CreateBaseDirs();
        String QuiltString = HttpUtils.download(apiUrl + config.config.version + "/" + v + "/profile/json",
                GameManifest.toString());
        QuiltObj = json.parse(QuiltString).getAsJsonObject();
        configParser baseConfig = new configParser();
        Config vanillaConfig = new Config();
        vanillaConfig.version = QuiltObj.get("inheritsFrom").getAsString();
        vanillaConfig.launcher = "Vanilla";
        baseConfig.loadConfig(vanillaConfig);
        baseConfig.loadVmanifest();
        baseConfig.GetVersionTypeAndUrl();
        VanillaLauncher vanilla = new VanillaLauncher(baseConfig, config);
        vanilla.Install();
        QuiltDownloadDependencies();
    }

    public void Launch() throws Exception {
        manifest = json.parse(FileUtils.read(BaseManifest.toString())).getAsJsonObject();
        QuiltObj = json.parse(FileUtils.read(GameManifest.toString())).getAsJsonObject();
        if (manifest.has("logging")) {
            String id = manifest.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                    .getAsJsonObject().get("id").getAsString();
            LoggingConfig = new File(LoggingDir + "/" + id);
        }
        AssetId = manifest.get("assetIndex").getAsJsonObject().get("id").getAsString();
        StringBuilder libs = new StringBuilder();
        for (File x : Objects.requireNonNull(LibrariesDir.listFiles())) {
            libs.append(x.getAbsolutePath());
            libs.append(";");
        }
        libs.append(GameJar.getAbsolutePath());
        libs.append(";");
        String mainClass = QuiltObj.get("mainClass").getAsString();
        if (Objects.equals(mainClass, "net.minecraft.launchwrapper.Launch")) {
            mainClass = "net.minecraft.client.Minecraft";
        }
        System.out.println(mainClass);
        String JDK = "E:/Program Files/Java/jdk-20/bin/java.exe";
        if (!new File(JDK).exists()) {
            JDK = "C:/Program Files/Java/jdk-20/bin/java.exe";
        }
        List<String> args = new ArrayList<>();
        args.add(JDK);
        args.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        args.add("-Dos.name=Windows 10");
        args.add("-Dos.version=10.0");
        args.add("-Xss1M");
        args.add("-Djava.library.path=" + NativesDir.getAbsolutePath());
        args.add("-Dminecraft.client.jar=" + GameJar.getAbsolutePath());
        args.add("-Dminecraft.launcher.brand=minecraft-launcher");
        args.add("-Dminecraft.launcher.version=2.6.16");
        args.add("-cp");
        args.add(libs.toString());
        args.add("-Xmx2G");
        args.add("-XX:+UnlockExperimentalVMOptions");
        args.add("-XX:+UseG1GC");
        args.add("-XX:G1NewSizePercent=20");
        args.add("-XX:G1ReservePercent=20");
        args.add("-XX:MaxGCPauseMillis=50");
        args.add("-XX:G1HeapRegionSize=32M");
        if (LoggingConfig != null) {
            args.add("-Dlog4j.configurationFile=" + LoggingConfig);
        }
        args.add(mainClass);
        args.add("--version");
        args.add(config.config.version);
        args.add("--gameDir");
        args.add(GameDir.getAbsolutePath());
        args.add("--assetsDir");
        args.add(AssetsDir.getAbsolutePath());
        args.add("--assetIndex");
        args.add(AssetId);
        configParser.addUserArgs(args, config.config);
        args.add("--versionType");
        args.add(manifest.get("type").getAsString());
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.inheritIO();
        build.start();
    }

    public void QuiltDownloadDependencies() throws Exception {
        List<String> dep = new ArrayList<>();
        for (int i = 0; i < QuiltObj.get("libraries").getAsJsonArray().size(); i++) {
            JsonObject x = QuiltObj.get("libraries").getAsJsonArray().get(i).getAsJsonObject();
            String name = x.get("name").getAsString();
            String[] sp = name.split(":");
            String path = sp[0];
            String jar = sp[1];
            String ver = sp[2];
            path = path.replace(".", "/");
            path += "/" + jar + "/" + ver;
            path += "/" + jar + "-" + ver + ".jar";
            dep.add(x.get("url").getAsString() + path);
        }
        for (int i = 0; i < dep.size(); i++) {
            // System.out.println(new File(new URI(dep.get(i)).getPath()).getName());
            HttpUtils.download(dep.get(i), LibrariesDir + "/" + (new File(new URI(dep.get(i)).getPath()).getName()));
            System.out.println("Installing Quilt Library >>> " + new File(new URI(dep.get(i)).getPath()).getName());
        }
        // System.out.println("Libraries Downloaded "+dep.size());
    }

}
