package com.github.skinmanagersample

import com.github.skinmanager.SkinManager

/**
 * 模块:
 * 用途:
 * 作者: Created by NessCure
 * 日期: 2021/3/1
 */
class AnotherClassUseSkin {

    fun test() {
        val skinManager = SkinManager.getInstance()
        val assetsDrawable = skinManager.getDrawable("assetsDrawable")
        //use assetsDrawable
    }
}