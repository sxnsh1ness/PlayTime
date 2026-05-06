# PlayTime

PlayTime is a standalone Paper plugin for tracking how long players have played on a server.

The plugin stores data in SQLite, provides `/playtime` commands, PlaceholderAPI placeholders, custom Bukkit events, and a small developer API for reward plugins and other integrations.

## Features

- Tracks playtime from the player's first join.
- Saves data to SQLite: `plugins/PlayTime/playtime.db`.
- Saves on quit, server shutdown, and by configurable autosave interval.
- Commands:
  - `/playtime`
  - `/playtime <player>`
- Configurable messages in `messages.yml`.
- Configurable autosave interval in `config.yml`.
- PlaceholderAPI support.
- Custom Bukkit events.
- Public API through Bukkit `ServicesManager`.

## Requirements

- Paper `1.21.1+`
- Java `21+`
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
build/libs/PlayTime-0.0.1.jar
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

## PlaceholderAPI

If PlaceholderAPI is installed, PlayTime registers the `PlayTime` expansion automatically.

Available placeholders:

| Placeholder | Description |
| --- | --- |
| `%playtime_time%` | Formatted playtime. |
| `%playtime_formatted%` | Same as `time`. |
| `%playtime_millis%` | Total playtime in milliseconds. |
| `%playtime_seconds%` | Total playtime in seconds. |
| `%playtime_minutes%` | Total playtime in minutes. |
| `%playtime_hours%` | Total playtime in hours. |
| `%playtime_days%` | Total playtime in days. |
| `%playtime_first_join%` | First tracked join time. |
| `%playtime_last_seen%` | Last seen time. |

Example reward condition in another plugin:

```text
%playtime_hours% >= 10
```

## Developer API

PlayTime exposes its API through Bukkit `ServicesManager`.

Add PlayTime as a compile-only dependency in your plugin project, then declare a soft dependency in your `plugin.yml`.

```yaml
softdepend:
  - PlayTime
```

### Getting the API

```java
import com.github.sxnsh1ness.playtime.api.PlayTimeAPI;
import com.github.sxnsh1ness.playtime.api.providers.PlayTimeProvider;

PlayTimeProvider.get().ifPresent(api -> {
    long millis = api.getPlayTimeMillis(player.getUniqueId());
});
```

Or directly through Bukkit:

```java
PlayTimeAPI api = Bukkit.getServicesManager().load(PlayTimeAPI.class);
if (api == null) {
    return;
}
```

### API Methods

```java
long getPlayTimeMillis(UUID uuid);
Duration getPlayTime(UUID uuid);
String formatPlayTime(UUID uuid);

Optional<PlayerPlayTime> getPlayer(UUID uuid);
Optional<PlayerPlayTime> getPlayer(String name);

List<PlayerPlayTime> getTop(int limit);
```

### Reward Example

```java
import com.github.sxnsh1ness.playtime.api.providers.PlayTimeProvider;

public void tryGiveReward(Player player) {
    PlayTimeProvider.get().ifPresent(api -> {
        if (api.getPlayTime(player.getUniqueId()).toHours() >= 10) {
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

### Top PlayTime Example

```java
for (PlayerPlayTime entry : api.getTop(10)) {
    Bukkit.getLogger().info(entry.name() + ": " + entry.playtime().toHours() + " hours");
}
```

## Custom Events

PlayTime also exposes Bukkit events for developers who need to react to playtime changes.

### PlayerPlayTimeStartEvent

Called when PlayTime starts tracking a player's current session.

```java
@EventHandler
public void onPlayTimeStart(PlayerPlayTimeStartEvent event) {
    UUID uuid = event.getUuid();
    String name = event.getPlayerName();
}
```

### PlayerPlayTimeSaveEvent

Called after a playtime delta is saved to SQLite.

This event can be asynchronous. Check `event.isAsynchronous()` before using Bukkit APIs that must run on the main thread.

```java
@EventHandler
public void onPlayTimeSave(PlayerPlayTimeSaveEvent event) {
    long previous = event.getPreviousTotalMillis();
    long delta = event.getSavedDeltaMillis();
    long total = event.getNewTotalMillis();
}
```

### PlayerPlayTimeStopEvent

Called when a tracked session ends, usually on player quit.

```java
@EventHandler
public void onPlayTimeStop(PlayerPlayTimeStopEvent event) {
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
build/libs/PlayTime-{VERSION}.jar
```
