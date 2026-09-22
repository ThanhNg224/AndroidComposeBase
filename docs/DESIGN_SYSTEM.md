# DESIGN_SYSTEM.md

This document specifies the Jetpack Compose Design System, design tokens, and reusable components shipped with `:core:ui`. The live, interactive showcase is `feature/designsystem/presentation/ui/DesignSystemScreen.kt`.

---

## 🎨 Color Tokens & Theming

Theme definitions live in `core/ui/src/main/java/com/thanhng224/androidcomposebase/core/ui/theme/`.

### 1. Color Palette

The color system is built on Material 3 dynamic and semantic color roles:

- `Primary`: Brand accent used for high-emphasis buttons, active indicators, and links.
- `OnPrimary`: Text/icon color drawn on top of `Primary`.
- `Surface` & `SurfaceVariant`: Card, sheet, and background containers.
- `OnSurface` & `OnSurfaceVariant`: Text and iconography for standard content and muted captions.
- `Error` & `OnError`: Feedback states for network failures and validation errors.

### 2. Adaptive Theming (`AndroidComposeBaseTheme`)

The root theme wraps Compose content with appropriate `ColorScheme` (Light, Dark, or System) and passes down `MaterialTheme`:

```kotlin
@Composable
fun AndroidComposeBaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
```

---

## 📐 Spacing & Dimensions (`Dimens`)

All dimensions follow a strict **8-point grid scale** and touch target accessibility guidelines ($\ge 48\text{dp}$). Defined in `Dimens.kt`:

| Token | Value | Purpose |
|---|---|---|
| `Dimens.SpaceTiny` | `2.dp` | Hairline gaps, micro-offsets |
| `Dimens.SpaceExtraSmall` | `4.dp` | Tight padding within small chips |
| `Dimens.SpaceSmall` | `8.dp` | Standard gap between related elements |
| `Dimens.SpaceMedium` | `16.dp` | Standard screen margin & card padding |
| `Dimens.SpaceLarge` | `24.dp` | Spacing between distinct groups |
| `Dimens.SpaceExtraLarge` | `32.dp` | Spacing between major sections |
| `Dimens.SpaceSection` | `48.dp` | Large section dividers and bottom bar clearance |
| `Dimens.MinTouchTarget` | `48.dp` | Minimum accessible touch target |
| `Dimens.IconSmall` | `16.dp` | Inline icons |
| `Dimens.IconMedium` | `24.dp` | Standard action and navigation icons |
| `Dimens.IconLarge` | `32.dp` | Featured icons, headers |

---

## 🔤 Typography

Defined in `Typography.kt` using standard Material 3 roles:

- **Headline Large / Medium**: Page headers and primary modal titles.
- **Title Large / Medium / Small**: Card headers, list section headers, button labels.
- **Body Large / Medium / Small**: Primary and secondary readable copy.
- **Label Large / Medium / Small**: Captions, timestamps, small tags.

---

## 🧩 Reusable UI Components (`:core:ui`)

All components reside under `com.thanhng224.androidcomposebase.core.ui.component`:

### 1. `FloatingNavBar`

A floating, pill-shaped bottom navigation bar with expressive active pill indicators and tonal elevation:

```kotlin
FloatingNavBar(
    items = navItems,
    currentRoute = currentRoute,
    onItemSelected = { item -> navController.navigate(item.route) },
)
```

- Positioned in the comfortable bottom thumb-zone.
- Minimum 48dp touch target for every item.
- Accommodates window safe insets automatically.

### 2. `AppButton`

Standard button component supporting filled and outlined variants with built-in loading spinner:

```kotlin
AppButton(
    text = "Submit",
    onClick = { viewModel.submit() },
    isLoading = state.isLoading,
    enabled = !state.isLoading,
    modifier = Modifier.fillMaxWidth(),
)
```

### 3. `AppDialog`

Standard Material 3 modal dialog for confirmation, alerts, or single-choice selection lists:

```kotlin
AppDialog(
    title = "Select Theme",
    onDismissRequest = { showDialog = false },
    confirmButton = {
        TextButton(onClick = { showDialog = false }) { Text("OK") }
    },
) {
    // Dialog content or selection radio buttons
}
```

### 4. `AsyncContent`

A state-driven container that unifies loading indicators, empty states, and content presentation:

```kotlin
AsyncContent(
    isLoading = state.isLoading,
    isEmpty = state.items.isEmpty(),
    emptyMessage = "No items found",
    onRefresh = viewModel::refresh,
) {
    ItemList(state.items)
}
```

### 5. `AppTopBar`

Consistent top app bar with navigation back button, title, and action icons:

```kotlin
AppTopBar(
    title = "Settings",
    onNavigateBack = { navController.popBackStack() },
)
```

### 6. `UiText`

Sealed interface for passing strings from ViewModels without leaking Android `Context`:

```kotlin
val message: UiText = UiText.StringResource(R.string.error_network)
// In Composable:
val text = message.asString()
```

---

## 📱 Live Reference

The complete showcase is implemented in `DesignSystemScreen.kt` (`feature/designsystem/presentation/ui/DesignSystemScreen.kt`), displaying every component, color role, typography tier, and dialog in action.
