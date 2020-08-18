## Sibyl

modular matterbridge bot framework

[![Bintray](https://img.shields.io/bintray/v/nikkyai/github/sibyl?style=for-the-badge)](https://bintray.com/nikkyai/github/sibyl)
[![Bintray](https://img.shields.io/bintray/v/nikkyai/github/sibyl-dev?style=for-the-badge)](https://bintray.com/nikkyai/github/sibyl-dev)


### setup

TODO: publish to bintray, so i can write about how you can add it as dependency here

### running the sample application

start matterbridge, with api on port 4242, and no authorization token, see [matterbridge/matterbridge-sample.toml](./matterbridge/matterbridge-sample.toml)

run 
```bash
./gradlew :sample:run
```

#### TODO:

- url previews
- database stuff
  - each module on a separate schema
  - setup postgres (pgdata on docker)
  - figure out migrations
- reminders
- leaving messages for offline people


TODO list features

- chatlogs
- commands
  - reminders (cronjob?)
- dice rolling (on any message)
- url previews
- quotes (lookup random quotes, or by id or search for keywords)
- special interaction system
  - fondeness tracker (give her cookies and flowers)
  - remember nicknames (accounts)
