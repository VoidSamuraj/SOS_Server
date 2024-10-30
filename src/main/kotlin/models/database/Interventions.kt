
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


/**
 * Object representing the Interventions table in the database.
 *
 * Defines the schema for the Interventions table, including fields for the intervention ID, report ID,
 * guard ID, employee ID, start time, end time, and status code. The primary key is set to the ID field.
 */
object Interventions : Table() {
    val id = integer("id").autoIncrement()
    val report_id = reference("report_id", Reports.id, onDelete = ReferenceOption.CASCADE)
    val guard_id = reference("guard_id", Guards.id, onDelete = ReferenceOption.NO_ACTION)
    val employee_id = reference("dispatcher_id", Employees.id, onDelete = ReferenceOption.NO_ACTION)
    val start_time = datetime("start_time")
    val end_time = datetime("end_time")
    val status = short("status")
    override val primaryKey = PrimaryKey(id)
}