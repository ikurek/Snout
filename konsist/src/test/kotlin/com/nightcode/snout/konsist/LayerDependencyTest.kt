package com.nightcode.snout.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import io.kotest.core.spec.style.ShouldSpec

private const val Snout = "com.nightcode.snout"
private const val Shared = "$Snout.shared"
private const val Common = "$Shared.common"
private const val Api = "$Shared.api"
private const val Data = "$Shared.data"
private const val Storage = "$Shared.storage"
private const val Models = "$Shared.domain.model"
private const val Repositories = "$Shared.domain.data"
private const val UseCases = "$Shared.domain.usecase"

/** Dependencies point inwards: the layers in docs/ARCHITECTURE.md, checked through each file's imports. */
class LayerDependencyTest :
    ShouldSpec({

        val files = Konsist.scopeFromProduction().files

        should("keep domain models free of every other layer") {
            files.inPackage(Models).assertOnlyImportsFromSnout(rule, Models)
        }

        should("let repository interfaces see only domain models") {
            files.inPackage(Repositories).assertOnlyImportsFromSnout(rule, Models, Repositories)
        }

        should("let use cases see only repository interfaces and domain models") {
            files.inPackage(UseCases).assertOnlyImportsFromSnout(rule, Models, Repositories, UseCases)
        }

        // The API layer reads the key and instance URL from storage itself.
        should("keep the API layer out of data and the domain's repositories and use cases") {
            files.inPackage(Api).assertNoImportsFrom(rule, Data, Repositories, UseCases)
        }

        should("keep the data layer out of use cases") {
            files.inPackage(Data).assertNoImportsFrom(rule, UseCases)
        }

        should("keep the shared modules out of the UI") {
            files.inPackage(Shared).assertFalse(testName = rule) { file ->
                file.imports.any { it.name.startsWith("$Snout.") && !it.name.startsWith("$Shared.") }
            }
        }

        should("let the UI see models and use cases only, never repositories, data, API or storage") {
            files.filter { it.packagee?.name?.startsWith(Shared) == false }
                .assertNoImportsFrom(rule, Api, Data, Storage, Repositories)
        }
    })

private fun List<KoFileDeclaration>.inPackage(prefix: String) =
    filter { it.packagee?.name?.let { name -> name == prefix || name.startsWith("$prefix.") } == true }

private fun List<KoFileDeclaration>.assertNoImportsFrom(rule: String, vararg forbidden: String) =
    assertFalse(testName = rule, additionalMessage = "Must not import from ${forbidden.joinToString()}.") { file ->
        file.imports.any { import -> forbidden.any { import.name.startsWith("$it.") } }
    }

/** `shared.common` (the `@TestData` opt-in) is allowed everywhere. */
private fun List<KoFileDeclaration>.assertOnlyImportsFromSnout(rule: String, vararg allowed: String) {
    val packages = allowed.toList() + Common
    assertFalse(testName = rule, additionalMessage = "May only import Snout code from ${packages.joinToString()}.") {
        it.imports.any { import ->
            import.name.startsWith("$Snout.") && packages.none { prefix -> import.name.startsWith("$prefix.") }
        }
    }
}
