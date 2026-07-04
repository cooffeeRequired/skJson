# SkJson Enhanced Util — příkazy a reference

Skript: `run/plugins/Skript/scripts/skjson-util.sk`  
Konfigurace: `run/plugins/Skript/scripts/skjson/util-config.jsonc`  
Vyžaduje: **Skript 2.15+**, **SkJson 5.6+**

---

## Rychlý přehled

| Oblast | Příkazy |
|---|---|
| Domy | `/sethome`, `/home`, `/delhome` |
| Warpy | `/setwarp`, `/warp`, `/delwarp` |
| RTP | `/rtp` |
| TPA | `/tpa`, `/tpaccept`, `/tpdeny` |
| Inventář | `/invbackup`, `/invrestore`, `/invslots` |
| GUI | `/skutil` |
| Testy | `/skjsontest`, `/skjsontest bench` |
| Full util testy | `/fullutil`, `/fullutil kit`, `/fullutil test …` |

---

## Domy (homes)

Ukládání osobních spawn bodů do `skjson/homes.json`.

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/sethome [název]` | `/createhome` | `skjson.util.home` | Uloží aktuální pozici. Bez argumentu použije výchozí název z configu (`home`). |
| `/home [název]` | `/homes` | `skjson.util.home` | Teleport na home. Bez argumentu otevře GUI se seznamem domů. |
| `/delhome [název]` | `/deletehome` | `skjson.util.home` | Smaže home. |

**Pravidla:**
- Název: pouze `a–z`, `A–Z`, `0–9`, `_`, `-`
- Max domů na hráče: `homes.max_per_player` (výchozí **5**)
- Teleport má warmup (viz [Teleport](#teleport))

**Příklady:**
```
/sethome
/sethome baze
/home baze
/home
/delhome baze
```

---

## Warpy (server)

Globální body na mapě v `skjson/warps.json`.

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/setwarp <název>` | — | `skjson.util.warp.admin` | Vytvoří / přepíše warp na aktuální pozici. |
| `/delwarp <název>` | — | `skjson.util.warp.admin` | Smaže warp. |
| `/warp [název]` | `/warps` | `skjson.util.warp` | Teleport na warp. Bez argumentu otevře GUI. |

**Příklady:**
```
/setwarp spawn
/warp spawn
/warp
/delwarp spawn
```

---

## RTP (náhodný teleport)

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/rtp` | `/randomtp` | `skjson.util.rtp` | Náhodný teleport v okruhu kolem spawnu. |

**Chování:**
- Svět, radius a počet pokusů z configu (`rtp.world`, `rtp.radius`, `rtp.attempts`)
- Hledá nejvyšší solidní blok, teleportuje o 1 blok nad něj
- Cooldown: `rtp.cooldown_seconds` (výchozí **120 s**)
- Lze vypnout: `rtp.enabled: false`

---

## TPA (teleport k hráči)

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/tpa <hráč>` | — | `skjson.util.tpa` | Pošle žádost o teleport k cílovému hráči. |
| `/tpaccept` | `/tpyes` | `skjson.util.tpa` | Přijme žádost — **žadatel** se teleportuje k tobě. |
| `/tpdeny` | `/tpno` | `skjson.util.tpa` | Odmítne žádost. |

**Pravidla:**
- Nelze poslat sám sobě
- Žádost vyprší po `teleport.tpa_expire_seconds` (výchozí **60 s**)
- Teleport žadatele má warmup

**Příklad:**
```
/tpa Steve
/tpaccept    # Steve přijme
/tpdeny      # Steve odmítne
```

---

## Inventář (JSON backup)

Perzistentní zálohy inventáře v `skjson/inventories.json`.

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/invbackup [slot]` | `/saveinv`, `/backupinv` | `skjson.util.inv` | Uloží inventář do slotu (1–N). Výchozí slot **1**. |
| `/invrestore [slot]` | `/loadinv`, `/restoreinv` | `skjson.util.inv` | Obnoví inventář ze slotu. |
| `/invslots` | — | `skjson.util.inv` | Otevře GUI se sloty (klik = backup / restore). |

**Limity:** max slotů = `inventories.max_slots_per_player` (výchozí **3**)

**Příklady:**
```
/invbackup
/invbackup 2
/invrestore 1
/invslots
```

---

## GUI

| Příkaz | Alias | Permise | Popis |
|---|---|---|---|
| `/skutil` | `/util`, `/skjsongui` | `skjson.util.gui` | Hlavní menu Util systému. |

**Hlavní menu (`/skutil`):**

| Slot | Akce |
|---|---|
| Kompas (11) | Otevře `/home` GUI |
| Ender pearl (13) | Otevře `/warp` GUI |
| Tráva (15) | Spustí `/rtp` |
| Truhla (17) | Otevře `/invslots` |
| Bariéra (26) | Zavře menu |

**Home / Warp GUI:** klik na položku = teleport na daný bod.  
**Inv GUI:** prázdný slot = backup, uložený slot = restore.

---

## Teleport (společné chování)

Všechny teleporty (`/home`, `/warp`, `/rtp`, TPA) používají `utilTeleport()`:

| Nastavení | Config klíč | Výchozí |
|---|---|---|
| Warmup | `teleport.warmup_seconds` | 3 s |
| Zrušení při pohybu | `teleport.cancel_on_move` | `true` |

Během warmupu se zobrazí odpočet; pohyb > 0.5 bloku teleport zruší.

---

## Full Util Test Suite (`full-util-test-suite.sk`)

Rozšířené in-game testy util modulů a serializace speciálních itemů (enchanty, lore, shulker, bundle, lektvary…).

| Příkaz | Permise | Popis |
|---|---|---|
| `/fullutil` | `skjson.util.test` | Nápověda |
| `/fullutil kit` | `skjson.util.test` | Dá testovací itemy do hotbaru (sloty 0–8) |
| `/fullutil test` | `skjson.util.test` | Všechny automatické testy (vyžaduje hráče) |
| `/fullutil test console` | `skjson.util.test` | Headless testy z konzole serveru (bez hráče) |
| `/fullutil test <modul>` | `skjson.util.test` | Jeden modul (viz níže) |
| `/fullutil report` | `skjson.util.test` | Uloží JSON report posledního běhu |

**Moduly** (`/fullutil test <modul>`):

| Modul | Co testuje |
|---|---|
| `items` | Všechny item testy najednou |
| `plain` | Obyčejný stack + meč |
| `enchant` | Enchantovaný meč + roundtrip |
| `meta` | Název + lore |
| `shulker` | Shulker box s obsahem (sloty 0, 1, 13) |
| `bundle` | Pytlíček (bundle) — shell + JSON roundtrip; obsah zatím není v JSON |
| `potion` | Lektvary (normální + splash) |
| `arrow` | Tipped arrow |
| `inv` | JSON backup/restore celého inventáře se shulkerem + bundle |
| `homes` | Zápis/čtení/smazání home v `homes.json` |
| `warps` | Zápis/čtení/smazání warpu v `warps.json` |
| `caches` | Všechny JSON cache (util + join) |
| `config` | Klíče v `util-config.jsonc` |
| `rtp` | RTP config (bez teleportu) |
| `merge` | Deep merge + remove path |
| `skjson` | Všechny SkJson core testy |
| `parse` | `parse as json`, typy, empty check |
| `paths` | has/contains path, set/read, path of alias |
| `copy` | Deep copy |
| `shallow` | Shallow merge |
| `literal` | Literální read/write + path loop |
| `location` | Location serialize + cache |
| `playerjson` | Player + inventář serialize |
| `file` | Create/read JSON soubor |
| `virtual` | Virtual cache read/write |
| `compact` | Compact JSON + json size |
| `join` | playersdb probe write/read/cleanup |
| `remove` | remove path + delete path alias |
| `http` | Všechny HTTP testy (vyžaduje síť) |
| `http-get` | GET + query params |
| `http-post` | POST + JSON body |
| `http-put` | PUT + JSON body |
| `http-delete` | DELETE |
| `http-patch` | PATCH + JSON body |
| `http-head` | HEAD (bez těla) |
| `http-mock` | MOCK metoda (non-standard) |
| `http-website` | `json from website` (lokální mock `/products/1`) |
| `http-async` | `execute as non blocking` + event |
| `http-headers` | Custom headers echo (json from multi-header) |

**HTTP syntaxe v testech:** query params přes `add "a:b" to query params`, headers `json from "{'Key':'val'}"`.

| `tpa` | Manuální checklist (vyžaduje 2 hráče) |

Report: `plugins/SkJson/perfomance/fullutil-<yyyy-MM-dd_H-mm>.json`

Alias: `/futil`, `/fullutiltest`

---

## Testování (skjson-test-suite)

| Příkaz | Permise | Popis |
|---|---|---|
| `/skjsontest` | `skjson.test` | Funkční testy SkJson (parse, path, cache, location, inventář). |
| `/skjsontest bench` | `skjson.test` | Micro-benchmark výkonu + uložení JSON reportu. |

Report se ukládá do:
`plugins/SkJson/perfomance/<yyyy-MM-dd_H-mm>.json`

---

## Permise (doporučené nastavení)

```yaml
# Výchozí hráč
skjson.util.home: true
skjson.util.warp: true
skjson.util.rtp: true
skjson.util.tpa: true
skjson.util.inv: true
skjson.util.gui: true

# Admin / moderátor
skjson.util.warp.admin: true

# Testování / dev
skjson.test: true
skjson.util.test: true
skjson.join.admin: true   # /skprofile (join skript)
```

**LuckPerms příklad:**
```
/lp group default permission set skjson.util.home true
/lp group default permission set skjson.util.warp true
/lp group default permission set skjson.util.rtp true
/lp group default permission set skjson.util.tpa true
/lp group default permission set skjson.util.inv true
/lp group default permission set skjson.util.gui true
/lp group admin permission set skjson.util.warp.admin true
```

---

## Konfigurace (`util-config.jsonc`)

| Klíč | Význam | Výchozí |
|---|---|---|
| `enabled` | Zapne/vypne celý util systém | `true` |
| `prefix` | Prefix zpráv v chatu | gradient `[Util]` |
| `homes.max_per_player` | Max domů | `5` |
| `homes.default_name` | Výchozí název home | `home` |
| `rtp.enabled` | RTP zapnuto | `true` |
| `rtp.world` | Svět pro RTP | `world` |
| `rtp.radius` | Radius od (0,0) | `1500` |
| `rtp.cooldown_seconds` | Cooldown RTP | `120` |
| `rtp.attempts` | Pokusy najít místo | `8` |
| `teleport.warmup_seconds` | Warmup teleportu | `3` |
| `teleport.cancel_on_move` | Zrušit při pohybu | `true` |
| `teleport.tpa_expire_seconds` | Platnost TPA | `60` |
| `inventories.max_slots_per_player` | Backup sloty | `3` |
| `gui.marker` | Název hlavního GUI inventáře | `SkJsonUtil` |

Po úpravě configu stačí uložit soubor — SkJson watcher cache automaticky reloadne.

---

## Datové soubory (JSON)

| Soubor | Cache ID | Obsah |
|---|---|---|
| `skjson/util-config.jsonc` | `utilconfig` | Konfigurace |
| `skjson/homes.json` | `homesdb` | Domy hráčů |
| `skjson/warps.json` | `warpsdb` | Server warpy |
| `skjson/inventories.json` | `invdb` | Inventářové zálohy |

Struktura home v JSON:
```json
{
  "homes": {
    "<uuid>": {
      "home": { "...": "org.bukkit.Location" }
    }
  }
}
```

---

## Související skripty

| Skript | Příkaz | Popis |
|---|---|---|
| `skjson-join.sk` | `/skprofile [hráč]` | Zobrazí JSON profil hráče z `players.json` (`skjson.join.admin`) |
| `skjson-test-suite.sk` | `/skjsontest` | Viz [Testování](#testování-skjson-test-suite) |
| `full-util-test-suite.sk` | `/fullutil` | Viz [Full Util Test Suite](#full-util-test-suite-full-util-test-suitesk) |

---

## Instalace / reload

```bash
# Skripty musí být v:
run/plugins/Skript/scripts/skjson-util.sk
run/plugins/Skript/scripts/skjson/

# Na serveru:
/sk reload skjson-util
# nebo
/sk reload all
```

Vypni debug v produkci: `plugins/SkJson/config.yml` → `debug: false`
