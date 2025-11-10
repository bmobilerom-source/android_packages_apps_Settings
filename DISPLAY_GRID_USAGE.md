# DisplayGrid Usage Guide

The DisplayGrid is a reusable layout component that can be added to any Settings page. It displays a grid of cards and circular buttons, each independently clickable.

## Quick Start

### 1. Add to XML Preference Screen

Add this to any preference screen XML file:

```xml
<com.android.settingslib.widget.LayoutPreference
    android:key="display_grid"
    android:selectable="false"
    android:layout="@layout/display_grid_layout" />
```

### 2. Initialize in Fragment

In your fragment's `onViewCreated` method:

```java
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    DisplayGridHelper.setupDisplayGrid(getContext(), getPreferenceScreen(), 
            "display_grid", getMetricsCategory());
}
```

That's it! The DisplayGrid will automatically populate with default items.

## Customizing Items

To customize the items, modify `DisplayGridHelper.createDefaultItems()` or create your own list:

```java
List<DisplayGridAdapter.CardItem> items = new ArrayList<>();

// Large left card with button
items.add(new DisplayGridAdapter.CardItem(
    DisplayGridAdapter.CARD_TYPE_LARGE_LEFT,
    R.string.your_title,
    R.string.your_summary,
    null, // No icon for large left card
    "com.epic.fragments.YourFragment",
    "Button Text"));

// About Us style card
items.add(new DisplayGridAdapter.CardItem(
    DisplayGridAdapter.CARD_TYPE_ABOUT_US,
    R.string.your_title,
    R.string.your_summary,
    R.drawable.your_icon,
    "com.epic.fragments.YourFragment"));

// Status Bar style card
items.add(new DisplayGridAdapter.CardItem(
    DisplayGridAdapter.CARD_TYPE_STATUS_BAR,
    R.string.your_title,
    R.string.your_summary,
    R.drawable.your_icon,
    "com.epic.fragments.YourFragment"));

// Circular button
items.add(new DisplayGridAdapter.CardItem(
    DisplayGridAdapter.CARD_TYPE_CIRCULAR_BUTTON,
    R.string.your_title,
    R.string.your_summary,
    R.drawable.your_icon,
    "com.epic.fragments.YourFragment"));

// Then set the adapter
RecyclerView rv = findViewById(R.id.display_grid_recycler);
rv.setAdapter(new DisplayGridAdapter(context, items, sourceMetrics));
```

## Card Types

1. **CARD_TYPE_LARGE_LEFT**: Large card spanning 2 rows, with toggle switches and a button
2. **CARD_TYPE_ABOUT_US**: Standard card with icon at top, title and summary
3. **CARD_TYPE_STATUS_BAR**: Standard card with icon at bottom
4. **CARD_TYPE_CIRCULAR_BUTTON**: Circular button with icon

## Fragment Navigation

Each card/button can navigate to:
- Anatolia fragments: `"com.epic.fragments.YourFragment"`
- AOSP fragments: `"com.android.settings.display.DisplaySettings"`
- LineageParts activities: `"org.lineageos.lineageparts.YourActivity"`

## Example: Adding to LockScreenSettings

See `anatolia_settings_lockscreen.xml` and `LockScreenSettings.java` for a complete example.

