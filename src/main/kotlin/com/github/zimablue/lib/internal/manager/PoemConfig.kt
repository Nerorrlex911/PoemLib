package com.github.zimablue.lib.internal.manager

import com.github.zimablue.lib.PoemLib
import com.github.zimablue.lib.api.manager.ConfigManager
import taboolib.common.platform.function.info

object PoemConfig : ConfigManager(PoemLib) {
    override val priority: Int = 0

    val debugEnable: Boolean
        get() = this["config"].getBoolean("options.debug")

    override fun onLoad() {
        createIfNotExists("scripts","example.js")
    }
    @JvmStatic
    fun debug(debug: () -> Unit) {
        if (debugEnable) {
            debug.invoke()
        }
    }
    @JvmStatic
    fun debug(vararg debug: String) {
        if (debugEnable) {
            info(debug)
        }
    }
}