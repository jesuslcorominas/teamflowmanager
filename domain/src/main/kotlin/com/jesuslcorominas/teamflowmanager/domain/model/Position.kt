package com.jesuslcorominas.teamflowmanager.domain.model

sealed class Position(
    val id: String,
) {
    data object Goalkeeper : Position("goalkeeper")

    data object Defender : Position("defender")

    data object RightBack : Position("right_back")

    data object LeftBack : Position("left_back")

    data object CenterBack : Position("center_back")

    data object Midfielder : Position("midfielder")

    data object DefensiveMidfielder : Position("defensive_midfielder")

    data object CentralMidfielder : Position("central_midfielder")

    data object AttackingMidfielder : Position("attacking_midfielder")

    data object Forward : Position("forward")

    data object Winger : Position("winger")

    data object Striker : Position("striker")

    companion object {
        fun fromId(id: String): Position? =
            when (id) {
                "goalkeeper" -> Goalkeeper
                "defender" -> Defender
                "right_back" -> RightBack
                "left_back" -> LeftBack
                "center_back" -> CenterBack
                "midfielder" -> Midfielder
                "defensive_midfielder" -> DefensiveMidfielder
                "central_midfielder" -> CentralMidfielder
                "attacking_midfielder" -> AttackingMidfielder
                "forward" -> Forward
                "winger" -> Winger
                "striker" -> Striker
                else -> null
            }

        // TODO insert into database?
        fun getAllPositions(): List<Position> = listOf(
            Goalkeeper,
            Defender,
            RightBack,
            LeftBack,
            CenterBack,
            Midfielder,
            DefensiveMidfielder,
            CentralMidfielder,
            AttackingMidfielder,
            Forward,
            Winger,
            Striker
        )
    }
}
