package com.github.zimablue.lib.internal.manager



import com.github.zimablue.lib.PoemLib
import com.github.zimablue.lib.PoemLib.configManager
import com.github.zimablue.lib.api.manager.sub.script.ScriptManager
import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.map.BaseMap
import com.github.zimablue.lib.internal.manager.PoemConfig.debug
import com.github.zimablue.lib.internal.core.script.nashorn.CompiledScript
import com.github.zimablue.lib.internal.core.script.nashorn.NashornHooker
import com.github.zimablue.lib.internal.core.script.nashorn.impl.LegacyNashornHookerImpl
import com.github.zimablue.lib.internal.core.script.nashorn.impl.NashornHookerImpl
import com.github.zimablue.lib.util.*
import taboolib.common.platform.function.getDataFolder
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.text.charset


/**
 * 脚本文件管理器, 用于管理所有js节点的脚本文件, 同时提供公用ScriptEngine用于解析公式节点内容
 *
 * @constructor 构建脚本文件管理器
 */
object ScriptManagerImpl: ScriptManager() {

    override val key: String = "ScriptManager"
    override val priority: Int = 5
    override val subPoem: SubPoem = PoemLib
    override val global = BaseMap<String,File>()
    val globalDirs = configManager["script"].getStringList("global-scripts")
    /**
     * 获取该脚本对应的ScriptEngine
     */
    val nashornHooker: NashornHooker =
        try {
            Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")
            // jdk自带nashorn
            LegacyNashornHookerImpl()
        } catch (error: Throwable) {
            // 主动下载nashorn
            NashornHookerImpl()
        }

    /**
     * 获取公用ScriptEngine
     */
    val scriptEngine = nashornHooker.getNashornEngine()

    /**
     * 加载全部脚本
     */
    private fun loadScripts() {
        addGlobalDir(File(getDataFolder(),"scripts/core"))
        addScriptDir(File(getDataFolder(),"scripts"))
    }

    override fun addScript(file: File) {
        if (file.isDirectory) {
            addScriptDir(file)
            return
        }
        safe { CompiledScript(file).apply {
            register()
            for((_,globalScript) in global) {
                scriptEngine.eval(InputStreamReader(FileInputStream(globalScript), charset(globalScript)))
            }
        } }
    }

    override fun addScriptDir(file: File) {
        if(!file.isDirectory) return
        if(globalDirs.any { file.getScriptName().startsWith(it) }) {
            addGlobalDir(file)
            return
        }
        file.listSubFiles().forEach {
            addScript(it)
        }
    }

    override fun addGlobalDir(dir: File) {
        for(file in getAllFiles(dir)) {
            val fileName = file.path.replace(
                "plugins${File.separator}",
                ""
            )
            for ((name,script) in this) {
                script.scriptEngine.eval(InputStreamReader(FileInputStream(file), charset(file)))
                debug("global script $file loaded for $name")
            }
            global.register(fileName,file)
        }
    }

    override fun onEnable() {
        onReload()
    }

    /**
     * 重载脚本管理器
     */
    override fun onReload() {
        clear()
        global.clear()
        // 加载脚本
        debug("加载脚本")
        loadScripts()
    }
}
