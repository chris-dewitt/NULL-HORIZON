package com.nullhorizon.app.simulation.git

data class GitCommandResult(
    val state: GitRepositoryState,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int = 0,
)

class GitSimulator {
    fun execute(state: GitRepositoryState, line: String): GitRepositoryState {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            return record(state, trimmed, stderr = "Empty command.", exitCode = 2)
        }
        if (!trimmed.startsWith("git ")) {
            return record(
                state,
                trimmed,
                stderr = "Only git commands are supported in this panel. Example: git status",
                exitCode = 2,
            )
        }
        val tokens = tokenize(trimmed.removePrefix("git ").trim())
        if (tokens.isEmpty()) {
            return record(state, trimmed, stderr = "git: missing command", exitCode = 2)
        }
        val result = try {
            when (val command = tokens.first()) {
                "status" -> status(state, tokens.drop(1))
                "diff" -> diff(state, tokens.drop(1))
                "add" -> add(state, tokens.drop(1))
                "commit" -> commit(state, tokens.drop(1))
                "log" -> log(state, tokens.drop(1))
                "branch" -> branch(state, tokens.drop(1))
                "switch" -> switch(state, tokens.drop(1))
                "merge" -> merge(state, tokens.drop(1))
                "checkout" -> checkout(state, tokens.drop(1))
                else -> GitCommandResult(
                    state = state,
                    stderr = "git: '$command' is not supported. Supported: status, diff, add, commit, log, branch, switch, merge, checkout",
                    exitCode = 1,
                )
            }
        } catch (error: IllegalArgumentException) {
            GitCommandResult(state = state, stderr = error.message ?: "git failed", exitCode = 1)
        }
        return record(
            result.state,
            trimmed,
            stdout = result.stdout,
            stderr = result.stderr,
            exitCode = result.exitCode,
        )
    }

    private fun status(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.isEmpty()) { "git status: unexpected arguments" }
        val lines = mutableListOf("On branch ${state.currentBranch}")
        if (state.conflicts.isNotEmpty()) {
            lines += "You have unmerged paths."
            lines += "Unmerged paths:"
            state.conflicts.keys.sorted().forEach { lines += "\tboth modified:\t$it" }
        }
        val staged = linkedMapOf<String, String>()
        val modified = linkedMapOf<String, String>()
        val untracked = mutableListOf<String>()
        val head = state.headTree()
        for ((path, content) in state.index) {
            if (head[path] != content) {
                staged[path] = if (head.containsKey(path)) "modified" else "new file"
            }
        }
        for ((path, content) in state.workingTree) {
            if (state.conflicts.containsKey(path)) continue
            val indexContent = state.index[path]
            when {
                indexContent == null && !head.containsKey(path) -> untracked += path
                indexContent != null && indexContent != content -> modified[path] = "modified"
                indexContent == null && head[path] != content -> modified[path] = "modified"
            }
        }
        // Deleted from working tree but present in index/head
        for (path in (head.keys + state.index.keys)) {
            if (!state.workingTree.containsKey(path) && !state.conflicts.containsKey(path)) {
                modified[path] = "deleted"
            }
        }
        if (staged.isNotEmpty()) {
            lines += "Changes to be committed:"
            staged.toSortedMap().forEach { (path, kind) -> lines += "\t$kind:\t$path" }
        }
        if (modified.isNotEmpty()) {
            lines += "Changes not staged for commit:"
            modified.toSortedMap().forEach { (path, kind) -> lines += "\t$kind:\t$path" }
        }
        if (untracked.isNotEmpty()) {
            lines += "Untracked files:"
            untracked.sorted().forEach { lines += "\t$it" }
        }
        if (staged.isEmpty() && modified.isEmpty() && untracked.isEmpty() && state.conflicts.isEmpty()) {
            lines += "nothing to commit, working tree clean"
        }
        return GitCommandResult(state = state, stdout = lines.joinToString("\n"))
    }

    private fun diff(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.size <= 1) { "git diff: too many arguments" }
        val pathFilter = args.firstOrNull()
        val chunks = mutableListOf<String>()
        val head = state.headTree()
        val paths = (state.workingTree.keys + head.keys)
            .filter { pathFilter == null || it == pathFilter }
            .sorted()
        for (path in paths) {
            if (state.conflicts.containsKey(path)) continue
            val working = state.workingTree[path]
            val baseline = state.index[path] ?: head[path]
            if (working == baseline) continue
            chunks += "diff -- git a/$path b/$path"
            chunks += "--- a/$path"
            chunks += "+++ b/$path"
            chunks += "-${baseline ?: ""}"
            chunks += "+${working ?: ""}"
        }
        return GitCommandResult(state = state, stdout = chunks.joinToString("\n"))
    }

    private fun add(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.isNotEmpty()) { "git add: missing pathspec" }
        var next = state
        for (path in args) {
            val conflict = next.conflicts[path]
            if (conflict != null) {
                val working = next.workingTree[path]
                    ?: throw IllegalArgumentException("git add: '$path' is unmerged and missing from working tree")
                if (working.contains("<<<<<<<")) {
                    throw IllegalArgumentException("git add: '$path' still has conflict markers")
                }
                next = next.copy(
                    index = next.index + (path to working),
                    conflicts = next.conflicts - path,
                )
            } else {
                val working = next.workingTree[path]
                    ?: throw IllegalArgumentException("git add: pathspec '$path' did not match any files")
                next = next.copy(index = next.index + (path to working))
            }
        }
        return GitCommandResult(state = next)
    }

    private fun commit(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(state.conflicts.isEmpty()) { "git commit: U unresolved conflicts" }
        val message = parseCommitMessage(args)
        val head = state.headTree()
        val tree = state.index.toMap()
        val mergeParent = state.mergeParentHash
        if (mergeParent == null && tree == head) {
            throw IllegalArgumentException("git commit: nothing to commit")
        }
        val parents = if (mergeParent != null) {
            listOf(state.headHash, mergeParent)
        } else {
            listOf(state.headHash)
        }
        val hash = GitHash.commit(message, "operator", parents, tree)
        val commit = GitCommit(
            hash = hash,
            message = message,
            author = "operator",
            parents = parents,
            tree = tree,
        )
        val next = state.copy(
            commits = state.commits + (hash to commit),
            branches = state.branches + (state.currentBranch to hash),
            index = tree,
            workingTree = tree,
            mergeParentHash = null,
        )
        return GitCommandResult(
            state = next,
            stdout = "[${state.currentBranch} ${hash.take(7)}] $message",
        )
    }

    private fun log(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.isEmpty()) { "git log: unexpected arguments" }
        val lines = mutableListOf<String>()
        var current: String? = state.headHash
        val seen = mutableSetOf<String>()
        while (current != null && current !in seen) {
            seen += current
            val commit = state.commits[current] ?: break
            lines += "commit ${commit.hash}"
            lines += "Author: ${commit.author}"
            lines += ""
            lines += "    ${commit.message}"
            lines += ""
            current = commit.parents.firstOrNull()
        }
        return GitCommandResult(state = state, stdout = lines.joinToString("\n").trimEnd())
    }

    private fun branch(state: GitRepositoryState, args: List<String>): GitCommandResult {
        return when {
            args.isEmpty() -> {
                val lines = state.branches.keys.sorted().map { name ->
                    if (name == state.currentBranch) "* $name" else "  $name"
                }
                GitCommandResult(state = state, stdout = lines.joinToString("\n"))
            }
            args.size == 1 -> {
                val name = args[0]
                require(!state.branches.containsKey(name)) { "git branch: branch '$name' already exists" }
                GitCommandResult(
                    state = state.copy(branches = state.branches + (name to state.headHash)),
                )
            }
            else -> throw IllegalArgumentException("git branch: too many arguments")
        }
    }

    private fun switch(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.size == 1) { "git switch: expected branch name" }
        require(state.conflicts.isEmpty()) { "git switch: cannot switch while conflicts exist" }
        require(isClean(state)) { "git switch: working tree not clean" }
        val branch = args[0]
        require(state.branches.containsKey(branch)) { "git switch: invalid reference: $branch" }
        val tree = state.commits.getValue(state.branches.getValue(branch)).tree
        return GitCommandResult(
            state = state.copy(
                currentBranch = branch,
                index = tree,
                workingTree = tree,
            ),
            stdout = "Switched to branch '$branch'",
        )
    }

    private fun merge(state: GitRepositoryState, args: List<String>): GitCommandResult {
        require(args.size == 1) { "git merge: expected branch name" }
        require(state.conflicts.isEmpty()) { "git merge: conflicts still unresolved" }
        require(isClean(state)) { "git merge: working tree not clean" }
        val other = args[0]
        require(state.branches.containsKey(other)) { "git merge: branch '$other' not found" }
        val oursHash = state.headHash
        val theirsHash = state.branches.getValue(other)
        if (oursHash == theirsHash) {
            return GitCommandResult(state = state, stdout = "Already up to date.")
        }
        val ours = state.commits.getValue(oursHash)
        val theirs = state.commits.getValue(theirsHash)
        val baseTree = findMergeBaseTree(state, oursHash, theirsHash)
        val conflicts = linkedMapOf<String, GitConflict>()
        val merged = linkedMapOf<String, String>()
        val paths = (ours.tree.keys + theirs.tree.keys).toSortedSet()
        for (path in paths) {
            val o = ours.tree[path]
            val t = theirs.tree[path]
            val b = baseTree[path]
            when {
                o == t -> if (o != null) merged[path] = o
                o == b && t != null -> merged[path] = t
                t == b && o != null -> merged[path] = o
                o != null && t != null && o != t -> {
                    conflicts[path] = GitConflict(path = path, ours = o, theirs = t)
                    merged[path] = conflictMarkers(path, o, t)
                }
                o == null && t != null && b == null -> merged[path] = t
                t == null && o != null && b == null -> merged[path] = o
                else -> if (o != null) merged[path] = o else if (t != null) merged[path] = t
            }
        }
        if (conflicts.isNotEmpty()) {
            val staged = merged.filterKeys { it !in conflicts }
            return GitCommandResult(
                state = state.copy(
                    workingTree = merged,
                    index = staged,
                    conflicts = conflicts,
                    mergeParentHash = theirsHash,
                ),
                stdout = "Auto-merging failed\nCONFLICT (${conflicts.size} files)",
                exitCode = 1,
            )
        }
        val hash = GitHash.commit(
            message = "Merge branch '$other'",
            author = "operator",
            parents = listOf(oursHash, theirsHash),
            tree = merged,
        )
        val commit = GitCommit(
            hash = hash,
            message = "Merge branch '$other'",
            author = "operator",
            parents = listOf(oursHash, theirsHash),
            tree = merged,
        )
        return GitCommandResult(
            state = state.copy(
                commits = state.commits + (hash to commit),
                branches = state.branches + (state.currentBranch to hash),
                index = merged,
                workingTree = merged,
                mergeParentHash = null,
            ),
            stdout = "Merge made by the 'ort' strategy.",
        )
    }

    private fun checkout(state: GitRepositoryState, args: List<String>): GitCommandResult {
        // Supports:
        // git checkout -- <path>
        // git checkout --ours|--theirs <path>
        // git checkout <commit>
        return when {
            args.size == 2 && args[0] == "--" -> {
                val path = args[1]
                val content = state.headTree()[path]
                    ?: throw IllegalArgumentException("git checkout: pathspec '$path' did not match")
                GitCommandResult(
                    state = state.copy(
                        workingTree = state.workingTree + (path to content),
                        index = state.index + (path to content),
                        conflicts = state.conflicts - path,
                    ),
                )
            }
            args.size == 2 && (args[0] == "--ours" || args[0] == "--theirs") -> {
                val path = args[1]
                val conflict = state.conflicts[path]
                    ?: throw IllegalArgumentException("git checkout: '$path' is not unmerged")
                val content = if (args[0] == "--ours") conflict.ours else conflict.theirs
                GitCommandResult(
                    state = state.copy(
                        workingTree = state.workingTree + (path to content),
                    ),
                    stdout = "Updated 1 path from the index",
                )
            }
            args.size == 1 && !args[0].startsWith("-") -> {
                val commitHash = resolveCommitRef(state.commits, args[0])
                val commit = state.commits.getValue(commitHash)
                GitCommandResult(
                    state = state.copy(
                        branches = state.branches + (state.currentBranch to commitHash),
                        workingTree = commit.tree,
                        index = commit.tree,
                        conflicts = emptyMap(),
                        mergeParentHash = null,
                    ),
                    stdout = "HEAD is now at $commitHash ${commit.message}",
                )
            }
            else -> throw IllegalArgumentException(
                "git checkout: supported forms are 'git checkout -- <path>', " +
                    "'git checkout --ours|--theirs <path>', and 'git checkout <commit>'",
            )
        }
    }

    private fun resolveCommitRef(commits: Map<String, GitCommit>, ref: String): String {
        commits[ref]?.let { return it.hash }
        commits.values.firstOrNull { it.hash.startsWith(ref) }?.let { return it.hash }
        throw IllegalArgumentException("git checkout: pathspec '$ref' did not match any file(s) known to git")
    }

    private fun findMergeBaseTree(
        state: GitRepositoryState,
        oursHash: String,
        theirsHash: String,
    ): Map<String, String> {
        val oursAncestors = ancestors(state, oursHash)
        var current: String? = theirsHash
        while (current != null) {
            if (current in oursAncestors) {
                return state.commits.getValue(current).tree
            }
            current = state.commits[current]?.parents?.firstOrNull()
        }
        return emptyMap()
    }

    private fun ancestors(state: GitRepositoryState, start: String): Set<String> {
        val seen = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue += start
        while (queue.isNotEmpty()) {
            val hash = queue.removeFirst()
            if (!seen.add(hash)) continue
            state.commits[hash]?.parents?.forEach { queue += it }
        }
        return seen
    }

    private fun isClean(state: GitRepositoryState): Boolean {
        return state.conflicts.isEmpty() &&
            state.workingTree == state.headTree() &&
            state.index == state.headTree()
    }

    private fun conflictMarkers(path: String, ours: String, theirs: String): String {
        return buildString {
            appendLine("<<<<<<< ours")
            appendLine(ours.trimEnd())
            appendLine("=======")
            appendLine(theirs.trimEnd())
            append(">>>>>>> theirs")
        }
    }

    private fun parseCommitMessage(args: List<String>): String {
        if (args.size >= 2 && args[0] == "-m") {
            return args.drop(1).joinToString(" ").trim().removeSurrounding("\"").removeSurrounding("'")
        }
        throw IllegalArgumentException("git commit: use git commit -m \"message\"")
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingle = false
        var inDouble = false
        for (ch in input) {
            when {
                ch == '\'' && !inDouble -> inSingle = !inSingle
                ch == '"' && !inSingle -> inDouble = !inDouble
                ch.isWhitespace() && !inSingle && !inDouble -> {
                    if (current.isNotEmpty()) {
                        tokens += current.toString()
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        require(!inSingle && !inDouble) { "Unclosed quote in git command." }
        if (current.isNotEmpty()) tokens += current.toString()
        return tokens
    }

    private fun record(
        state: GitRepositoryState,
        command: String,
        stdout: String = "",
        stderr: String = "",
        exitCode: Int = 0,
    ): GitRepositoryState {
        val entry = GitHistoryEntry(command = command, stdout = stdout, stderr = stderr, exitCode = exitCode)
        return state.copy(
            lastCommand = command,
            lastStdout = stdout,
            lastStderr = stderr,
            lastExitCode = exitCode,
            history = state.history + entry,
        )
    }

    companion object {
        fun fromDefinition(definition: GitRepositoryDefinition): GitRepositoryState {
            require(definition.commits.isNotEmpty()) { "git repository requires at least one commit" }
            val commits = linkedMapOf<String, GitCommit>()
            var previousHash: String? = null
            for (commitDef in definition.commits) {
                val parents = when {
                    commitDef.parents.isNotEmpty() -> commitDef.parents.map { ref ->
                        resolveCommitRef(commits, ref)
                    }
                    commitDef.parent != null -> listOf(resolveCommitRef(commits, commitDef.parent))
                    previousHash != null -> listOf(previousHash)
                    else -> emptyList()
                }
                val tree = if (commitDef.files != null) {
                    val base = parents.firstOrNull()?.let { commits.getValue(it).tree } ?: emptyMap()
                    base + commitDef.files
                } else {
                    parents.firstOrNull()?.let { commits.getValue(it).tree } ?: emptyMap()
                }
                val hash = commitDef.id ?: GitHash.commit(commitDef.message, commitDef.author, parents, tree)
                commits[hash] = GitCommit(
                    hash = hash,
                    message = commitDef.message,
                    author = commitDef.author,
                    parents = parents,
                    tree = tree,
                )
                previousHash = hash
            }
            val branchTips = definition.branches.ifEmpty {
                mapOf(definition.initialBranch to commits.keys.last())
            }.mapValues { (_, tip) -> resolveCommitRef(commits, tip) }
            val headHash = branchTips.getValue(definition.initialBranch)
            val headTree = commits.getValue(headHash).tree
            val working = if (definition.workingTree != null) {
                headTree + definition.workingTree
            } else {
                headTree
            }
            return GitRepositoryState(
                currentBranch = definition.initialBranch,
                branches = branchTips,
                commits = commits,
                index = headTree,
                workingTree = working,
            )
        }

        private fun resolveCommitRef(commits: Map<String, GitCommit>, ref: String): String {
            commits[ref]?.let { return it.hash }
            commits.values.firstOrNull { it.hash.startsWith(ref) }?.let { return it.hash }
            error("Unknown commit ref '$ref'")
        }
    }
}
