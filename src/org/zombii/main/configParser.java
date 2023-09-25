package org.zombii.main;

import com.google.gson.Gson;
import org.zombii.utils.HttpUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

class versions {
    public String id;
    public String type;
    public String url;
    public String time;
    public String releaseTime;
    public String sha1;
    public int complianceLevel;
}

class latest {
    public String release;
    public String snapshot;
}

class VersionManifest {
    public latest latest;
    public versions[] versions;
}

public class configParser {
    Gson gson = new Gson();

    public String vType;
    public String vUrl;
    public Config config;
    public VersionManifest vManifest = null;
    public String versionManifest = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public configParser() {
    }

    public VersionManifest loadVmanifest() throws Exception {

        HttpUtils.download(versionManifest, "versionManifest_v2.json");
        try (Reader reader = new FileReader("versionManifest_v2.json")) {
            vManifest = gson.fromJson(reader, VersionManifest.class);
            // System.out.println(vManifest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vManifest;
    }

    public Config loadConfig() throws Exception {

        try (Reader reader = new FileReader("config.json")) {
            config = gson.fromJson(reader, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    public Config loadConfig(Config config) throws Exception {

        this.config = config;

        return config;
    }

    public void GetVersionTypeAndUrl() {
        for (int i = 0; i < vManifest.versions.length; i++) {
            if (config.version.strip().equals(vManifest.versions[i].id.strip())) {
                vType = vManifest.versions[i].type;
                vUrl = vManifest.versions[i].url;
                break;
            }
        }
    }

    public static void addUserArgs(List<String> args, Config config) {
        args.add("--username");
        args.add(config.name);
        args.add("--uuid");
        args.add("27c5d8e7889c4c20b63cd1d54bd93412");
        args.add("--xuid");
        args.add("2938472468424572");
        args.add("--userProperties");
        args.add("{}");
        args.add("--clientId");
        args.add("");
        args.add("--accessToken");
        args.add("");
        args.add("--userType");
        args.add("msa");
    }

}
