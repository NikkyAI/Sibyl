## Sibyl

modular matterbridge bot framework

[![Bintray](https://img.shields.io/bintray/v/nikkyai/github/sibyl?style=for-the-badge)](https://bintray.com/nikkyai/github/sibyl)
[![Bintray](https://img.shields.io/bintray/v/nikkyai/snapshot/sibyl?style=for-the-badge)](https://bintray.com/nikkyai/snapshot/sibyl)


### Design

THe chatbot is equally accessible on all chat platforms supported by [matterbridge](https://github.com/42wim/matterbridge)  
users from all platforms can use the bot the same way. There is no special features forcing users to switch platforms introduced.

Features (Modules) are designed to be written modularly with only the minimal dependency of core
which also provides basic functionality like parsing configs, providing a database connection and more generic utilities

All messages transformations happens though Interceptors registered on the incoming and outgoing pipelines  
Interceptors are (simplified): `(T) -> T?` transformation functions that can modify or consume the message as it passes through them  
Interceptors are registered along with stages, all interceptors on a stage are processed before the next stage is processed  
All Modules can register their interceptors at any level

Commands are written using https://ajalt.github.io/clikt  
The Core Module processes command parsing and invocation  
Modules can register any number of top level and subcommands

Modules can check for the existence of modules they depend on
and possibly register the dependencies or skip loading (TODO: implement that)

Live Reloading or disabling modules after being enabled is not planned, simplifying the design  
if you need to change modules just restart  
the matterbridge will keep running and provide missed backlog to the bot

### Usage

Usage as a dependency

```
repositories {
    maven(url = "https://dl.bintray.com/nikkyai/github") {
        name = "bintray-nikky"
    }
    maven(url = "https://dl.bintray.com/nikkyai/snapshot") {
        name = "bintray-nikky-snapshot"
    }
}

dependencies {
    implementation("moe.nikky.sibyl:core:_")
}
```

### Setup

development environment setup

```bash
# required once before starting the postgres container
# required because docker on windows cannot get permissions right on mounted folders
docker volume create sibyl_pgdata

# starting all required services
docker-compose up -d

# applying migrations, baseline is only required once and after flywayClean
./gradlew flywayBaseline
./gradlew flywayMigrate
./gradlew flywayValidate
```

now you should be ready to start hacking away

### Running the sample application

1. follow [#setup](#setup)
2. start matterbridge  
    make sure to have a api section configured
    see [matterbridge/matterbridge-sample.toml](./matterbridge/matterbridge-sample.toml) as reference
3. run `./gradlew :sample:run` once, expect it to fail, this is just to generate the first few config files
4. edit `run/sample.json`  
   ```json
   {
       "$schema": "./schemas/sample.schema.json",
       "host": "localhost",
       "token": "mytoken",
       "useWebsocket": false,
       "port": 4242
   }
   ```
   all config files have referenced json schemas,
   so that IDEs (vscode, IJ idea, etc) are able to provide smart code completion
5. run `./gradlew :sample:run`

