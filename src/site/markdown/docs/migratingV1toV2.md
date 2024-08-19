# V1 to V2 Migration Guide

Version 2 of MyBatis Dynamic SQL introduced many new features. On this page we will provide examples for the more
significant changes - changes that are more substantial than following deprecation messages.

## Kotlin Join Syntax

The Java DSL for joins was changed to allow much more flexible joins. Of course, not all capabilities are supported in
all databases, but you should now be able to code most joins specification that are supported by your database.
The changes in the Java DSL are mostly internal and should not impact most users. The `equalTo` methods has been
deprecated in favor of `isEqualTo`, but all other changes should be hidden.

Like the Java DSL, the V2 Kotlin DSL offers a fully flexible join specification and allows for much more flexible join
specifications. The changes in the Kotlin DSL allow a more natural expressions of a join specification. The main
difference is that the "on" keyword should be moved outside the join specification lambda (it is now an infix function).
Inside the lambda, the conditions should be rewritten to match the syntax of a where clause.

V1 (Deprecated) Join Specification Example:
```kotlin
val selectStatement = select(
    orderMaster.orderId, orderMaster.orderDate,
    orderDetail.lineNumber, orderDetail.description, orderDetail.quantity
) {
    from(orderMaster, "om")
    join(orderDetail, "od") {
        on(orderMaster.orderId) equalTo orderDetail.orderId
        and(orderMaster.orderId) equalTo constant("1")
    }
}
```

V2 Join Specification Example:
```kotlin
val selectStatement = select(
    orderMaster.orderId, orderMaster.orderDate,
    orderDetail.lineNumber, orderDetail.description, orderDetail.quantity
) {
    from(orderMaster, "om")
    join(orderDetail, "od") on {
        orderMaster.orderId isEqualTo orderDetail.orderId
        and { orderMaster.orderId isEqualTo constant("1") }
    }
}
```

Notice that the "on" keyword has been moved outside the lambda, and the conditions are coded with the same syntax used
by WHERE, HAVING, and CASE expressions.

The prior syntax is deprecated and will be removed in a future release.
