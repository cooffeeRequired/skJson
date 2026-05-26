# 📊 Performance Summary

### [spark](https://spark.lucko.me/Kx2TobQQLi)
### [-> download performance skript](https://raw.githubusercontent.com/cooffeeRequired/skJson/refs/heads/main/performance.sk)
![image](https://github.com/user-attachments/assets/19b77a9b-e793-4b7f-b60f-a38cfc226831)


## 🔹 Low Impact (≤ 2x metadata)
These entries have minimal performance overhead:

- **metadata**
- **local variables** (1.39×)
- **headless** (1.74×)
- **memory variables (before pr)** (2.18×)

_Read/write times: 0–0.01 µs/op_

---

## 🟡 Moderate Impact (2–10× metadata)
Slight performance cost, acceptable in most cases:

- **skjson-cache-value_value_key_changer-variable** (3.28×)
- **skjson-value_value_key_changer-variable** (3.96×)
- **skjson-cache** (6.42×)
- **skjson-literal** (7.52×)
- **memory variables (player index)** (7.8×)

_Read/write times: 0.01–0.03 µs/op_

### 5.5 optimizations
- Path token cache is bounded (1024 entries) and avoids redundant parsing.
- JSON path existence checks no longer emit warnings when probing missing keys.
- `getAsParsedArray` uses fixed-size arrays instead of growing lists.
- File watcher compares compact JSON snapshots to skip duplicate reload work.

### 5.6 optimizations
- `Parser.fromJson` uses a fast path for JSON primitives (no Gson round-trip).
- `Parser.toJson` returns existing `JsonElement` instances and fast-paths numbers/booleans.
- Path reads use `resolve` / `resolveParsed` with integer index parsing for array keys.
- `handle` read mode delegates to `navigate` (fixes array index reads).
- Object key pickers (`last` / `random` / indexed) avoid `entrySet().toArray()` allocations.
- File writes reuse the shared Gson instance from `Parser`.
- Watcher snapshot compare uses `hashCode` before full string equality.

---

## 🔴 High Impact (10–50× metadata)
These entries show noticeable overhead:

- **nbt (chunk)** — 13.5×
  - Read: 0.05 µs/op
  - Write: 0.04 µs/op
- **nbt (item)** — 36.6×
  - Read: 0.06 µs/op
  - Write: 0.09 µs/op
- **nbt (cow)** — 44.33×
  - Read: 0.16 µs/op
  - Write: 0.15 µs/op
