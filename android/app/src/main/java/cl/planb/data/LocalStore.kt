package cl.planb.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favoritos")
data class Favorito(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String,
    val costo: Int,
    val minutos: Int,
    val interior: Boolean,
)

@Dao
interface FavoritoDao {
    @Query("SELECT * FROM favoritos ORDER BY nombre") fun observar(): Flow<List<Favorito>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun guardar(favorito: Favorito)

    @Query("DELETE FROM favoritos WHERE id=:id") suspend fun eliminar(id: Long)
}

@Database(entities = [Favorito::class], version = 1, exportSchema = false)
abstract class LocalStore : RoomDatabase() {
    abstract fun favoritos(): FavoritoDao

    companion object {
        @Volatile private var instancia: LocalStore? = null

        fun abrir(context: Context): LocalStore =
            instancia
                ?: synchronized(this) {
                    instancia
                        ?: Room.databaseBuilder(
                                context.applicationContext,
                                LocalStore::class.java,
                                "planb-local.db",
                            )
                            .build()
                            .also { instancia = it }
                }
    }
}
