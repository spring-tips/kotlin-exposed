package com.example.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID.randomUUID

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

        val ids: Iterable<Int> = listOf("Yuxin", "Josh", "Dave", "Madhura")
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


        // Query and print orders for the user

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

