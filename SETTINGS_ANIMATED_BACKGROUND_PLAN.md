# Settings Animated Background & Theme Integration Plan

## Executive Summary

This plan outlines how to integrate animated MP4 backgrounds and theme overlays into the Settings app, similar to how WallpaperPicker2 handles video wallpapers and how system theme overlays apply colors. The implementation will allow 4 MP4 files to loop as backgrounds behind all Settings UI elements, working globally across all pages without affecting Settings functionality.

---

## 1. Current System Analysis

### 1.1 WallpaperPicker2 Video Handling

**Key Components:**
- `VideoWallpaperUtils.java` - Handles video wallpaper transitions and fade-in effects
- `WallpaperEngineConnection.kt` - Manages SurfaceView connections for live wallpapers
- Uses `SurfaceView` for rendering video content
- Handles `IWallpaperEngine` interface for live wallpaper services
- Supports preview and full-screen video playback

**Video Rendering Approach:**
- Uses `SurfaceView` for hardware-accelerated video rendering
- Connects to wallpaper engine via `IWallpaperConnection`
- Handles lifecycle: attachEngine → engineShown → resizePreview
- Extracts colors from video frames for Monet theming

### 1.2 Theme Overlay System

**SystemTheme Overlays Structure:**
```
packages/overlays/Lineage/customizations/
├── SystemThemeBlack/
│   ├── res/values-night/colors.xml (Monet color overrides)
│   └── AndroidManifest.xml (targetPackage="android", priority=1)
├── SystemThemeEspresso/
├── SystemThemeSnowpaint/
└── SystemThemeVivid/
```

**How Overlays Work:**
- Runtime Resource Overlays (RRO) replace system colors at runtime
- `android:targetPackage="android"` targets framework resources
- `android:priority` determines overlay precedence
- Colors defined in `values-night/colors.xml` override system colors
- Applied globally via `OverlayManager` service

**Current Theme Colors:**
- `system_neutral1_*` and `system_neutral2_*` (0-1000 shades)
- `system_surface_*_dark` (background colors)
- Overrides Monet palette colors dynamically

### 1.3 Settings App Background System

**Current Implementation:**
- `SettingsBaseActivity.java` - Base activity for all Settings pages
- Uses `ThemeHelper.trySetDynamicColor()` for Monet theming
- Background colors set via `findViewById(android.R.id.content).setBackgroundColor()`
- `SettingsHomepageActivity.java` sets background in `updateSystemBarColor()`
- Background comes from `Utils.getColorAttrDefaultColor(this, android.R.attr.colorBackground)`

**Layout Structure:**
- `R.layout.settings_base_layout` - Base layout for Settings activities
- `R.layout.collapsing_toolbar_base_layout` - For expressive theme
- Content view hierarchy: `Window → ContentView → FragmentContainer`

---

## 2. Implementation Architecture

### 2.1 High-Level Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Settings App Activity                     │
│  ┌───────────────────────────────────────────────────────┐ │
│  │         VideoBackgroundManager (Singleton)            │ │
│  │  - Manages VideoView/TextureView lifecycle             │ │
│  │  - Handles MP4 playback and looping                    │ │
│  │  - Coordinates with theme overlays                     │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              VideoBackgroundView                       │ │
│  │  - TextureView for hardware-accelerated rendering     │ │
│  │  - Positioned behind all UI (z-order: -1)            │ │
│  │  - Handles video scaling and looping                   │ │
│  └───────────────────────────────────────────────────────┘ │
│  ┌───────────────────────────────────────────────────────┐ │
│  │              Settings UI (Fragments, Views)            │ │
│  │  - All existing Settings UI elements                   │ │
│  │  - Transparent/semi-transparent backgrounds           │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Component Breakdown

#### A. VideoBackgroundManager (Singleton)
**Purpose:** Central manager for video background across all Settings activities

**Responsibilities:**
- Load and manage 4 MP4 files from assets/res/raw
- Handle video selection based on user preference
- Manage MediaPlayer lifecycle
- Coordinate video playback across activity transitions
- Handle pause/resume during Settings navigation
- Extract colors from video frames for Monet integration

**Location:** `packages/apps/Settings/src/com/android/settings/core/VideoBackgroundManager.java`

**Key Methods:**
```java
public class VideoBackgroundManager {
    private static VideoBackgroundManager sInstance;
    private MediaPlayer mMediaPlayer;
    private TextureView mVideoView;
    private int mCurrentVideoIndex = 0;
    private static final int[] VIDEO_RESOURCES = {
        R.raw.settings_bg_video_1,
        R.raw.settings_bg_video_2,
        R.raw.settings_bg_video_3,
        R.raw.settings_bg_video_4
    };
    
    public static VideoBackgroundManager getInstance(Context context);
    public void attachToActivity(Activity activity);
    public void detachFromActivity();
    public void setVideoIndex(int index);
    public void pause();
    public void resume();
    public void release();
}
```

#### B. VideoBackgroundView (Custom View)
**Purpose:** Custom view that renders video behind Settings UI

**Implementation:**
- Extends `TextureView` for hardware acceleration
- Handles video scaling (CENTER_CROP or FIT_CENTER)
- Manages looping automatically
- Positioned at z-order -1 (behind all content)

**Location:** `packages/apps/Settings/src/com/android/settings/widget/VideoBackgroundView.java`

**Key Features:**
- Hardware-accelerated rendering via TextureView
- Automatic looping via MediaPlayer.setLooping(true)
- Proper scaling to fill background
- Low opacity overlay for readability (optional)

#### C. SettingsBaseActivity Integration
**Purpose:** Integrate video background into base Settings activity

**Modifications:**
- In `onCreate()`: Initialize VideoBackgroundManager
- In `onResume()`: Resume video playback
- In `onPause()`: Pause video (keep alive for smooth transitions)
- In `onDestroy()`: Release resources if last activity

**Changes to `SettingsBaseActivity.java`:**
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Existing code...
    
    // Initialize video background
    VideoBackgroundManager.getInstance(this).attachToActivity(this);
}

@Override
protected void onResume() {
    super.onResume();
    VideoBackgroundManager.getInstance(this).resume();
}

@Override
protected void onPause() {
    super.onPause();
    // Don't pause video - keep playing for smooth transitions
    // VideoBackgroundManager.getInstance(this).pause();
}

@Override
protected void onDestroy() {
    // Only release if this is the last Settings activity
    if (isTaskRoot()) {
        VideoBackgroundManager.getInstance(this).release();
    }
    super.onDestroy();
}
```

#### D. Layout Integration
**Purpose:** Add video view to Settings base layouts

**Modified Files:**
1. `packages/apps/Settings/res/layout/settings_base_layout.xml`
2. `packages/apps/Settings/res/layout/collapsing_toolbar_base_layout.xml` (if exists)

**Layout Structure:**
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Video Background (behind everything) -->
    <com.android.settings.widget.VideoBackgroundView
        android:id="@+id/video_background"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        android:alpha="0.3" /> <!-- Optional: reduce opacity for readability -->
    
    <!-- Existing Settings Content -->
    <FrameLayout
        android:id="@android:id/content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/transparent" />
        
</FrameLayout>
```

#### E. Theme Overlay Integration
**Purpose:** Allow theme overlays to work alongside video backgrounds

**Approach:**
- Video background renders behind theme colors
- Theme overlays apply semi-transparent color overlays
- Combine: Video (bottom) → Theme overlay (middle) → Settings UI (top)

**New Overlay Structure:**
```
packages/overlays/Lineage/customizations/SystemThemeBlack/
└── res/
    └── values-night/
        └── colors.xml
            <!-- Add semi-transparent overlay color -->
            <color name="settings_video_overlay">#80000000</color>
```

**Settings Integration:**
- Read overlay color from theme
- Apply as semi-transparent layer above video
- Maintain existing Monet color extraction from video frames

---

## 3. Video File Management

### 3.1 MP4 File Placement

**Location:** `packages/apps/Settings/res/raw/`

**Files:**
- `settings_bg_video_1.mp4` - First animated background
- `settings_bg_video_2.mp4` - Second animated background
- `settings_bg_video_3.mp4` - Third animated background
- `settings_bg_video_4.mp4` - Fourth animated background

**File Requirements:**
- Format: MP4 (H.264 video codec, AAC audio)
- Resolution: 1080p (1920x1080) or 1440p (2560x1440) for high-DPI
- Frame rate: 30fps (smooth but not too resource-intensive)
- Duration: 5-15 seconds (will loop seamlessly)
- File size: < 5MB each (optimized for app size)
- Looping: Should loop seamlessly (last frame → first frame)

### 3.2 Video Selection Logic

**Storage:** `Settings.Secure.SETTINGS_BACKGROUND_VIDEO_INDEX` (0-3)

**Default:** Index 0 (first video)

**User Selection:**
- Add preference in Display settings
- Allow preview before selection
- Save preference persistently

---

## 4. Implementation Phases

### Phase 1: Core Video Background System

**Goal:** Basic video playback behind Settings UI

**Tasks:**
1. Create `VideoBackgroundManager.java` singleton
2. Create `VideoBackgroundView.java` custom view
3. Modify `SettingsBaseActivity.java` to integrate video
4. Update `settings_base_layout.xml` to include video view
5. Add 4 placeholder MP4 files (or use test videos)
6. Test video playback and looping

**Success Criteria:**
- Video plays behind Settings UI
- Video loops continuously
- No performance degradation
- Works across activity transitions

### Phase 2: Theme Overlay Integration

**Goal:** Combine video backgrounds with theme overlays

**Tasks:**
1. Modify theme overlay colors.xml to include video overlay color
2. Apply overlay color as semi-transparent layer above video
3. Ensure Monet colors still work with video backgrounds
4. Test with all 4 theme overlays (Black, Espresso, Snowpaint, Vivid)

**Success Criteria:**
- Theme overlays work with video backgrounds
- Colors blend correctly
- No visual artifacts

### Phase 3: User Controls & Preferences

**Goal:** Allow users to select video and control settings

**Tasks:**
1. Add video selection preference in Display settings
2. Create video preview dialog
3. Add enable/disable toggle for video backgrounds
4. Add opacity control slider
5. Persist preferences

**Success Criteria:**
- Users can select which video to use
- Users can enable/disable video backgrounds
- Preferences persist across reboots

### Phase 4: Performance Optimization

**Goal:** Ensure smooth performance and battery efficiency

**Tasks:**
1. Optimize video decoding (use hardware acceleration)
2. Implement frame skipping for low-end devices
3. Add battery saver mode (disable video when battery low)
4. Profile memory usage
5. Test on various device configurations

**Success Criteria:**
- Smooth 60fps UI performance
- Minimal battery impact
- Works on low-end devices (with optimizations)

### Phase 5: Color Extraction & Monet Integration

**Goal:** Extract colors from video frames for Monet theming

**Tasks:**
1. Implement frame sampling from video
2. Extract dominant colors using `WallpaperColors.fromBitmap()`
3. Update Monet palette based on video colors
4. Handle color changes as video plays (optional)

**Success Criteria:**
- Video colors influence Monet theme
- Smooth color transitions
- No performance impact

---

## 5. Technical Implementation Details

### 5.1 MediaPlayer Setup

```java
MediaPlayer mediaPlayer = new MediaPlayer();
mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + 
    context.getPackageName() + "/" + R.raw.settings_bg_video_1));
mediaPlayer.setSurface(surface); // From TextureView
mediaPlayer.setLooping(true);
mediaPlayer.setVolume(0, 0); // Mute audio
mediaPlayer.prepare();
mediaPlayer.start();
```

### 5.2 TextureView Integration

```java
TextureView textureView = findViewById(R.id.video_background);
textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // Initialize MediaPlayer with surface
        VideoBackgroundManager.getInstance(context).setSurface(new Surface(surface));
    }
    
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Handle size changes
    }
    
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // Cleanup
        return false; // Don't release surface immediately
    }
    
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Optional: Extract colors from frames here
    }
});
```

### 5.3 Z-Order Management

**Key:** Video must be behind all UI elements

**Implementation:**
- Video view added first in FrameLayout (lowest z-order)
- All Settings UI added after (higher z-order)
- Use `bringToFront()` for Settings content if needed
- Set video view `elevation` to 0 or negative

### 5.4 Activity Lifecycle Management

**Challenge:** Keep video playing across Settings activity transitions

**Solution:**
- Don't pause video in `onPause()` for Settings activities
- Only pause when Settings app loses focus (onUserLeaveHint)
- Resume when Settings regains focus
- Release only when Settings app is destroyed

```java
@Override
protected void onUserLeaveHint() {
    super.onUserLeaveHint();
    // Settings app lost focus - pause video
    VideoBackgroundManager.getInstance(this).pause();
}

@Override
protected void onResume() {
    super.onResume();
    // Settings app regained focus - resume video
    VideoBackgroundManager.getInstance(this).resume();
}
```

### 5.5 Memory Management

**Concerns:**
- Video decoding uses significant memory
- Multiple activities shouldn't create multiple players

**Solution:**
- Singleton pattern ensures only one MediaPlayer instance
- Reuse MediaPlayer across activity transitions
- Release MediaPlayer only when Settings app closes
- Use `setDataSource()` to switch videos without recreating player

---

## 6. Theme Overlay Integration Details

### 6.1 Overlay Color Application

**Approach:** Add semi-transparent overlay above video

**Layout Structure:**
```xml
<FrameLayout>
    <!-- Video Background -->
    <VideoBackgroundView />
    
    <!-- Theme Overlay (semi-transparent) -->
    <View
        android:id="@+id/theme_overlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="?attr/settingsVideoOverlayColor" />
    
    <!-- Settings Content -->
    <FrameLayout android:id="@android:id/content" />
</FrameLayout>
```

**Theme Attribute:**
```xml
<!-- In theme overlay values/colors.xml -->
<color name="settings_video_overlay">#80000000</color>

<!-- In Settings theme -->
<item name="settingsVideoOverlayColor">@color/settings_video_overlay</item>
```

### 6.2 Monet Color Extraction from Video

**Implementation:**
- Sample video frames periodically (every 2-3 seconds)
- Extract bitmap from TextureView
- Use `WallpaperColors.fromBitmap()` to extract colors
- Update Monet palette via `WallpaperManager.setWallpaperColors()`

**Code:**
```java
private void extractColorsFromVideo() {
    Bitmap frame = textureView.getBitmap();
    if (frame != null) {
        WallpaperColors colors = WallpaperColors.fromBitmap(frame);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        wallpaperManager.setWallpaperColors(WallpaperManager.FLAG_SYSTEM, colors);
    }
}
```

---

## 7. Resource Requirements

### 7.1 New Files to Create

1. **Java/Kotlin:**
   - `VideoBackgroundManager.java` - Singleton manager
   - `VideoBackgroundView.java` - Custom TextureView
   - `SettingsBackgroundPreferenceController.java` - Preference controller

2. **XML Layouts:**
   - Modify `settings_base_layout.xml`
   - Modify `collapsing_toolbar_base_layout.xml` (if exists)
   - `video_background_preview.xml` - Preview dialog

3. **Resources:**
   - `res/raw/settings_bg_video_1.mp4`
   - `res/raw/settings_bg_video_2.mp4`
   - `res/raw/settings_bg_video_3.mp4`
   - `res/raw/settings_bg_video_4.mp4`
   - `res/values/strings.xml` - New strings
   - `res/xml/settings_background.xml` - Preference screen

4. **Theme Overlays:**
   - Update all 4 theme overlays' `colors.xml`
   - Add `settings_video_overlay` color

### 7.2 Modified Files

1. `SettingsBaseActivity.java` - Add video background lifecycle
2. `SettingsHomepageActivity.java` - Ensure video works on homepage
3. Theme overlay `colors.xml` files (4 files)

---

## 8. Testing Plan

### 8.1 Functional Testing

- [ ] Video plays behind Settings UI
- [ ] Video loops continuously
- [ ] Video works across activity transitions
- [ ] Video selection preference works
- [ ] Enable/disable toggle works
- [ ] Theme overlays work with video
- [ ] Monet colors extract from video
- [ ] Preferences persist across reboots

### 8.2 Performance Testing

- [ ] Smooth 60fps UI performance
- [ ] No memory leaks
- [ ] Battery usage acceptable
- [ ] Works on low-end devices
- [ ] No frame drops during video playback

### 8.3 Compatibility Testing

- [ ] Works with all 4 theme overlays
- [ ] Works in light/dark mode
- [ ] Works in portrait/landscape
- [ ] Works on tablets (two-pane mode)
- [ ] Works with accessibility features

---

## 9. Potential Challenges & Solutions

### Challenge 1: Performance on Low-End Devices
**Solution:** 
- Add device capability check
- Disable video on low-end devices automatically
- Implement frame skipping
- Use lower resolution videos for low-end devices

### Challenge 2: Battery Drain
**Solution:**
- Disable video when battery saver mode active
- Add user preference to disable video
- Optimize video codec and compression
- Use hardware acceleration

### Challenge 3: Memory Usage
**Solution:**
- Singleton pattern prevents multiple players
- Release MediaPlayer when not needed
- Use efficient video codecs
- Limit video resolution

### Challenge 4: Activity Lifecycle Complexity
**Solution:**
- Careful lifecycle management
- Don't pause on activity transitions
- Only pause when app loses focus
- Test thoroughly across all Settings pages

### Challenge 5: Theme Overlay Conflicts
**Solution:**
- Semi-transparent overlays blend correctly
- Test with all theme combinations
- Ensure Monet colors still work
- Provide fallback if overlay conflicts

---

## 10. Future Enhancements

1. **Dynamic Video Selection:** Allow users to add custom MP4 files
2. **Video Effects:** Add blur, saturation, brightness controls
3. **Synchronized Colors:** Real-time Monet color updates from video
4. **Multiple Videos:** Different videos for different Settings sections
5. **Video Transitions:** Smooth transitions between videos
6. **Performance Modes:** Auto-adjust quality based on device performance

---

## 11. Implementation Checklist

### Phase 1: Core System
- [ ] Create VideoBackgroundManager.java
- [ ] Create VideoBackgroundView.java
- [ ] Modify SettingsBaseActivity.java
- [ ] Update settings_base_layout.xml
- [ ] Add placeholder MP4 files
- [ ] Test basic video playback

### Phase 2: Theme Integration
- [ ] Update theme overlay colors.xml files
- [ ] Add overlay color application
- [ ] Test with all themes
- [ ] Ensure Monet compatibility

### Phase 3: User Controls
- [ ] Add preference screen
- [ ] Create video preview
- [ ] Add enable/disable toggle
- [ ] Add opacity control
- [ ] Test preferences

### Phase 4: Optimization
- [ ] Performance profiling
- [ ] Memory optimization
- [ ] Battery optimization
- [ ] Low-end device support

### Phase 5: Color Extraction
- [ ] Frame sampling implementation
- [ ] Color extraction logic
- [ ] Monet integration
- [ ] Test color updates

---

## 12. Conclusion

This plan provides a comprehensive roadmap for integrating animated MP4 backgrounds into the Settings app, similar to how WallpaperPicker2 handles video wallpapers. The implementation will:

1. **Work Globally:** Video background applies to all Settings pages
2. **Non-Intrusive:** Doesn't affect Settings functionality
3. **Theme Compatible:** Works with existing theme overlays
4. **Performance Optimized:** Efficient and battery-friendly
5. **User Controllable:** Users can select videos and control settings

The architecture uses a singleton pattern for efficient resource management, TextureView for hardware-accelerated rendering, and careful lifecycle management to ensure smooth operation across all Settings activities.

