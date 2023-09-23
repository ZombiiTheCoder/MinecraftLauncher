package org.zombii.main;

import com.google.gson.JsonParser;
import org.zombii.utils.HttpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class gameLauncher {

    private final configParser config;

    public gameLauncher(configParser config) { this.config = config; }

    private void addUserArgs(List<String> args) {
        args.add("--username");
        args.add("Zombiefied4728");
        args.add("--uuid");
        args.add("27c5d8e7889c4c40b63cc1d54db72580");
        args.add("--xuid");
        args.add("2535472368475572");
        args.add("--userProperties");
        args.add("{}");
        args.add("--clientId");
        args.add("YmQyNDViY2UtOGFhNC00ODNmLWI4NzctNmFiZmIxZWE4MWY5");
        args.add("--accessToken");
        args.add("eyJraWQiOiJhYzg0YSIsImFsZyI6IkhTMjU2In0.eyJ4dWlkIjoiMjUzNTQ3MjM2ODQ3NTU3MiIsImFnZyI6IkFkdWx0Iiwic3ViIjoiYTM3OWU3YzAtNDA2Ni00MTMyLWE3NjgtMTRlZDdlZjhmODk0IiwiYXV0aCI6IlhCT1giLCJucyI6ImRlZmF1bHQiLCJyb2xlcyI6W10sImlzcyI6ImF1dGhlbnRpY2F0aW9uIiwiZmxhZ3MiOlsidHdvZmFjdG9yYXV0aCIsIm1pbmVjcmFmdF9uZXQiLCJvcmRlcnNfMjAyMiJdLCJwcm9maWxlcyI6eyJtYyI6IjI3YzVkOGU3LTg4OWMtNGM0MC1iNjNjLWMxZDU0ZGI3MjU4MCJ9LCJwbGF0Zm9ybSI6IlVOS05PV04iLCJ5dWlkIjoiMDdkOWVhMmQ3YTUyYjkwYjllZWNmNThiOWQ0OTQ5OGEiLCJuYmYiOjE2OTQ4ODkyMDAsImV4cCI6MTY5NDk3NTYwMCwiaWF0IjoxNjk0ODg5MjAwfQ.s6cZQJ48F04vMnjj1ANfJkMUTDnCSAWQitTJY_tQRJM");
        args.add("--userType");
        args.add("msa");
    }

    public void Launch() throws Exception {
        gameDownloader g = new gameDownloader(config);
        g.DownloadClientJar();
        g.DownloadDependencies();
        g.ExtractAndDownloadNatives();
        g.DownloadAssets();
        ArrayList<File> q = new ArrayList<>(List.of(Objects.requireNonNull(new File(g.LibrariesDir).listFiles())));
        StringBuilder libs = new StringBuilder();
        for (File x : q) {
            libs.append(x.getAbsolutePath());
            libs.append(";");
        }
        libs.append(g.gameJar);
        libs.append(";");
        String mainClass = g.obj.get("mainClass").getAsString();
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
        args.add("-Djava.library.path=" + g.NativesDir);
        args.add("-Dminecraft.client.jar=" + g.gameJar);
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
        if (g.logger != null) {
            args.add("-Dlog4j.configurationFile=" + g.logger);
        }
        args.add(mainClass);
        args.add("--version");
        args.add(config.config.version);
        args.add("--gameDir");
        args.add(g.gameDir);
        args.add("--assetsDir");
        args.add(g.assetsDir);
        args.add("--assetsIndex");
        args.add(g.assetIndex);
        addUserArgs(args);
        args.add("--versionType");
        args.add(config.vType);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.inheritIO();
        build.start();
    }

    public void LaunchFabric(boolean isQuilt) throws Exception {
        String manifest = "";
        if (isQuilt) {
            JsonParser parser = new JsonParser();
            String v = parser.parse(HttpUtils.read("https://meta.quiltmc.org/v3/versions/loader/" + config.config.version)).getAsJsonArray().get(0).getAsJsonObject().get("loader").getAsJsonObject().get("version").getAsString();
            manifest = "https://meta.quiltmc.org/v3/versions/loader/"+config.config.version+"/"+v+"/profile/json";
        } else {
            JsonParser parser = new JsonParser();
            String v = parser.parse(HttpUtils.read("https://meta.fabricmc.net/v2/versions/loader/" + config.config.version)).getAsJsonArray().get(0).getAsJsonObject().get("loader").getAsJsonObject().get("version").getAsString();
            manifest = "https://meta.fabricmc.net/v2/versions/loader/"+config.config.version+"/"+v+"/profile/json";
        }
        configParser cc2 = new configParser();
        Config z2 = new Config();
        z2.version = config.config.launcher+"_"+config.config.version;
        z2.launcher = config.config.launcher;
        cc2.loadConfig(z2);
        cc2.loadVmanifest();
        new File(".minecraft/versions/"+config.config.launcher+"_"+config.config.version+"/").mkdirs();
        HttpUtils.download(manifest, ".minecraft/versions/"+config.config.launcher+"_"+config.config.version+"/"+config.config.launcher+"_"+config.config.version+".json");
        gameDownloader gz = new gameDownloader(cc2, false);
        configParser cc = new configParser();
        Config z = new Config();
        z.version = gz.obj.get("inheritsFrom").getAsString();
        z.launcher = "Mojang";
        cc.loadConfig(z);
        cc.loadVmanifest();
        cc.GetVersionTypeAndUrl();
        gameDownloader g = new gameDownloader(cc, cc2, true);
        g.DownloadDependencies();
        g.ExtractAndDownloadNatives();
        gz.FabDownloadDependencies();
        g.DownloadClientJar();
//        g.DownloadAssets();
        ArrayList<File> q = new ArrayList<>(List.of(Objects.requireNonNull(new File(g.LibrariesDir).listFiles())));
        StringBuilder libs = new StringBuilder();
        for (File x : q) {
            libs.append(x.getAbsolutePath());
            libs.append(";");
        }
        libs.append(gz.gameJar);
        libs.append(";");
        String mainClass = gz.obj.get("mainClass").getAsString();
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
        args.add("-Djava.library.path=" + gz.NativesDir);
        args.add("-Dminecraft.client.jar=" + gz.gameJar);
        args.add("-Dminecraft.launcher.brand=minecraft-launcher");
        args.add("-Dminecraft.launcher.version=2.8.2");
        args.add("-cp");
        args.add(libs.toString());
        if (!isQuilt) {
            for (int i = 0; i < gz.obj.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray().size(); i++) {
                args.add(gz.obj.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray().get(i).getAsString());
            }
        }
        args.add("-Xmx2G");
        args.add("-XX:+UnlockExperimentalVMOptions");
        args.add("-XX:+UseG1GC");
        args.add("-XX:G1NewSizePercent=20");
        args.add("-XX:G1ReservePercent=20");
        args.add("-XX:MaxGCPauseMillis=50");
        args.add("-XX:G1HeapRegionSize=32M");
        if (g.logger != null) {
            args.add("-Dlog4j.configurationFile=" + g.logger);
        }
        args.add(mainClass);
        args.add("--version");
        System.out.println(gz.obj.get("id").getAsString());
        args.add(gz.obj.get("id").getAsString());
        args.add("--gameDir");
        args.add(gz.gameDir);
        args.add("--assetsDir");
        args.add(g.assetsDir);
        args.add("--assetsIndex");
        args.add(g.assetIndex);
        addUserArgs(args);
        System.out.println(cc.vType);
        args.add("--versionType");
        args.add(cc.vType);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.start();
    }

    public void preLaunchFabric(boolean isQuilt) throws Exception {
        configParser cc2 = new configParser();
        Config z2 = new Config();
        z2.version = config.config.launcher+"_"+config.config.version;
        z2.launcher = config.config.launcher;
        cc2.loadConfig(z2);
        cc2.loadVmanifest();
        gameDownloader gz = new gameDownloader(cc2, false);
        configParser cc = new configParser();
        Config z = new Config();
        z.version = gz.obj.get("inheritsFrom").getAsString();
        z.launcher = "Mojang";
        cc.loadConfig(z);
        cc.loadVmanifest();
        cc.GetVersionTypeAndUrl();
        gameDownloader g = new gameDownloader(cc, cc2, false);
        ArrayList<File> q = new ArrayList<>(List.of(Objects.requireNonNull(new File(g.LibrariesDir).listFiles())));
        StringBuilder libs = new StringBuilder();
        for (File x : q) {
            libs.append(x.getAbsolutePath());
            libs.append(";");
        }
        libs.append(gz.gameJar);
        libs.append(";");
        String mainClass = gz.obj.get("mainClass").getAsString();
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
        args.add("-Djava.library.path=" + gz.NativesDir);
        args.add("-Dminecraft.client.jar=" + gz.gameJar);
        args.add("-Dminecraft.launcher.brand=minecraft-launcher");
        args.add("-Dminecraft.launcher.version=2.8.2");
        args.add("-cp");
        args.add(libs.toString());
        if (!isQuilt) {
            for (int i = 0; i < gz.obj.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray().size(); i++) {
                args.add(gz.obj.get("arguments").getAsJsonObject().get("jvm").getAsJsonArray().get(i).getAsString());
            }
        }
        args.add("-Xmx2G");
        args.add("-XX:+UnlockExperimentalVMOptions");
        args.add("-XX:+UseG1GC");
        args.add("-XX:G1NewSizePercent=20");
        args.add("-XX:G1ReservePercent=20");
        args.add("-XX:MaxGCPauseMillis=50");
        args.add("-XX:G1HeapRegionSize=32M");
        if (g.logger != null) {
            args.add("-Dlog4j.configurationFile=" + g.logger);
        }
        args.add(mainClass);
        args.add("--version");
        System.out.println(gz.obj.get("id").getAsString());
        args.add(gz.obj.get("id").getAsString());
        args.add("--gameDir");
        args.add(gz.gameDir);
        args.add("--assetsDir");
        args.add(g.assetsDir);
        args.add("--assetsIndex");
        args.add(g.assetIndex);
        addUserArgs(args);
        System.out.println(cc.vType);
        args.add("--versionType");
        args.add(cc.vType);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.start();
    }

    public void preLaunch() throws Exception {
        gameDownloader g = new gameDownloader(config);
        ArrayList<File> q = new ArrayList<>(List.of(Objects.requireNonNull(new File(g.LibrariesDir).listFiles())));
        StringBuilder libs = new StringBuilder();
        for (File x : q) {
            libs.append(x.getAbsolutePath());
            libs.append(";");
        }
        libs.append(g.gameJar);
        libs.append(";");
        String mainClass = g.obj.get("mainClass").getAsString();
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
        args.add("-Djava.library.path=" + g.NativesDir);
        args.add("-Dminecraft.client.jar=" + g.gameJar);
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
        if (g.logger != null) {
            args.add("-Dlog4j.configurationFile=" + g.logger);
        }
        args.add(mainClass);
        args.add("--version");
        args.add(config.config.version);
        args.add("--gameDir");
        args.add(g.gameDir);
        args.add("--assetsDir");
        args.add(g.assetsDir);
        args.add("--assetsIndex");
        args.add(g.assetIndex);
        addUserArgs(args);
        args.add("--versionType");
        args.add(config.vType);
        ProcessBuilder build = new ProcessBuilder();
        build.command(args.toArray(new String[0]));
        build.inheritIO();
        build.start();
    }

}
