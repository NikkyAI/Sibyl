## Accounts

creating a account (might be done implicitly)

```
user@irc !accounts create
```

linking procedure

```
user@irc: !account whoami
sibyl: useraccountname-deadbeef69
sibyl: request connecting with `!account connect request useraccountname` from other platforms

user@discord: !account connect request useraccountname
sibyl: please confirm on `irc.esper` by sending `!account connect confirm user discord.guildname`
sibyl: or deny with `account connect deny --all` to cancel all open requests

user@irc: !account connect list
sibyl: open requests: `user @ discord.guildname`
sibyl: confirm with `!account connect confirm user discord.guildname`
sibyl: or deny with `account connect deny user discord.guildname` to cancel this request

user@irc: !account connect confirm user discord.guildname
sibyl: you added `user @ discord.guildname` to your account

```