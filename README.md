# PainterJS

**PainterJS** is a KubeJS addon for Minecraft that allows server administrators and modpack developers to draw custom, dynamic UI elements on players' screens using server-side JavaScript.

From simple info text to complex, animated mana bars or custom HUDs, PainterJS provides the tools to render Rectangles, Text, Items, and Gradients with support for math expressions evaluated client-side for smooth animations.

## Features

-   **Server-Side Control**: Define UI elements in your `server_scripts`.
-   **Client-Side Performance**: Animations (like position, size, color) are handled by math expressions on the client, ensuring smooth rendering without network spam.
-   **Rich Primitives**:
    -   **Rectangles**: Solid colors or textured.
    -   **Text**: Scalable, colored, shadowed, multiline.
    -   **Items**: Render any item stack, with optional overlays.
    -   **Gradients**: 4-corner color gradients.
-   **Alignment**: Easy positioning with `alignX` and `alignY`.

## Installation

1.  Install **[KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs)**.
2.  Download **PainterJS** and place it in your `mods` folder.
3.  Launch the game.

## Usage Example

Create a file in `kubejs/server_scripts/my_hud.js`:

```javascript
PlayerEvents.loggedIn(event => {
    Painter.paint(event.player, {
        welcome_msg: {
            type: 'text',
            text: 'Welcome to the Server!',
            x: '$screenW / 2',
            y: 50,
            alignX: 'center',
            draw: 'gui',
            color: '#00FF00'
        }
    })
})
```

For full documentation, please visit our **[Wiki](wiki/Home.md)**.

## License

All Rights Reserved.
