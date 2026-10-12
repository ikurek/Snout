package com.nightcode.snout.shared.api

import com.nightcode.snout.shared.storage.datastore.AuthDataStore

/** Builds `{instance}/api/{path}` for the signed-in instance. */
suspend fun AuthDataStore.instanceUrl(path: String): String =
    "${requireNotNull(getInstanceUrl()) { "Instance URL is missing" }}/api/$path"

/** Builds `{instance}/api/projects/{projectId}/{path}` for the signed-in instance. */
suspend fun AuthDataStore.projectUrl(projectId: Long, path: String): String = instanceUrl("projects/$projectId/$path")
