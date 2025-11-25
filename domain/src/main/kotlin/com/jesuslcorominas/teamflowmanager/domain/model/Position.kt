package com.jesuslcorominas.teamflowmanager.domain.model

enum class Position(val id: String) {
    Goalkeeper("goalkeeper"),
    Defender("defender"),
    RightBack("right_back"),
    LeftBack("left_back"),
    CenterBack("center_back"),
    Midfielder("midfielder"),
    DefensiveMidfielder("defensive_midfielder"),
    CentralMidfielder("central_midfielder"),
    AttackingMidfielder("attacking_midfielder"),
    Forward("forward"),
    Winger("winger"),
    Striker("striker");

    companion object {
        fun fromId(id: String): Position? =
            entries.find { it.id == id }

        fun getAllPositions(): List<Position> = entries.toList()
    }
}
