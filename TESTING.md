# SkJson — kompletní testování

## 1. Automatické unit testy (CI / lokálně)

```bash
./gradlew shadowJar -Penv=TEST
python3 test_runner.py --configuration=0 --jdk=auto --system=auto --no-interactive
```

Nebo jedním příkazem:

```bash
./scripts/run-full-test.sh
```

Testuje všechny skripty v `src/test/scripts/custom/` proti Skript **2.15.4** + Paper **26.1.2**.

### Co se testuje
| Soubor | Oblast |
|---|---|
| `core.sk` | literály, map, keys |
| `features-5.5.sk` | path, merge, deep copy, aliases |
| `conditions.sk` | type checks, cache, watcher |
| `caching.sk` | virtual storage, bind |
| `new json.sk` | json from file/website/location |
| `util-integration.sk` | util patterns |
| `join-integration.sk` | join/profile patterns |

---

## 2. In-game testy (run server)

```bash
./gradlew shadowJar -Penv=TEST
cp build/libs/skjson.jar run/plugins/skjson.jar
# restart nebo /sk reload all
```

### Funkční test
```
/skjsontest
```
Ověří: parse, path R/W, location serialize, inventory serialize, JSON file create, všechny cache (util + join).

### Full util test suite
```
/fullutil kit          # testovací itemy (enchant, shulker, bundle…)
/fullutil test         # všechny moduly
/fullutil test skjson      # všechny SkJson core testy
/fullutil test paths       # jen path API
/fullutil test http          # všechny HTTP metody
/fullutil test http-post    # jen POST
/fullutil test http-async   # non-blocking + on received http response
/fullutil test inv     # backup/restore inventáře
/fullutil report       # JSON report
```
Vyžaduje permisi `skjson.util.test`.

Testuje: homes, warps, inv backup, caches, config, merge + serializaci enchantů, lore, shulker boxů, bundle (pytlíčků), lektvarů a šípů.

**Známé omezení:** bundle ukládá jen typ/název do JSON, ne obsah uvnitř (shulker `minecraft:container` funguje).

HTTP testy vyžadují `enabled-http: true` v `plugins/SkJson/config.yml` a běžící lokální mock server:

```bash
./scripts/start-http-mock.sh   # http://127.0.0.1:18080
./scripts/stop-http-mock.sh
```

**Console (bez hráče):** na serverové konzoli `fullutil test console` — caches, skjson, http, config, merge + JSON report.

Endpointy: `/get`, `/post`, `/put`, `/patch`, `/delete`, `/products/1` (náhrada httpbin + dummyjson).

**Správná SkJson HTTP syntaxe:**
```
reset query params of {_req}
add "key:value" to query params of {_req}
add "key2:value2" to query params of {_req}
set {_req}'s headers to json from "{'Accept':'application/json','X-Test':'ok'}"
```
Více query params najednou přes `add` (set s čárkou/and je nespolehlivé).

### Performance micro-benchmark
```
/skjsontest bench
```
5000× literal read/write, path read/write (cache), 500× location, 100× inventory serialize.

### Plný benchmark (oficiální)
```
/sk reload performance
/profile
```
Porovnej výsledky s [performance.md](performance.md).

---

## Výsledky (poslední běh)

| Vrstva | Stav | Detail |
|---|---|---|
| Unit testy (`quickTest`) | ✅ 38/38 | Skript 2.15.4 + Paper 26.1.2 + Java 25 |
| Server load (run/) | ✅ | 4 skripty, 0 chyb po opravách util |
| Console self-test | ✅ | `OK: 4 FAIL: 0`, path write 2000× |
| In-game `/skjsontest` | ⏳ manuálně | Vyžaduje připojeného hráče s `skjson.test` |
| `performance.sk` | ⚠️ vypnuto | Přesunuto do `scripts-disabled/` — vyžaduje NBT addon |

### Spuštění celé sady

```bash
./scripts/run-full-test.sh
```

Po deployi na run server:

```
/sk reload all
/skjsontest          # funkční test (7+ kontrol)
/skjsontest bench    # micro-benchmark (5000 iterací)
/skprofile           # JSON profil hráče (dříve /profile)
```

### Manuální checklist util

| Oblast | Příkazy | Permise |
|---|---|---|
| Homes | `/sethome`, `/home`, `/delhome` | `skjson.util.home` |
| Warps | `/setwarp spawn`, `/warp` | `.warp` / `.warp.admin` |
| RTP | `/rtp` | `skjson.util.rtp` |
| TPA | `/tpa`, `/tpaccept`, `/tpdeny` | `skjson.util.tpa` |
| Inventář | `/invbackup 1`, `/invrestore 1` | `skjson.util.inv` |
| GUI | `/skutil` | `skjson.util.gui` |
| Join | reconnect (first/return join) | — |

Pro plný NBT benchmark zkopíruj `scripts-disabled/performance.sk` zpět po instalaci Skript-NBT addonu.

| Operace | Typický dopad |
|---|---|
| literal read/write | ~0.01 µs/op (baseline) |
| path cache R/W | ~0.02 µs/op (3–6× metadata) |
| location serialize | závisí na počtu (viz bench) |
| inventory serialize | vyšší (komplexní objekt) |
| NBT | 10–50× metadata (pokud enabled-nbt) |

Vypni debug v produkci: `run/plugins/SkJson/config.yml` → `debug: false`
