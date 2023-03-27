package com.github.zimablue.lib

import com.github.zimablue.lib.api.manager.ManagerData
import com.github.zimablue.lib.api.manager.sub.script.ScriptManager
import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.annotation.PoemManager
import com.github.zimablue.lib.internal.manager.PoemConfig
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import taboolib.platform.util.bukkitPlugin

object PoemLib : Plugin(),SubPoem {

    override val key = "PoemLib"

    override val plugin: JavaPlugin by lazy {
        bukkitPlugin
    }
    /** Config */
    @Config(migrate = true, autoReload = true)
    lateinit var config: ConfigFile

    @Config("script.yml", true, autoReload = true)
    lateinit var script: ConfigFile

    /** Managers */
    override lateinit var managerData: ManagerData

    @JvmStatic
    @PoemManager
    lateinit var configManager: PoemConfig

    @JvmStatic
    @PoemManager
    lateinit var scriptManager: ScriptManager

    override fun onLoad() {
        load()
    }

    override fun onEnable() {
        enable()
    }

    override fun onActive() {
        active()
    }

    override fun onDisable() {
        disable()
    }
}