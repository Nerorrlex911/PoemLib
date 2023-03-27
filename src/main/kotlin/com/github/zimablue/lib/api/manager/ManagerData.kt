package com.github.zimablue.lib.api.manager

import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.TotalManager
import com.github.zimablue.lib.api.plugin.map.KeyMap
import com.github.zimablue.lib.api.plugin.map.SingleExecMap
import com.github.zimablue.lib.api.plugin.map.component.Registrable
import com.github.zimablue.lib.internal.core.plugin.PoemManagerUtils.getPoemManagers
import com.github.zimablue.lib.util.safe
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.function.submit
import java.util.*

class ManagerData(val subPoem: SubPoem) : KeyMap<String, Manager>(), Registrable<SubPoem> {
    private val managers = ArrayList<Manager>()
    val plugin: JavaPlugin = subPoem.plugin
    override val key: SubPoem = subPoem

    override fun register(key: String, value: Manager) {
        super.register(key, value)
        managers.add(value)
        managers.sort()
    }

    init {
        for (manager in subPoem.getPoemManagers()) {
            this.register(manager)
        }
        val dataField = subPoem.javaClass.getField("managerData")
        dataField.set(subPoem, this)
    }

    override fun register() {
        TotalManager.register(subPoem, this)
    }

    fun load() {
        managers.forEach {
            safe(it::onLoad)
        }
    }

    fun enable() {
        managers.forEach {
            safe(it::onEnable)
        }
    }

    fun active() {
        managers.forEach {
            safe(it::onActive)
        }
    }

    private var onReload = SingleExecMap()
    fun reload() {
        submit(async = true) {
            managers.forEach {
                safe(it::onReload)
            }
            onReload.values.forEach { it() }
        }
    }

    fun onReload(key: String = UUID.randomUUID().toString(), exec: () -> Unit) {
        onReload[key] = exec
    }

    fun disable() {
        managers.forEach {
            safe(it::onDisable)
        }
    }

}