package project.capstone.percobaan_capstone.history

data class HistoryItem(
    val imageUrl: String,       // Sesuai dengan Supabase
    val predictedWeight: Float,
    val weightCategory: String,
    val timestamp: String       // Tambahkan timestamp agar sesuai dengan data di Supabase
)
