package com.github.zimablue.lib.api.plugin

import com.github.zimablue.lib.api.manager.ManagerData
import com.github.zimablue.lib.api.plugin.annotation.AutoRegister
import com.github.zimablue.lib.api.plugin.handler.ClassHandler
import com.github.zimablue.lib.api.plugin.map.KeyMap
import com.github.zimablue.lib.api.plugin.map.component.Registrable
import com.github.zimablue.lib.internal.core.plugin.SubPoemHandler
import com.github.zimablue.lib.util.existClass
import com.github.zimablue.lib.util.instance
import com.github.zimablue.lib.util.plugin.PluginUtils
import com.github.zimablue.lib.util.safe
import com.github.zimablue.lib.util.static
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.reflex.ClassStructure
import taboolib.library.reflex.ReflexClass
import taboolib.platform.util.bukkitPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TotalManager : KeyMap<SubPoem, ManagerData>() {
    internal val pluginData = ConcurrentHashMap<Plugin, SubPoem>()
    val allStaticClasses = ConcurrentHashMap<String, Any>()
    private val allClasses = HashSet<ClassStructure>()

    @Awake(LifeCycle.LOAD)
    fun load() {
        Bukkit.getPluginManager().plugins
            .filter { isDependPouvoir(it) }
            .sortedWith { p1, p2 ->
                if (p1.isDepend(p2)) 1 else -1
            }
            .forEach {
                safe { loadSubPou(it) }
            }
        allClasses.forEach { clazz ->
            handlers.forEach {
                it.inject(clazz)
            }
        }
    }

    private val handlers = LinkedList<ClassHandler>()

    private fun loadSubPou(plugin: Plugin) {
        if (!isDependPouvoir(plugin)) return

        val classes = PluginUtils.getClasses(plugin::class.java).map { ReflexClass.of(it).structure }

        classes.forEach {
            kotlin.runCatching { allStaticClasses[it.simpleName.toString()] = it.owner.static() }
        }
        allClasses.addAll(classes)

        handlers.addAll(classes
            .filter { ClassHandler::class.java.isAssignableFrom(it.owner) && it.simpleName != "ClassHandler" }
            .mapNotNull {
                it.owner.instance as? ClassHandler?
            })

        classes.forEach classFor@{ clazz ->
            //优先加载Managers
            safe { SubPoemHandler.inject(clazz, plugin) }
        }
        pluginData[plugin]?.let {
            ManagerData(it).register()
        }

        classes.filter { clazz ->
            clazz.isAnnotationPresent(AutoRegister::class.java)
        }.forEach { clazz ->
            kotlin.runCatching {
                val auto = clazz.getAnnotation(AutoRegister::class.java)
                val test = auto.property<String>("test") ?: ""
                if ((test.isEmpty() || test.existClass()))
                    (clazz.owner.instance as? Registrable<*>?)?.register()
            }.exceptionOrNull()?.printStackTrace()
        }
        classes
            .forEach { clazz ->
                clazz.fields.forEach { field ->
                    if (field.isAnnotationPresent(AutoRegister::class.java)) {
                        safe {
                            val autoRegister = field.getAnnotation(AutoRegister::class.java)
                            val test = autoRegister.property<String>("test") ?: ""
                            val obj = field.get()
                            if (obj is Registrable<*> && (test.isEmpty() || test.run { if (startsWith("!")) substring(1) else this }
                                    .existClass())) obj.register()
                        }
                    }
                }
            }
    }

    private fun Plugin.isDepend(other: Plugin) =
        description.depend.contains(other.name) || description.softDepend.contains(other.name)

    private fun isDependPouvoir(plugin: Plugin): Boolean {
        return plugin.isDepend(bukkitPlugin) || plugin.name == "Pouvoir"
    }
}