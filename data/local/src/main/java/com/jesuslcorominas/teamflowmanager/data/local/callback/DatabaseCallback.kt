package com.jesuslcorominas.teamflowmanager.data.local.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Carlos', 'García', 'Portero')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Miguel', 'López', 'Defensa,Lateral derecho')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('David', 'Martínez', 'Defensa central')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Javier', 'Sánchez', 'Defensa,Lateral izquierdo')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Antonio', 'Fernández', 'Centrocampista,Pivote')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Manuel', 'González', 'Centrocampista,Mediocentro')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Francisco', 'Rodríguez', 'Centrocampista,Interior')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('José', 'Pérez', 'Delantero,Extremo')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Daniel', 'Moreno', 'Delantero,Media punta')")
        db.execSQL("INSERT INTO players (firstName, lastName, positions) VALUES ('Pablo', 'Jiménez', 'Delantero centro')")
    }
}
