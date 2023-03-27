package com.github.zimablue.lib.internal.core.plugin

import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.TotalManager
import org.bukkit.plugin.Plugin
import taboolib.library.reflex.ClassStructure

object SubPoemHandler {
    fun inject(clazz: ClassStructure, plugin: Plugin) {
        val owner = clazz.owner
        if (SubPoem::class.java.isAssignableFrom(owner) && clazz.simpleName != "SubPouvoir")
            TotalManager.pluginData[plugin] = PoemManagerUtils.initPoemManagers(owner) ?: return
    }
}
