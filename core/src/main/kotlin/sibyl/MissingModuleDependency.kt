package sibyl

import java.lang.IllegalStateException

class MissingModuleDependency(val missing: List<String>) : IllegalStateException("requires modules: $missing")
