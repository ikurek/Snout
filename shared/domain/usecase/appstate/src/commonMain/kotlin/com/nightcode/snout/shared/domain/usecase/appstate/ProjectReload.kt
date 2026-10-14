package com.nightcode.snout.shared.domain.usecase.appstate

/**
 * A request to load the selected project's data again, emitted by [ObserveProjectReloads]. [projectChanged] is true
 * for the first selected project and whenever another one is selected, so what is on screen belongs to another
 * project; false when the same project's data changed, so a screen may keep what it shows while it reloads.
 */
data class ProjectReload(val projectChanged: Boolean)
