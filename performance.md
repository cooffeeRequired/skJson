# Performance Summary

[Spark profile](https://spark.lucko.me/Kx2TobQQLi) · [Download benchmark skript](https://raw.githubusercontent.com/cooffeeRequired/skJson/refs/heads/main/performance.sk)

![Benchmark chart](https://github.com/user-attachments/assets/19b77a9b-e793-4b7f-b60f-a38cfc226831)

Baseline: plain Skript **metadata** / local variables (~**1×**).

---

## Low impact (~1–2×)

Minimal overhead — safe for hot paths and tick loops:

- local variables (~**1.4×**)
- headless reads (~**1.7×**)
- memory variables (~**2×**)

Typical read/write: **sub‑microsecond** per operation.

---

## Moderate impact (~3–8×)

Noticeable but usually fine for config, joins, and occasional updates:

- path read/write via cache (~**3–4×**)
- virtual json cache (~**6×**)
- json literals (~**7×**)
- player-indexed storage (~**8×**)

Typical read/write: **low microsecond** range.

---

## High impact (~10–30×)

Heavier work — batch or cache when possible:

| Area | Overhead | Notes |
| --- | --- | --- |
| NBT chunk | ~**10×** | enable only when needed |
| NBT item | ~**25×** | LRU cache in `config.yml` |
| NBT entity | ~**30×** | largest tested object |

---

## 6.0 optimizations

Focused on HTTP and JSON hot paths (no API changes required):

- **Query params** — `add "key:value" to query params of {_req}`; parser uses `split(":", 2)` so multi-param URLs stay cheap and correct
- **MOCK requests** — `prepare MOCK request on …` / `execute {_req}` skips the network stack entirely
- **Path reads** — `value at path "…" in {_json}` reuses a bounded path-token cache (size in `config.yml`)
- **Path writes** — `set value at path "…" in {_json} to …` avoids extra work in tight update loops
- **Primitives** — `parse "…" as json` fast-path for numbers/booleans/strings without full Gson round-trips
- **File watcher** — bind/watch compares snapshot hash before full reload
- **Disk writes** — shared JSON serializer instead of one instance per save
- **NBT** — optional LRU cache (`nbt-cache-size` in `config.yml`)

### Earlier releases (still included in 6.0)

- **5.6** — array index paths, object key pickers without bulk allocations, `.jsonc` support, configurable path-token cache
- **5.5** — quieter `has path` / `contains path` probes, compact watcher snapshots, fixed-size array parsing

---

## Tips

- Prefer **`json cache`** + path reads over re-parsing strings every tick.
- Use **`set value at path … in … to …`** for nested updates instead of rebuilding objects.
- Keep **`enabled-nbt: false`** unless you serialize NBT-heavy items or entities.
- Tune **`path-token-cache-size`** and **`nbt-cache-size`** in `plugins/SkJson/config.yml` on busy servers.
