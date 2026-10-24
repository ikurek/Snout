## What and why

<!-- What this changes and why. Link the issue it addresses, e.g. "Closes #12". -->

## Checklist

- [ ] `./gradlew testAndroidHostTest detekt :composeApp:compileKotlinIosSimulatorArm64` passes
- [ ] `./gradlew :androidApp:validateDebugScreenshotTest` passes, or references are updated and committed for an intended UI change
- [ ] Tests added or updated for changed logic
- [ ] New API key scopes added to `KeyScopes.kt` and the README (if any)
- [ ] No API keys or other secrets in code, fixtures or screenshots
- [ ] Added myself to `AUTHORS` (first contribution)
