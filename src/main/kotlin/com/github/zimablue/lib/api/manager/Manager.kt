package com.github.zimablue.lib.api.manager

import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.map.component.Registrable

interface Manager : Registrable<String>, Comparable<Manager> {
    val priority: Int
    val subPoem: SubPoem

    fun onLoad() {
    }

    fun onEnable() {}
    fun onActive() {}

    fun onReload() {}
    fun onDisable() {}

    override fun register() {}

    override fun compareTo(other: Manager): Int {
        return if (priority == other.priority) 0
        else if (priority > other.priority) 1
        else -1
    }

}