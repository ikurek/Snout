package com.nightcode.snout.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withoutAnnotationNamed
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.ShouldSpec

/** Naming and shape conventions from docs/ARCHITECTURE.md. */
class ConventionTest :
    ShouldSpec({

        val scope = Konsist.scopeFromProduction()
        val classes = scope.classes()

        context("DTOs") {
            val serializableApiClasses = classes
                .withPackage("com.nightcode.snout.shared.api..")
                .withAnnotationNamed("Serializable")

            should("be named XDto") {
                serializableApiClasses.assertTrue(testName = rule) { it.name.endsWith("Dto") }
            }

            should("live in an api module") {
                classes.withNameEndingWith("Dto").assertTrue(testName = rule) {
                    it.resideInPackage("com.nightcode.snout.shared.api..")
                }
            }

            should("map every field with @SerialName") {
                serializableApiClasses.assertTrue(testName = rule) { dto ->
                    dto.primaryConstructor?.parameters.orEmpty()
                        .filter { it.isVal || it.isVar }
                        .all { it.hasAnnotationWithName("SerialName") }
                }
            }
        }

        context("repositories") {
            val implementations = classes.withNameEndingWith("RepositoryImpl")

            should("be implemented by an internal XRepositoryImpl in a data module") {
                implementations.assertTrue(testName = rule) {
                    it.resideInPackage("com.nightcode.snout.shared.data..") && it.hasInternalModifier
                }
            }

            should("implement the domain interface named after them and bind it in Koin") {
                implementations.assertTrue(testName = rule) { impl ->
                    val repository = impl.name.removeSuffix("Impl")
                    val binding = "binds = [$repository::class]"
                    impl.hasParentWithName(repository) &&
                        impl.annotations.any { it.name == "Single" && binding in it.text }
                }
            }

            should("declare their interfaces in a domain/data module") {
                scope.interfaces().withNameEndingWith("Repository").assertTrue(testName = rule) {
                    it.resideInPackage("com.nightcode.snout.shared.domain.data..")
                }
            }
        }

        context("use cases") {
            val useCases = classes
                .withPackage("com.nightcode.snout.shared.domain.usecase..")
                .withAnnotationNamed("Single")

            should("be called through a public operator fun invoke") {
                useCases.assertTrue(testName = rule) { useCase ->
                    useCase.functions().any { it.name == "invoke" && it.hasOperatorModifier && !it.hasPrivateModifier }
                }
            }

            should("be named for what they do, without a UseCase suffix") {
                useCases.assertTrue(testName = rule) { !it.name.endsWith("UseCase") }
            }
        }

        context("view models") {
            val allViewModels = classes.filter { it.hasParentWithName("ViewModel") }
            // The Android host's StartupViewModel loads the first screen; it isn't a screen's own.
            val viewModels = allViewModels.withPackage("com.nightcode.snout.features..")

            should("be internal XViewModel classes registered with @KoinViewModel") {
                viewModels.assertTrue(testName = rule) {
                    it.name.endsWith("ViewModel") && it.hasInternalModifier && it.hasAnnotationWithName("KoinViewModel")
                }
            }

            should("stay out of the shared modules") {
                allViewModels.withPackage("com.nightcode.snout.shared..").assertEmpty(testName = rule)
            }

            should("expose a single uiState StateFlow") {
                viewModels.assertTrue(testName = rule) { it.uiStateType?.startsWith("StateFlow<") == true }
            }

            // Settings screens show the preferences as they are, so their state is a domain type.
            should("declare a UI state type of their own as XUiState in XUiState.kt next to them") {
                viewModels.assertTrue(testName = rule) { viewModel ->
                    val state = viewModel.uiStateType.orEmpty().removePrefix("StateFlow<").removeSuffix(">")
                    val expected = viewModel.name.removeSuffix("ViewModel") + "UiState"
                    val expectedPath = viewModel.containingFile.path.substringBeforeLast('/') + "/$expected.kt"
                    val stateFile = scope.files.singleOrNull { it.path == expectedPath }
                    val declared = stateFile != null &&
                        (stateFile.classes() + stateFile.interfaces()).any { it.name == expected }
                    !state.endsWith("UiState") || (state == expected && declared)
                }
            }

            should("take use cases, never repositories or APIs") {
                viewModels.assertTrue(testName = rule) { viewModel ->
                    viewModel.primaryConstructor?.parameters.orEmpty().none { parameter ->
                        val type = parameter.type.name
                        type.endsWith("Repository") || type.endsWith("Api") || type.endsWith("Impl")
                    }
                }
            }
        }

        context("previews") {
            val previews = scope.functions().withAnnotationNamed("PreviewLightDark")

            should("be public XPreview functions, so the screenshot tests can call them") {
                previews.assertTrue(testName = rule) {
                    it.name.endsWith("Preview") && !it.hasPrivateModifier && !it.hasInternalModifier
                }
            }

            should("render in the app theme") {
                previews.assertTrue(testName = rule) { it.hasAnnotationWithName("PreviewWrapper") }
            }

            should("use @PreviewLightDark rather than @Preview") {
                scope.functions().withAnnotationNamed("Preview").withoutAnnotationNamed("PreviewLightDark")
                    .assertEmpty(testName = rule)
            }
        }
    })

private val KoClassDeclaration.uiStateType: String?
    get() = properties().singleOrNull { it.name == "uiState" }?.type?.text
