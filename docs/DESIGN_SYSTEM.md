# Compose Design System

The reusable Compose theme and components are in `:core:ui`, under `com.thanhng224.androidcomposebase.core.ui`. App-specific screens should use these when they fit and keep feature-specific behavior in `:app`.

## Theme

`AppRoot` wraps app content with `AndroidComposeBaseTheme`. For another host, wrap its Compose content with the theme. It maps the shared `:core` color resources to Material 3 color roles, supports an explicit light/dark choice, and uses dynamic color on supported Android versions when enabled. Read colors, typography, and shapes through `MaterialTheme` rather than duplicating theme values.

## Tokens

Use `Dimens` for shared spacing, touch, corner, and elevation values. Existing names include `spaceSmall`, `spaceMedium`, `spaceLarge`, `spaceXLarge`, `minTouchTarget`, `buttonHeight`, `radiusMedium`, and `elevationLow`. The complete source of truth is [Dimens.kt](../core/ui/src/main/java/com/thanhng224/androidcomposebase/core/ui/theme/Dimens.kt).

Keep user-facing text in localized Android string resources, preserve a minimum 48dp touch target, and provide meaningful semantics for interactive controls. Prefer `MaterialTheme.colorScheme` and typography over hard-coded presentation values. Allow system font scaling and use scrollable content for screens that can exceed the available height.

## Components

Current reusable public Compose components include:

- `AppPrimaryButton`, `AppSecondaryButton`, and `AppOutlinedButton`.
- `AppTopBar` and `AppCenterTopBar`.
- `AndroidComposeBaseTheme`, `AppShapes`, `AppTypography`, and `Dimens`.
- `ComposeView.setThemedContent` for an intentional Compose/View interop boundary.

Import the component from `core.ui.components` and use its actual parameters. Add a reusable component to `:core:ui` only when its visual behavior is useful across app features and does not depend on app-specific state or resources.

Use Material 3 components directly for dialogs and adaptive app navigation. The app shell uses `NavigationSuiteScaffold`, which selects a bottom navigation bar or rail based on available window size. Screens should consume scaffold padding and safe drawing insets instead of adding a fixed navigation-bar spacer.

## Sample gallery

`sample/designsystem` in `:app` is a live example gallery for the shared components and theme. It is sample code, not a dependency of the design system itself. The initializer's `--clean-samples` option removes that gallery while retaining the app shell and shared theme. Check the Kotlin source when an example and this summary differ.
