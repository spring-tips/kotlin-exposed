package com.example.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID.randomUUID

@ImportRuntimeHints(Hints::class)
@SpringBootApplication
class ExposedApplication

fun main(args: Array<String>) {
    runApplication<ExposedApplication>(*args)
}

@Component
@Transactional
class Go : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {

        SchemaUtils.create(Customers, Orders)

        Orders.deleteAll()
        Customers.deleteAll()

        val ids: Iterable<Int> = listOf(
                "Olga", "Violetta", "Dr. Syer", "Stéphane", "Hadi", "Yuxin", "Josh", "Dave", "Madhura")
                .map { Customer(null, it) }
                .map { customer ->
                    Customers.insertAndGetId {
                        it[name] = customer.name
                    }
                }
                .map { it.value }

        val first = ids.first()
        listOf(randomUUID().toString(), randomUUID().toString())
                .forEach { sku ->
                    Orders.insert {
                        it[Orders.sku] = sku
                        it[Orders.customerId] = first
                    }
                }

        println("=".repeat(100))
        Customers
                .selectAll()
                .map { Customer(it[Customers.id].value, it[Customers.name]) }
                .forEach { println("got ${it}") }
        println("=".repeat(100))


        // query and print orders for the customer
       val ordersForCustomer =  (Customers innerJoin Orders)
               .selectAll().where { Orders.customerId eq first }
                .map { Order (it[Orders.id].value ,it[Orders.sku]) }
        println(ordersForCustomer)

    }
}

data class Customer(val id: Int?, val name: String)

data class Order(val id: Int, val sku: String)

object Orders : IntIdTable("orders") {
    val sku = text("sku")
    val customerId = reference("customerId", Customers) // This creates the "1 to many" relationship
}

object Customers : IntIdTable("customers") {
    val name = varchar("name", 50)
}


class Hints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        println("running the Exposed ORM hints!")
        val classes = arrayOf(
                org.jetbrains.exposed.spring.DatabaseInitializer::class,
                org.jetbrains.exposed.spring.SpringTransactionManager::class,

                java.util.Collections::class,
                Column::class,
                Database::class,
                Op::class ,
                Op.Companion::class ,
                DdlAware::class,
                Expression::class,
                ExpressionWithColumnType::class,
                ColumnType::class,
                DatabaseConfig::class,
                IColumnType::class,
                IntegerColumnType::class,
                PreparedStatementApi::class,
                ForeignKeyConstraint::class,
                IColumnType::class,
                QueryBuilder::class,
                Table::class,
                Transaction::class,
                TransactionManager::class,
                Column::class,
                Database::class,
                kotlin.jvm.functions.Function0::class,
                kotlin.jvm.functions.Function1::class,
                kotlin.jvm.functions.Function2::class,
                kotlin.jvm.functions.Function3::class,
                kotlin.jvm.functions.Function4::class,
                kotlin.jvm.functions.Function5::class,
                kotlin.jvm.functions.Function6::class,
                kotlin.jvm.functions.Function7::class,
                kotlin.jvm.functions.Function8::class,
                kotlin.jvm.functions.Function9::class,
                kotlin.jvm.functions.Function10::class,
                kotlin.jvm.functions.Function11::class,
                kotlin.jvm.functions.Function12::class,
                kotlin.jvm.functions.Function13::class,
                kotlin.jvm.functions.Function14::class,
                kotlin.jvm.functions.Function15::class,
                kotlin.jvm.functions.Function16::class,
                kotlin.jvm.functions.Function17::class,
                kotlin.jvm.functions.Function18::class,
                kotlin.jvm.functions.Function19::class,
                kotlin.jvm.functions.Function20::class,
                kotlin.jvm.functions.Function21::class,
                kotlin.jvm.functions.Function22::class,
                kotlin.jvm.functions.FunctionN::class
            )
            .map {  it.java }
        val mcs = MemberCategory.values()
        classes.forEach {
            hints.reflection().registerType(it, *mcs)
        }

        val resources = listOf("META-INF/services/org.jetbrains.exposed.dao.id.EntityIDFactory",
                "META-INF/services/org.jetbrains.exposed.sql.DatabaseConnectionAutoRegistration",
                "META-INF/services/org.jetbrains.exposed.sql.statements.GlobalStatementInterceptor")
                .map { ClassPathResource(it) }
        resources.forEach {
            hints.resources().registerResource(it)
        }


    }

}