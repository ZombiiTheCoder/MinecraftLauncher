package org.zombii.launcher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.zombii.main.configParser;
import org.zombii.utils.FileUtils;
import org.zombii.utils.HttpUtils;
import org.zombii.utils.ZipUtils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private boolean mainClassAlteration;
    private boolean customLaunchWrapper, authConfig;

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

    public boolean UseCustomLaunchWrapper() { return this.customLaunchWrapper; }
    public void UseCustomLaunchWrapper(boolean wrapper) { this.customLaunchWrapper = wrapper; }

    public boolean AlterMainClass() { return this.mainClassAlteration; }
    public void AlterMainClass(boolean alter) { this.mainClassAlteration = alter; }

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
        for (int i = 0; i < ks.length; i++) {
            String object = ks[i];
            String hash = assetManifest.get("objects").getAsJsonObject().get(object).getAsJsonObject().get("hash").getAsString();
            String hashq = hash.split("(?!^)")[0] + hash.split("(?!^)")[1];
            String dir = AssetsDir + "/objects/" + hashq;
            new File(dir).mkdirs();
            try {
                HttpUtils.download("https://resources.download.minecraft.net/" + hashq + "/" + hash, dir + "/" + hash);
                System.out.println("Installing Asset "+Math.round(((double) 100.0 / (double) ks.length)*i)+"% >>> " + object + "  " + hashq + "  " + hash);
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
        JRE jre = null;

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
        String mainClass = manifest.get("mainClass").getAsString();
        System.out.println(mainClass);
        if (Objects.equals(mainClass, "net.minecraft.launchwrapper.Launch") && this.mainClassAlteration || Objects.equals(mainClass, "net.minecraft.launchwrapper.Launch") && this.customLaunchWrapper) {
            if (this.customLaunchWrapper) {
                libs.append(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath());
                System.out.println(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().getAbsolutePath());
                libs.append(";");
                mainClass = "org.zombii.launcher.wrapper.Launch";
            } else {
                mainClass = "net.minecraft.client.Minecraft";
            }
        }

        String JRE_JDK = "E:/Program Files/Java/jdk-20/bin/java.exe";
        if (!new File(JRE_JDK).exists()) {
            JRE_JDK = "C:/Program Files/Java/jdk-20/bin/java.exe";
        }

        if (manifest.get("mainClass").getAsString().equals("net.minecraft.launchwrapper.Launch")) {
            jre = new JRE(
                    manifest.get("javaVersion").getAsJsonObject().get("component").getAsString(),
                    manifest.get("javaVersion").getAsJsonObject().get("majorVersion").getAsInt()
            );
            if (new File("C:/Windows/CCM").exists()) {
                System.out.println("---------------------> Version "+config.config.version+" Not Supported on Windows Enterprise");
                System.exit(1);
            }
            if (!jre.JRE_LOCATION.exists()) jre.Install();
            JRE_JDK = jre.JRE_LOCATION+"/bin/java.exe";
        }
        if (jre == null && !new File(JRE_JDK).exists() && !new File("C:/Windows/CCM").exists()) {
            jre = new JRE(
                    "jre-20",
                    manifest.get("javaVersion").getAsJsonObject().get("majorVersion").getAsInt()
            );
            if (!jre.JRE_LOCATION.exists()) jre.Install();
            JRE_JDK = jre.JRE_LOCATION+"/bin/java.exe";
        }
        libs.append(GameJar.getAbsolutePath());
        libs.append(";");
        List<String> args = new ArrayList<>();
        args.add(JRE_JDK);
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
        if (authConfig) {
            configParser.addUserArgs(args, config.authConfig);
        } else {
            configParser.addUserArgs(args, config.config);
        }
        args.add("--versionType");
        args.add(config.vType);
        if (manifest.has("minecraftArguments")) {
            Pattern pattern = Pattern.compile("net.*Tweaker", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(manifest.get("minecraftArguments").getAsString());
//            System.out.println(matcher.find() + " " + matcher.group());
            while (matcher.find()) {
                String g = matcher.group();
                System.out.println(g);
                args.add("--tweakClass");
                args.add(g.strip());
            }
        }
        System.out.println(args);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.inheritIO();
        build.start();
    }

    public void useAuthConfig(boolean b) {
        authConfig = b;
    }
}
