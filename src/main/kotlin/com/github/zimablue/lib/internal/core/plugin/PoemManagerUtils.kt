package com.github.zimablue.lib.internal.core.plugin

import com.github.zimablue.lib.api.manager.Manager
import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.annotation.PoemManager
import com.github.zimablue.lib.util.instance
import taboolib.common.platform.function.warning
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object PoemManagerUtils {
    private fun isPoemManagerField(field: Field) = field.isAnnotationPresent(PoemManager::class.java)

    fun SubPoem.getPoemManagers(): Set<Manager> = this.javaClass.fields
        .filter { field -> isPoemManagerField(field) && field.get(this) != null }
        .map { field -> field.get(this) as Manager }
        .toSet()

    internal fun initPoemManagers(clazz: Class<*>): SubPoem? {
        val fields = clazz.fields
        val subPoem = clazz.instance as? SubPoem? ?: return null
        for (field in fields.filter { field -> isPoemManagerField(field) }) {
            field.isAccessible = true
            val mainPackage = subPoem.javaClass.`package`?.name
            val managerName = field.type.simpleName

            val PoemManagerClass = field.type

            val implClass: Class<*> =
                if (!Modifier.isAbstract(PoemManagerClass.modifiers)) PoemManagerClass
                else kotlin.runCatching { Class.forName("$mainPackage.internal.manager.${managerName}Impl") }
                    .getOrNull() ?: continue
            val PoemManager = implClass.instance
            if (PoemManager == null) {
                warning("Can't find the PoemManager ImplClass ${implClass.name} !")
                continue
            }
            field.set(subPoem, PoemManager)
        }
        return subPoem
    }

}