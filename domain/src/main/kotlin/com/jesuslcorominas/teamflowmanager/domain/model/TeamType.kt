package com.jesuslcorominas.teamflowmanager.domain.model

enum class TeamType(val players: Int) {
    FOOTBALL_5(5),
    FOOTBALL_7(7),
    FOOTBALL_8(8),
    FOOTBALL_11(11);

    companion object {
        fun fromPlayers(players: Int): TeamType {
            return entries.find { it.players == players } ?: FOOTBALL_11
        }
    }
}
