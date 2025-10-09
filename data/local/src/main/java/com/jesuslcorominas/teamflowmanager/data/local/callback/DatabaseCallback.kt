package com.jesuslcorominas.teamflowmanager.data.local.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Carlos', 'García', '2010-03-15', 'goalkeeper')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Miguel', 'López', '2011-05-20', 'defender,right_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('David', 'Martínez', '2010-09-10', 'center_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Javier', 'Sánchez', '2011-01-25', 'defender,left_back')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Antonio', 'Fernández', '2010-07-08', 'midfielder,defensive_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Manuel', 'González', '2011-11-12', 'central_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Francisco', 'Rodríguez', '2010-04-30', 'midfielder,attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('José', 'Pérez', '2011-08-18', 'forward,winger')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Daniel', 'Moreno', '2010-12-05', 'attacking_midfielder')")
        db.execSQL("INSERT INTO players (firstName, lastName, dateOfBirth, positions) VALUES ('Pablo', 'Jiménez', '2011-06-22', 'striker')")
    }
}
