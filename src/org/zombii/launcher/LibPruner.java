package org.zombii.launcher;

import java.util.*;
import java.util.stream.Collectors;

public class LibPruner {

    private List<String> libs;
    public List<String> winNatives = new ArrayList<>();

    public LibPruner(List<String> libs) {
        this.libs = libs;
    }

    public Dictionary<String, List<String>> prune() {
        List<String> distinctLibs = removeDuplicateLibs();
        Dictionary<String, List<String>> separated = separateNative(distinctLibs);
        List<String> noMac = removeMacItems(separated.get("libs"), separated.get("mac"), separated.get("win"));
        libs = removeLinuxItems(noMac, separated.get("lin"), separated.get("win"));
        libs.addAll(separated.get("win"));
        Dictionary<String, List<String>> result = new Hashtable<>();
        result.put("win", separated.get("win"));
        result.put("lib", libs);
        return result;
    }

    public List<String> removeDuplicateLibs() {
        return new ArrayList<>(new HashSet<>(libs.stream().map(String::strip).collect(Collectors.toList())));
    }

    public Dictionary<String, List<String>> separateNative(List<String> tlibs) {
        List<String> libss = new ArrayList<>();
        List<String> MacNatives = new ArrayList<>();
        List<String> LinuxNatives = new ArrayList<>();

        for (String x : tlibs) {
            if (x.contains("-macos") || x.contains("-osx")) {
                MacNatives.add(x);
            } else if (x.contains("-windows") && !x.contains("x32") && !x.contains("arm")) {
                winNatives.add(x);
            } else if (x.contains("-linux")) {
                LinuxNatives.add(x);
            } else if (!x.contains("-natives")) {
                libss.add(x);
            }
        }

        Dictionary<String, List<String>> z = new Hashtable<>();
        z.put("libs", libss);
        z.put("mac", MacNatives);
        z.put("win", winNatives);
        z.put("lin", LinuxNatives);
        return z;
    }

    public List<String> removeMacItems(List<String> tlibs, List<String> mac, List<String> win) {
        List<String> libz = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        for (String x : mac) {
            String replacement = x.replace("osx", "windows").replace("macos", "windows");
            if (!win.contains(replacement)) {
                remove.add(x.replace("-natives-osx", "").replace("-natives-macos", ""));
            }
        }

        for (String x : tlibs) {
            if (!remove.contains(x)) {
                libz.add(x);
            }
        }

        return libz;
    }

    public List<String> removeLinuxItems(List<String> tlibs, List<String> lin, List<String> win) {
        List<String> libz = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        for (String x : lin) {
            String replacement = x.replace("linux", "windows");
            if (!win.contains(replacement)) {
                remove.add(x.replace("-natives-linux", ""));
            }
        }

        for (String x : tlibs) {
            if (!remove.contains(x)) {
                libz.add(x);
            }
        }

        return libz;
    }
}
