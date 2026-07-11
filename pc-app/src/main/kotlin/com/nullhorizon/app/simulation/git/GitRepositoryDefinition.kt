package com.nullhorizon.app.simulation.git

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitRepositoryDefinition(
    @SerialName("initial_branch") val initialBranch: String = "main",
    val commits: List<GitCommitDefinition>,
    val branches: Map<String, String> = emptyMap(),
    @SerialName("working_tree") val workingTree: Map<String, String>? = null,
)

@Serializable
data class GitCommitDefinition(
    val id: String? = null,
    val message: String,
    val author: String = "crew",
    val parent: String? = null,
    val parents: List<String> = emptyList(),
    val files: Map<String, String>? = null,
)
