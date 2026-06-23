package com.universalwallet.loyalty.core.backup

import android.content.Context
import android.net.Uri
import com.universalwallet.loyalty.core.export.ExportManager
import com.universalwallet.loyalty.core.security.PasswordCrypto
import com.universalwallet.loyalty.core.utils.IoDispatcher
import com.universalwallet.loyalty.domain.repository.LoyaltyCardRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces wallet backups. Serialization is delegated to [ExportManager]; this
 * layer adds optional password encryption (via [PasswordCrypto]) and the file
 * write. When a non-blank password is supplied the payload is AES-GCM encrypted;
 * otherwise plaintext JSON is written (the user's explicit choice).
 *
 * Cloud destinations are intentionally interface-only for now — see
 * [CloudBackupTarget]. No live cloud integration ships in this phase.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val io: CoroutineDispatcher,
    private val exportManager: ExportManager,
    private val cardRepository: LoyaltyCardRepository,
) {
    /** Writes a backup of all cards to [uri]. Encrypts if [password] is non-blank. */
    suspend fun backupToUri(uri: Uri, password: String? = null): Result<Int> = withContext(io) {
        runCatching {
            val cards = cardRepository.observeCards().first()
            val json = exportManager.toJson(cards)
            val payload = if (!password.isNullOrBlank()) PasswordCrypto.encrypt(json, password) else json
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(payload.toByteArray(Charsets.UTF_8))
            } ?: error("Could not open output stream")
            cards.size
        }
    }
}

/**
 * Cloud backup destination contract (architecture only — no implementation
 * ships in this phase). A future Drive/iCloud-style provider implements this.
 */
interface CloudBackupTarget {
    val displayName: String
    suspend fun upload(fileName: String, payload: ByteArray): Result<Unit>
    suspend fun download(fileName: String): Result<ByteArray>
    suspend fun list(): Result<List<String>>
}
