package org.zombii.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.zombii.utils.FileUtils;
import org.zombii.utils.HttpUtils;
import org.zombii.utils.ZipUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class VanillaLauncher {
    private final configParser config;
    private final File AssetsDir;
    private final File LoggingDir;
    private final File NativesDir;
    private final File LibrariesDir;
    private final File AssetIndexes;
    private File LoggingConfig = null;
    private File VersionDir;
    private File GameDir;
    private File GameJar;
    private File GameManifest;
    private JsonParser json = new JsonParser();
    private JsonObject manifest, assetManifest;
    private File AssetManifest;
    private String AssetId;

    public VanillaLauncher(configParser config) {
        this.config = config;
        VersionDir = new File("versions/" + config.config.version);
        GameDir = new File(VersionDir + "/.minecraft");
        GameJar = new File(VersionDir + "/" + config.config.version + ".jar");
        GameManifest = new File(VersionDir + "/" + config.config.version + ".json");
        AssetsDir = new File(GameDir + "/assets");
        AssetIndexes = new File(AssetsDir + "/indexes");
        LoggingDir = new File(GameDir + "/assets/log_configs");
        NativesDir = new File(VersionDir + "/natives");
        LibrariesDir = new File(VersionDir + "/libs");
    }

    public VanillaLauncher(configParser config, configParser moddedconfig) {
        this.config = config;
        String simpleName = moddedconfig.config.launcher + "_" + moddedconfig.config.version;
        VersionDir = new File("versions/" + simpleName);
        GameDir = new File(VersionDir + "/.minecraft");
        GameJar = new File(VersionDir + "/" + simpleName + ".jar");
        GameManifest = new File(VersionDir + "/" + config.config.version + ".json");
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
        return new File("versions/" + config.config.version).exists();
    }

    public void Install() throws Exception {
        CreateBaseDirs();
        manifest = json.parse(HttpUtils.download(config.vUrl, GameManifest.toString())).getAsJsonObject();
        AssetManifest = new File(
                AssetIndexes + "/" + manifest.get("assetIndex").getAsJsonObject().get("id").getAsString() + ".json");
        AssetId = manifest.get("assetIndex").getAsJsonObject().get("id").getAsString();
        assetManifest = json
                .parse(HttpUtils.download(manifest.get("assetIndex").getAsJsonObject().get("url").getAsString(),
                        AssetManifest.toString()))
                .getAsJsonObject();
        HttpUtils.download(
                manifest.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString(),
                GameJar.toString());
        System.out.println("Installing Client >>> " + GameJar.toString());
        DownloadLibsAndNatives();
        DownloadAssets();
        if (manifest.has("logging")) {
            String id = manifest.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                    .getAsJsonObject().get("id").getAsString();
            LoggingConfig = new File(LoggingDir + "/" + id);
            String url = manifest.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                    .getAsJsonObject().get("url").getAsString();
            HttpUtils.download(url, LoggingConfig.toString());
        }
    }

    public void DownloadLibsAndNatives() throws Exception {
        List<String> libs = new ArrayList<>();
        for (int i = 0; i < manifest.get("libraries").getAsJsonArray().size(); i++) {
            JsonObject downloads = manifest.get("libraries").getAsJsonArray().get(i).getAsJsonObject().get("downloads")
                    .getAsJsonObject();
            CollectParentAndChildLibs(0, downloads, downloads, libs);
        }
        LibPruner p = new LibPruner(libs);
        ExtractAndDownloadNatives(p.prune().get("win"));
        for (String lib : p.prune().get("lib")) {
            System.out.println("Installing Library >>> " + new File(new URI(lib).getPath()).getName());
            HttpUtils.download(lib, LibrariesDir + "/" + new File(new URI(lib).getPath()).getName());
        }
    }

    public void DownloadAssets() {
        String[] ks = assetManifest.get("objects").getAsJsonObject().keySet().toArray(new String[0]);
        for (String object : ks) {
            String hash = assetManifest.get("objects").getAsJsonObject().get(object).getAsJsonObject().get("hash").getAsString();
            String hashq = hash.split("(?!^)")[0] + hash.split("(?!^)")[1];
            String dir = AssetsDir + "/objects/" + hashq;
            new File(dir).mkdirs();
            try {
                System.out.println("Installing Asset "+Math.abs((double) ks.length)+" >>> " + object + "  " + hashq + "  " + hash);
                HttpUtils.download("https://resources.download.minecraft.net/" + hashq + "/" + hash, dir + "/" + hash);
            } catch (Exception ignore) {
            }
        }

    }

    public void ExtractAndDownloadNatives(List<String> natives) throws Exception {
        List<String> dir = new ArrayList<>();
        for (String x : natives) {
            dir.add(NativesDir + "Jars/" + new File(new URI(x).getPath()).getName());
            HttpUtils.download(x, NativesDir + "Jars/" + new File(new URI(x).getPath()).getName());
        }

        for (int i = 0; i < dir.size(); i++) {
            String x = dir.get(i);
            ZipUtils.unzip(x, NativesDir.toString());
            FileUtils.deleteDirectory(new File(NativesDir + "/META-INF"));
        }
        FileUtils.deleteDirectory(new File(NativesDir + "Jars"));
    }

    private void CollectParentAndChildLibs(int deepness, JsonObject parent, JsonObject sub, List<String> libs) {
        JsonObject child = sub;
        String[] ks = child.getAsJsonObject().keySet().toArray(new String[0]);
        for (int i = 0; i < ks.length; i++) {
            child = sub;
            if (child.has(ks[i])) {
                child = child.get(ks[i]).getAsJsonObject();
                if (child.has("url")) {
                    libs.add(child.get("url").getAsString());
                } else {
                    CollectParentAndChildLibs(deepness + 1, parent, child, libs);
                }
            } else {
                libs.add(child.getAsString());
            }
        }
    }

    public void Launch() throws Exception {
        manifest = json.parse(FileUtils.read(GameManifest.toString())).getAsJsonObject();
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
        libs.append(GameJar);
        libs.append(";");
        String mainClass = manifest.get("mainClass").getAsString();
        if (Objects.equals(mainClass, "net.minecraft.launchwrapper.Launch")) {
            mainClass = "net.minecraft.client.Minecraft";
        }
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
        args.add("-Djava.library.path=" + NativesDir);
        args.add("-Dminecraft.client.jar=" + GameJar);
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
        args.add(GameDir.toString());
        args.add("--assetsDir");
        args.add(AssetsDir.toString());
        args.add("--assetIndex");
        args.add(AssetId);
        configParser.addUserArgs(args, config.config);
        args.add("--versionType");
        args.add(config.vType);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.inheritIO();
        build.start();
    }

}
