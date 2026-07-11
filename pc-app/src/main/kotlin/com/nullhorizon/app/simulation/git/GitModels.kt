package com.nullhorizon.app.simulation.git

import kotlinx.serialization.Serializable

@Serializable
data class GitCommit(
    val hash: String,
    val message: String,
    val author: String,
    val parents: List<String> = emptyList(),
    val tree: Map<String, String> = emptyMap(), // path -> content
)

@Serializable
data class GitConflict(
    val path: String,
    val ours: String,
    val theirs: String,
)

@Serializable
data class GitHistoryEntry(
    val command: String,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int = 0,
)

@Serializable
data class GitRepositoryState(
    val currentBranch: String,
    val branches: Map<String, String>, // branch -> commit hash
    val commits: Map<String, GitCommit>,
    val index: Map<String, String> = emptyMap(), // path -> content staged
    val workingTree: Map<String, String> = emptyMap(), // path -> content
    val conflicts: Map<String, GitConflict> = emptyMap(),
    /** When set, the next successful commit is a merge commit with this second parent. */
    val mergeParentHash: String? = null,
    val lastCommand: String = "",
    val lastStdout: String = "",
    val lastStderr: String = "",
    val lastExitCode: Int = 0,
    val history: List<GitHistoryEntry> = emptyList(),
) {
    val headHash: String
        get() = branches.getValue(currentBranch)

    val headCommit: GitCommit
        get() = commits.getValue(headHash)

    fun headTree(): Map<String, String> = headCommit.tree
}

internal object GitHash {
    fun of(parts: List<String>): String {
        var hash = 5381
        for (part in parts) {
            for (ch in part) {
                hash = ((hash shl 5) + hash) + ch.code
            }
            hash = ((hash shl 5) + hash) + 0
        }
        return (hash.toUInt() and 0xFFFFFFFFu).toString(16).padStart(8, '0')
    }

    fun commit(message: String, author: String, parents: List<String>, tree: Map<String, String>): String {
        val treeFingerprint = tree.toSortedMap().entries.joinToString("|") { "${it.key}=${it.value}" }
        return of(listOf(message, author, parents.joinToString(","), treeFingerprint))
    }
}
