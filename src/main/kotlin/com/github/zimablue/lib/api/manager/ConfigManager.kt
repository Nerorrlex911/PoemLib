package com.github.zimablue.lib.api.manager

import com.github.zimablue.lib.util.plugin.Pair
import com.github.zimablue.lib.api.plugin.SubPoem
import com.github.zimablue.lib.api.plugin.map.BaseMap
import com.github.zimablue.lib.util.loadYaml
import com.github.zimablue.lib.util.safe
import org.bukkit.configuration.file.YamlConfiguration
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.warning
import taboolib.common5.FileWatcher
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.lang.Language
import java.io.File

/**
 * Config manager
 *
 * @constructor Create empty Config manager
 * @property subPoem
 */
abstract class ConfigManager(final override val subPoem: SubPoem) : Manager,
    BaseMap<String, YamlConfiguration>() {
    override val key = "ConfigManager"
    private val fileMap = BaseMap<File, YamlConfiguration>()
    private val watcher = FileWatcher()

    /** Server file */
    val serverFile: File by lazy {
        File(
            getDataFolder().parentFile.absolutePath.toString().replace("\\plugins", "")
        )
    }

    init {
        val map = HashMap<String, Pair<File, YamlConfiguration>>()
        //Init Map
        for (field in subPoem::class.java.fields) {
            if (!field.annotations.any { it.annotationClass.simpleName == "Config" }) continue
            val file = field.get(subPoem).getProperty<File>("file") ?: continue
            map[field.name] = Pair(file, file.loadYaml()!!)
        }
        //Register Config
        map.forEach {
            val key = it.key
            val pair = it.value
            val file = pair.key
            val yaml = pair.value
            fileMap.register(file, yaml)
            this.register(key, yaml)
        }
        for (it in fileMap.keys) {
            if (watcher.hasListener(it)) {
                watcher.removeListener(it)
            }
            watcher.addSimpleListener(it) {
                val yaml = fileMap[it]!!
                yaml.load(it)
                this[it.nameWithoutExtension] = yaml
            }
        }
    }

    override operator fun get(key: String): YamlConfiguration {
        val result = super.get(key) ?: kotlin.run {
            warning("The config $key dose not exist in the SubPouvoir ${subPoem.key}!")
            return YamlConfiguration.loadConfiguration(getDataFolder())
        }
        return result
    }

    /** Sub reload */
    protected open fun subReload() {}

    final override fun onReload() {
        Language.reload()
        subReload()
    }


    /**
     * Create if not exists
     *
     * @param name
     * @param fileNames
     */
    fun create(name: String, vararg fileNames: String) {
        val path = subPoem.plugin.dataFolder.path
        val dir = File("$path/$name")
        dir.mkdir()
        for (fileName in fileNames) {
            safe { subPoem.plugin.saveResource("$name/$fileName", true) }
        }
    }

    /**
     * Create if not exists
     *
     * @param name
     * @param fileNames
     */
    fun createIfNotExists(name: String, vararg fileNames: String) {
        val path = subPoem.plugin.dataFolder.path
        val dir = File("$path/$name")
        if (!dir.exists()) {
            dir.mkdir()
            for (fileName in fileNames) {
                safe { subPoem.plugin.saveResource("$name/$fileName", true) }
            }
        }
    }
}