# Zerochan Downloader

A beautiful, robust, and dedicated cross-platform desktop application built for interacting with the Zerochan Image Board. Developed entirely in Kotlin using JetBrains Compose Multiplatform targeting the Java Virtual Machine (JVM).

## Features

- **Blazing Fast Browsing**: Infinite scrolling staggered grid showing dynamic anime art and tags.
- **Advanced Global Search**: Auto-correcting tag searching, dynamic dimensional / color filtering, and type-to-search history caching.
- **Robust Networking Capabilities**: 
  - Dynamic user-agent spoofing out of the box.
  - Rate limiting logic to avoid server blacklisting.
  - Deep-retry loop resolving to uncover and download true full-resolution images, safely bypassing generic Cloudflare 403s.
- **Real-Time Downloads**: A seamless unified visual progress tracking interface powered by a custom OkHttp interceptor and Compose state management flow.
- **Built-in File Library**: Visually view, manage, and explore downloaded images directly within the app natively.
- **GIF Optimization**: Handles animated previews without hanging up network pipelines by offloading fetching and decoding to local temporary caches.

## Technologies Used

- **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (Declarative UI structure)
- **Language**: [Kotlin](https://kotlinlang.org/) (JVM Target)
- **Networking**: [Ktor Client](https://ktor.io/docs/client.html) running the [OkHttp](https://square.github.io/okhttp/) engine for advanced raw socket management.
- **Image Processing/Caching**: [Coil3 Compose](https://coil-kt.github.io/coil/)
- **Serialization**: `kotlinx.serialization`
- **Linting & Best Practices**: Built-in JVM styling utilizing `ktlint`.

## Architecture

This project strictly follows the **MVVM (Model-View-ViewModel)** architectural pattern. 

### Presentation/View Layer
Located in `composeApp/src/jvmMain/kotlin/com/jp319/zerochan/ui/`.
Stateless discrete UI components (`ImageCard`, `SearchBar`, `FilterPanel`) subscribe to unidirectional flows emitted globally from the `GalleryScreen`, maximizing UI reusability. 

### ViewModel Layer
`GalleryViewModel` is the sole source of truth dictating UI state. It handles pagination indices, asynchronous job queue scopes (such as downloading), and state validation natively.

### Data Layer
Located in `composeApp/src/jvmMain/kotlin/com/jp319/zerochan/data/`.
Encapsulates `ZerochanRepository` for centralized Ktor usage, mapping domain properties to generic standard Data Classes (`ZerochanItem`), and abstracting persistent preferences via `ProfileManager`. A custom logger (`com.jp319.zerochan.utils.Logger`) standardizes formatted logging.

## Setup & Execution

To download and run the development instance natively on your target platform:

1. Validate you have JDK 17+ configured locally.
2. Ensure you have cloned the repository.
3. Use Gradle to trigger the standard runtime:

### On macOS / Linux
```bash
./gradlew :composeApp:run
```

### On Windows
```cmd
.\gradlew.bat :composeApp:run
```

> [!NOTE] 
> The application uses **ktlint** for code standardization. Running `./gradlew ktlintCheck` verifies files, and `./gradlew ktlintFormat` resolves syntactical mismatches automatically.

## Maintenance & Contribution

1. **Clean Logging**: Only use `Logger.info(...)`, `Logger.debug(...)`, or `Logger.error(...)`. Direct `println()` calls or log emojis are discouraged for consistency.
2. **KDoc Guidelines**: Provide descriptive block-level KDocs above core interfaces, models, and significant public repository bindings.
3. **Safety Handling**: Be advised that network requests depend heavily on enforcing a 1-second delay and a strict customized User-Agent derived from `ProfileManager`; disabling these risks zerochan IP bans.