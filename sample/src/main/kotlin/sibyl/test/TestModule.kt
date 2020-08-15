package sibyl.test

import sibyl.SibylModule

class TestModule : SibylModule(name = "test", description = "experimental functionality") {
    override val commands = listOf(
        TestCommand(),
        EchoCommand(),
        WhoAmICommand(),
        ExitCommand()
    )
}

