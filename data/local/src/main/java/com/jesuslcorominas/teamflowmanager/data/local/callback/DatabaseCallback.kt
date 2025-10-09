package com.jesuslcorominas.teamflowmanager.data.local.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Carlos', 'García', NULL, 'goalkeeper')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Miguel', 'López', NULL, 'defender,right_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('David', 'Martínez', NULL, 'center_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Javier', 'Sánchez', NULL, 'defender,left_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Antonio', 'Fernández', NULL, 'midfielder,defensive_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Manuel', 'González', NULL, 'central_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Francisco', 'Rodríguez', NULL, 'midfielder,attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('José', 'Pérez', NULL, 'forward,winger')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Daniel', 'Moreno', NULL, 'attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Pablo', 'Jiménez', NULL, 'striker')")
    }
}
