package com.example.rotator.core

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

class PreferencesManagerTest {

    private val inMemoryPrefs = HashMap<String, Any>()
    private lateinit var mockPrefs: SharedPreferences

    @Before
    fun setUp() {
        inMemoryPrefs.clear()

        val sharedPreferencesHandler = java.lang.reflect.InvocationHandler { _, method, args ->
            when (method.name) {
                "getString" -> inMemoryPrefs[args[0] as String] as? String ?: args[1]
                "getInt" -> (inMemoryPrefs[args[0] as String] as? Number)?.toInt() ?: (args[1] as Int)
                "getBoolean" -> inMemoryPrefs[args[0] as String] as? Boolean ?: (args[1] as Boolean)
                "edit" -> createEditorProxy()
                else -> null
            }
        }

        mockPrefs = Proxy.newProxyInstance(
            SharedPreferences::class.java.classLoader,
            arrayOf(SharedPreferences::class.java),
            sharedPreferencesHandler
        ) as SharedPreferences
    }

    private fun createEditorProxy(): SharedPreferences.Editor {
        val editorHandler = java.lang.reflect.InvocationHandler { proxy, method, args ->
            when (method.name) {
                "putString" -> {
                    inMemoryPrefs[args[0] as String] = args[1]
                    proxy
                }
                "putInt" -> {
                    inMemoryPrefs[args[0] as String] = (args[1] as Number).toInt()
                    proxy
                }
                "putBoolean" -> {
                    inMemoryPrefs[args[0] as String] = args[1]
                    proxy
                }
                "apply", "commit" -> true
                else -> proxy
            }
        }

        return Proxy.newProxyInstance(
            SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java),
            editorHandler
        ) as SharedPreferences.Editor
    }

    @Test
    fun testDefaultOverlayCustomizations() {
        val prefs = PreferencesManager(mockPrefs)
        assertEquals(90, prefs.overlayOpacity)
        assertEquals(0.90f, prefs.overlayAlpha, 0.001f)
        assertTrue(prefs.autoCollapseIdle)
        assertEquals(5, prefs.autoCollapseTimeoutSeconds)
        assertTrue(prefs.snapToEdge)
    }

    @Test
    fun testOpacityCoercion() {
        val prefs = PreferencesManager(mockPrefs)
        prefs.overlayOpacity = 150
        assertEquals(100, prefs.overlayOpacity)
        assertEquals(1.0f, prefs.overlayAlpha, 0.001f)

        prefs.overlayOpacity = 10
        assertEquals(20, prefs.overlayOpacity)
        assertEquals(0.20f, prefs.overlayAlpha, 0.001f)

        prefs.overlayOpacity = 75
        assertEquals(75, prefs.overlayOpacity)
        assertEquals(0.75f, prefs.overlayAlpha, 0.001f)
    }

    @Test
    fun testAutoCollapseTimeoutCoercion() {
        val prefs = PreferencesManager(mockPrefs)
        prefs.autoCollapseTimeoutSeconds = 1
        assertEquals(3, prefs.autoCollapseTimeoutSeconds)

        prefs.autoCollapseTimeoutSeconds = 50
        assertEquals(30, prefs.autoCollapseTimeoutSeconds)

        prefs.autoCollapseTimeoutSeconds = 10
        assertEquals(10, prefs.autoCollapseTimeoutSeconds)
    }

    @Test
    fun testToggleSnapToEdge() {
        val prefs = PreferencesManager(mockPrefs)
        assertTrue(prefs.snapToEdge)

        prefs.snapToEdge = false
        assertFalse(prefs.snapToEdge)
    }

    @Test
    fun testHasSeenPermissionRationale() {
        val prefs = PreferencesManager(mockPrefs)
        assertFalse(prefs.hasSeenPermissionRationale)

        prefs.hasSeenPermissionRationale = true
        assertTrue(prefs.hasSeenPermissionRationale)
    }
}
