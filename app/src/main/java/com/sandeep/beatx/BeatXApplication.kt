package com.sandeep.beatx

import android.app.Application
import com.sandeep.beatx.db.BeatXDatabase

class BeatXApplication : Application() {
    val database by lazy { BeatXDatabase.getDatabase(this) }
}
