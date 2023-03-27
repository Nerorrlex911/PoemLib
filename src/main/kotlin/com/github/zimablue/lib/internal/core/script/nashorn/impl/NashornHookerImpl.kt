package com.github.zimablue.lib.internal.core.script.nashorn.impl

import com.github.zimablue.lib.internal.core.script.nashorn.NashornHooker
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import java.io.Reader
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.Invocable
import javax.script.ScriptEngine

@RuntimeDependencies(
    RuntimeDependency(
        "!org.openjdk.nashorn:nashorn-core:15.4",
        test = "!jdk.nashorn.api.scripting.NashornScriptEngineFactory"
    )
)
/**
 * openjdk nashorn挂钩
 *
 * @constructor 启用openjdk nashorn挂钩
 */
class NashornHookerImpl : NashornHooker() {
    override fun getNashornEngine(): ScriptEngine {
        return NashornScriptEngineFactory().getScriptEngine(arrayOf("-Dnashorn.args=--language=es6"), this::class.java.classLoader)
    }

    override fun compile(string: String): CompiledScript {
        return (NashornScriptEngineFactory().getScriptEngine(arrayOf("-Dnashorn.args=--language=es6"), this::class.java.classLoader) as Compilable).compile(string)
    }

    override fun compile(reader: Reader): CompiledScript {
        return (NashornScriptEngineFactory().getScriptEngine(arrayOf("-Dnashorn.args=--language=es6"), this::class.java.classLoader) as Compilable).compile(reader)
    }

    override fun invoke(compiledScript: com.github.zimablue.lib.internal.core.script.nashorn.CompiledScript, function: String, map: MutableMap<String, Any>?, vararg args: Any): Any? {
        val newObject: ScriptObjectMirror = (compiledScript.scriptEngine as Invocable).invokeFunction("newObject") as ScriptObjectMirror
        map?.forEach { (key, value) -> newObject[key] = value }
        return newObject.callMember(function, *args)
    }
}
