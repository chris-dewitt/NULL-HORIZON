package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.toStateMap
import com.nullhorizon.app.simulation.git.GitSimulator
import com.nullhorizon.app.simulation.sql.SqlSimulator
import com.nullhorizon.app.simulation.terminal.TerminalSimulator
import com.nullhorizon.app.simulation.terminal.VirtualFileSystem
import com.nullhorizon.app.simulation.terminal.VirtualFsEntry

/**
 * Deterministic mission lifecycle. Reset always restores environment.seed initial state.
 */
class MissionStateMachine(
    private val mission: MissionDefinition,
    private val objectiveEngine: ObjectiveEngine = ObjectiveEngine(),
    private val hintEngine: HintEngine = HintEngine(),
) {
    private val terminalSimulator: TerminalSimulator? = mission.environment.filesystem?.let { fsDef ->
        val vfs = VirtualFileSystem.fromEntries(
            fsDef.entries.map { entry ->
                VirtualFsEntry(path = entry.path, type = entry.type, content = entry.content)
            },
        )
        TerminalSimulator(vfs)
    }

    private val gitSimulator: GitSimulator? =
        if (mission.environment.git != null) GitSimulator() else null

    private val initialGitState = mission.environment.git?.let { GitSimulator.fromDefinition(it) }

    private val sqlSimulator: SqlSimulator? =
        mission.environment.databases.firstOrNull()?.let { SqlSimulator(it) }

    fun initialState(): MissionSessionState {
        val terminal = terminalSimulator?.initialState(
            mission.environment.filesystem?.cwd ?: "/",
        )
        val sql = sqlSimulator?.reset()
        return MissionSessionState(
            phase = MissionPhase.Briefing,
            worldState = mission.environment.initialState.toStateMap(),
            terminal = terminal,
            git = initialGitState,
            sql = sql,
            completedObjectiveIds = emptySet(),
            hintLevel = 0,
            lastActionMessage = null,
        )
    }

    fun begin(state: MissionSessionState): MissionSessionState {
        if (state.phase == MissionPhase.Completed) return state
        return evaluate(
            state.copy(
                phase = MissionPhase.InProgress,
                lastActionMessage = null,
            ),
        )
    }

    fun reset(): MissionSessionState = initialState()

    fun requestHint(state: MissionSessionState): MissionSessionState {
        val nextLevel = hintEngine.nextLevel(state.hintLevel, mission.hints.size)
        return state.copy(hintLevel = nextLevel)
    }

    fun applyAction(state: MissionSessionState, actionId: String): MissionSessionState {
        if (state.phase != MissionPhase.InProgress) {
            return state.copy(lastActionMessage = "Start the mission before applying actions.")
        }
        val action = mission.environment.actions.firstOrNull { it.id == actionId }
            ?: return state.copy(lastActionMessage = "Unknown action.")

        val requires = action.requires.toStateMap()
        val unmet = requires.filter { (key, expected) -> state.worldState[key] != expected }
        if (unmet.isNotEmpty()) {
            return state.copy(
                lastActionMessage = "Action blocked. Requirements not met: ${unmet.keys.joinToString()}",
            )
        }

        val nextWorld = state.worldState + action.effects.toStateMap()
        return evaluate(
            state.copy(
                worldState = nextWorld,
                lastActionMessage = "Applied: ${action.label}",
            ),
        )
    }

    fun runCommand(state: MissionSessionState, line: String): MissionSessionState {
        if (state.phase != MissionPhase.InProgress) {
            return state.copy(lastActionMessage = "Start the mission before using the terminal.")
        }
        val simulator = terminalSimulator
            ?: return state.copy(lastActionMessage = "This mission has no terminal.")
        val terminalState = state.terminal
            ?: return state.copy(lastActionMessage = "Terminal is not initialized.")
        val nextTerminal = simulator.execute(terminalState, line)
        return evaluate(
            state.copy(
                terminal = nextTerminal,
                lastActionMessage = null,
            ),
        )
    }

    fun runGitCommand(state: MissionSessionState, line: String): MissionSessionState {
        if (state.phase != MissionPhase.InProgress) {
            return state.copy(lastActionMessage = "Start the mission before using Git.")
        }
        val simulator = gitSimulator
            ?: return state.copy(lastActionMessage = "This mission has no Git repository.")
        val gitState = state.git
            ?: return state.copy(lastActionMessage = "Git repository is not initialized.")
        val nextGit = simulator.execute(gitState, line)
        return evaluate(
            state.copy(
                git = nextGit,
                lastActionMessage = null,
            ),
        )
    }

    fun runSqlQuery(state: MissionSessionState, query: String): MissionSessionState {
        if (state.phase != MissionPhase.InProgress) {
            return state.copy(lastActionMessage = "Start the mission before using SQL.")
        }
        val simulator = sqlSimulator
            ?: return state.copy(lastActionMessage = "This mission has no SQL database.")
        val sqlState = state.sql
            ?: return state.copy(lastActionMessage = "SQL console is not initialized.")
        val nextSql = simulator.execute(sqlState, query)
        return evaluate(
            state.copy(
                sql = nextSql,
                lastActionMessage = null,
            ),
        )
    }

    private fun evaluate(state: MissionSessionState): MissionSessionState {
        val completed = objectiveEngine.completedObjectiveIds(mission, state)
        val isComplete = objectiveEngine.isMissionComplete(mission, completed)
        return state.copy(
            completedObjectiveIds = completed,
            phase = if (isComplete) MissionPhase.Completed else state.phase,
        )
    }
}
