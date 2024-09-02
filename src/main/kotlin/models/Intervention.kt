import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

@Serializable
data class Intervention(val id:Int,val report_id: Int,val guard_id:Int, val employee_id:Int, val patrol_number:Int)
object Interventions : Table() {
    val id = integer("id").autoIncrement()
    val reportId = reference("report_id", Reports.id, onDelete = ReferenceOption.CASCADE )
    val guardId = reference("guard_id", Guards.id, onDelete = ReferenceOption.NO_ACTION)
    val employeeId = reference("dispatcher_id", Employees.id, onDelete = ReferenceOption.NO_ACTION)
    val patrolNumber = integer("patrol_number")
    override val primaryKey = PrimaryKey(id)
}