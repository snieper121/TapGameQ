package com.example.tapgame

import android.app.Application
import android.os.Build
import com.topjohnwu.superuser.Shell
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.core.util.BuildUtils

class TapGameApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR))
        if (Build.VERSION.SDK_INT >= 28) {
            HiddenApiBypass.setHiddenApiExemptions("")
        }
        if (BuildUtils.atLeast30) {
            System.loadLibrary("adb")
        }
    }
}

