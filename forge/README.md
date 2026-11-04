# Forge targets

Minecraft1.20.1 lives in `forge/1.20.1` and uses JDK17.
From repository root: `./gradlew.bat -p forge/1.20.1 build`.
Root `./gradlew.bat build` forwards to the same target.

Shared integration source and calibration resources remain in root `src`.
New output: `forge/1.20.1/build/libs`; historical root outputs are retained.
