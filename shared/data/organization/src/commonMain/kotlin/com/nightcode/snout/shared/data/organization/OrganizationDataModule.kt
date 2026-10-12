package com.nightcode.snout.shared.data.organization

import com.nightcode.snout.shared.api.organizations.OrganizationsApiModule
import com.nightcode.snout.shared.storage.database.DatabaseModule
import com.nightcode.snout.shared.storage.datastore.DataStoreModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [OrganizationsApiModule::class, DataStoreModule::class, DatabaseModule::class])
@ComponentScan("com.nightcode.snout.shared.data.organization")
class OrganizationDataModule
