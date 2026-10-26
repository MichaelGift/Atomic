package com.myth.atomic.data.local

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Upgrades databases built from the exported schemas in app/schemas, as a real install would. */
@RunWith(RobolectricTestRunner::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AtomicDatabase::class.java)

    @Test
    fun version1To2KeepsGoalsAndGivesThemTheDefaultColor() {
        helper.createDatabase(dbPath, 1).apply {
            execSQL(
                "INSERT INTO goals (id, title, description, deadline, createdAt, completedAt) " +
                    "VALUES (1, 'Run a 10k', '', NULL, 0, NULL)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbPath, 2, true)

        db.query("SELECT title, colorIndex FROM goals WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals("Run a 10k", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    // An absolute path: Room's driver matches the requested file against this name, and a bare
    // name fails that check on Windows, where Robolectric's paths use backslashes.
    private val dbPath = ApplicationProvider.getApplicationContext<Context>().getDatabasePath("migration-test.db").absolutePath
}
