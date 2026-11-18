Testing of valhalla

Important: Please make sure  you are using Java 28 build 10 or later
For maven please use latest version (tested with 3.9.16)

confirm you are on latest maven and use Java 28 build 10 or later by running ```mvn -version```

Step 1: build by doing mvn clean verify

Step 2 : Test non valhalla version by running script ./run-main.sh

Step 3 : Test valhalla version by running script ./run-main-valhalla.sh

## Results: `run-main.sh` vs `run-main-valhalla.sh`

Both programs build an array of 100,000,000 `Point3D` (three `float` fields), call `System.gc()`,
then print the heap in use and the elapsed time. "MB" in their output is really MiB (bytes / 1024²).

Measured on Windows 11, 32 GB RAM, OpenJDK `28-ea+24-1988`, default settings
(G1, max heap 8 GB, compact object headers on by default), 5 runs each:

| | `run-main.sh` (`Main`) | `run-main-valhalla.sh` (`MainValhalla`) |
|---|---|---|
| Heap in use after GC | 2,673.6 – 2,674.3 MB | 384.6 MB |
| Total time | 1,515 – 1,722 ms (median 1,676 ms) | 755 – 886 ms (median 799 ms) |

At first sight Valhalla uses 7× less memory and is 2× faster. **That is not caused by flattening**:
the Valhalla array in this program is not flat, and the program does far less work. Details below.

### Why `Main` uses about 2.6 GB

`Main` creates 100 million separate objects plus an array of references to them:

| Part | Size |
|---|---|
| Each `Point3D` object: 8 B header + 3 × 4 B floats = 20 B, padded to 24 B | 100M × 24 B = 2,400,000,000 B |
| `Point3D[]`: 100M compressed references of 4 B | 400,000,016 B |
| **Total** | **≈ 2,800,000,000 B = 2,670 MiB** (the rest is normal JDK overhead) |

### Why `MainValhalla` shows only 384.6 MB

1. **The array is not flattened.** Adding `ValueClass.isFlatArray(myArray)` prints `false`
   (and `isAtomicArray` prints `true`). The code asks for a *non-atomic* array, but `Point3D` is not
   annotated `@LooselyConsistentValue`, so the JVM creates an atomic array instead, and the 12 bytes of
   fields are then not flattened. The array holds 4-byte references, like a normal `Point3D[]`.
2. **Every element points to the same object.** `newNullRestrictedNonAtomicArray(..., new Point3D(0f, 0f, 0f))`
   fills all 100M slots with the one initial value, and there is no loop creating new points.
   So the heap holds 100M × 4 B references = 381.5 MiB plus a single `Point3D`: the 384.6 MB printed.
3. **It is faster because it allocates almost nothing.** `Main` allocates 100M objects and the GC must
   then trace all of them; `MainValhalla` only fills an array with copies of one reference.

### Like-for-like comparison

To compare fairly, these variants of `MainValhalla` were measured with the same JDK and settings
(3 runs each; the source in this repo is unchanged):

| Variant | Flat array? | Heap after GC | Total time |
|---|---|---|---|
| `Main` (identity class, one `new` per element) | – | 2,674 MB | 1,515 – 1,722 ms |
| `MainValhalla` as in this repo | no | 384.6 MB | 755 – 796 ms |
| `MainValhalla` + loop storing a `new Point3D` in every element | no | 2,674 MB | 2,677 – 2,708 ms |
| `MainValhalla` + `@LooselyConsistentValue` | **yes** | **1,528.6 MB** | 2,855 – 2,863 ms |
| `MainValhalla` + `@LooselyConsistentValue` + loop storing a `new Point3D` in every element | **yes** | **1,528.6 MB** | 3,057 – 3,076 ms |

With `@LooselyConsistentValue` the array really is flat: each element is stored inline in 16 bytes
(12 B of floats, padded), with no object header and no reference. A heap histogram (10M elements) shows a single
`Point3D[]` of 160,000,016 B, labelled `[flat, null-restricted, non-atomic]`, and **no** `Point3D` objects at all, even after storing new points
in every element. That is **43% less memory** than `Main` for the same 100M distinct points.

### Where the time goes

Timing the steps separately (2 runs each):

| | Create the array (and objects) | `System.gc()` |
|---|---|---|
| `Main`: `new Point3D[]` + 100M `new Point3D(...)` | ≈ 940 ms | ≈ 470 ms |
| Flat array (`@LooselyConsistentValue`) | ≈ 2,870 ms | ≈ 10 ms |
| `MainValhalla` as in this repo (reference array) | ≈ 450 ms | ≈ 95 ms |

* The flat array almost removes GC work: there are no objects and no references for the GC to trace.
* Creating the flat array is the slow part: initialising 100M flat elements took about 2.9 s in this
  early-access build, which is why the flat variants are slower overall than `Main` in this test.

### How to get the flattened layout

Annotate the value class in `MainValhalla.java`:

```java
@jdk.internal.vm.annotation.LooselyConsistentValue
static value class Point3D { ... }
```

The required `--add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED` is already in `pom.xml`
and `run-main-valhalla.sh`. Check the result with `ValueClass.isFlatArray(myArray)`, or with the
diagnostic flags commented out in `run-main-valhalla.sh`
(`-XX:+UnlockDiagnosticVMOptions -XX:+PrintFlatArrayLayout`).
