package com.denchic45.kts.data.pref.core

import java.util.prefs.Preferences


/**
 * PreferencesFactory implementation that stores the preferences in a user-defined file. To use it,
 * set the system property <tt>java.util.prefs.PreferencesFactory</tt> to
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 *
 *
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the system property
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>
 *
 * @author David Croft ([www.davidc.net](http://www.davidc.net))
 * @version $Id: FilePreferencesFactory.java 282 2009-06-18 17:05:18Z david $
 */

class FilePreferencesFactory(name: String) : java.util.prefs.PreferencesFactory {
    private var rootPreferences: Preferences = FilePreferences(null, name)
    override fun systemRoot(): Preferences {
        return userRoot()
    }

    override fun userRoot(): Preferences {
        return rootPreferences
    }

    companion object {


//        @Throws(java.util.prefs.BackingStoreException::class)
//        @JvmStatic
//        fun main(args: Array<String>) {
//            System.setProperty(
//                "java.util.prefs.PreferencesFactory",
//                FilePreferencesFactory::class.java.name
//            )
//            val p: Preferences =
//                Preferences.userNodeForPackage(FilePreferences::class.java)
//            for (s in p.keys()) {
//                println("p[" + s + "]=" + p.get(s, null))
//            }
//            p.putBoolean("hi", true)
//            p.put("Number", System.currentTimeMillis().toString())
//        }
    }
}