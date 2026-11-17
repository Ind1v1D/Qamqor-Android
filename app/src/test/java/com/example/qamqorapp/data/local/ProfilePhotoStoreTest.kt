package com.example.qamqorapp.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProfilePhotoStoreTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val dir get() = File(tmp.root, "profile").apply { mkdirs() }
    private fun store() = ProfilePhotoStore(dir, "test.fileprovider")
    private fun capture(text: String) = File(dir, "avatar_tmp.jpg").writeText(text)

    @Test
    fun commitCapture_replacesPhoto_andBumpsVersion() {
        val store = store()
        assertNull(store.photoFile())
        assertEquals(0L, store.version.value)

        capture("jpeg")

        assertTrue(store.commitCapture())
        assertNotNull(store.photoFile())
        assertTrue(store.version.value > 0L)
        assertFalse(File(dir, "avatar_tmp.jpg").exists())
    }

    @Test
    fun commitCapture_withoutCapture_returnsFalse() {
        assertFalse(store().commitCapture())
    }

    @Test
    fun discardCapture_keepsOldPhoto() {
        val store = store()
        capture("old")
        store.commitCapture()
        val version = store.version.value

        capture("new")
        store.discardCapture()

        assertEquals("old", store.photoFile()?.readText())
        assertEquals(version, store.version.value)
        assertFalse(File(dir, "avatar_tmp.jpg").exists())
    }

    @Test
    fun failedCommit_keepsOldAvatar_andDiscardsTemp() {
        val avatarBlocker = File(dir, "avatar.jpg").apply { mkdirs() }
        File(avatarBlocker, "old.txt").writeText("old")
        val store = store()
        val version = store.version.value
        capture("new")

        assertFalse(store.commitCapture())

        assertEquals("old", File(avatarBlocker, "old.txt").readText())
        assertEquals(version, store.version.value)
        assertFalse(File(dir, "avatar_tmp.jpg").exists())
        assertFalse(File(dir, "avatar_new.jpg").exists())
    }

    @Test
    fun delete_removesPhoto() {
        val store = store()
        capture("x")
        store.commitCapture()

        store.delete()

        assertNull(store.photoFile())
        assertEquals(0L, store.version.value)
    }
}
