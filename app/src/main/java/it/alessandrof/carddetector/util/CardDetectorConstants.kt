package it.alessandrof.carddetector.util

import android.Manifest

const val MAX_RESULT_DISPLAY = 1 // Numero massimo di risultati restituiti, una possibile implementazione è riconoscere più elementi per questo ho creato una lista di Recognition
const val TAG = "Card Detector" // Nome per il logging
const val REQUEST_CODE_PERMISSIONS = 999 // Return code after asking for permission
val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA) // permission needed