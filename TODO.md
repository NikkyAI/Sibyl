## TODO:

- [ ] url previews
- [ ] database stuff
  - [x] each module on a separate schema
  - [x] setup postgres (pgdata on docker)
  - [x] figure out migrations
  - [ ] figure out migration test data
- [ ] add errorhandling to module installation and interceptor invocation  
      log errors and skip modules
- reminders
  - [x] `!remind in <DURATION> <MESSAGE>`
  - [ ] `!remind at <TIMESTAMP> <MESSAGE>`
- [ ] leaving messages for offline people
  - [ ] identifying people across platforms
    - command based handshake
    - generating 2 part tokens
    - sending first token in pairing request
    - sending second token in pairing accept
    - verify tokens cryptographically
- [x] publish build-logic as plugin, so custom modules can reuse it
- [ ] publish to oss sonatype (look at sqldelight as example)
- [ ] sibyl-launcher (kotlin-scripting based executable jar)


### planned features and stuff

- [x] chatlogs
- commands
  - reminders (cronjob?)
- [x] dice rolling (on any message)
- url previews
- quotes (lookup random quotes, or by id or search for keywords)
- special interaction system
  - fondeness tracker (give her cookies and flowers)
  - remember nicknames (accounts)
