package com.universalwallet.loyalty.core.backup

import android.content.Context
import android.net.Uri
import com.universalwallet.loyalty.core.export.ConflictPolicy
import com.universalwallet.loyalty.core.export.ImportManager
import com.universalwallet.loyalty.core.export.ImportSummary
import com.universalwallet.loyalty.core.export.WalletExport
import com.universalwallet.loyalty.core.security.PasswordCrypto
import com.universalwallet.loyalty.core.security.SecurityError
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads, decrypts, validates, and previews a backup before any data is written,
 * then applies it on confirmation. Decryption is automatic when the file is
 * password-encrypted; validation and conflict detection run against the current
 * wallet so the UI can show a restore preview first.
 */
@Singleton
class RestoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val importManager: ImportManager,
    private val importValidator: ImportValidator,
    private val cardRepository: LoyaltyCardRepository,
) {
    /** Builds a preview. [password] is only needed for encrypted backups. */
    suspend fun preview(uri: Uri, password: String? = null): Result<RestorePreview> = withContext(io) {
        runCatching {
            val raw = readText(uri)
            val encrypted = PasswordCrypto.isEncrypted(raw)
            val json = if (encrypted) {
                val pwd = password?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException(SecurityError.WrongPassword.message)
                runCatching { PasswordCrypto.decrypt(raw, pwd) }
                    .getOrElse { throw IllegalStateException(SecurityError.WrongPassword.message) }
            } else {
                raw
            }
            val export = importManager.parse(json)
                .getOrElse { throw IllegalStateException(SecurityError.InvalidBackup.message) }
            buildPreview(export, encrypted)
        }
    }

    /** Applies a previously-previewed export. */
    suspend fun restore(uri: Uri, password: String? = null, policy: ConflictPolicy): Result<ImportSummary> =
        withContext(io) {
            runCatching {
                val raw = readText(uri)
                val json = if (PasswordCrypto.isEncrypted(raw)) {
                    val pwd = password?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException(SecurityError.WrongPassword.message)
                    PasswordCrypto.decrypt(raw, pwd)
                } else {
                    raw
                }
                val export = importManager.parse(json)
                    .getOrElse { throw IllegalStateException(SecurityError.InvalidBackup.message) }
                importManager.importCards(export, policy)
            }
        }

    private suspend fun buildPreview(export: WalletExport, encrypted: Boolean): RestorePreview {
        val validation = importValidator.validate(export)
        val existing = cardRepository.observeCards().first()
        val existingKeys = existing.map { it.storeId to it.cardNumber }.toSet()
        val conflicts = export.cards
            .filter { (it.storeId to it.cardNumber) in existingKeys }
            .map { ConflictInfo(it.storeName, it.cardNumber) }
        val newCards = export.cards.filter { (it.storeId to it.cardNumber) !in existingKeys }
        return RestorePreview(
            validation = validation,
            newCards = newCards,
            conflicts = conflicts,
            wasEncrypted = encrypted,
            exportedAt = export.exportedAt,
        )
    }

    private fun readText(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalStateException(SecurityError.CorruptedBackup.message)
}
