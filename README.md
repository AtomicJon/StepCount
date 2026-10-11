# StepCount

A simple Android step counter that uses your phone's built-in step sensor. All data stays on the device - the app does not request internet access.

This is a personal project that I'm sharing in case others find it useful. There's no guarantee of long-term support, updates or fixes.

## Features

- Daily step count with distance and calorie estimates
- Adjustable daily step goal
- Hourly breakdown of steps through the day
- History view with a monthly calendar and bar chart
- Home screen widgets (standard and 1x1)
- CSV export of daily and hourly data
- Miles or kilometres

## How it works

The app is written in Kotlin with Jetpack Compose for the UI, Room (a SQLite wrapper) for step history and DataStore for settings.

```
Step sensor
    |
StepTrackerService ----> Room database + DataStore
    |                           |
    +--> notification           +--> StepRepository --> ViewModels --> Compose screens
    +--> widgets (read DB directly)
```

### Counting steps

Android's step counter sensor reports the total steps since the phone last booted, not steps per day. `StepTrackerService` turns that running total into a daily count:

- At the start of each day it saves the current sensor reading as a **baseline**. Today's steps are the current reading minus the baseline.
- If the reading drops below the baseline, the phone has rebooted and the sensor has reset to zero. The steps counted so far today are saved as a **stored base** and counting continues on top of them.
- The baseline, stored base and current tracking date live in DataStore so they survive the app being killed.

The calculation itself is in `domain/StepCalculator.kt`, along with the distance and calorie estimates (fixed averages of 0.762 m per step and 0.04 kcal per step).

### Background tracking

`StepTrackerService` runs as a foreground service with an ongoing notification, which stops Android from killing it. It's started when the app opens (once activity permission is granted) and by `BootReceiver` after a reboot.

On each sensor update the service:

1. Updates today's total in the `step_records` table
2. Works out the current hour's share and updates the `hourly_step_records` table
3. Refreshes the notification and home screen widgets

### Data storage

| Store | Contents |
| --- | --- |
| `step_records` table | One row per day: date, steps, goal for that day |
| `hourly_step_records` table | One row per day and hour: date, hour (0-23), steps |
| DataStore preferences | Daily goal, miles/km setting and the sensor tracking state |

Database schema changes must use an explicit Room migration (see `StepDatabase.kt`) so existing step history is never lost.

### UI

`MainActivity` hosts three screens (Home, History, Settings), each with its own ViewModel. The ViewModels read from `StepRepository`, which wraps the database and preferences and exposes them as Kotlin Flows, so the screens update automatically as new steps are saved. CSV export writes files to the app cache and shares them through a `FileProvider`.

### Project layout

```
app/src/main/java/com/smashedtatoes/stepcount/
  data/db/          Room database, tables and queries
  data/export/      CSV generation
  data/pref/        DataStore settings
  data/repository/  Single access point for data used by the UI
  domain/           Step, distance and calorie calculations
  service/          Background tracking service and boot receiver
  ui/               Compose screens, ViewModels, charts and theme
  widget/           Home screen widgets
```

## Requirements

- Android 13 (API 33) or newer
- A device with a hardware step counter sensor

The app asks for these permissions:

- **Physical activity** - to read the step sensor
- **Notifications** - to show the ongoing tracking notification
- **Run at startup** - to resume tracking after a reboot

## Building

Open the project in Android Studio, or build from the command line:

```sh
./gradlew assembleDebug
```

Run the unit tests with:

```sh
./gradlew test
```

There is also a `preview` build type, which is a release build with code shrinking enabled, signed with the debug key so it can be installed directly:

```sh
./gradlew assemblePreview
```
