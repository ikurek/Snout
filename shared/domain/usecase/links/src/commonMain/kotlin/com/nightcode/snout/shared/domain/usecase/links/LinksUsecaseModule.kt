package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.data.links.LinksDataModule
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import com.nightcode.snout.shared.domain.usecase.appstate.AppStateUsecaseModule
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.ParsePostHogLink
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.AuthUsecaseModule
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.insights.FindInsightIdByShortId
import com.nightcode.snout.shared.domain.usecase.insights.InsightsUsecaseModule
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.OrganizationUsecaseModule
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        LinksDataModule::class,
        AppStateUsecaseModule::class,
        AuthUsecaseModule::class,
        InsightsUsecaseModule::class,
        OrganizationUsecaseModule::class,
    ],
)
class LinksUsecaseModule {
    @Single
    fun findLinkInText(parsePostHogLink: ParsePostHogLink) = FindLinkInText(parsePostHogLink)

    @Single
    fun submitIncomingLink(findLinkInText: FindLinkInText, repository: IncomingLinkRepository) =
        SubmitIncomingLink(findLinkInText, repository)

    @Single
    fun observePendingLink(repository: IncomingLinkRepository) = ObservePendingLink(repository)

    @Single
    fun clearPendingLink(repository: IncomingLinkRepository) = ClearPendingLink(repository)

    @Single
    fun openInBrowser(launcher: ExternalAppLauncher) = OpenInBrowser(launcher)

    @Single
    fun canOpenLinkSettings(launcher: ExternalAppLauncher) = CanOpenLinkSettings(launcher)

    @Single
    fun openLinkSettings(launcher: ExternalAppLauncher) = OpenLinkSettings(launcher)

    @Single
    @Suppress("LongParameterList") // the resolver needs every step of opening a link
    fun resolvePostHogLink(
        parsePostHogLink: ParsePostHogLink,
        getInstanceUrl: GetInstanceUrl,
        getSelectedProject: GetSelectedProject,
        observeOrganizations: ObserveOrganizations,
        refreshOrganizations: RefreshOrganizations,
        saveSelectedProject: SaveSelectedProject,
        findInsightIdByShortId: FindInsightIdByShortId,
    ) = ResolvePostHogLink(
        parsePostHogLink,
        getInstanceUrl,
        getSelectedProject,
        observeOrganizations,
        refreshOrganizations,
        saveSelectedProject,
        findInsightIdByShortId,
    )
}
