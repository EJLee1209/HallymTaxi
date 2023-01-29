package com.dldmswo1209.hallymtaxi.common

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Base64
import com.kakao.util.maps.helper.Utility
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object HashKeyStore {

    fun getKeyHashBase64(context: Context?): String? {
        val packageInfo: PackageInfo =
            Utility.getPackageInfo(context, PackageManager.GET_SIGNATURES)
                ?: return null
        for (signature in packageInfo.signatures) {
            try {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.DEFAULT)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
        return null
    }
}