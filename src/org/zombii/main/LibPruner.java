package org.zombii.main;

import java.util.*;

public class LibPruner {

    private List<String> libs;
    public List<String> winNatives = new ArrayList<>();
    public LibPruner(List<String> libs) {
        this.libs = libs;
    }

    public Dictionary<String, List<String>> prune() {
        List<String> lbs = RemoveDupeLibs();
        Dictionary<String, List<String>> separated = SeperateNative(lbs);
        List<String> noMac = RemoveMacItems(separated.get("libs"), separated.get("mac"), separated.get("win"));
        libs = RemoveLinuxItems(noMac, separated.get("lin"), separated.get("win"));
        libs.addAll(separated.get("win"));
        Dictionary<String, List<String>> f = new Hashtable<>();
        f.put("win", separated.get("win"));
        f.put("lib", libs);
        return f;
    }

    public List<String> RemoveDupeLibs() {
        Set<String> nlibs = new HashSet<>();

        for (int i = 0; i < this.libs.size(); i++) {
            nlibs.add(this.libs.get(i).strip());
        }
        List<String> newLibs = new ArrayList<String>(0);
        // System.out.println(nlibs.size());
        newLibs.addAll(Arrays.asList(nlibs.toArray(new String[0])));
        return newLibs;
    }

    public Dictionary<String, List<String>> SeperateNative(List<String> tlibs) {
        List<String> libss = new ArrayList<>();
        List<String> MacNatives = new ArrayList<>();
//        List<String> WindowsNatives = new ArrayList<>();
        List<String> LinuxNatives = new ArrayList<>();
        for (int i = 0; i < tlibs.size(); i++) {
            String x = tlibs.get(i);
            if (x.contains("-macos") || x.contains(("-osx"))) {
                MacNatives.add(x);
            } else if (x.contains("-windows") && !x.contains("x32") && !x.contains("arm")) {
//                WindowsNatives.add(x);
                winNatives.add(x);
            } else if (x.contains("-linux")) {
                LinuxNatives.add(x);
            } else if (!x.contains("-natives")) {
                libss.add(x);
            }
        }
        Dictionary<String, List<String>> z =  new Hashtable<>();
        z.put("libs", libss);
        z.put("mac", MacNatives);
//        z.put("win", WindowsNatives);
        z.put("win", winNatives);
        z.put("lin", LinuxNatives);
        return z;
    }

    public List<String> RemoveMacItems(List<String> tlibs, List<String> mac, List<String> win) {
        List<String> libz = new ArrayList<>();
        List<String> remove = new ArrayList<>();
        for(int i = 0; i < mac.toArray(new String[0]).length; i++) {
            String x = mac.toArray(new String[0])[i];
            if (!win.contains(x.replace("osx", "windows").replace("macos", "windows"))) {
                remove.add(x.replace("-natives-osx", "").replace("-natives-macos", ""));
            }
        }

        for(int i = 0; i < tlibs.toArray(new String[0]).length; i++) {
            String x = tlibs.toArray(new String[0])[i];
            if (!remove.contains(x)) {
                libz.add(x);
            }
        }
        return libz;
    }

    public List<String> RemoveLinuxItems(List<String> tlibs, List<String> lin, List<String> win) {
        List<String> libz = new ArrayList<>();
        List<String> remove = new ArrayList<>();
        for(int i = 0; i < lin.toArray(new String[0]).length; i++) {
            String x = lin.toArray(new String[0])[i];
            if (!win.contains(x.replace("linux", "windows"))) {
                remove.add(x.replace("-natives-linux", ""));
            }
        }

        for(int i = 0; i < tlibs.toArray(new String[0]).length; i++) {
            String x = tlibs.toArray(new String[0])[i];
            if (!remove.contains(x)) {
                libz.add(x);
            }
        }
        return libz;
    }
}
