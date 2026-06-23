package com.universalwallet.loyalty.core.architecture

/**
 * Marks a type as the public entry point of a feature package and documents the
 * contract every feature must honour. Applying it has no runtime effect; it is
 * a discoverable, enforceable statement of the rules below (a static
 * architecture test can assert that feature packages expose exactly one
 * annotated entry point).
 *
 * Feature rules:
 *  1. A feature owns its ViewModel layer and UI; nothing outside reaches in.
 *  2. A feature MUST NOT access the database (Room) directly.
 *  3. A feature obtains data only through repositories (via the domain layer).
 *  4. A feature MUST NOT depend on another feature package.
 *  5. Cross-feature communication happens through the domain layer (shared
 *     models / use cases) or navigation, never direct calls.
 *
 * Keeping features independent like this is what makes the codebase scale: each
 * can be built, tested, and later extracted into its own Gradle module in
 * isolation, and a change in one feature cannot ripple into another.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class FeatureContract
