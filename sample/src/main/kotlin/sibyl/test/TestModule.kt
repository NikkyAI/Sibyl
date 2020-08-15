package sibyl.test

import sibyl.SibylModule

class TestModule : SibylModule(name = "test") {
    override val commands = listOf(
        TestCommand(),
        EchoCommand(),
        WhoAmICommand(),
        ExitCommand()
    )
}

