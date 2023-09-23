import os
import shutil; src = set(); import subprocess; import zipfile
for root, dirs, files in os.walk("src"):
    for file in files:
        src.add(os.path.join("Compiled"+root, file))
        if not os.path.exists("Compiled"+root):
            os.makedirs("Compiled"+root)
        fr = open(os.path.join(root, file), "rb")
        f = open(os.path.join("Compiled"+root, file), "wb")
        f.write(fr.read())
        f.close()
        fr.close()
JDK = 'E:/Program Files/Java/jdk-20/bin'
# JDK = 'C:/Program Files/Java/jdk-20/bin'
print(os.path.join(JDK, "javac.exe")); args = [os.path.join(JDK, "javac.exe"), "-cp", "libs/gson-2.10.1.jar"]; args.extend(src); subprocess.call(args)
with zipfile.ZipFile("libs/gson-2.10.1.jar", "r") as f:
    f.extractall("Compiledsrc/")
    f.close()

for root, dirs, files in os.walk("Compilesrc/META-INF"):
    for file in files:
        try:
            os.remove(os.path.join(root, file))
        except:
            print(os.path.join(root, file))
    for dirr in dirs:
        try:
            os.chmod(os.path.join(root, dirr), 0o777)
            shutil.rmtree(os.path.join(root, dirr), ignore_errors=True)

        except:
            print("q", os.path.realpath(os.path.join(root, dirr)))
    shutil.rmtree("Compilesrc/META-INF", ignore_errors=True)

os.chdir("Compiledsrc")
print(os.path.join(JDK, "jar.exe")); args = [os.path.join(JDK, "jar.exe"), "cfe", "../bin/Launcher.jar", "org.zombii.main.Main", "org", "com"]; subprocess.call(args)
os.chdir("../")
shutil.rmtree("Compiledsrc", ignore_errors=True)
os.chdir("bin")
os.system("java -jar Launcher.jar")