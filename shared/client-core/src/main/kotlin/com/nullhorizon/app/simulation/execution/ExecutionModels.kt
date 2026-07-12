package com.nullhorizon.app.simulation.execution

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkspaceFileDefinition(
    val path: String,
    val editable: Boolean = true,
    val content: String = "",
    val source: String? = null,
)

@Serializable
data class ExecutionFixtureMatch(
    @SerialName("file_contains") val fileContains: Map<String, String> = emptyMap(),
    @SerialName("file_equals") val fileEquals: Map<String, String> = emptyMap(),
)

@Serializable
data class ExecutionTestFixture(
    val id: String,
    val status: String,
    val message: String? = null,
    val expected: String? = null,
    val actual: String? = null,
)

@Serializable
data class ExecutionResultFixture(
    val status: String = "completed",
    val stdout: String = "",
    val stderr: String = "",
    val tests: List<ExecutionTestFixture> = emptyList(),
)

@Serializable
data class FakeExecutionFixture(
    val id: String,
    val match: ExecutionFixtureMatch? = null,
    val result: ExecutionResultFixture,
)

@Serializable
data class ExecutionDefinition(
    val provider: String = "fake",
    val fixtures: List<FakeExecutionFixture> = emptyList(),
)

@Serializable
data class WorkspaceDefinition(
    val files: List<WorkspaceFileDefinition> = emptyList(),
    val execution: ExecutionDefinition? = null,
)
