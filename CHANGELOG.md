# Changelog

All notable changes to this project will be documented in this file.

## [1.2.0]

### Added

- **Dynamic Variables (`VariableSet`):** Introduced a powerful new system to manage and sync variables from server to
  client.
- **Improved Color Support:** Support for full ARGB hex colors and dynamic color functions.
- **RGB/HSV Helpers:** New `rgb(r,g,b)` and `hsv(h,s,v)` math functions for easy rainbow and color-cycling effects.
- **Variable Injection in Text:** Use `$var` directly inside text strings to create dynamic labels (e.g., "Mana: $
  mana").
- **Auto-Sync:** Variables now automatically synchronize at the end of every tick, reducing manual script boilerplate.

### Changed

- **Performance Optimizations:** Major internal refactor to reduce CPU and network overhead during rendering.
- **Refined API:** Cleaned up object property handling for better consistency across Rectangle, Gradient, Text, and Item
  types.

### Fixed

- **Improved Error Logging:** Expression compilation errors now properly logged instead of silently printing to stderr.
- **Robust Item Handling:** Invalid item IDs no longer cause crashes; gracefully defaults to empty item.
- **Code Quality:** Extracted common frame variable setup to reduce code duplication.

## [1.1.0]

### Added

- **Client Script Capability:** Allows defining PainterJS logic directly in client scripts.

### Changed

- **Compatibility:** Lowered KubeJS dependency version to `2101.7.2-build.348` for wider modpack compatibility.

## [1.0.0] - Initial Release

### Added

- Port of the old PainterAPI to modern KubeJS and NeoForge.
- Server-side drawing of custom dynamic UI elements.
- Support for Rectangles, Gradients, Text, and Items on the HUD.
