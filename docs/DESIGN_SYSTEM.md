# Compose Design System

The reusable Compose theme and components are in `:core:ui`, under `com.thanhng224.androidcomposebase.core.ui`. App-specific screens should use these when they fit and keep feature-specific behavior in `:app`.

## Theme

`AppRoot` already wraps app content with `AndroidComposeBaseTheme`. For another host, wrap its Compose content with the theme. It applies Material 3 color scheme, typography, and shapes; dynamic color is used on supported Android versions when enabled. Read semantic colors, typography, and shapes through `MaterialTheme` rather than duplicating theme values.

## Tokens

Use `Dimens` for shared spacing, touch, corner, and elevation values. Existing names include `spaceSmall`, `spaceMedium`, `spaceLarge`, `spaceXLarge`, `minTouchTarget`, `buttonHeight`, `radiusMedium`, and `elevationLow`. The complete source of truth is [Dimens.kt](../core/ui/src/main/java/com/thanhng224/androidcomposebase/core/ui/theme/Dimens.kt).

Keep text in Android string resources, preserve a minimum 48dp touch target, and provide meaningful semantics for interactive controls. Prefer `MaterialTheme.colorScheme` and typography over hard-coded presentation values.

## Components

Current reusable public Compose components include:

- `AppDialog` for app dialogs.
- `AsyncContent` and `AsyncState` for loading, success, and error content.
- `AppPrimaryButton`, `AppSecondaryButton`, and `AppOutlinedButton`.
- `AppTopBar` and `AppCenterTopBar`.
- `FloatingNavBar(items: List<NavItem>)`; each `NavItem` owns its title, icons, selection state, badge count, and click callback.

Import the component from `core.ui.components` and use its actual parameters. Add a reusable component to `:core:ui` only when its visual behavior is useful across app features and does not depend on app-specific state or resources.

## Sample gallery

`sample/designsystem` in `:app` is a live example gallery for the shared components and theme. It is sample code, not a dependency of the design system itself. Check the Kotlin source when an example and this summary differ.
