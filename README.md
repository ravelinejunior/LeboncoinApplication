# Leboncoin Android Technical Test

This repository contains a production-grade Android application developed for Leboncoin’s technical
assessment. The app fetches album data from a remote JSON endpoint, displays each album’s title and
image, and ensures complete offline functionality through local persistence.

## Table of Contents

1. [Overview](#overview)
2. [Architecture and Design Patterns](#architecture-and-design-patterns)
3. [Data Flow and Layering](#data-flow-and-layering)
4. [Offline Persistence](#offline-persistence)
5. [Networking and User-Agent Header](#networking-and-user-agent-header)
6. [User Interface with Jetpack Compose](#user-interface-with-jetpack-compose)
7. [Dependency Injection (Hilt)](#dependency-injection-hilt)
8. [Network Monitoring](#network-monitoring)
9. [Testing Strategy](#testing-strategy)
10. [Running the Application](#running-the-application)
11. [Rationale for Key Decisions](#rationale-for-key-decisions)

---

## Overview

This Android application targets **API Level 24 and above** and implements the following core
requirements:

* **Remote Data Retrieval**: Fetches album data from
  `https://static.leboncoin.fr/img/shared/technical-test.json` at runtime.
* **User-Agent Header**: Attaches `User-Agent: LeboncoinApp/1.0` exclusively to image requests.
* **Offline Capabilities**: Persists data locally using Room to support offline usage and maintain
  data across app restarts.
* **Responsive UI**: Offers both **list** and **grid** layouts with smooth animations.
* **Configuration Resilience**: Retains UI state (layout type and scroll position) across
  configuration changes.
* **Automatic Refresh**: Detects network availability and refreshes data upon reconnection.

---

## Architecture and Design Patterns

The application is structured following a simplified **Clean Architecture** approach:

* **Presentation Layer**: Composable UI components orchestrated by **ViewModels** (MVVM pattern).
* **Domain Layer**: Business logic encapsulated in **Use Cases** (e.g., `GetAlbumsUseCase`).
* **Data Layer**: Implements **Repository Pattern** combining local (`AlbumDao`) and remote (
  `AlbumApi`) sources.
* **Dependency Injection**: Managed by **Hilt** to decouple components and facilitate testing.

This design ensures separation of concerns, modularity, and high testability.

---

## Data Flow and Layering

1. **ViewModel** invokes `GetAlbumsUseCase(forceRefresh: Boolean)`.
2. **Use Case** calls `AlbumRepository.getAlbums(forceRefresh)`.
3. **Repository**:

    * Queries local cache via `AlbumDao.getAll()`.
    * If empty or `forceRefresh == true`, fetches from `AlbumApi.getAlbums()`, maps DTOs to
      entities, and persists via `insertAll()`.
    * Returns a single source of truth (`List<AlbumEntity>`).
4. **ViewModel** updates `AlbumsUiState`, triggering recomposition in Compose.

---

## Offline Persistence

* **Room** is used to define the local data store:

    * `AlbumEntity` models the table structure.
    * `AlbumDao` provides `getAll()` and `insertAll()` with `OnConflictStrategy.REPLACE`.
    * Database instantiated as a singleton via Hilt’s `DatabaseModule`.

**Reasoning**: Room offers a robust, type-safe, and coroutine-friendly API, guaranteeing consistent
and performant offline data access.

---

## Networking and User-Agent Header

* **Retrofit** handles JSON data retrieval.
* **Coil** manages image loading, with custom headers:

  ```kotlin
  ImageRequest.Builder(context)
    .data(imageUrl)
    .httpHeaders(NetworkHeaders.Builder()
      .add("User-Agent", "LeboncoinApp/1.0")
      .build()
    )
    .build()
  ```

**Reasoning**: Splitting JSON and image requests ensures headers only apply where required,
improving network efficiency and adhering to the technical requirement.

---

## User Interface with Jetpack Compose

* **Composable Screens**:

    * `AlbumsScreen`: Displays a toggleable list/grid of albums with animated icon transitions.
    * `AlbumDetailScreen`: Shows detailed view with full-size image and metadata.
* **State Management**:

    * Layout preference and scroll position are saved using `SavedStateHandle` and
      `rememberSaveable`.
* **Performance**:

    * Lists keyed by `album.id` to optimize recomposition.
    * Vector placeholders ensure rapid feedback while images load.

**Reasoning**: Jetpack Compose delivers a declarative and highly performant UI, reducing boilerplate
and facilitating rapid iterations.

---

## Dependency Injection (Hilt)

* **NetworkModule**: Provides `OkHttpClient`, `Retrofit`, and `AlbumApi`.
* **DatabaseModule**: Provides `AppDatabase` and `AlbumDao`.
* **Repository and Use Cases**: Injected wherever needed.

**Reasoning**: Hilt simplifies component scoping, lifecycle management, and enables easy swapping of
implementations for testing.

---

## Network Monitoring

* **NetworkMonitor**: Exposes a `Flow<Boolean>` reflecting connectivity changes via
  `ConnectivityManager`
* **Auto-Refresh**: `AlbumsViewModel` observes this flow and triggers
  `refreshAlbums(forceRefresh = true)` upon reconnection.

**Reasoning**: Enhances user experience by seamlessly updating data when connectivity resumes.

---

## Testing Strategy

* **Unit Tests**:

    * `AlbumRepositoryTest`: Mocks `AlbumApi` and `AlbumDao` to validate caching logic, error
      handling, and force-refresh behavior.
    * `GetAlbumsUseCaseTest`: Confirms correct orchestration of repository calls.
    * `AlbumsViewModelTest`: Simulates network availability and refresh behavior.
    * `AlbumDetailViewModelTest`: Verifies error handling and non-integer albumId handling.

* **Instrumented Tests**:

    * End-to-end scenarios with fake image loader and Hilt test components.

**Reasoning**: A comprehensive test suite ensures reliability, prevents regressions, and documents
expected behaviors.

---

## Rationale for Key Decisions

| Component       | Choice                               | Reasoning                                                                                                                   |
|-----------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Local Storage   | Room                                 | Type-safe SQL integration, coroutine support, compile-time verification, robust offline caching.                            |
| JSON Networking | Retrofit + Gson                      | Mature, extensible, seamless coroutine integration, clear separation of concerns.                                           |
| Image Loading   | Coil                                 | Native Compose compatibility, flexible header configuration, built-in caching, lightweight footprint.                       |
| DI Framework    | Hilt                                 | Simplifies setup over Dagger, integrates with Android lifecycles, promotes testability via component injection.             |
| UI Toolkit      | Jetpack Compose                      | Declarative, reduces boilerplate, programmable animations, state-driven rendering, future-forward for Android development.  |
| Architecture    | MVVM + Clean Architecture Principles | Enhances maintainability, testability, and scalability by decoupling UI, business logic, and data layers.                   |
| Network Monitor | ConnectivityManager + Kotlin Flow    | Provides reactive updates, minimal boilerplate, ensures app remains in sync with network state for optimal user experience. |
| Testing         | JUnit4 + Compose Test                | Combination covers unit, UI, and integration levels, ensuring broad coverage and reliability in diverse scenarios.          |

---

<div align="center">
  Developed by a Franklin Junior.
</div>
