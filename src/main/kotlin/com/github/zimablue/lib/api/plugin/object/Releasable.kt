package com.github.zimablue.lib.api.plugin.`object`

/**
 * @className Releasable
 *
 * @author Glom
 * @date 2023/1/22 22:56 Copyright 2023 user. All rights reserved.
 */
interface Releasable {
    /** 是否在重载时注销 */
    var release: Boolean

    fun unregister()
}