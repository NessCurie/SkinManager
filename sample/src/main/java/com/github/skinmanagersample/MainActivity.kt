package com.github.skinmanagersample

import com.github.skinmanager.SkinActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SkinActivity() {

    override fun onCreate() {
        setContentView(R.layout.activity_main)
        setDefaultFolder("Example")

        setBackground(ivAssets1, "assetsDrawable")
        setTextColor(tv1)
        setResBackground(ivRes1, "res_drawable")

        checkSkinPath() //for launcher check
    }

    override fun onResourceComplete() {
        super.onResourceComplete()
        AnotherClassUseSkin().test()

        val assetsDrawable = getDrawable("assetsDrawable")
        ivAssets2.background = assetsDrawable

        tv2.setTextColor(colorPrimary)

        ivRes2.background = getResDrawable("res_drawable")

        val colorBackgroundDark = getColor("colorBackgroundDark")
        llWhole.setBackgroundColor(colorBackgroundDark)

        val assetsDrawableJPG = getJPGDrawable("assetsDrawable")

        changeSkin(DEFAULT_PATH_US) {}  //for changeSKin

    }
}