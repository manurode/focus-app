---
name: Mindful Precision
colors:
  surface: '#051424'
  surface-dim: '#051424'
  surface-bright: '#2c3a4c'
  surface-container-lowest: '#010f1f'
  surface-container-low: '#0d1c2d'
  surface-container: '#122131'
  surface-container-high: '#1c2b3c'
  surface-container-highest: '#273647'
  on-surface: '#d4e4fa'
  on-surface-variant: '#c5c6cc'
  inverse-surface: '#d4e4fa'
  inverse-on-surface: '#233143'
  outline: '#8f9096'
  outline-variant: '#45474c'
  surface-tint: '#bfc7d7'
  primary: '#bfc7d7'
  on-primary: '#29313e'
  primary-container: '#121a26'
  on-primary-container: '#7b8392'
  inverse-primary: '#575f6d'
  secondary: '#44e2cd'
  on-secondary: '#003731'
  secondary-container: '#03c6b2'
  on-secondary-container: '#004d44'
  tertiary: '#cebdff'
  on-tertiary: '#381385'
  tertiary-container: '#1f0059'
  on-tertiary-container: '#8c70dd'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dbe3f4'
  primary-fixed-dim: '#bfc7d7'
  on-primary-fixed: '#141c28'
  on-primary-fixed-variant: '#3f4755'
  secondary-fixed: '#62fae3'
  secondary-fixed-dim: '#3cddc7'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005047'
  tertiary-fixed: '#e8ddff'
  tertiary-fixed-dim: '#cebdff'
  on-tertiary-fixed: '#21005e'
  on-tertiary-fixed-variant: '#4f319c'
  background: '#051424'
  on-background: '#d4e4fa'
  surface-variant: '#273647'
typography:
  display:
    fontFamily: Manrope
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Manrope
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Manrope
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1200px
  gutter: 24px
  margin-desktop: 64px
  margin-mobile: 20px
---

## Brand & Style

The design system is built for a mindfulness and focus application, targeting users who seek mental clarity in a high-stimulation world. The brand personality is **quiet, intentional, and premium**. It rejects the typical "lifestyle" aesthetic of bright pastels in favor of a sophisticated, nocturnal-leaning environment that reduces eye strain and minimizes cognitive load.

The design style is a hybrid of **Modern Minimalism** and **Glassmorphism**. It utilizes heavy whitespace—or rather, "negative space"—to allow content to breathe. Subtle translucency and background blurs are used to create a sense of layering without adding visual clutter. The emotional response should be one of immediate decompression, similar to walking into a high-end, dimly lit spa or a library at dusk.

## Colors

The palette is anchored in a deep, nocturnal base to foster focus and calm. 

- **Primary (Deep Navy/Slate):** Used for the core canvas and structural backgrounds. It provides a stable, grounding foundation.
- **Secondary (Teal Glow):** Used for primary actions, progress indicators, and "active" focus states. This color should feel like a soft light source.
- **Tertiary (Soft Lavender):** Reserved for secondary highlights, mindfulness categories, and gentle notifications. 
- **Neutral (Slate Grey):** Used for secondary text and borders to maintain low contrast and high legibility without being harsh.

Surface colors should use varying shades of the primary navy (e.g., `#1E293B`) to create depth rather than relying on pure black or grey.

## Typography

This design system utilizes **Manrope** as the primary typeface for its modern, balanced, and humanist qualities. It feels approachable yet precise. Headlines use tighter letter-spacing and heavier weights to command attention quietly.

For technical data—such as timer countdowns or focus statistics—**JetBrains Mono** is employed. This monospaced choice introduces a "tool-like" precision that contrasts beautifully with the soft, organic feel of Manrope, reinforcing the "intentional" aspect of the brand.

Typography should always be high-contrast against the dark background (using off-whites like `#F8FAFC`) but should avoid pure white to prevent "halation" or glowing text effects that cause eye fatigue.

## Layout & Spacing

The layout philosophy follows a **Fixed Grid** model on desktop to keep content centered and focused, preventing the eye from wandering across ultra-wide monitors. On mobile, it transitions to a fluid system with generous side margins.

- **The 8px Rule:** All spacing between elements must be a multiple of 8px to ensure a consistent rhythmic flow.
- **Clarity through Margin:** Use larger-than-standard margins (64px+) between major sections to emphasize the "quiet" nature of the app. 
- **Center-Alignment:** Key focus elements (like a meditation timer or a single daily task) should be center-aligned to create a "zen" focal point.

## Elevation & Depth

Depth is conveyed through **Tonal Layers** and **Glassmorphism**, avoiding heavy, opaque shadows.

1.  **Base Layer:** The darkest navy (`#121A26`).
2.  **Surface Layer:** A slightly lighter navy (`#1E293B`) with a subtle 1px border (`#334155`) to define edges.
3.  **Floating Elements:** Use a semi-transparent background (e.g., `rgba(30, 41, 59, 0.7)`) with a `blur(12px)` effect.
4.  **Shadows:** Shadows should be long, soft, and tinted with the primary navy color (e.g., `0px 20px 40px rgba(2, 6, 23, 0.4)`), making them feel like an extension of the object rather than a dark stain.

## Shapes

The shape language is defined by **Rounded** geometry. There are no sharp corners in this design system, as sharp angles evoke tension.

- **Standard Elements:** Buttons and input fields use a `0.5rem` (8px) radius.
- **Containers:** Cards and modals use `1rem` (16px) to feel substantial and protective.
- **Pill Elements:** Tags, chips, and "Start" buttons should use a fully rounded (pill) shape to encourage interaction and feel "soft" to the touch.

## Components

- **Buttons:** Primary buttons use a solid Teal (`#2DD4BF`) with dark navy text. Secondary buttons are "Ghost" style—transparent with a Lavender outline.
- **Cards:** Use the Glassmorphism approach: a subtle backdrop blur, a very thin 1px border slightly lighter than the background, and no heavy fill.
- **Inputs:** Input fields should be dark with a bottom-only border that glows (Teal) when focused. 
- **Chips/Tags:** Small, pill-shaped elements with low-opacity Lavender backgrounds and high-opacity Lavender text.
- **Focus Timer:** A bespoke component featuring a thin, circular progress ring using the Teal accent, with large, monospaced JetBrains Mono digits in the center.
- **List Items:** Separated by generous vertical padding (16px+) and subtle dividers that fade out at the edges to maintain the "infinite" feel of the layout.