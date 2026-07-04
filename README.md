<center>

![GitHub release](https://img.shields.io/github/release/SkJsonTeam/skJson?style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues-raw/SkJsonTeam/skJson?style=for-the-badge)
![GitHub closed issues](https://img.shields.io/github/issues-closed-raw/SkJsonTeam/skJson.svg?style=for-the-badge)
![GitHub All Downloads](https://img.shields.io/github/downloads/SkJsonTeam/skJson/total?style=for-the-badge)
[![Discord](https://img.shields.io/discord/425192525091831808.svg?style=for-the-badge)](https://discord.gg/dsZq5Cs9fd)
![License](https://img.shields.io/github/license/SkJsonTeam/skJson?style=for-the-badge)
[![CodeFactor](https://www.codefactor.io/repository/github/cooffeerequired/skjson/badge)](https://www.codefactor.io/repository/github/cooffeerequired/skjson)

</center>

<br />

<center>

[//]: # (<- Header ->)
<p align="center" style="align: center; text-align: center">
<img align="center" style="border-radius: 20px;" alt="SkJson" width="10%" src="https://github.com/user-attachments/assets/1783063e-2910-47db-b3c8-303af31735a7">


<h6 align="center">Addon for handle JSON easily in <b>SkriptLang</b></h6>
<hr>

### 6.0 Highlights
- **HTTP reliability** — robust `key:value` query params (`add "key:value" to query params`), MOCK without network, GET/POST/PUT/PATCH/DELETE/HEAD, async `on received http response`
- **Core JSON** — `set value at path … in … to …`, `remove path` / `delete path`, `.jsonc` files (comments + trailing commas)
- Skript **2.15+** with modern `SyntaxRegistry` APIs; shared HTTP client with configurable timeouts
- Performance: NBT conversion cache, bounded path token cache, fast-path JSON parsing ([`performance.md`](performance.md))

> **Note:** SkJson handles **outgoing** HTTP requests (`execute {_request}` / `on http response`).
> It does **not** use Java `HttpExchange` or incoming web-server syntax like `reply … with …`.
> That comes from addons such as **SkriptWebAPI** — do not confuse the two.

> **Requires Skript 2.15.0 or newer.** SkJson will not load on Skript 2.14 or older.

### Performance comparison
See [`performance.md`](performance.md) for benchmarks.


### Support for servers

| Spigot            | Paper            | Purpur           | Leaf               |
|-------------------|------------------|------------------|--------------------|
| Java 21+          | Java 21+         | Java 21+         | Java 21+           |
| Minecraft 1.16.5+ | Minecraft 1.16.5+| Minecraft 1.16.5+| Minecraft 1.21.1+  |
| Skript 2.15+      | Skript 2.15+     | Skript 2.15+     | Skript 2.15+       |

**CI / reference stack:** Paper **26.2**, Skript **2.15.4**, Java **25**.

</center>

## Recommended Tools

* **[Visual Studio Code](https://code.visualstudio.com/download)**
* **[Skript Extension](https://marketplace.visualstudio.com/items?itemName=JohnHeikens.skript)**


## Where Can I Get Help?

* **[Discord](https://discord.gg/dsZq5Cs9fd)**
* **[SkUnity](https://skunity.com/)**
* **[Email](mailto:admin@coffeerequired.info)**

## What can SkJson do?

* create JSON from strings and other sources — Bukkit objects such as `Location`, `Player`, `Entity`, `Inventory`, and more
* work with `.json` and `.jsonc` files (JSON with comments and trailing commas)
* send **outgoing** HTTP requests and parse JSON responses (not an embedded web server)
* watch JSON files on disk and refresh cache on change (e.g. `ops.json`, config files)
* fast in-memory **`MemoryCache`** and **`VirtualCachedJson`** storage (typical read/write ~100–1000 µs)
* NBT support via **NBT-API** (optional, `enabled-nbt` in config)
* serialize and deserialize most Minecraft-related objects Skript exposes

## Get started

1. Download **`skjson.jar`** from [Releases](https://github.com/cooffeeRequired/skJson/releases) and place it in `<server>/plugins/`
2. Install **Skript 2.15+** first
3. Configure `plugins/SkJson/config.yml` (HTTP, NBT, watchers, cache sizes)

## Wiki & docs

* [**Wiki**](https://github.com/cooffeeRequired/skJson/wiki)
* [**release-6.0.md**](release-6.0.md) — full 6.0.0 release notes

<center>

[<img style="width: 10%; margin-right: 1rem;" src="https://skripthub.net/static/addon/ViewTheDocsButton.png">](https://skripthub.net/docs/?addon=skJson)
[<img style="width: 12%; margin-right: 1rem;" src="https://skunity.com/branding/buttons/get_on_docs_4.png">](https://docs.skunity.com/syntax/search/addon:skjson)
[<img style="width: 5%" src="https://static.spigotmc.org/img/spigot.png">](https://www.spigotmc.org/resources/skjson.106019/)

</center>
