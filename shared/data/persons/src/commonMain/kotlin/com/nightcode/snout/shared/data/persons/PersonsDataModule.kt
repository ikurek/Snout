package com.nightcode.snout.shared.data.persons

import com.nightcode.snout.shared.api.persons.PersonsApiModule
import com.nightcode.snout.shared.data.common.CommonDataModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [PersonsApiModule::class, CommonDataModule::class])
@ComponentScan("com.nightcode.snout.shared.data.persons")
class PersonsDataModule
