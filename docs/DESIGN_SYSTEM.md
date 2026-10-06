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

The app shell uses Navigation 3 and adapts between the reusable `AppFloatingNavBar` on compact viewports and Material `NavigationRail` on wide viewports (width >= 600dp and height >= 480dp), showing navigation while the selected tab is at its root. Each tab has its own back stack. Selecting the current tab does nothing. In the floating bar, changing tabs emits one callback and one haptic.

`AppFloatingNavBar` accepts caller-owned selection, stable unique item IDs, colors, and placement. Give it a bounded width with at least 48dp per tab plus its 16dp internal horizontal padding; the app also supplies 16dp outer margins. Below that width the touch targets shrink evenly rather than failing layout. The row reserves the minimum touch widths and visible badge space first and constrains the combined expansion of outgoing and incoming labels to the remaining width. Labels use one line and ellipsis without reducing system font scaling. When space is tight, inner label spacing contracts while touch targets stay at least 48dp, until the row itself is narrower than that. Badges stay inside the capsule. Item order and placement support RTL.

Each item exposes one named `Role.Tab` node, selected state, and click action inside a selectable group. Its full label remains accessible when visually truncated; a caller-provided `contentDescription` overrides that name. Badge values above 99 display `99+`, while the accessible name appends the localized actual count (for example "Inbox, 3 notifications"); the platform's selected announcement is left intact. Badge contents do not create separate click targets or repeat the tab name.

The compact app shell measures the complete navigation slot, including margins and system navigation inset, before composing content in the same layout pass. It supplies and consumes that bottom padding once. When the slot is empty, navigation padding is zero and screens continue handling their own safe drawing insets. Screens should consume scaffold padding and safe drawing insets instead of adding a fixed navigation-bar spacer. System animation scale, including zero, controls navigation motion.

Navigation instrumentation covers constrained widths, large fonts, RTL, interrupted motion, callbacks, semantics, and saved state. The `NavigationBenchmark` in `:baselineprofile` measures warm Home–Settings and Home–Design transitions separately from startup. It uses externally controlled compilation and `CompilationMode.Ignore()` to preserve application data; benchmark results describe the complete screen transition, not an isolated component.

## Sample gallery

`sample/designsystem` in `:app` is a live example gallery for the shared components and theme. It is sample code, not a dependency of the design system itself. The initializer's `--clean-samples` option removes that gallery while retaining the app shell and shared theme. Check the Kotlin source when an example and this summary differ.
