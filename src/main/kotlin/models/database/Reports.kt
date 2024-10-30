
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


/**
 * Object representing the Reports table in the database.
 *
 * Defines the schema for the Reports table, including fields for the report ID, client ID, location,
 * date, and status code. The primary key is set to the ID field.
 */

object Reports : Table() {
    val id = integer("id").autoIncrement()
    val client_id = reference("client_id", Customers.id, onDelete = ReferenceOption.NO_ACTION)
    val location = varchar("location", 255)
    val date = datetime("date")
    val status = short("status")
    override val primaryKey = PrimaryKey(id)
}
