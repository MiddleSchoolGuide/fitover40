# App Icon Setup Instructions

The FitOver40 app icon should be an adaptive icon that looks professional on all Android devices.

### 1. Generating with Android Studio (Recommended)
1.  Open the project in **Android Studio**.
2.  Right-click the `app/src/main/res` folder.
3.  Select **New > Image Asset**.
4.  **Icon Type**: Launcher Icons (Adaptive and Legacy).
5.  **Name**: ic_launcher.
6.  **Foreground Layer**:
    *   **Source Asset**: Image or Clip Art.
    *   **Image Path**: Use a bold running figure + dumbbell icon.
    *   **Scaling**: Trim = Yes, Resize = ~75% to fit safe zone.
7.  **Background Layer**:
    *   **Source Asset**: Color.
    *   **Color**: `#00897B` (Teal) or `#1565C0` (Blue).
8.  Click **Next** and **Finish**. This will generate all the `mipmap-*` folders automatically.

### 2. Manual Verification
Ensure the following files exist in `res/mipmap-*`:
- `ic_launcher.png` (Adaptive background + foreground)
- `ic_launcher_round.png` (Circular version)
- `ic_launcher_foreground.xml` (SVG for modern devices)

### 3. Play Store Spec
For the Play Store Console listing, you will need a high-res icon:
- **Format**: 512x512 PNG.
- **Color Space**: sRGB.
- **Max File Size**: 1MB.
- **Transparency**: Not allowed (Alpha = No).
