package com.github.zimablue.lib.api.plugin

import com.github.zimablue.lib.api.manager.ManagerData
import com.github.zimablue.lib.api.plugin.map.component.Registrable
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

interface SubPoem : Registrable<String> {
    var managerData: ManagerData
    val plugin: JavaPlugin


    fun load() {
        console().sendLang("plugin-load", key)
        managerData.load()
    }

    fun enable() {
        console().sendLang("plugin-enable", key)
        managerData.enable()
    }

    fun active() {
        managerData.active()
    }

    fun disable() {
        console().sendLang("plugin-disable", key)
        managerData.disable()
    }

    override fun register() {
        TotalManager.register(this.managerData)
    }

    fun reload() {
        managerData.reload()
    }
}