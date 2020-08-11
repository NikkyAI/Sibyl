package modules.test

import modules.SibylModule

class TestModule : SibylModule(name = "test") {
    override val commands = listOf(
        TestCommand(),
        EchoCommand(),
        WhoAmICommand()
    )
}

