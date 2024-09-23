import io.ktor.server.auth.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

@Serializable
data class CustomerInfo(val id:Int, val phone:String, val pesel:String, val email:String, val account_deleted:Boolean, val protection_expiration_date: LocalDateTime?=null): Principal

@Serializable
data class Customer(val id:Int, val login: String, val password:String, val phone:String, val pesel:String, val email:String, val account_deleted:Boolean, val protection_expiration_date: LocalDateTime?=null): Principal
object Customers : Table() {
    val id = integer("id").autoIncrement()
    val login = varchar("login", 20).uniqueIndex()
    val password = varchar("password", 60)
    val phone = varchar("phone", 20).uniqueIndex()
    val pesel = varchar("pesel", 15).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val protection_expiration_date =  datetime("protection_expiration_date").nullable()
    val account_deleted=bool("account_deleted")
    override val primaryKey = PrimaryKey(id)
}