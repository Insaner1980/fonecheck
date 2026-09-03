package com.insaner.fonecheck.ui.screens.storage

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class StoragePresentationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun normalOverviewShowsEachMeasuredCapacityOnceAndKeepsPrivateAccessOutsideTheHeadline() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            StorageOverviewSection(storageInfo())
        }

        composeRule.onNodeWithText("24", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("% used", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("%", useUnmergedTree = true).assertDoesNotExist()
        composeRule.onAllNodesWithText("58.93 GB", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithText("187 GB", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithText("246 GB", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onNodeWithText(context.getString(R.string.storage_private_access)).assertDoesNotExist()
    }

    @Test
    fun storageAccessShowsPrivateAccessAndHighConfidence() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            StorageAccessSection(storageInfo())
        }

        composeRule.onNodeWithText(context.getString(R.string.storage_private_access)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.confidence_high)).assertExists()
    }

    @Test
    fun overviewWithoutUsagePercentOmitsTheHeadline() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            StorageOverviewSection(storageInfo(usagePercent = null))
        }

        composeRule.onNodeWithText("24", useUnmergedTree = true).assertDoesNotExist()
        composeRule
            .onNodeWithText(
                context.getString(R.string.storage_usage_unit),
                ignoreCase = true,
                useUnmergedTree = true,
            ).assertDoesNotExist()
        composeRule.onNodeWithText("246 GB", useUnmergedTree = true).assertExists()
    }

    @Test
    fun emptyVolumesKeepTheUnavailableNote() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            StorageVolumesSection(emptyList())
        }

        composeRule.onNodeWithText(context.getString(R.string.storage_no_shared_volumes)).assertIsDisplayed()
    }

    @Test
    fun primaryNonRemovableVolumeDoesNotRepeatOverviewCapacity() {
        val context = localizedContext(Locale.ENGLISH)
        val primaryVolume =
            AppStorageVolumeInfo(
                isPrimary = true,
                isRemovable = false,
                stateCode = "mounted",
                isMounted = true,
                totalBytes = TOTAL_BYTES,
                availableBytes = AVAILABLE_BYTES,
            )

        render(context) {
            StorageOverviewSection(storageInfo())
            StorageAccessSection(storageInfo().copy(appAccessibleVolumes = listOf(primaryVolume)))
            StorageVolumesSection(listOf(primaryVolume))
        }

        composeRule.onNodeWithText(context.getString(R.string.storage_volume_primary)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_state_mounted)).assertExists()
        composeRule.onAllNodesWithText("246 GB", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithText("187 GB", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun primaryRemovableAndNonPrimaryNonRemovableVolumesEachKeepTheirStateAndCapacityRows() {
        val context = localizedContext(Locale.ENGLISH)

        render(context) {
            StorageVolumesSection(
                listOf(
                    AppStorageVolumeInfo(
                        isPrimary = true,
                        isRemovable = true,
                        stateCode = "mounted",
                        isMounted = true,
                        totalBytes = REMOVABLE_PRIMARY_TOTAL_BYTES,
                        availableBytes = REMOVABLE_PRIMARY_AVAILABLE_BYTES,
                    ),
                    AppStorageVolumeInfo(
                        isPrimary = false,
                        isRemovable = false,
                        stateCode = "mounted_ro",
                        isMounted = true,
                        totalBytes = ADDITIONAL_TOTAL_BYTES,
                        availableBytes = ADDITIONAL_AVAILABLE_BYTES,
                    ),
                ),
            )
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.storage_volume_primary))
            .assertExists()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.storage_volume_number, 2))
            .assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_mounted_state)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_state_mounted)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_state_read_only)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_removable_storage)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.status_yes)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.status_no)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_total)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.storage_available)).assertExists()
        composeRule.onNodeWithText("128 GB", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("64 GB", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("32 GB", useUnmergedTree = true).assertExists()
    }

    @Test
    fun storageLabelsResolveToLockedEnglishAndFinnishText() {
        val english = localizedContext(Locale.ENGLISH)
        val finnish = localizedContext(Locale("fi"))

        assertStorageLabels(
            english,
            usageUnit = "% used",
            accessTitle = "Storage access",
            privateStorage = "Private app storage",
            primaryStorage = "Primary shared storage",
            removableStorage = "Removable storage",
            benchmarkTitle = "Storage speed check",
            limitationsTitle = "Limitations",
        )
        assertStorageLabels(
            finnish,
            usageUnit = "% käytetty",
            accessTitle = "Tallennustilan käyttö",
            privateStorage = "Sovelluksen yksityinen tallennustila",
            primaryStorage = "Ensisijainen jaettu tallennustila",
            removableStorage = "Irrotettava tallennustila",
            benchmarkTitle = "Tallennustilan nopeustesti",
            limitationsTitle = "Rajoitukset",
        )
    }

    // CPD-OFF
    // Camera and storage presentation tests intentionally use the same localized Compose harness.
    private fun render(
        context: Context,
        content: @androidx.compose.runtime.Composable () -> Unit,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides context.resources.configuration,
            ) {
                FonecheckTheme(content = content)
            }
        }
    }

    private fun localizedContext(locale: Locale): Context {
        val configuration =
            Configuration(
                InstrumentationRegistry
                    .getInstrumentation()
                    .targetContext
                    .resources
                    .configuration,
            )
        configuration.setLocale(locale)
        return InstrumentationRegistry.getInstrumentation().targetContext.createConfigurationContext(configuration)
    }
    // CPD-ON

    private fun assertStorageLabels(
        context: Context,
        usageUnit: String,
        accessTitle: String,
        privateStorage: String,
        primaryStorage: String,
        removableStorage: String,
        benchmarkTitle: String,
        limitationsTitle: String,
    ) {
        org.junit.Assert.assertEquals(usageUnit, context.getString(R.string.storage_usage_unit))
        org.junit.Assert.assertEquals(accessTitle, context.getString(R.string.storage_access_title))
        org.junit.Assert.assertEquals(privateStorage, context.getString(R.string.storage_private_access))
        org.junit.Assert.assertEquals(primaryStorage, context.getString(R.string.storage_volume_primary))
        org.junit.Assert.assertEquals(removableStorage, context.getString(R.string.storage_removable_storage))
        org.junit.Assert.assertEquals(benchmarkTitle, context.getString(R.string.storage_benchmark_title))
        org.junit.Assert.assertEquals(limitationsTitle, context.getString(R.string.storage_limitations_title))
    }

    private fun storageInfo(usagePercent: Double? = 24.0) =
        StorageInfo(
            totalBytes = TOTAL_BYTES,
            usedBytes = USED_BYTES,
            availableBytes = AVAILABLE_BYTES,
            usagePercent = usagePercent,
            internalStorageAccessible = true,
            appAccessibleVolumes = emptyList(),
            capturedAt = Instant.EPOCH,
        )

    private companion object {
        const val TOTAL_BYTES = 246_000_000_000L
        const val AVAILABLE_BYTES = 187_070_000_000L
        const val USED_BYTES = 58_930_000_000L
        const val REMOVABLE_PRIMARY_TOTAL_BYTES = 128_000_000_000L
        const val REMOVABLE_PRIMARY_AVAILABLE_BYTES = 64_000_000_000L
        const val ADDITIONAL_TOTAL_BYTES = 64_000_000_000L
        const val ADDITIONAL_AVAILABLE_BYTES = 32_000_000_000L
    }
}
