package com.nightcode.snout.shared.domain.usecase.links

import com.nightcode.snout.shared.domain.data.appstate.AppStateRepository
import com.nightcode.snout.shared.domain.data.auth.AuthRepository
import com.nightcode.snout.shared.domain.data.links.ExternalAppLauncher
import com.nightcode.snout.shared.domain.data.links.IncomingLinkRepository
import com.nightcode.snout.shared.domain.usecase.appstate.GetSelectedProject
import com.nightcode.snout.shared.domain.usecase.appstate.SaveSelectedProject
import com.nightcode.snout.shared.domain.usecase.auth.GetInstanceUrl
import com.nightcode.snout.shared.domain.usecase.insights.FindInsightIdByShortId
import com.nightcode.snout.shared.domain.usecase.organization.ObserveOrganizations
import com.nightcode.snout.shared.domain.usecase.organization.RefreshOrganizations
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class LinksUsecaseModule {
    @Single
    fun provideParsePostHogLink() = ParsePostHogLink()

    @Single
    fun provideGetPostHogUrl(authRepository: AuthRepository, appStateRepository: AppStateRepository) =
        GetPostHogUrl(authRepository, appStateRepository)

    @Single
    fun provideFindLinkInText(parsePostHogLink: ParsePostHogLink) = FindLinkInText(parsePostHogLink)

    @Single
    fun provideSubmitIncomingLink(findLinkInText: FindLinkInText, repository: IncomingLinkRepository) =
        SubmitIncomingLink(findLinkInText, repository)

    @Single
    fun provideObservePendingLink(repository: IncomingLinkRepository) = ObservePendingLink(repository)

    @Single
    fun provideClearPendingLink(repository: IncomingLinkRepository) = ClearPendingLink(repository)

    @Single
    fun provideOpenInBrowser(launcher: ExternalAppLauncher) = OpenInBrowser(launcher)

    @Single
    fun provideCanOpenLinkSettings(launcher: ExternalAppLauncher) = CanOpenLinkSettings(launcher)

    @Single
    fun provideOpenLinkSettings(launcher: ExternalAppLauncher) = OpenLinkSettings(launcher)

    @Single
    @Suppress("LongParameterList") // the resolver needs every step of opening a link
    fun provideResolvePostHogLink(
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
