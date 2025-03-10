// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

var global = Z(0)

inline class Z(val x: Int) {
    var nonNullTest: Z
        get() = Z(global.x + this.x)
        set(value) {
            global = Z(this.x + value.x)
        }

    var nullableTest: Z?
        get() = Z(global.x + this.x)
        set(value) {
            global = Z(this.x + value!!.x)
        }
}

inline class S(val x: String) {
    var nonNullTest: Z
        get() = Z(global.x + x.toInt())
        set(value) {
            global = Z(this.x.toInt() + value.x)
        }

    var nullableTest: Z?
        get() = Z(global.x + x.toInt())
        set(value) {
            global = Z(this.x.toInt() + value!!.x)
        }
}

inline class A(val x: Any) {
    var nonNullTest: Z
        get() = Z(global.x + this.x as Int)
        set(value) {
            global = Z(this.x as Int + value.x)
        }

    var nullableTest: Z?
        get() = Z(global.x + this.x as Int)
        set(value) {
            global = Z(this.x as Int + value!!.x)
        }

}

fun box(): String {
    val zZero = Z(0)
    val zOne = Z(1)
    val zTwo = Z(2)
    val zThree = Z(3)
    val zFour = Z(4)

    val sOne = S("1")

    val aOne = A(1)

    global = zZero
    assertEquals(zOne, Z::nonNullTest.call(zOne))
    assertEquals(zOne, zOne::nonNullTest.call())
    assertEquals(zOne, Z::nonNullTest.getter.call(zOne))
    assertEquals(zOne, zOne::nonNullTest.getter.call())
    Z::nonNullTest.setter.call(zOne, zTwo)
    assertEquals(zThree, global)
    zOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, Z::nullableTest.call(zOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, zOne::nullableTest.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, Z::nullableTest.getter.call(zOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, zOne::nullableTest.getter.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        Z::nullableTest.setter.call(zOne, zTwo)
        assertEquals(zThree, global)
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        zOne::nullableTest.setter.call(zThree)
        assertEquals(zFour, global)
    }

    global = zZero
    assertEquals(zOne, S::nonNullTest.call(sOne))
    assertEquals(zOne, sOne::nonNullTest.call())
    assertEquals(zOne, S::nonNullTest.getter.call(sOne))
    assertEquals(zOne, sOne::nonNullTest.getter.call())
    S::nonNullTest.setter.call(sOne, zTwo)
    assertEquals(zThree, global)
    sOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, S::nullableTest.call(sOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, sOne::nullableTest.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, S::nullableTest.getter.call(sOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, sOne::nullableTest.getter.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        S::nullableTest.setter.call(sOne, zTwo)
        assertEquals(zThree, global)
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        sOne::nullableTest.setter.call(zThree)
        assertEquals(zFour, global)
    }

    global = zZero
    assertEquals(zOne, A::nonNullTest.call(aOne))
    assertEquals(zOne, aOne::nonNullTest.call())
    assertEquals(zOne, A::nonNullTest.getter.call(aOne))
    assertEquals(zOne, aOne::nonNullTest.getter.call())
    A::nonNullTest.setter.call(aOne, zTwo)
    assertEquals(zThree, global)
    aOne::nonNullTest.setter.call(zThree)
    assertEquals(zFour, global)

    global = zZero
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, A::nullableTest.call(aOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, aOne::nullableTest.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, A::nullableTest.getter.call(aOne))
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        assertEquals(zOne, aOne::nullableTest.getter.call())
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        A::nullableTest.setter.call(aOne, zTwo)
        assertEquals(zThree, global)
    }
    assertFailsWith<IllegalArgumentException>("Remove assertFailsWith and try again, as this problem may have been fixed.") {
        aOne::nullableTest.setter.call(zThree)
        assertEquals(zFour, global)
    }

    return "OK"
}
