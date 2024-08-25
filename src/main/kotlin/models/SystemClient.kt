import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class SystemClient(val id:Int,val login: String,val password:String, val phone:String, val pesel:String, val email:String, val account_deleted:Boolean): Principal
object SystemClients : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 40)
    val phone = varchar("phone", 20).uniqueIndex()
    val pesel = varchar("pesel", 15).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val account_deleted=bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}