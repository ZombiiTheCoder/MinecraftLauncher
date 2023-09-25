package org.zombii.launcher;

import org.zombii.utils.FileUtils;
import org.zombii.utils.HttpUtils;
import org.zombii.utils.ZipUtils;

import java.io.File;
import java.net.URI;

public class JRE {
    private String apiUrl;
    private int MajorVersion;
    public File JRE_LOCATION;
    private String JRE_ZIP_LOCATION;
    public JRE(String CodeName, int MajorVersion) {
        apiUrl = "https://api.adoptium.net/v3/binary/latest/"+MajorVersion+"/ga/windows/x64/jre/hotspot/normal/eclipse";
        JRE_LOCATION = new File("JavaRuntime/"+CodeName);
        this.MajorVersion = MajorVersion;
        JRE_ZIP_LOCATION = new File("JavaRuntime/"+CodeName+"/"+CodeName+".zip").getAbsolutePath();
    }

    public void Install() throws Exception {
        JRE_LOCATION.mkdirs();
        HttpUtils.download(apiUrl, JRE_ZIP_LOCATION);
//        Pattern pattern = Pattern.compile("h.*_[0-9].*[0-9]", Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher("OpenJDK8U-jdk_x64_windows_hotspot_8u382b05");
        String innerFolder = MajorVersion == 8 ? "jdk8u382-b05-jre" : "jdk-20.0.2+9-jre";
//        if (matcher.find()) { innerFolder = matcher.group().replace("hotspot_", "").strip(); }
        System.out.println(URI.create("jar:file:///"+JRE_ZIP_LOCATION.replace('\\', '/')+"!/"));
        ZipUtils.extractSubDir(URI.create("jar:file:///"+JRE_ZIP_LOCATION.replace('\\', '/')+"!/"), innerFolder, JRE_LOCATION.getAbsolutePath());
        FileUtils.remove(JRE_ZIP_LOCATION);

    }
}
