# V1 to V2 Migration Guide

Version 2 of MyBatis Dynamic SQL introduced many new features. This page will document how to migrate code
from prior releases to version 2.

## Java Join Syntax

Version2 offers a fully flexible join specification and reuses the capabilities of where clauses. Of course,
not all capabilities are supported in databases, but you should now be able to code any type of join specification.

The changes in the Java DSL are mostly internal and should not impact most users. The `equalTo` methods has been
deprecated in favor of `isEqualTo`, but all other changes should be hidden.

V1 Join Specification Example:
```java
SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDetail.lineNumber, orderDetail.quantity)
        .from(orderMaster, "om")
        .join(orderDetail, "od", on(orderMaster.orderId, equalTo(orderDetail.orderId)))
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

V2 Join Specification Example:
```java
SelectStatementProvider selectStatement = select(orderMaster.orderId, orderDetail.lineNumber, orderDetail.quantity)
        .from(orderMaster, "om")
        .join(orderDetail, "od", on(orderMaster.orderId, isEqualTo(orderDetail.orderId)))
        .build()
        .render(RenderingStrategies.MYBATIS3);
```

## Kotlin Join Syntax

Like the Java DSL, the V2 Kotlin DSL offers a fully flexible join specification and reuses the capabilities of where
clauses. Of course, not all capabilities are supported in databases, but you should now be able to code any type of
join specification.

The changes in the Kotlin DSL allow a more natural expressions of a join specification. The main difference is that
the "on" keyword should be moved outside the join specification lambda (it is now an infix function). Inside the lambda,
the conditions should be rewritten to match the syntax of a where clause.

V1 Join Specification Example:
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
