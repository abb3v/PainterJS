# PainterJS Wiki

**PainterJS** is a powerful KubeJS addon that empowers server owners and modpack developers to render custom, dynamic UI elements directly on a player's screen. From simple text overlays to complex, animated HUDs, PainterJS handles it all through server-side scripts.

## Table of Contents
1. [Installation](#installation)
2. [Getting Started](#getting-started)
3. [The `Painter` Object](#the-painter-object)
4. [Coordinate System & Alignment](#coordinate-system--alignment)
5. [Math Expressions & Animations](#math-expressions--animations)
6. [Object Properties Reference](#object-properties-reference)
    - [Common Properties](#common-properties)
    - [Rectangle](#rectangle)
    - [Gradient](#gradient)
    - [Text](#text)
    - [Item](#item)
7. [Advanced Examples](#advanced-examples)

---

## Installation

1.  **Dependencies:** Ensure you have [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs) installed for your Minecraft version.
2.  **Install PainterJS:** Add the PainterJS jar to your `mods` folder.
3.  **Run:** Launch the game once to generate the `kubejs` directory.

---

## Getting Started

PainterJS works by sending "paint objects" to a client. These objects persist on the client's screen until they are explicitly removed or the client disconnects/reloads.

All interaction happens through the global `Painter` object in your KubeJS server scripts (`kubejs/server_scripts`).

### Basic Syntax
```javascript
// Listen for a player event, like joining the world
PlayerEvents.loggedIn(event => {
    // Call the paint method
    Painter.paint(event.player, {
        // Define a unique ID for your element ('example_box')
        example_box: {
            type: 'rectangle',
            x: 10,
            y: 10,
            w: 50,
            h: 50,
            color: '#FF0000',
            draw: 'gui'
        }
    });
});
```

---

## The `Painter` Object

### `Painter.paint(player, data)`
The primary method to add, update, or remove UI elements.

*   **`player`**: The `ServerPlayer` entity (e.g., `event.player`).
*   **`data`**: A JavaScript object (Map) containing the UI elements.
    *   **Keys**: Unique String IDs for the elements. If you use an ID that already exists, the old object is updated/replaced.
    *   **Values**: The object definition (properties).

### Removing Objects
To remove an object, send its ID again with `remove: true`.

```javascript
Painter.paint(event.player, {
    example_box: { remove: true }
});
```

---

## Coordinate System & Alignment

By default, the coordinate system starts at **(0, 0)** in the **Top-Left** corner of the screen.
-   **X** increases to the right.
-   **Y** increases downwards.

### Alignment (`alignX`, `alignY`)
You can change the anchor point of an object relative to the screen using alignment.

| Axis | Values | Effect |
| :--- | :--- | :--- |
| **alignX** | `'left'` (Default) | `x` is offset from the left edge. |
| | `'center'` | `x` is offset from the horizontal center. |
| | `'right'` | `x` is offset from the right edge. |
| **alignY** | `'top'` (Default) | `y` is offset from the top edge. |
| | `'center'` | `y` is offset from the vertical center. |
| | `'bottom'` | `y` is offset from the bottom edge. |

**Example:** Placing text exactly in the middle of the screen:
```javascript
center_text: {
    type: 'text',
    text: 'Middle',
    x: 0, y: 0,
    alignX: 'center', alignY: 'center'
}
```

---

## Math Expressions & Animations

One of PainterJS's most powerful features is **client-side evaluation**. Instead of a static number, you can pass a string expression for almost any numeric property (positions, dimensions, etc.). These expressions are evaluated *every frame* on the client.

### Available Variables
| Variable | Description |
| :--- | :--- |
| `$screenW` | The current width of the screen. |
| `$screenH` | The current height of the screen. |
| `$mouseX` | The current X position of the mouse. |
| `$mouseY` | The current Y position of the mouse. |
| `$delta` | The render partial tick (useful for smooth interpolation). |
| `time` | A standard time variable (seconds) for animations. |

### Available Functions & Constants
*   **Math:** `sin(x)`, `cos(x)`, `tan(x)`, `asin(x)`, `acos(x)`, `atan(x)`, `atan2(y,x)`
*   **Utils:** `sqrt(x)`, `abs(x)`, `min(a,b)`, `max(a,b)`, `pow(a,b)`, `log(x)`
*   **Rounding:** `floor(x)`, `ceil(x)`
*   **Conversion:** `rad(deg)` (degrees to radians), `deg(rad)` (radians to degrees)
*   **Random:** `random()` (returns 0.0 to 1.0)
*   **Constants:** `PI`, `HALF_PI`, `TWO_PI`, `E`, `true` (1), `false` (0)

### Animation Example
A box that pulses in size and follows the mouse horizontally:
```javascript
pulsing_box: {
    type: 'rectangle',
    x: '$mouseX', // Follows mouse X
    y: '$screenH / 2', // Fixed at vertical center
    w: '50 + sin(time * 5) * 10', // Width oscillates between 40 and 60
    h: 50,
    alignX: 'center',
    color: '#00FF00'
}
```

---

## Object Properties Reference

### Common Properties
These apply to **all** object types.

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `type` | String | `rectangle` | `rectangle`, `gradient`, `text`, `item`. |
| `visible` | Bool | `true` | If false, the object is skipped during rendering. |
| `draw` | String | `ingame` | `ingame` (rendered in the world view) or `gui` (rendered on the HUD). |
| `x` | Float/Expr | `0` | Base X position. |
| `y` | Float/Expr | `0` | Base Y position. |
| `z` | Float/Expr | `0` | Z-Index. Higher values render on top of lower values. |
| `w` | Float/Expr | `0` | Width. |
| `h` | Float/Expr | `0` | Height. |
| `alignX` | String | `left` | `left`, `center`, `right`. |
| `alignY` | String | `top` | `top`, `center`, `bottom`. |
| `moveX` | Float/Expr | `0` | Additional X offset (useful for animations added on top of base pos). |
| `moveY` | Float/Expr | `0` | Additional Y offset. |
| `expandW`| Float/Expr | `0` | Additional Width (added to `w`). |
| `expandH`| Float/Expr | `0` | Additional Height (added to `h`). |

---

### Rectangle
`type: 'rectangle'`
Draws a solid color or a textured quad.

| Property | Type | Description |
| :--- | :--- | :--- |
| `color` | String/Int | Hex code (`#RRGGBB` or `#AARRGGBB`) or integer color. Default: White. |
| `texture` | String | Resource path (e.g., `minecraft:textures/block/dirt.png`). |
| `u0`, `v0` | Float | Top-Left UV coordinates (0.0 to 1.0). Default `0.0`. |
| `u1`, `v1` | Float | Bottom-Right UV coordinates (0.0 to 1.0). Default `1.0`. |

---

### Gradient
`type: 'gradient'`
Draws a rectangle with smooth color transitions between corners.

| Property | Type | Description |
| :--- | :--- | :--- |
| `colorTL` | String/Int | Color for Top-Left corner. |
| `colorTR` | String/Int | Color for Top-Right corner. |
| `colorBL` | String/Int | Color for Bottom-Left corner. |
| `colorBR` | String/Int | Color for Bottom-Right corner. |
| `color` | String/Int | Sets ALL corners to this color. |
| `colorT` | String/Int | Sets Top-Left and Top-Right. |
| `colorB` | String/Int | Sets Bottom-Left and Bottom-Right. |
| `colorL` | String/Int | Sets Top-Left and Bottom-Left. |
| `colorR` | String/Int | Sets Top-Right and Bottom-Right. |
| `texture` | String | Resource path to blend with the gradient. |

---

### Text
`type: 'text'`
Draws a string or Minecraft Text Component.

| Property | Type | Description |
| :--- | :--- | :--- |
| `text` | String/Json | The content. Can be a simple string or a Component JSON object (e.g., `{"text":"Hi", "color":"red"}`). |
| `textLines`| Array | List of strings/components for multi-line text. |
| `scale` | Float | Text size multiplier. Default `1.0`. |
| `shadow` | Bool | Draw text shadow. Default `true`. |
| `centered` | Bool | If true, text is centered horizontally around `x`. |
| `lineSpacing`| Float | Space between lines. Default `1.0`. |
| `color` | String/Int | Global tint for the text. Default `0xFFFFFFFF` (White). |

---

### Item
`type: 'item'`
Renders a standard Minecraft item stack.

| Property | Type | Description |
| :--- | :--- | :--- |
| `item` | String | Item ID (e.g., `minecraft:iron_sword`). |
| `overlay` | Bool | Render count and durability bar. Default `false`. |
| `customText`| String | Custom text to display instead of count (requires `overlay: true`). |
| `rotation` | Float | Rotation in degrees (Z-axis). |

---

## Advanced Examples

### 1. Dynamic Mana Bar
A gradient bar at the bottom of the screen that depletes based on a KubeJS variable.

```javascript
// Assuming you calculate mana percentage (0.0 to 1.0)
let manaPercent = 0.75; 

Painter.paint(event.player, {
    mana_bg: {
        type: 'rectangle',
        w: 200, h: 10,
        alignX: 'center', alignY: 'bottom',
        x: 0, y: -20,
        color: '#000000'
    },
    mana_bar: {
        type: 'gradient',
        // Width calculated by expression? No, pass the value directly for game logic
        w: 200 * manaPercent, 
        h: 10,
        alignX: 'center', alignY: 'bottom',
        // Example: Offset x to keep it left-aligned relative to the centered background
        // Or simpler: Just align left and calculate X manually.
        // Here we use 'moveX' to shift it if we want center alignment visual tricks
        x: -100 + (100 * manaPercent), // Math to keep it filling from left
        y: -20,
        colorL: '#0000FF', // Blue on left
        colorR: '#00FFFF'  // Cyan on right
    }
});
```

### 2. Floating Notification
A text notification that slides in from the right.

```javascript
// Create
Painter.paint(event.player, {
    notif: {
        type: 'text',
        text: 'Objective Updated!',
        alignX: 'right',
        x: '200 - min(time * 100, 210)', // Slides from 200 to -10 (onscreen)
        y: 20,
        color: '#FFFF55'
    }
});

// Later, remove
event.server.scheduleInTicks(60, ctx => {
    Painter.paint(event.player, { notif: { remove: true } });
});
```