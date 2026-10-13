package co.ariasalejo.blumtherapy

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.net.URI

/**
 * Opens only validated HTTPS links from the curated resource catalogue.
 * No user-supplied URL is trusted merely because it came from a data model.
 */
internal fun isAllowedExternalUrl(rawUrl: String): Boolean {
    val parsed = runCatching { URI(rawUrl.trim()) }.getOrNull() ?: return false
    return parsed.scheme.equals("https", ignoreCase = true) &&
        !parsed.host.isNullOrBlank() &&
        parsed.userInfo == null
}

internal fun openExternalUrl(context: Context, rawUrl: String, languageCode: String) {
    val spanish = languageCode == "es"
    val message = if (spanish) {
        "No se pudo abrir el enlace. Comprueba la dirección e inténtalo de nuevo."
    } else {
        "This link could not be opened. Check the address and try again."
    }

    if (!isAllowedExternalUrl(rawUrl)) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        return
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl.trim()))
        .addCategory(Intent.CATEGORY_BROWSABLE)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
