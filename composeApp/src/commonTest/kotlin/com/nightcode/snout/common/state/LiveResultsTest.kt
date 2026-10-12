package com.nightcode.snout.common.state

import arrow.core.Either
import com.nightcode.snout.shared.domain.model.common.DataError
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration

class LiveResultsTest :
    ShouldSpec({
        val searched = mutableListOf<String>()
        val search: suspend (String) -> Either<DataError, List<String>> = { query ->
            searched += query
            when (query) {
                "none" -> Either.Right(emptyList())
                "fail" -> Either.Left(DataError.RateLimited)
                else -> Either.Right(listOf("$query-1", "$query-2"))
            }
        }

        beforeTest { searched.clear() }

        should("fall back to the cache while the query is blank") {
            flowOf(" ").searchResults(Duration.ZERO, search).toList() shouldBe listOf(null)
            searched shouldBe emptyList()
        }

        should("load results for a typed query") {
            flowOf("dark").searchResults(Duration.ZERO, search).toList() shouldBe listOf(
                CachedUiState.Loading,
                CachedUiState.Content(listOf("dark-1", "dark-2"), isRefreshing = false),
            )
        }

        should("report empty results") {
            flowOf("none").searchResults(Duration.ZERO, search).toList() shouldBe listOf(
                CachedUiState.Loading,
                CachedUiState.Empty(isRefreshing = false),
            )
        }

        should("report a failed search") {
            flowOf("fail").searchResults(Duration.ZERO, search).toList() shouldBe listOf(
                CachedUiState.Loading,
                CachedUiState.Error(DataError.RateLimited),
            )
        }

        should("search a repeated query only once") {
            flowOf("dark", "dark").searchResults(Duration.ZERO, search).toList()
            searched shouldBe listOf("dark")
        }

        should("fetch only the filters marked live") {
            val fetch: suspend (Int) -> Either<DataError, List<Int>> = { Either.Right(listOf(it)) }
            flowOf(2).liveResults(isLive = { it != 2 }, fetch = fetch).toList() shouldBe listOf(null)
            flowOf(3).liveResults(isLive = { it != 2 }, fetch = fetch).toList() shouldBe listOf(
                CachedUiState.Loading,
                CachedUiState.Content(listOf(3), isRefreshing = false),
            )
        }
    })
