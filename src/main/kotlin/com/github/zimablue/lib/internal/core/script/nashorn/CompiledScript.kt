package com.github.zimablue.lib.internal.core.script.nashorn


import com.github.zimablue.lib.internal.manager.PoemConfig.debug
import com.github.zimablue.lib.internal.manager.ScriptManagerImpl.nashornHooker
import com.github.zimablue.lib.util.charset
import com.github.zimablue.lib.PoemLib.scriptManager
import com.github.zimablue.lib.api.plugin.map.component.Registrable
import com.github.zimablue.lib.util.getScriptName
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.script.ScriptEngine

class CompiledScript : Registrable<String>{

    override var key: String
    /**
     * 获取已编译脚本
     */
    private val compiledScript: javax.script.CompiledScript

    /**
     * 获取该脚本对应的ScriptEngine
     */
    val scriptEngine: ScriptEngine

    /**
     * 编译js脚本并进行包装, 便于调用其中的指定函数
     *
     * @property file js脚本文件
     * @constructor 编译js脚本并进行包装
     * */
    constructor(file: File){
        key = file.getScriptName()
        compiledScript = nashornHooker.compile(InputStreamReader(FileInputStream(file), charset(file)))
        scriptEngine = compiledScript.engine
        for ((globalFileName,globalFile) in scriptManager.global) {
            scriptEngine.eval(InputStreamReader(FileInputStream(globalFile), charset(globalFile)))
            debug("global script $globalFileName loaded for ${file.name}")
        }
        magicFunction()
    }

    /**
     * 编译js脚本并进行包装, 便于调用其中的指定函数
     *
     * @property script js脚本文本
     * @constructor 编译js脚本并进行包装
     * */
    constructor(name: String,script: String){
        key = name
        compiledScript = nashornHooker.compile(script)
        scriptEngine = compiledScript.engine
        magicFunction()
    }

    /**
     * 执行脚本中的指定函数
     *
     * @param function 函数名
     * @param map 传入的默认对象
     * @param args 传入对应方法的参数
     * @return 解析值
     */
    fun invoke(function: String, map: MutableMap<String, Any>?, vararg args: Any): Any? {
        return nashornHooker.invoke(this, function, map, *args)
    }

    override fun register() {
        scriptManager.register(this)
    }
    /**
     * 此段代码用于解决js脚本的高并发调用问题, 只可意会不可言传
     */
    private fun magicFunction() {
        compiledScript.eval()
        scriptEngine.eval("""
            function SealCraftNumberOne() {}
            SealCraftNumberOne.prototype = this
            function newObject() { return new SealCraftNumberOne() }
        """)
    }
}