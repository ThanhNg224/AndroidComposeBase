# Compose Design System

The reusable Compose theme and components are in `:core:ui`, under `com.thanhng224.androidcomposebase.core.ui`. App-specific screens should use these when they fit and keep feature-specific behavior in `:app`.

## Theme

`AppRoot` wraps app content with `AndroidComposeBaseTheme`. For another host, wrap its Compose content with the theme. It maps the shared `:core` color resources to Material 3 color roles, supports an explicit light/dark choice, and uses dynamic color on supported Android versions when enabled. Read colors, typography, and shapes through `MaterialTheme` rather than duplicating theme values. The static (non-dynamic) palette defines the Material 3 surface container roles (`surfaceContainerLowest` through `surfaceContainerHighest`) from the same neutral palette for light and dark, so container surfaces step visibly above `background`; left unset they would fall back to the Material baseline colors.

The app persists the Light/Dark/System choice in DataStore. On Android 12+ it applies the app-specific platform night mode (including clearing the app override for System), while older versions use AppCompat. Startup gives the persisted-theme read at most two seconds of splash time; a later result still applies. This is a bounded wait, not a measured startup target. Dynamic Material 3 color remains supported. The platform and locale persistence policy is described in [Core modules](CORE_MODULES.md#theme-and-locale-behavior).

## Tokens

Use `Dimens` for shared spacing, touch, corner, and elevation values. Existing names include `spaceSmall`, `spaceMedium`, `spaceLarge`, `spaceXLarge`, `minTouchTarget`, `buttonHeight`, `radiusMedium`, and `elevationLow`. `Dimens` also carries width caps for large-screen content columns: `maxContentWidth` (720.dp, a screen's main content column, e.g. Home, Settings, Demo, DesignSystem), `maxReadableWidth` (560.dp, a narrow reading/CTA column, e.g. Onboarding), and `maxNavBarWidth` (600.dp, the floating top-level navigation bar). Icon sizes are tokens too: `iconSizeSmall` (18.dp, an icon inside a button or dense control), `iconSizeMedium` (24.dp, standard icons such as navigation items, list leading icons, and inline metrics), and `iconSizeLarge` (48.dp, the illustrative icon of an empty/error state). The complete source of truth is [Dimens.kt](../core/ui/src/main/java/com/thanhng224/androidcomposebase/core/ui/theme/Dimens.kt).

Keep user-facing text in localized Android string resources, preserve a minimum 48dp touch target, and provide meaningful semantics for interactive controls. Prefer `MaterialTheme.colorScheme` and typography over hard-coded presentation values. Allow system font scaling and use scrollable content for screens that can exceed the available height.

## Components

Current reusable public Compose components include:

- `AppPrimaryButton`, `AppSecondaryButton`, and `AppOutlinedButton`: standard buttons with optional composable icon slots, caller-controlled width (fill or wrap), and accessible loading state announcement.
- `AppCard`, the standard content container. It is a Material 3 `Card` on the `surfaceContainer` tonal role with no drop shadow and `MaterialTheme.shapes.large`, so it stays distinct from the background in dark theme, where shadows are invisible. It applies no inner padding: callers pad their content (usually `Dimens.spaceMedium` or `Dimens.spaceLarge`). Pass `onClick` to make the whole card one clickable target.
- `AppTopBar` and `AppCenterTopBar`: top app bars supporting both simple string titles and custom composable title/navigation/actions slots.
- `AppLoadingState`, `AppEmptyState`, and `AppErrorState`: full-bleed screen/section placeholders. Each centers its content with `Dimens.spaceLarge` padding and reads colors from `MaterialTheme.colorScheme` only; `AppLoadingState`'s message is a polite live region, and `AppErrorState`'s `onRetry` renders an `AppPrimaryButton` labelled with `core_ui_retry`.
- `AppDialog`: modal dialog surface container enforcing a 560dp maximum width, 24dp horizontal margins, `surfaceContainerHigh` tonal elevation, and `shapes.extraLarge` shape. Caller supplies inner content and scroll/action layouts. Material 3 `AlertDialog` is used directly for standard confirmation/action alerts.
- `AppModalBottomSheet`: bottom sheet wrapper with caller-owned visibility, private composition-managed `SheetState`, cancellation-safe hide mechanics, and `dismissEnabled` handling.
- `AppSwitchRow`, `AppCheckboxRow`, and `AppRadioRow`: selection rows with full 48dp single-touch-target ergonomics and hoisted selection semantics.
- `AppSnackbarEffect` and `AppSnackbarMessage`: reliable snackbar queue management ensuring tail appends do not cancel the active snackbar and results are acknowledged once.
- `BaseComposeActivity`: base activity extending `AppCompatActivity` for Compose screens requiring AppCompat backwards compatibility (locales, day/night).
- `AndroidComposeBaseTheme`, `AppShapes`, `AppTypography`, and `Dimens`.
- `ComposeView.setThemedContent` for an intentional Compose/View interop boundary.

Import the component from `core.ui.components` (or `core.ui.feedback`/`core.ui.base`) and use its actual parameters. Add a reusable component to `:core:ui` only when its visual behavior is useful across app features and does not depend on app-specific state or resources.

`AppButton`, `AppCard`, `AppTopBar`, `AppStates`, `AppDialog`, `AppModalBottomSheet`, and `AppSelectionRow` each carry private `@Preview` functions (light and dark, via `uiMode`) so Android Studio's preview pane renders them without running the app. Extend that pattern for new components instead of relying only on the sample gallery.

The app shell uses Navigation 3 and adapts between the reusable `AppFloatingNavBar` on compact viewports and Material `NavigationRail` on wide/expanded viewports (width >= 600dp and height >= 480dp), showing navigation while the selected tab is at its root. Each bar item is at least 48dp in both dimensions and exposes `Role.Tab` with its selected state inside a selectable group. Each tab has its own back stack. Screens should consume scaffold padding and safe drawing insets instead of adding a fixed navigation-bar spacer.

## Sample gallery

`sample/designsystem` in `:app` is a live example gallery for the shared components and theme. It is sample code, not a dependency of the design system itself. The initializer's `--clean-samples` option removes that gallery while retaining the app shell and shared theme. Check the Kotlin source when an example and this summary differ.
