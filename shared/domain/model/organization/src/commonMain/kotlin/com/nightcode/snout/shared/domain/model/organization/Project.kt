package com.nightcode.snout.shared.domain.model.organization

import com.nightcode.snout.shared.common.testdata.TestData

data class Project(val id: Long, val name: String)

@TestData
val TestProject = Project(id = 1L, name = "My Project")
