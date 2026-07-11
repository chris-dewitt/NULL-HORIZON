package com.nullhorizon.app.simulation.terminal

fun interface TerminalCommand {
    fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution
}

data class TerminalCommandContext(
    val fs: VirtualFileSystem,
    val cwd: String,
    val processes: List<VirtualProcess>,
)

data class CommandExecution(
    val result: CommandResult,
    val cwd: String,
    val processes: List<VirtualProcess>? = null,
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
                "ps" to PsCommand,
                "kill" to KillCommand,
            ),
        )
    }
}

object PwdCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.isNotEmpty()) {
            return CommandExecution(CommandResult.error("pwd: too many arguments"), ctx.cwd)
        }
        return CommandExecution(CommandResult.ok(ctx.cwd), ctx.cwd)
    }
}

object LsCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        val target = args.firstOrNull() ?: "."
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("ls: too many arguments"), ctx.cwd)
        }
        val path = ctx.fs.resolve(ctx.cwd, target)
        if (!ctx.fs.exists(path)) {
            return CommandExecution(
                CommandResult.error("ls: cannot access '$target': No such file or directory"),
                ctx.cwd,
            )
        }
        if (ctx.fs.isFile(path)) {
            return CommandExecution(CommandResult.ok(path.substringAfterLast('/')), ctx.cwd)
        }
        val listing = ctx.fs.list(path).joinToString("\n")
        return CommandExecution(CommandResult.ok(listing), ctx.cwd)
    }
}

object CdCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("cd: too many arguments"), ctx.cwd)
        }
        val target = args.firstOrNull() ?: "/"
        val path = ctx.fs.resolve(ctx.cwd, target)
        if (!ctx.fs.exists(path)) {
            return CommandExecution(CommandResult.error("cd: $target: No such file or directory"), ctx.cwd)
        }
        if (!ctx.fs.isDirectory(path)) {
            return CommandExecution(CommandResult.error("cd: $target: Not a directory"), ctx.cwd)
        }
        return CommandExecution(CommandResult.ok(), path)
    }
}

object CatCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.isEmpty()) {
            return CommandExecution(CommandResult.error("cat: missing file operand"), ctx.cwd)
        }
        if (args.size > 1) {
            return CommandExecution(CommandResult.error("cat: too many arguments"), ctx.cwd)
        }
        val path = ctx.fs.resolve(ctx.cwd, args.first())
        val node = ctx.fs.get(path)
            ?: return CommandExecution(
                CommandResult.error("cat: ${args.first()}: No such file or directory"),
                ctx.cwd,
            )
        if (node is VirtualDirectory) {
            return CommandExecution(CommandResult.error("cat: ${args.first()}: Is a directory"), ctx.cwd)
        }
        val file = node as VirtualFile
        return CommandExecution(CommandResult.ok(file.content.trimEnd('\n')), ctx.cwd)
    }
}

object GrepCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.size < 2) {
            return CommandExecution(CommandResult.error("grep: usage: grep PATTERN FILE"), ctx.cwd)
        }
        if (args.size > 2) {
            return CommandExecution(CommandResult.error("grep: too many arguments"), ctx.cwd)
        }
        val pattern = args[0]
        val path = ctx.fs.resolve(ctx.cwd, args[1])
        val node = ctx.fs.get(path)
            ?: return CommandExecution(
                CommandResult.error("grep: ${args[1]}: No such file or directory"),
                ctx.cwd,
            )
        if (node is VirtualDirectory) {
            return CommandExecution(CommandResult.error("grep: ${args[1]}: Is a directory"), ctx.cwd)
        }
        val file = node as VirtualFile
        val matches = file.content.lineSequence().filter { it.contains(pattern) }.toList()
        return CommandExecution(CommandResult.ok(matches.joinToString("\n")), ctx.cwd)
    }
}

object PsCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.isNotEmpty()) {
            return CommandExecution(CommandResult.error("ps: too many arguments"), ctx.cwd)
        }
        val running = ctx.processes.filter { it.status == "running" }.sortedBy { it.pid }
        val lines = buildList {
            add("  PID CMD")
            for (process in running) {
                val cmd = process.command.ifBlank { process.name }
                add("%5d %s".format(process.pid, cmd))
            }
        }
        return CommandExecution(CommandResult.ok(lines.joinToString("\n")), ctx.cwd)
    }
}

object KillCommand : TerminalCommand {
    override fun execute(ctx: TerminalCommandContext, args: List<String>): CommandExecution {
        if (args.size != 1) {
            return CommandExecution(CommandResult.error("kill: usage: kill PID"), ctx.cwd)
        }
        val pid = args[0].toIntOrNull()
            ?: return CommandExecution(CommandResult.error("kill: invalid pid '${args[0]}'"), ctx.cwd)
        val index = ctx.processes.indexOfFirst { it.pid == pid }
        if (index < 0) {
            return CommandExecution(CommandResult.error("kill: ($pid) - No such process"), ctx.cwd)
        }
        val process = ctx.processes[index]
        if (process.status != "running") {
            return CommandExecution(CommandResult.error("kill: ($pid) - No such process"), ctx.cwd)
        }
        val next = ctx.processes.toMutableList()
        next[index] = process.copy(status = "stopped")
        return CommandExecution(CommandResult.ok(), ctx.cwd, processes = next)
    }
}
