# KarSu-Badge

Lightweight and customizable badge drawable library for Android, written in Kotlin.

Inspired by [nekocode/Badge](https://github.com/nekocode/Badge).

## Badge Types

| Type | Description |
|------|-------------|
| `TYPE_NUMBER` | Circular number badge (e.g. notification count) |
| `TYPE_ONLY_ONE_TEXT` | Single text badge (e.g. "VIP", "NEW") |
| `TYPE_WITH_TWO_TEXT` | Dual section badge with white backgrounds |
| `TYPE_WITH_TWO_TEXT_COMPLEMENTARY` | Dual section badge with complementary styling |

## Setup

Add the `karsu_badge` module to your project:

**settings.gradle.kts**
```kotlin
include(":karsu_badge")
```

**app/build.gradle.kts**
```kotlin
dependencies {
    implementation(project(":karsu_badge"))
}
```

## Usage

### Kotlin DSL

```kotlin
val badge = badgeDrawable {
    type(BadgeDrawable.TYPE_NUMBER)
    number(9)
    badgeColor(0xffCC3333.toInt())
}
```

### Builder Pattern

```kotlin
val badge = BadgeDrawable.Builder()
    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
    .text1("VIP")
    .badgeColor(0xff336699.toInt())
    .textColor(0xffFFFFFF.toInt())
    .build()
```

### Display in TextView (Inline)

```kotlin
val spannable = SpannableString(
    TextUtils.concat("Status: ", badge.toSpannable(), " ok")
)
textView.text = spannable
```

### Display in ImageView

```kotlin
imageView.setImageDrawable(badge)
```

### Modify Existing Badge (buildUpon)

```kotlin
val updated = badge.buildUpon()
    .text2("2.0")
    .badgeColor(0xffF44336.toInt())
    .build()
```

### Dynamic Updates (Setters)

```kotlin
badge.setNumber(99)
badge.setBadgeColor(0xff009688.toInt())
badge.setText1("Updated")
```

## Examples

### Number Badge
```kotlin
badgeDrawable {
    type(BadgeDrawable.TYPE_NUMBER)
    number(42)
    badgeColor(0xff336699.toInt())
}
```

### Single Text Badge
```kotlin
badgeDrawable {
    type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
    text1("PREMIUM")
    badgeColor(0xffFF9800.toInt())
    textColor(0xff000000.toInt())
}
```

### Two Text Badge
```kotlin
badgeDrawable {
    type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
    text1("BUILD")
    text2("Pass")
    badgeColor(0xff4CAF50.toInt())
}
```

### Complementary Badge
```kotlin
badgeDrawable {
    type(BadgeDrawable.TYPE_WITH_TWO_TEXT_COMPLEMENTARY)
    text1("LEVEL")
    text2("10")
    badgeColor(0xffCC9933.toInt())
}
```

### Custom Styling
```kotlin
badgeDrawable {
    type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
    text1("Custom")
    badgeColor(0xffE91E63.toInt())
    textSize(spToPx(16f))
    cornerRadius(dpToPx(20f))
    typeface(Typeface.MONOSPACE)
    padding(left = dpToPx(12f), top = dpToPx(4f), right = dpToPx(12f), bottom = dpToPx(4f))
    strokeWidth(dpToPx(2f).toInt())
}
```

## Configuration Options

| Method | Description | Default |
|--------|-------------|---------|
| `type()` | Badge type | `TYPE_NUMBER` |
| `number()` | Number to display | `0` |
| `text1()` | First text field | `""` |
| `text2()` | Second text field | `""` |
| `badgeColor()` | Background color | `0xffCC3333` |
| `textColor()` | Text color | `0xffFFFFFF` |
| `text2Color()` | Complementary section background color | `null` (uses textColor) |
| `textSize()` | Text size in pixels | `12sp` |
| `typeface()` | Font typeface | `Typeface.DEFAULT_BOLD` |
| `cornerRadius()` | Corner radius in pixels | `2dp` |
| `padding()` | Left, top, right, bottom, center padding | `2dp, 2dp, 2dp, 2dp, 3dp` |
| `strokeWidth()` | Border width for dual text types | `1dp` |

## License

```
Copyright 2026 KarsuBadge

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
