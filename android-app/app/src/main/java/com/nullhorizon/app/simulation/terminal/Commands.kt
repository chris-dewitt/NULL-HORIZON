package com.nullhorizon.app.simulation.terminal

fun interface TerminalCommand {
    fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution
}

data class CommandExecution(
    val result: CommandResult,
    val cwd: String,
)

class CommandRegistry(
    private val commands: Map<String, TerminalCommand>,
) {
    fun get(name: String): TerminalCommand? = commands[name]

    fun names(): Set<String> = commands.keys

    companion object {
        fun default(): CommandRegistry = CommandRegistry(
            mapOf(
                "pwd" to PwdCommand,
                "ls" to LsCommand,
                "cd" to CdCommand,
                "cat" to CatCommand,
                "grep" to GrepCommand,
            ),
        )
    }
}

object PwdCommand : TerminalCommand {
    override fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution {
        if (args.isNotEmpty()) {
            return CommandExecution(CommandResult.error("pwd: too many arguments"), cwd)
        }
        return CommandExecution(CommandResult.ok(cwd), cwd)
    }
}

object LsCommand : TerminalCommand {
    override fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution {
        val target = args.firstOrNull() ?: "."
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("ls: too many arguments"), cwd)
        }
        val path = fs.resolve(cwd, target)
        if (!fs.exists(path)) {
            return CommandExecution(CommandResult.error("ls: cannot access '$target': No such file or directory"), cwd)
        }
        if (fs.isFile(path)) {
            return CommandExecution(CommandResult.ok(path.substringAfterLast('/')), cwd)
        }
        val listing = fs.list(path).joinToString("\n")
        return CommandExecution(CommandResult.ok(listing), cwd)
    }
}

object CdCommand : TerminalCommand {
    override fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution {
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("cd: too many arguments"), cwd)
        }
        val target = args.firstOrNull() ?: "/"
        val path = fs.resolve(cwd, target)
        if (!fs.exists(path)) {
            return CommandExecution(CommandResult.error("cd: $target: No such file or directory"), cwd)
        }
        if (!fs.isDirectory(path)) {
            return CommandExecution(CommandResult.error("cd: $target: Not a directory"), cwd)
        }
        return CommandExecution(CommandResult.ok(), path)
    }
}

object CatCommand : TerminalCommand {
    override fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution {
        if (args.isEmpty()) {
            return CommandExecution(CommandResult.error("cat: missing file operand"), cwd)
        }
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("cat: too many arguments"), cwd)
        }
        val path = fs.resolve(cwd, args.first())
        val node = fs.get(path)
            ?: return CommandExecution(
                CommandResult.error("cat: ${args.first()}: No such file or directory"),
                cwd,
            )
        if (node is VirtualDirectory) {
            return CommandExecution(CommandResult.error("cat: ${args.first()}: Is a directory"), cwd)
        }
        val file = node as VirtualFile
        return CommandExecution(CommandResult.ok(file.content.trimEnd('\n')), cwd)
    }
}

object GrepCommand : TerminalCommand {
    override fun execute(fs: VirtualFileSystem, cwd: String, args: List<String>): CommandExecution {
        if (args.size < 2) {
            return CommandExecution(CommandResult.error("grep: usage: grep PATTERN FILE"), cwd)
        }
        if (args.size > 2) {
            return CommandExecution(CommandResult.error("grep: too many arguments"), cwd)
        }
        val pattern = args[0]
        val path = fs.resolve(cwd, args[1])
        val node = fs.get(path)
            ?: return CommandExecution(
                CommandResult.error("grep: ${args[1]}: No such file or directory"),
                cwd,
            )
        if (node is VirtualDirectory) {
            return CommandExecution(CommandResult.error("grep: ${args[1]}: Is a directory"), cwd)
        }
        val file = node as VirtualFile
        val matches = file.content.lineSequence().filter { it.contains(pattern) }.toList()
        return CommandExecution(CommandResult.ok(matches.joinToString("\n")), cwd)
    }
}
