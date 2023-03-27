package com.github.zimablue.lib.api.manager.sub.script

import com.github.zimablue.lib.api.manager.Manager
import com.github.zimablue.lib.api.plugin.map.BaseMap
import com.github.zimablue.lib.api.plugin.map.KeyMap
import com.github.zimablue.lib.internal.core.script.nashorn.CompiledScript
import java.io.File

abstract class ScriptManager : Manager, KeyMap<String, CompiledScript>(){
    abstract val global: BaseMap<String,File>
    abstract fun addScript(file: File)
    abstract fun addScriptDir(file: File)
    abstract fun addGlobalDir(dir: File)
}