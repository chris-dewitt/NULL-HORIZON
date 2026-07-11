package com.nullhorizon.app.simulation.terminal

/**
 * Immutable virtual filesystem node. Never backed by Android storage.
 */
sealed interface VirtualNode {
    val path: String
}

data class VirtualDirectory(
    override val path: String,
) : VirtualNode

data class VirtualFile(
    override val path: String,
    val content: String,
) : VirtualNode

data class VirtualFileSystem(
    private val nodes: Map<String, VirtualNode>,
) {
    fun get(path: String): VirtualNode? = nodes[normalize(path)]

    fun exists(path: String): Boolean = nodes.containsKey(normalize(path))

    fun isDirectory(path: String): Boolean = get(path) is VirtualDirectory

    fun isFile(path: String): Boolean = get(path) is VirtualFile

    fun list(path: String): List<String> {
        val dir = normalize(path).trimEnd('/')
        val prefix = if (dir == "/") "/" else "$dir/"
        return nodes.keys
            .filter { key ->
                if (dir == "/") {
                    key != "/" && key.count { it == '/' } == 1
                } else {
                    key.startsWith(prefix) &&
                        key.removePrefix(prefix).isNotEmpty() &&
                        !key.removePrefix(prefix).contains('/')
                }
            }
            .map { key ->
                if (dir == "/") key.removePrefix("/") else key.removePrefix(prefix)
            }
            .sorted()
    }

    fun resolve(cwd: String, target: String): String {
        val absolute = when {
            target.startsWith("/") -> target
            else -> {
                val base = if (cwd == "/") "" else cwd.trimEnd('/')
                "$base/$target"
            }
        }
        return normalize(absolute)
    }

    companion object {
        fun normalize(path: String): String {
            val parts = mutableListOf<String>()
            for (part in path.split('/')) {
                when (part) {
                    "", "." -> Unit
                    ".." -> if (parts.isNotEmpty()) parts.removeAt(parts.lastIndex)
                    else -> parts += part
                }
            }
            return if (parts.isEmpty()) "/" else "/" + parts.joinToString("/")
        }

        fun fromEntries(entries: List<VirtualFsEntry>): VirtualFileSystem {
            val nodes = linkedMapOf<String, VirtualNode>()
            nodes["/"] = VirtualDirectory("/")
            for (entry in entries) {
                val path = normalize(entry.path)
                require(path.startsWith("/")) { "VFS paths must be absolute: ${entry.path}" }
                require(".." !in entry.path.split('/')) { "VFS path escapes sandbox: ${entry.path}" }
                when (entry.type) {
                    "dir" -> nodes[path] = VirtualDirectory(path)
                    "file" -> nodes[path] = VirtualFile(path, entry.content.orEmpty())
                    else -> error("Unknown VFS node type: ${entry.type}")
                }
                // Ensure parent directories exist.
                var parent = path.substringBeforeLast('/', missingDelimiterValue = "")
                while (parent.isNotEmpty()) {
                    val normalizedParent = normalize(parent)
                    if (!nodes.containsKey(normalizedParent)) {
                        nodes[normalizedParent] = VirtualDirectory(normalizedParent)
                    }
                    parent = normalizedParent.substringBeforeLast('/', missingDelimiterValue = "")
                    if (normalizedParent == "/") break
                }
            }
            return VirtualFileSystem(nodes)
        }
    }
}

data class VirtualFsEntry(
    val path: String,
    val type: String,
    val content: String? = null,
)
