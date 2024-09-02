package dao

import Customers
import Employees
import Guards
import Interventions
import Reports
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(jdbcURL: String , driverClassName: String, user: String, password: String):Database {
        val database = Database.connect(jdbcURL, driverClassName,user=user, password=password)
        transaction(database) {
            SchemaUtils.create(Customers,Employees,Guards,Interventions,Reports)
        }
        return database
    }
    fun init(jdbcURL: String , driverClassName: String):Database {
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Customers,Employees,Guards,Interventions,Reports)
        }
        return database
    }
}

