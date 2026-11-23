<p align="center">
  <img src="assets/ss.png" alt="Skid Solutions Banner" width="350" />
  <br>
  <font size="6"><b>OpenAAC4</b></font>
  <br><br>
  <a href="https://discord.gg/WYjATa6BrU">
    <img src="https://img.shields.io/badge/Discord-Skid%20Solutions-18181b?style=for-the-badge&logo=discord&logoColor=ffffff&labelColor=09090b" alt="Discord" />
  </a>
  <img src="https://img.shields.io/badge/Version-4.3.7-27272a?style=for-the-badge&labelColor=09090b" alt="Version 4.3.7" />
  <img src="https://img.shields.io/badge/Java-17-3f3f46?style=for-the-badge&logo=openjdk&logoColor=ffffff&labelColor=09090b" alt="Java 17" />
  <img src="https://img.shields.io/badge/Gradle-8.14.3-52525b?style=for-the-badge&logo=gradle&logoColor=ffffff&labelColor=09090b" alt="Gradle 8.14.3" />
</p>

<p align="center">
  <a href="https://discord.gg/WYjATa6BrU">https://discord.gg/WYjATa6BrU</a>
</p>

<p align="center">
  <img src="assets/divider.svg" alt="" width="720" />
</p>

<p align="center">
  <img src="assets/jak.jpg" alt="jak" height="150" />
  <img src="assets/hack.jpg" alt="hack" height="150" />
  <img src="assets/deobf2.png" alt="deobf2" height="150" />
  <img src="assets/deobf.jpg" alt="deobf" height="150" />
</p>

<p align="center">
  <img src="assets/divider.svg" alt="" width="720" />
</p>

This is NOT a Remake or Recreation.


## How to build

### Prerequisites
- **Java Development Kit**: JDK 17 (the Gradle wrapper 8.14.3 does not run on newer JDKs)

### Building OpenAAC4

Run from this folder:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

.\gradlew.bat clean prepareIde build
```

Generated `build/libs/`:
- `AAC4-4.3.7-java17.jar`
- `AAC4-4.3.7-plain.jar`
- `AAC4-4.3.7-sources.jar`

### Usage

Place `build/libs/AAC4-4.3.7-java17.jar` alongside **ProtocolLib** (and optionally **ViaVersion**, **ProtocolSupport**, or **PlaceholderAPI**) in your server's `plugins/` directory.

## Credits

- **konsolas** — Original author of AAC 4.3.7.
