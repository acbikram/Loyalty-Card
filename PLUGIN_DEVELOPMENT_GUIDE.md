# Plugin Development Guide

Stores are added through the **plugin system** (`core/plugin`). A plugin teaches
the app about one retailer: its identity, supported barcode symbologies, theme,
card template, and how to validate/format its card numbers — all behind one
stable interface, so new stores need no changes to feature code.

## The contract

```kotlin
interface StorePluginContract {
    fun getStoreId(): String                              // stable unique id, e.g. "lulu"
    fun getStoreName(): String                            // display name
    fun getSupportedBarcodeTypes(): List<BarcodeSymbology> // e.g. EAN13, QR
    fun getTheme(): StoreTheme                            // colours/branding
    fun getCardTemplate(): CardTemplate                  // layout template id
    fun validateCard(cardNumber: String): ValidationResult
    fun formatCard(cardNumber: String): String           // display formatting
}
```

## Writing a plugin

1. Create a class implementing `StorePluginContract` (see the existing generic
   plugin and its test, `GenericStorePluginTest`, for a working reference).
2. Return a stable `getStoreId()` — it's the key used everywhere (cards reference
   it, sync uses it, the registry resolves by it). Never change it once shipped.
3. Implement `validateCard`/`formatCard` for the retailer's number format; return
   a `ValidationResult` describing validity and reason.

```kotlin
class LuluStorePlugin : StorePluginContract {
    override fun getStoreId() = "lulu"
    override fun getStoreName() = "Lulu Hypermarket"
    override fun getSupportedBarcodeTypes() = listOf(BarcodeSymbology.EAN_13)
    override fun getTheme(): StoreTheme = /* build as existing plugins do */
    override fun getCardTemplate(): CardTemplate = /* template id */
    override fun validateCard(cardNumber: String): ValidationResult =
        if (cardNumber.length in 8..16) ValidationResult.valid()
        else ValidationResult.invalid("Unexpected length")
    override fun formatCard(cardNumber: String) = cardNumber.chunked(4).joinToString(" ")
}
```

## Registering

Plugins are held by the Hilt-provided `StorePluginRegistry`:

```kotlin
registry.register(LuluStorePlugin())                 // -> DataResult<Unit>
registry.registerAll(listOf(/* … */))                // -> errors for any failures
val plugin = registry.resolve("lulu")                // falls back to a generic plugin
val strict = registry.resolveStrict("lulu")          // -> DataResult, no fallback
val all = registry.all()
```

Wire registration where the app composes its plugin set (the foundation module
that builds the registry), so it's available app-wide via injection.

## Testing your plugin
- Unit-test `validateCard`/`formatCard` and the symbology list (pure, JVM).
- Use the Developer-Mode **Plugin Validator** to sanity-check registered plugins
  (blank ids, empty symbologies) on-device.

## Guidelines
- Keep `getStoreId()` lowercase, stable, and unique.
- Don't bake in secrets; plugins describe public store metadata only.
- Prefer the generic plugin's patterns for theme/template construction to stay
  consistent with the design system.
