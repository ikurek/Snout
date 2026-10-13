package com.nightcode.snout.features.settings.libraries.detail

import com.mikepenz.aboutlibraries.Libs
import com.nightcode.snout.common.state.LoadState
import com.nightcode.snout.features.settings.libraries.LibraryDefinitions
import com.nightcode.snout.features.settings.libraries.TestApacheLicense
import com.nightcode.snout.features.settings.libraries.TestLibraries
import com.nightcode.snout.features.settings.libraries.TestMitLibrary
import com.nightcode.snout.features.settings.libraries.TestMitLicense
import com.nightcode.snout.navigation.LibraryDetailNavKey
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import com.nightcode.snout.shared.domain.model.common.DataError
import com.nightcode.snout.shared.domain.usecase.links.OpenInBrowser
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.mock
import dev.mokkery.verify
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class LibraryDetailViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        val launcher = mock<ExternalAppLauncher>(autoUnit)
        val definitions = LibraryDefinitions { Libs(TestLibraries, setOf(TestApacheLicense, TestMitLicense)) }

        fun subject(uniqueId: String, libraryDefinitions: LibraryDefinitions = definitions) = LibraryDetailViewModel(
            navKey = LibraryDetailNavKey(uniqueId),
            libraryDefinitions = libraryDefinitions,
            openInBrowser = OpenInBrowser(launcher),
        )

        should("show the library with its licenses") {
            subject(TestMitLibrary.uniqueId).uiState.value shouldBe LoadState.Loaded(TestMitLibrary)
        }

        should("report a library the app no longer ships") {
            subject("com.example:gone").uiState.value shouldBe LoadState.Error(DataError.NotFound)
        }

        should("fail when the bundled file can't be read") {
            val viewModel = subject(TestMitLibrary.uniqueId) { error("files/aboutlibraries.json is missing") }

            viewModel.uiState.value shouldBe LoadState.Error(DataError.UnexpectedError)
        }

        should("open the website in the browser") {
            subject(TestMitLibrary.uniqueId).openWebsite("http://www.slf4j.org")

            verify { launcher.openInBrowser("http://www.slf4j.org") }
        }
    })
