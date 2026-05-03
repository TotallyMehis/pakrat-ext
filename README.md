# Pakrat Extended

Original Pakrat was created by Rof.

You can download the latest build from the [Actions-tab](https://github.com/TotallyMehis/pakrat-ext/actions) and opening the latest workflow run.

## Features

- Java is not required to be on your system.
- `maps` and `scripts` folder automatic fix up.
- Perhaps faster due to newer Java 🤷

## Running

Unpack the windows or linux zip depending on your platform and run `bin/pakrat`.

You can also download the jar but you need to install JDK 25.

## Build

Requires JDK 25 & Maven.

```bash
# Creates runtime image in target-directory.
mvn clean verify -Pruntime-image
```
