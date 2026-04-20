# Low Level Keyboard Hook

The project was created to see how one could create a tool that could aid in learning what you've just done.

Started off with how I'd might want to get feedback if I lost my vision, although only got to the point of tracking 
activity using low-level hooks in Windows, with some attempts at Linux support.

## Requirements

* Windows (last tested on 10)
* Java 25 (via `~/.m2/toolchains.xml`) see https://maven.apache.org/guides/mini/guide-using-toolchains.html

## Running

Due to what JNA is doing the JVM option needs `--enable-native-access=com.sun.jna` defining.

Entry points for running the code:
* [LinuxKeyLogger.java](src/main/java/uk/co/baconi/keylogger/LinuxKeyLogger.java)
* [WindowsKeyLogger.java](src/main/java/uk/co/baconi/keylogger/WindowsKeyLogger.java)
* [WindowsMouseLogger.java](src/main/java/uk/co/baconi/keylogger/WindowsMouseLogger.java)
