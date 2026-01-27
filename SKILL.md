# Moto-Parking Development Guide

## Project Overview

A Kotlin Multiplatform (KMP) mobile app for finding motorcycle parking spots in Taiwan. Supports both Android (Google Maps) and iOS (Apple MapKit).

---

## Architecture

### Module Structure

```
moto-parking/
├── composeApp/          # Compose Multiplatform UI
│   ├── commonMain/      # Shared UI code
│   ├── androidMain/     # Android-specific (Google Maps)
│   └── iosMain/         # iOS-specific (MapKit)
├── shared/              # Business logic & data layer
│   └── commonMain/      # Repositories, data sources, DI
└── iosApp/              # iOS app entry point (SwiftUI)
```

### Key Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `HomeViewModel` | `composeApp/.../viewmodels/` | In-memory search location state |
| `ParkingRepository` | `shared/.../repository/` | Data access layer |
| `MapScreen` | `composeApp/.../screens/` | Platform-specific map implementations |
| `AppModule` | `composeApp/.../di/` | Koin DI for ViewModels |
| `sharedModule` | `shared/.../di/` | Koin DI for repositories |

---

## Search Location Behavior

### Design Decision: In-Memory Only (No Persistence)

Search location is stored in `HomeViewModel` as a `StateFlow`:

```kotlin
class HomeViewModel : ViewModel() {
    private val _searchLocation = MutableStateFlow(SearchLocationState(null, null, null))
    val searchLocation: StateFlow<SearchLocationState> = _searchLocation.asStateFlow()
}
```

**Behavior:**
- Location survives configuration changes and navigation (ViewModel lifecycle)
- Location is cleared when app process dies
- On fresh start, app uses current GPS location

**Rationale:** Users expect to search near their current location when opening the app. Restoring a week-old search location would be confusing.

---

## Known Issues & Fixes

### 1. iOS MiniMap SIGABRT Crash

**Problem:** Unsafe annotation cleanup in `onRelease` callback caused memory corruption.

**Solution:** Remove `onRelease` callback entirely - MKMapView handles its own cleanup.

```kotlin
// DON'T do this:
onRelease = { mapView ->
    mapView.removeAnnotations(mapView.annotations as List<MKAnnotationProtocol>)
}

// DO this: Simply omit onRelease, let MKMapView clean up itself
```

**Location:** `MapScreen.ios.kt`

### 2. Android Duplicate Key Crash in Pagination

**Problem:** API returns overlapping spots during pagination, causing LazyColumn key collision.

**Solution:** Deduplicate by ID when concatenating:

```kotlin
val allSpots = (_uiState.value.spots + moreSpots).distinctBy { it.id }
```

**Location:** `ParkingListViewModel.kt`

### 3. iOS Recenter Button Null Safety

**Problem:** `mapView.userLocation` can be null if location services disabled.

**Solution:** Add null check before accessing coordinates:

```kotlin
val userLocation = mapView.userLocation ?: return@let
```

**Location:** `MapScreen.ios.kt`

---

## Build & Run

### Build Both Platforms

```bash
./gradlew compileKotlinIosSimulatorArm64 compileDebugKotlin
```

### Run Android

```bash
./gradlew installDebug
```

### Run iOS

```bash
cd iosApp
xcodebuild -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build
xcrun simctl install 'iPhone 16 Pro' build/Build/Products/Debug-iphonesimulator/iosApp.app
xcrun simctl launch 'iPhone 16 Pro' com.motoparking.app
```

---

## Dependencies

| Library | Purpose |
|---------|---------|
| Compose Multiplatform | Shared UI |
| Koin | Dependency Injection |
| Ktor | HTTP client |
| Supabase | Backend (PostgREST, Auth) |
| SQLDelight | Local database |
| Google Maps Compose | Android maps |
| MapKit (UIKitView) | iOS maps |

---

## Changelog

### 2026-01-27
- Changed default tab from Map to List when app starts
- Added Compose UI test dependencies for Android unit tests
- Added HomeScreenDefaultTabTest to verify default tab behavior

### 2026-01-24
- Cleaned up unused imports and dead code from map screens
- Removed leftover marker customization code (Bitmap, Canvas, marker size constants)
- Fixed import ordering according to Kotlin conventions

### 2026-01-23
- Added search area feature with in-memory location state
- HomeViewModel manages search location (no persistence)
- "Search this area" button updates search location
- GeoUtils added for coordinate calculations

### Previous
- Fixed iOS MiniMap SIGABRT crash
- Fixed Android duplicate key crash in pagination
- Fixed iOS recenter button null safety
- Implemented Google Sign-In (Android & iOS)
- Added check-in cooldown (24 hours)
