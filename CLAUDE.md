# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**McKookChat** is a Minecraft 1.20.1 Fabric mod that bridges Minecraft server chat with KooK (a Chinese voice/chat platform). It runs a local HTTP/WebSocket server (port 8888) to sync server chat messages to a web interface, and receives messages from a KooK bot endpoint to display in-game.

- Mod ID: `mc-kook-chat`
- Package: `haicheng.mckookchat`
- Minecraft: 1.20.1
- Fabric Loader: 0.18.5
- Fabric API: 0.92.7+1.20.1
- Java: 17
- Build system: Gradle with Fabric Loom 1.15-SNAPSHOT

## Build & Run Commands

```bash
# Build the mod
./gradlew build

# Run the client (for testing)
./gradlew runClient

# Run the server (for testing)
./gradlew runServer

# Generate data (uses Fabric Data Generation API)
./gradlew runDatagen

# Clean build artifacts
./gradlew clean
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture: Split Source Sets

This project uses Fabric Loom's `splitEnvironmentSourceSets()` feature, which separates code into two source sets:

- **`src/main/`** — Common code that runs on both client and server (DedicatedServer entrypoint)
- **`src/client/`** — Client-only code (only loaded in a client environment)

Both source sets are merged into a single mod JAR at build time via the `loom.mods` block in `build.gradle`.

### Entry Points (defined in `fabric.mod.json`)

| Entrypoint       | Class                                      | Source Set | Purpose                          |
|------------------|-------------------------------------------|------------|----------------------------------|
| `main`           | `haicheng.mckookchat.McKookChat`          | main       | ModInitializer — server-side logic |
| `client`         | `haicheng.mckookchat.McKookChatClient`    | client     | ClientModInitializer — client-only |
| `fabric-datagen` | `haicheng.mckookchat.McKookChatDataGenerator` | client | DataGeneratorEntrypoint |

### Mixin Configuration

Two separate mixin config files:
- `src/main/resources/mc-kook-chat.mixins.json` — common mixins
- `src/client/resources/mc-kook-chat.client.mixins.json` — client-only mixins (environment: "client")

## Key Design Decisions

- The mod targets **server-side** primarily — chat interception and the HTTP/WebSocket server should live in `src/main/` since they need to run on dedicated servers.
- Client source set (`src/client/`) is for any client-only rendering, HUD, or display features.
- When adding new dependencies, add them to `build.gradle` under `dependencies`. Use `modImplementation` for other Fabric mods, `implementation` for regular libraries.
- The `fabric.mod.json` uses `${version}` which is substituted from `gradle.properties` at build time via the `processResources` task.

## Gradle Properties (gradle.properties)

All version numbers are centralized here. Update versions in this file, not in build scripts:
- `minecraft_version`, `yarn_mappings`, `loader_version` — Fabric platform versions
- `mod_version` — current mod version
- `maven_group`, `archives_base_name` — publishing coordinates
- `loom_version` — Fabric Loom plugin version
- `fabric_api_version` — Fabric API dependency version
