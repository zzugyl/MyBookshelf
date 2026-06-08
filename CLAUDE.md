# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MyBookshelf is an Android app for managing physical books via barcode scanning. Users scan ISBN barcodes, the app fetches metadata from Douban or OpenLibrary APIs, and organizes books with shelves, labels, and reading status. Written in Java, licensed GPLv3.

## Build Commands

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK
./gradlew installDebug         # Install on connected device
./gradlew clean                # Clean build outputs
./gradlew lint                 # Lint (non-blocking, abortOnError=false)
./gradlew test                 # Unit tests (JUnit 4)
./gradlew connectedAndroidTest # Instrumented tests (Espresso, requires device)
```

## Build Prerequisites

Two gitignored files must exist before building:

1. **`server.gradle`** in project root — defines the Douban API proxy URL:
   ```gradle
   ext.douban_server_url = "https://your-server-address"
   ```
   The Douban API is proxied through a self-hosted server ([douban-book-api](https://github.com/acdzh/douban-book-api)).

2. **`apiKeys.properties`** in `app/` directory — contains API keys (placeholder values are checked in by default).

## Architecture

### Modules (defined in `settings.gradle`)

- **`:app`** — Main application. All source under `app/src/main/java/com/smartjinyu/mybookshelf/`
- **`:compressor`** — Forked image compression library (from zetbaitsu/Compressor)

### Data Layer — Singleton "Lab" Pattern

- **`BookLab`** — CRUD for books via SQLite (`bookList.db`). Schema in `database/BookDBSchema`, `BookBaseHelper`, `BookCursorWrapper`.
- **`BookShelfLab`** — Bookshelves persisted as JSON in SharedPreferences (via Gson).
- **`LabelLab`** — Labels persisted as JSON in SharedPreferences (via Gson).

The `Book` model uses UUID as primary key. `BookShelf` and `Label` also use UUIDs. Books reference shelves/labels by ID.

### Book Fetching — Strategy Pattern

`BookFetcher` (abstract) defines `getBookInfo(context, isbn, mode)`. Three implementations:
- **`DoubanFetcher`** — Calls self-hosted Douban proxy via Retrofit
- **`OpenLibraryFetcher`** — Calls openlibrary.org via Retrofit
- **`GoogleBooksFetcher`** — Calls Google Books API via Retrofit

Fetcher callbacks reach Activities by casting `Context` to the specific Activity type (e.g., `((SingleAddActivity) mContext).fetchSucceed(...)`).

### Activities

- **`MainActivity`** — Book list (RecyclerView), navigation drawer (MaterialDrawer), bookshelf spinner, FAB menu, search, multi-select, sorting
- **`SingleAddActivity`** — Single book barcode scan and add
- **`BatchAddActivity`** — Batch scanning with `BatchScanFragment` and `BatchListFragment`
- **`BookEditActivity`** — Edit book details, cover photo, labels
- **`BookDetailActivity`** — View book details (sliding activity)
- **`SettingsActivity`/`SettingsFragment`** — Settings, backup/restore, CSV export
- **`AboutActivity`/`AboutFragment`** — Version info, update check

### Key Libraries

| Library | Purpose |
|---------|---------|
| Retrofit 2.9 + Gson | HTTP client for book APIs |
| MaterialDrawer 6.1.2 | Navigation drawer |
| ZXing barcode scanner 1.9.13 | Barcode scanning |
| clans:FAB 1.6.4 | Floating action button menu |
| Material Dialogs 0.9.6 | Dialogs |
| TinyPinyin 2.0.3 | Chinese Pinyin sorting |
| AppCenter 3.3.1 | Analytics/crash reporting |
| opencsv 3.9 (local JAR) | CSV export |

## Environment

| Item | Value |
|------|-------|
| Gradle | 8.12 |
| Android Gradle Plugin (AGP) | 8.10.0 |
| compileSdk | 36 (Android 16) |
| targetSdk | 36 (Android 16) |
| minSdk | 21 (Android 5.0) |
| Java source/target | 1.8 |
| AndroidX | Yes (Jetifier enabled) |
| Modules | `:app`, `:compressor` |

## Configuration

- Repositories use Aliyun mirrors (China), Google Maven, mavenCentral
- ProGuard exists but `minifyEnabled` is false for release builds
- Localization: English (default) and Chinese (`values-zh/`)
- `android.nonFinalResIds=false` for switch-case compatibility
- `buildFeatures { buildConfig true }` enabled

## Notes

- No tests exist in the `app` module. No CI/CD pipeline is configured.
- `Book` implements `Serializable` and is passed between Activities via Intent extras.
- Book sorting uses Pinyin conversion for Chinese character support.
