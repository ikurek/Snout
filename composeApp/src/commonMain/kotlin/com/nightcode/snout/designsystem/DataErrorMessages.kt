package com.nightcode.snout.designsystem

import com.nightcode.snout.shared.domain.model.common.DataError
import org.jetbrains.compose.resources.StringResource
import snout.composeapp.generated.resources.Res
import snout.composeapp.generated.resources.refresh_failed_forbidden
import snout.composeapp.generated.resources.refresh_failed_generic
import snout.composeapp.generated.resources.refresh_failed_offline
import snout.composeapp.generated.resources.refresh_failed_payment_required
import snout.composeapp.generated.resources.refresh_failed_rate_limited

/** Banner text for a reload that failed while what was loaded before stays on screen. */
internal fun DataError.refreshFailedMessage(): StringResource = when (this) {
    DataError.Unauthorized, DataError.Forbidden -> Res.string.refresh_failed_forbidden
    DataError.PaymentRequired -> Res.string.refresh_failed_payment_required
    DataError.RateLimited -> Res.string.refresh_failed_rate_limited
    DataError.ConnectionError -> Res.string.refresh_failed_offline
    DataError.NotFound, DataError.UnexpectedError -> Res.string.refresh_failed_generic
}

/**
 * Text for a failed action or inline load. The texts name what failed, so callers pass them: [forbidden] (usually
 * naming the missing scope) and [rateLimited] fall back to [generic] when the action has no dedicated wording.
 */
internal fun DataError.actionFailedMessage(
    generic: StringResource,
    forbidden: StringResource = generic,
    rateLimited: StringResource = generic,
): StringResource = when (this) {
    DataError.Unauthorized, DataError.Forbidden -> forbidden
    DataError.RateLimited -> rateLimited
    DataError.PaymentRequired, DataError.NotFound, DataError.ConnectionError, DataError.UnexpectedError -> generic
}
