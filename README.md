# Weather MCP Server

A Spring Boot [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server that exposes demo weather tools over **STDIO** transport. Claude Desktop (or any MCP client) can call these tools to look up forecasts and alerts for a small in-memory dataset — no external weather API or API keys required.

Built with Spring Boot `3.4.1` and Spring AI `1.1.7` (`spring-ai-starter-mcp-server`).

## Prerequisites

- **Java 17+** (JDK 21 recommended)
- Claude Desktop (to run the server as a local MCP tool host)

## Available tools

| Tool | Description | Example input |
|------|-------------|---------------|
| `getForecast` | Current forecast for a US city | `Phoenix`, `New York` |
| `getAlerts` | Active weather alerts for a US state (two-letter code) | `AZ`, `WA` |

### Demo data

**Forecasts:** Phoenix, Scottsdale, Seattle, New York, Chicago

**Alerts:** AZ, WA, NY, IL

## Build

From the project root:

```bash
./gradlew bootJar
```

This produces:

```text
build/libs/mcp-weather-server.jar
```

## Start the server with `java -jar`

The server speaks MCP JSON-RPC over **stdin/stdout**. You normally do **not** run it by hand for day-to-day use — Claude Desktop launches it. For a manual smoke test:

```bash
# Prefer an absolute path to your JDK's java binary
java -jar build/libs/mcp-weather-server.jar
```

Example with an explicit JDK on macOS:

```bash
/Users/YOUR_USER/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home/bin/java \
  -jar /Users/YOUR_USER/IdeaProjects/spring-boot-mcp-server/build/libs/mcp-weather-server.jar
```

The process will wait on stdin for MCP messages. Logs are written to `mcp-weather-server.log` (not the console), so stdout stays clean for the protocol.

> **Important:** Do not write banners or log lines to stdout. This project already disables the Spring banner, sets `web-application-type=none`, and clears the console log pattern for STDIO safety.

## Configure Claude Desktop (local)

1. Build the jar (see [Build](#build)).
2. Open Claude Desktop’s MCP config file:
   - **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
   - **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
3. Add (or merge) an `mcpServers` entry. Replace the paths with **absolute** paths on your machine:

```json
{
  "mcpServers": {
    "weather-server": {
      "command": "/Users/YOUR_USER/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home/bin/java",
      "args": [
        "-jar",
        "/Users/YOUR_USER/IdeaProjects/spring-boot-mcp-server/build/libs/mcp-weather-server.jar"
      ]
    }
  }
}
```

Notes:

- Use the full path to your JDK’s `java` binary. Claude Desktop’s environment often cannot resolve a bare `java` command.
- Use the full path to `mcp-weather-server.jar`. Placeholder paths like `/absolute/path/to/mcp-weather-server.jar` will cause Claude to report **Server disconnected**.
- If the file already has other servers, merge `weather-server` into the existing `mcpServers` object — do not delete unrelated entries.

4. **Fully quit** Claude Desktop (menu bar → Quit), then reopen it so the config reloads.
5. Confirm `weather-server` is connected and lists `getForecast` and `getAlerts`.

### Try it in Claude

Ask things like:

- “What’s the forecast for Phoenix?”
- “Any weather alerts for AZ?”
- “Compare Seattle and Chicago forecasts.”
- “Are there alerts in NY?”

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| `Server disconnected` in Claude | Wrong jar path, or `java` not found — check Claude’s MCP log |
| `Unable to access jarfile ...` | Config still has a placeholder or incorrect jar path |
| Tools missing after a code change | Rebuild with `./gradlew bootJar`, then fully restart Claude |

Claude MCP logs (macOS):

```text
~/Library/Logs/Claude/mcp-server-weather-server.log
~/Library/Logs/Claude/mcp.log
```

Server-side logs:

```text
mcp-weather-server.log
```

(created relative to the process working directory when the jar starts)

## Project layout

```text
src/main/java/com/example/mcpserver/
  McpServerApplication.java   # Boot app + ToolCallbackProvider bean
  WeatherService.java         # @Tool methods: getForecast, getAlerts
src/main/resources/
  application.properties      # STDIO MCP + logging config
```
