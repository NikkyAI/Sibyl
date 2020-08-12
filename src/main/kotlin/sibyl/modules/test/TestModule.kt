package sibyl.modules.test

import sibyl.modules.SibylModule

class TestModule : SibylModule(name = "test") {
    override val commands = listOf(
        TestCommand(),
        EchoCommand(),
        WhoAmICommand()
    )
}

