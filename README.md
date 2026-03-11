# InstaSprite

**InstaSprite** is a simple pixel art app for Android, designed to quickly create and edit pixel artwork. 

---

## Features

- Pixel-level drawing on a customizable grid
- Optimized for Android phones
- Save and load your pixel sprite
- Export sprites to images or .isprite
- Palette customize

---

## Installation

Clone the repository and open it with Android Studio:

```sh
git clone https://github.com/phuocn0302/InstaSprite.git
```

Then:

1. Open the project in **Android Studio**
2. Sync **Gradle** and build the project
3. Run on an emulator or connected Android device

---

## Why Protocol Buffers (Protobuf)?

InstaSprite uses [Protocol Buffers](https://protobuf.dev/) (protobuf) to serialize sprite data. Protobuf solves several critical problems that arise when persisting and transferring pixel-art data:

### The Problem

A pixel-art sprite is a complex, structured object: it contains multiple **layers**, each holding a large array of pixel color values, along with metadata such as layer names, visibility flags, and canvas dimensions. A modest 128×128 sprite with 5 layers stores over 80,000 integers — and canvases can be much larger.

The application needs to:
1. **Persist sprites to disk** in a custom `.isprite` file format that can be saved and reopened.
2. **Cache sprite data** in an Android Proto DataStore for fast, incremental reads/writes while the user is editing.
3. **Keep the format compact** — pixel arrays can easily reach megabytes, so bloated encodings waste storage and slow down I/O on mobile devices.
4. **Evolve the format safely** — new features (e.g., frame animation, opacity per layer) will add fields over time without breaking existing saved files.

### Why Not JSON or Custom Binary?

| Concern | JSON | Custom binary | Protobuf |
|---|---|---|---|
| **Size** | Very large — every integer is encoded as a text string, with delimiters and keys repeated for every element | Compact, but requires manual packing/unpacking code | Compact binary encoding with varint integers and packed repeated fields |
| **Speed** | Slow to parse large arrays; requires full deserialization | Fast, but error-prone to write and maintain | Fast generated parser; zero-copy reads in lite runtime |
| **Type safety** | None at the schema level; runtime errors from malformed data | Depends entirely on hand-written code | Schema-enforced types; generated Kotlin/Java classes with builders and accessors |
| **Schema evolution** | No built-in versioning; adding a field can break older readers | Requires manual offset management and version checks | Fields are identified by number; unknown fields are silently skipped, making forward- and backward-compatible changes easy |
| **Tooling** | Widely supported but not optimized for binary blobs | No standard tooling | First-class Gradle plugin generates code; integrates directly with Android DataStore |

### How InstaSprite Uses Protobuf

The schema is defined in [`app/src/main/proto/sprite_data.proto`](app/src/main/proto/sprite_data.proto):

```protobuf
syntax = "proto3";

message LayerData {
  string id = 1;
  string name = 2;
  bool is_visible = 3;
  bool is_locked = 4;
  repeated int32 pixels = 5;   // ARGB pixel values for every cell in the layer
}

message SpritePixels {
  repeated LayerData layers = 1;
  repeated int32 color_palette = 2;
  int32 width = 3;
  int32 height = 4;
}
```

At build time, the protobuf Gradle plugin generates Kotlin and Java lite classes (`SpritePixels`, `LayerData`) that are used throughout the data layer:

- **File I/O** — `FileRepository` writes a `SpritePixels` message to an output stream when saving a `.isprite` file, and parses one back when loading.
- **DataStore persistence** — `SpritePixelDataSource` stores each sprite's pixel data in a Proto DataStore file (`.pb`), backed by `SpritePixelsSerializer`.
- **Domain mapping** — `SpritePixelsMapper` converts between the domain `Sprite` model and the generated `SpritePixels` protobuf class.

In short, protobuf gives InstaSprite a **compact, type-safe, and evolvable** binary format that is purpose-built for structured data — exactly what is needed to efficiently store and transfer large pixel-art sprites on mobile devices.

---

## Project Structure

```
InstaSprite/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/olaz/instasprite/   # Kotlin source (data, domain, ui layers)
│   │   │   ├── proto/                        # Protobuf schema definitions
│   │   │   └── res/                          # Android resources
│   │   ├── test/                             # Unit tests
│   │   └── androidTest/                      # Instrumented tests
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml                 # Centralized dependency versions
└── README.md
```

---


## Author

Developed by:
[@phuocn0302](https://github.com/phuocn0302)
[@IchihanaYue](https://github.com/IchihanaYue)
[@anproa31](https://github.com/anproa31)
