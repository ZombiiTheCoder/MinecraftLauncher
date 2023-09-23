package org.zombii.main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.zombii.utils.FileUtils;
import org.zombii.utils.HttpUtils;
import org.zombii.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class gameDownloader {
    private configParser config;
    private JsonParser parser;
    public String gameJson, gameJar, gameDir, VersionDir, assetJson, assetsDir, LibrariesDir, NativesDir, NativeJarDir,
            logger = null;
    public JsonObject obj;
    private List<String> libraries = new ArrayList<String>();
    private List<String> WindowsNatives;
    public String assetIndex, modDir;

    public gameDownloader(configParser config) throws Exception {
        this.config = config;
        parser = new JsonParser();
        VersionDir = ".minecraft/versions/" + config.config.version;
        gameDir = VersionDir + "/GAME";
        new File(gameDir).mkdirs();
        gameJson = VersionDir + "/" + config.config.version + ".json";
        gameJar = VersionDir + "/" + config.config.version + ".jar";
        LibrariesDir = VersionDir + "/libraries";
        NativesDir = VersionDir + "/natives";
        assetsDir = new File(VersionDir + "/assets").getAbsolutePath();
        NativeJarDir = VersionDir + "/nativeJars";
        new File(LibrariesDir).mkdirs();
        new File(NativesDir).mkdirs();
        new File(NativeJarDir).mkdirs();
        new File(assetsDir).mkdirs();
        new File(assetsDir + "/indexes/").mkdirs();
        DownloadGameJson();
        obj = parser.parse(FileUtils.read(gameJson)).getAsJsonObject();
        assetJson = assetsDir + "/indexes/" + obj.get("assetIndex").getAsJsonObject().get("id").getAsString() + ".json";
        assetIndex = obj.get("assetIndex").getAsJsonObject().get("id").getAsString();
        HttpUtils.download(obj.get("assetIndex").getAsJsonObject().get("url").getAsString(), assetJson);
        if (obj.has("logging")) {
            String id = obj.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                    .getAsJsonObject().get("id").getAsString();
            logger = VersionDir + "/" + id;
        }
    }

    public gameDownloader(configParser config, boolean ignore) throws Exception {
        this.config = config;
        parser = new JsonParser();
        VersionDir = ".minecraft/versions/" + config.config.version;
        gameDir = VersionDir + "/GAME";
        modDir = VersionDir + "/GAME/mods";
        new File(modDir).mkdirs();
        gameJson = VersionDir + "/" + config.config.version + ".json";
        gameJar = VersionDir + "/" + config.config.version + ".jar";
        LibrariesDir = VersionDir + "/libraries";
        NativesDir = VersionDir + "/natives";
        assetsDir = new File(VersionDir + "/assets").getAbsolutePath();
        NativeJarDir = VersionDir + "/nativeJars";
        new File(LibrariesDir).mkdirs();
        new File(NativesDir).mkdirs();
        new File(NativeJarDir).mkdirs();
        new File(assetsDir).mkdirs();
        new File(assetsDir + "/indexes/").mkdirs();
        System.out.println(gameJson);
        obj = parser.parse(FileUtils.read(gameJson)).getAsJsonObject();

    }

    public gameDownloader(configParser config, configParser config2, boolean downloadLogger) throws Exception {
        this.config = config;
        parser = new JsonParser();
        VersionDir = ".minecraft/versions/" + config2.config.version;
        gameDir = VersionDir + "/GAME";
        new File(gameDir).mkdirs();
        gameJson = VersionDir + "/" + config.config.version + ".json";
        gameJar = VersionDir + "/" + config2.config.version + ".jar";
        LibrariesDir = VersionDir + "/libraries";
        NativesDir = VersionDir + "/natives";
        assetsDir = new File(VersionDir + "/assets").getAbsolutePath();
        NativeJarDir = VersionDir + "/nativeJars";
        new File(LibrariesDir).mkdirs();
        new File(NativesDir).mkdirs();
        new File(NativeJarDir).mkdirs();
        new File(assetsDir).mkdirs();
        new File(assetsDir + "/indexes/").mkdirs();
        if (downloadLogger) {
            DownloadGameJson();
        }
        obj = parser.parse(FileUtils.read(gameJson)).getAsJsonObject();
        assetJson = assetsDir + "/indexes/" + obj.get("assetIndex").getAsJsonObject().get("id").getAsString() + ".json";
        assetIndex = obj.get("assetIndex").getAsJsonObject().get("id").getAsString();
        HttpUtils.download(obj.get("assetIndex").getAsJsonObject().get("url").getAsString(), assetJson);
        if (obj.has("logging")) {
            String id = obj.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                    .getAsJsonObject().get("id").getAsString();
            logger = VersionDir + "/" + id;
            if (downloadLogger) {
                String url = obj.get("logging").getAsJsonObject().get("client").getAsJsonObject().get("file")
                        .getAsJsonObject().get("url").getAsString();
                // System.out.println(url);
                HttpUtils.download(url, logger);
            }
        }
    }

    private void DownloadGameJson() throws Exception {
        HttpUtils.download(config.vUrl, gameJson);
    }

    public void DownloadClientJar() throws Exception {

        HttpUtils.download(
                obj.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString(),
                gameJar);
        // System.out.println(obj.get("downloads").getAsJsonObject().get("client").getAsJsonObject().get("url").getAsString());
    }

    public void DownloadAssets() {
        JsonObject asObj = parser.parse(FileUtils.read(assetJson)).getAsJsonObject();
        for (String object : asObj.get("objects").getAsJsonObject().keySet()) {
            String hash = asObj.get("objects").getAsJsonObject().get(object).getAsJsonObject().get("hash")
                    .getAsString();
            String hashq = hash.split("(?!^)")[0] + hash.split("(?!^)")[1];
            String dir = assetsDir + "/objects/" + hashq;
            new File(dir).mkdirs();
            try {
                HttpUtils.download("https://resources.download.minecraft.net/" + hashq + "/" + hash, dir + "/" + hash);
                System.out.println(object + "  " + hashq + "  " + hash);
            } catch (Exception ignore) {
            }
        }

    }

    public void DownloadDependencies() throws Exception {
        List<String> dep = CollectDependencies();
        for (int i = 0; i < dep.size(); i++) {
            // System.out.println(new File(new URI(dep.get(i)).getPath()).getName());
            try {
                HttpUtils.download(dep.get(i),
                        LibrariesDir + "/" + (new File(new URI(dep.get(i)).getPath()).getName()));
            } catch (Exception ignored) {
            }
        }
        // System.out.println("Libraries Downloaded "+dep.size());
    }

    public void FabDownloadDependencies() throws Exception {
        List<String> dep = FabCollectDependencies();
        for (int i = 0; i < dep.size(); i++) {
            // System.out.println(new File(new URI(dep.get(i)).getPath()).getName());
            try {
                HttpUtils.download(dep.get(i),
                        LibrariesDir + "/" + (new File(new URI(dep.get(i)).getPath()).getName()));
            } catch (Exception ignored) {
            }
        }
        // System.out.println("Libraries Downloaded "+dep.size());
    }

    public void ExtractAndDownloadNatives() throws IOException {
        List<String> dir = new ArrayList<>();
        for (int i = 0; i < WindowsNatives.size(); i++) {
            try {
                dir.add(NativeJarDir + "/" + (new File(new URI(WindowsNatives.get(i)).getPath()).getName()));
                HttpUtils.download(WindowsNatives.get(i),
                        NativeJarDir + "/" + (new File(new URI(WindowsNatives.get(i)).getPath()).getName()));
            } catch (Exception ignore) {
            }
        }

        for (int i = 0; i < dir.size(); i++) {
            String x = dir.get(i);
            ZipUtils.unzip(x, NativesDir);
            FileUtils.deleteDirectory(new File(NativesDir + "/META-INF"));
        }
        FileUtils.deleteDirectory(new File(NativeJarDir));
    }

    public List<String> CollectDependencies() {
        for (int i = 0; i < obj.get("libraries").getAsJsonArray().size(); i++) {
            JsonObject downloads = obj.get("libraries").getAsJsonArray().get(i).getAsJsonObject().get("downloads")
                    .getAsJsonObject();
            CollectParentAndChildLibs(0, downloads, downloads);
        }
        LibPruner p = new LibPruner(libraries);
        WindowsNatives = p.prune().get("win");
        // System.out.println(WindowsNatives);
        return p.prune().get("lib");
    }

    public List<String> FabCollectDependencies() {
        for (int i = 0; i < obj.get("libraries").getAsJsonArray().size(); i++) {
            JsonObject x = obj.get("libraries").getAsJsonArray().get(i).getAsJsonObject();
            FabAddLib(x.get("url").getAsString(), x.get("name").getAsString());
        }
        return libraries;
    }

    private void addLib(String lib) {
        libraries.add(lib);
    }

    private void FabAddLib(String url, String name) {
        String[] sp = name.split(":");
        String path = sp[0];
        String jar = sp[1];
        String ver = sp[2];
        path = path.replace(".", "/");
        path += "/" + jar + "/" + ver;
        path += "/" + jar + "-" + ver + ".jar";
        System.out.println(url + path);
        libraries.add(url + path);
    }

    private void CollectParentAndChildLibs(int deepness, JsonObject parent, JsonObject sub) {
        JsonObject child = sub;
        String[] ks = child.getAsJsonObject().keySet().toArray(new String[0]);
        for (int i = 0; i < ks.length; i++) {
            child = sub;
            // // System.out.println(ks[i]);
            if (child.has(ks[i])) {
                child = child.get(ks[i]).getAsJsonObject();
                if (child.has("url")) {
                    addLib(child.get("url").getAsString());
                } else {
                    CollectParentAndChildLibs(deepness + 1, parent, child);
                }
            } else {
                addLib(child.getAsString());
            }
        }
    }

}
