package com.nightcode.snout.features.settings.libraries

import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.nightcode.snout.shared.common.testdata.TestData
import com.nightcode.snout.shared.common.testutils.MainDispatcherExtension
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

@OptIn(TestData::class)
class LibrariesViewModelTest :
    ShouldSpec({
        extension(MainDispatcherExtension)

        should("list the bundled libraries") {
            val definitions = LibraryDefinitions { Libs(TestLibraries, setOf(TestApacheLicense, TestMitLicense)) }

            LibrariesViewModel(definitions).uiState.value shouldBe LibrariesUiState(libraries = TestLibraries)
        }

        should("fail when the bundled file can't be read") {
            val definitions = LibraryDefinitions { error("files/aboutlibraries.json is missing") }

            LibrariesViewModel(definitions).uiState.value shouldBe LibrariesUiState(error = DataError.UnexpectedError)
        }

        should("fail when the bundled file lists no libraries") {
            val definitions = LibraryDefinitions { Libs(emptyList(), emptySet()) }

            LibrariesViewModel(definitions).uiState.value shouldBe LibrariesUiState(error = DataError.UnexpectedError)
        }

        should("load again on refresh") {
            var libraries = emptyList<Library>()
            val viewModel = LibrariesViewModel { Libs(libraries, emptySet()) }
            libraries = TestLibraries

            viewModel.refresh()

            viewModel.uiState.value shouldBe LibrariesUiState(libraries = TestLibraries)
        }
    })
