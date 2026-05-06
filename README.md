# OceanPlaytime

OceanPlaytime is a standalone Paper plugin for tracking how long players have played on a server.

The plugin stores data in SQLite, provides `/playtime` commands, PlaceholderAPI placeholders, custom Bukkit events, and a small developer API for reward plugins and other integrations.

## Features

- Tracks playtime from the player's first join.
- Saves data to SQLite: `plugins/OceanPlaytime/playtime.db`.
- Saves on quit, server shutdown, and by configurable autosave interval.
- Commands:
  - `/playtime`
  - `/playtime <player>`
  - `/ptime`
- Configurable messages in `messages.yml`.
- Configurable autosave interval in `config.yml`.
- PlaceholderAPI support.
- Custom Bukkit events.
- Public API through Bukkit `ServicesManager`.

## Requirements

- Paper `1.21.1`
- Java `21`
- Optional: PlaceholderAPI for placeholders

## Installation

1. Build the plugin:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

2. Put the jar into your server `plugins` folder:

```text
build/libs/OceanPlaytime-1.0.0.jar
```

3. Restart the server.

## Configuration

`config.yml`

```yaml
autosave-interval-seconds: 300
```

Minimum accepted value is `30` seconds.

`messages.yml`

```yaml
console-usage: "<red>Usage: %usage%"
command-usage: "<red>Usage: %usage%"
self-playtime: "<aqua>Your playtime: <white>%time%"
other-playtime: "<aqua>Playtime of %player%: <white>%time%"
player-not-found: "<red>No playtime data found for %player%."

time:
  days: "%value% д."
  hours: "%value% ч."
  minutes: "%value% мин."
  seconds: "%value% сек."
  separator: " "
```

Messages support MiniMessage formatting.

## Commands

| Command | Description |
| --- | --- |
| `/playtime` | Shows your own playtime. |
| `/playtime <player>` | Shows another player's stored playtime. |
| `/ptime` | Alias for `/playtime`. |

## PlaceholderAPI

If PlaceholderAPI is installed, OceanPlaytime registers the `oceanplaytime` expansion automatically.

Available placeholders:

| Placeholder | Description |
| --- | --- |
| `%oceanplaytime_time%` | Formatted playtime. |
| `%oceanplaytime_formatted%` | Same as `time`. |
| `%oceanplaytime_millis%` | Total playtime in milliseconds. |
| `%oceanplaytime_seconds%` | Total playtime in seconds. |
| `%oceanplaytime_minutes%` | Total playtime in minutes. |
| `%oceanplaytime_hours%` | Total playtime in hours. |
| `%oceanplaytime_days%` | Total playtime in days. |
| `%oceanplaytime_first_join%` | First tracked join time. |
| `%oceanplaytime_last_seen%` | Last seen time. |

Example reward condition in another plugin:

```text
%oceanplaytime_hours% >= 10
```

## Developer API

OceanPlaytime exposes its API through Bukkit `ServicesManager`.

Add OceanPlaytime as a compile-only dependency in your plugin project, then declare a soft dependency in your `plugin.yml`.

```yaml
softdepend:
  - OceanPlaytime
```

### Getting the API

```java
import me.moormxi.playtime.api.OceanPlaytimeApi;
import me.moormxi.playtime.api.OceanPlaytimeProvider;

OceanPlaytimeProvider.get().ifPresent(api -> {
    long millis = api.getPlaytimeMillis(player.getUniqueId());
});
```

Or directly through Bukkit:

```java
OceanPlaytimeApi api = Bukkit.getServicesManager().load(OceanPlaytimeApi.class);
if (api == null) {
    return;
}
```

### API Methods

```java
long getPlaytimeMillis(UUID uuid);
Duration getPlaytime(UUID uuid);
String formatPlaytime(UUID uuid);

Optional<PlayerPlaytime> getPlayer(UUID uuid);
Optional<PlayerPlaytime> getPlayer(String name);

List<PlayerPlaytime> getTop(int limit);
```

### Reward Example

```java
import me.moormxi.playtime.api.OceanPlaytimeProvider;

public void tryGiveReward(Player player) {
    OceanPlaytimeProvider.get().ifPresent(api -> {
        if (api.getPlaytime(player.getUniqueId()).toHours() >= 10) {
            // Give a reward for 10 hours of playtime.
        }
    });
}
```

### Player Data Example

```java
api.getPlayer(player.getUniqueId()).ifPresent(data -> {
    UUID uuid = data.uuid();
    String name = data.name();
    long millis = data.playtimeMillis();
    Duration playtime = data.playtime();
    Instant firstJoin = data.firstJoin();
    Instant lastSeen = data.lastSeen();
});
```

### Top Playtime Example

```java
for (PlayerPlaytime entry : api.getTop(10)) {
    Bukkit.getLogger().info(entry.name() + ": " + entry.playtime().toHours() + " hours");
}
```

## Custom Events

OceanPlaytime also exposes Bukkit events for developers who need to react to playtime changes.

### PlayerPlaytimeStartEvent

Called when OceanPlaytime starts tracking a player's current session.

```java
@EventHandler
public void onPlaytimeStart(PlayerPlaytimeStartEvent event) {
    UUID uuid = event.getUuid();
    String name = event.getPlayerName();
}
```

### PlayerPlaytimeSaveEvent

Called after a playtime delta is saved to SQLite.

This event can be asynchronous. Check `event.isAsynchronous()` before using Bukkit APIs that must run on the main thread.

```java
@EventHandler
public void onPlaytimeSave(PlayerPlaytimeSaveEvent event) {
    long previous = event.getPreviousTotalMillis();
    long delta = event.getSavedDeltaMillis();
    long total = event.getNewTotalMillis();
}
```

### PlayerPlaytimeStopEvent

Called when a tracked session ends, usually on player quit.

```java
@EventHandler
public void onPlaytimeStop(PlayerPlaytimeStopEvent event) {
    long sessionMillis = event.getSessionMillis();
    long totalMillis = event.getTotalMillis();
}
```

## Building From Source

```bash
./gradlew clean build
```

The compiled jar will be created in:

```text
build/libs/OceanPlaytime-1.0.0.jar
```
