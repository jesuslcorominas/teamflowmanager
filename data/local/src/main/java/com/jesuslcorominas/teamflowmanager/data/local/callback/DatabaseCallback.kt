package com.jesuslcorominas.teamflowmanager.data.local.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Carlos', 'García', 'goalkeeper')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Miguel', 'López', 'defender,right_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('David', 'Martínez', 'center_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Javier', 'Sánchez', 'defender,left_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Antonio', 'Fernández', 'midfielder,defensive_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Manuel', 'González', 'central_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Francisco', 'Rodríguez', 'midfielder,attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('José', 'Pérez', 'forward,winger')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Daniel', 'Moreno', 'attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Pablo', 'Jiménez', 'striker')")
    }
}
