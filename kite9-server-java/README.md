# Kite9 Server

Kite9 Server is a Spring Boot Executable Jar which handles the following concerns:

- Providing REST endpoints for creating Kite9 diagrams (in various formats)
- Providing User-level security and a Project abstraction (currently via GitHub connector)
- Providing a Command-based interface for modifying diagrams held internally in ADL format (i.e. Kite9 + SVG)
- Providing storage of stylesheets and related artifacts

## Deploying

These are run using a local Docker installation.  To start:

```
az login --use-device-code  (if needed)
mvn package
mvn azure-webapp:deploy
```

This can then be viewed on the Azure console at portal.azure.com

## Live Environment

Available at [www.kite9.org](https://www.kite9.org)

Management at [portal.azure.com](https://portal.azure.com)

## Extending


