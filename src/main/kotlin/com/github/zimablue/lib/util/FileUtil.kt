package com.github.zimablue.lib.util

import com.github.zimablue.lib.PoemLib
import com.github.zimablue.lib.util.plugin.Pair
import com.github.zimablue.lib.util.script.ColorUtil.uncolored
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.module.lang.sendLang
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 * IO相关工具类
 *
 * @constructor Create empty File utils
 */


fun File.pathNormalize(): String {
    return absolutePath.replace(PoemLib.configManager.serverFile.absolutePath, "").replace("\\", "/")
}

@Suppress("UNCHECKED_CAST")

fun <T : ConfigurationSerializable> loadMultiply(mainFile: File, clazz: Class<T>): List<Pair<T, File>> {
    return mainFile.run {
        val list = LinkedList<Pair<T, File>>()
        for (file in listSubFiles().filter { it.extension == "yml" }) {
            val config = file.loadYaml() ?: continue
            for (key in config.getKeys(false)) {
                try {
                    list.add(
                        Pair(
                            (clazz.getMethod(
                                "deserialize",
                                ConfigurationSection::class.java
                            ).invoke(null, config[key]!!) as? T? ?: continue), file
                        )
                    )
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
        list
    }
}


fun ConfigurationSection.toMap(): Map<String, Any> {
    val newMap = HashMap<String, Any>()
    for (it in this.getKeys(false)) {
        val value = this[it]
        if (value is ConfigurationSection) {
            newMap[it] = value.toMap()
            continue
        }
        newMap[it] = value!!
    }
    return newMap
}


fun File.loadYaml(): YamlConfiguration? {
    val config = YamlConfiguration()
    try {
        config.load(this)
    } catch (e: Throwable) {
        console().sendLang("wrong-config", name)
        console().sendLang("wrong-config-cause", uncolored(e.message ?: "null"))
        e.printStackTrace()
        return null
    }
    return config
}


val serverFolder = getDataFolder().parentFile.parentFile


fun File.listSubFiles(): List<File> {
    val files: MutableList<File> = ArrayList()
    if (isDirectory) {
        listFiles()?.forEach { files.addAll(it.listSubFiles()) }
    } else {
        files.add(this)
    }
    return files
}


fun File.loadYamls(): List<YamlConfiguration> {
    val yamls = LinkedList<YamlConfiguration>()
    for (subFile in listSubFiles()) {
        if (subFile.isFile && subFile.name.endsWith(".yml")) {
            yamls.add(subFile.loadYaml() ?: continue)
            continue
        }
        if (subFile.isDirectory)
            yamls.addAll(subFile.loadYamls())
    }
    return yamls

}


fun File.md5(): String? {
    val bi: BigInteger
    try {
        val buffer = ByteArray(8192)
        var len: Int
        val md = MessageDigest.getInstance("MD5")
        val fis = this.inputStream()
        while ((fis.read(buffer).also { len = it }) != -1) {
            md.update(buffer, 0, len)
        }
        fis.close()
        val b = md.digest()
        bi = BigInteger(1, b)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return bi.toString(16)
}

/**
 * 获取文件夹内所有文件
 *
 * @param dir 待获取文件夹
 * @return 文件夹内所有文件
 */

fun getAllFiles(dir: File): ArrayList<File> {
    val list = ArrayList<File>()
    val files = dir.listFiles() ?: arrayOf<File>()
    for (file: File in files) {
        if (file.isDirectory) {
            list.addAll(getAllFiles(file))
        } else {
            list.add(file)
        }
    }
    return list
}
/**
 * 获取文件夹内所有文件
 *
 * @param dir 待获取文件夹路径
 * @return 文件夹内所有文件
 */

fun getAllFiles(dir: String): ArrayList<File> {
    return getAllFiles(File(getDataFolder(), File.separator + dir))
}

fun charset(file: File): String {
    var charset = "GBK"
    val first3Bytes = ByteArray(3)
    try {
        var checked = false
        val bis = BufferedInputStream(FileInputStream(file))
        bis.mark(0)
        var read = bis.read(first3Bytes, 0, 3)
        if (read == -1) {
            bis.close()
            return charset
        } else if (first3Bytes[0] == 0xFF.toByte() && first3Bytes[1] == 0xFE.toByte()) {
            charset = "UTF-16LE"
            checked = true
        } else if (first3Bytes[0] == 0xFE.toByte() && first3Bytes[1] == 0xFF.toByte()) {
            charset = "UTF-16BE"
            checked = true
        } else if (first3Bytes[0] == 0xEF.toByte() && first3Bytes[1] == 0xBB.toByte() && first3Bytes[2] == 0xBF.toByte()) {
            charset = "UTF-8"
            checked = true
        }
        bis.reset()
        if (!checked) {
            while (bis.read().also { read = it } != -1) {
                if (read >= 0xF0) break
                if (read in 0x80..0xBF)
                    break
                if (read in 0xC0..0xDF) {
                    read = bis.read()
                    if (read in 0x80..0xBF)
                        continue else break
                } else if (read in 0xE0..0xEF) {
                    read = bis.read()
                    if (read in 0x80..0xBF) {
                        read = bis.read()
                        if (read in 0x80..0xBF) {
                            charset = "UTF-8"
                            break
                        } else break
                    } else break
                }
            }
        }
        bis.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return charset
}

fun File.getScriptName() = path.replace(
    "plugins${File.separator}",
    ""
)