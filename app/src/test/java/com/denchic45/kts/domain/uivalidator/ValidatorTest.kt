package com.denchic45.kts.domain.uivalidator

import com.denchic45.kts.data.model.domain.EitherMessage
import com.denchic45.kts.domain.uivalidator.util.AnyRule
import com.denchic45.kts.domain.uivalidator.util.CompositeRule
import com.denchic45.kts.domain.uivalidator.util.ErrorMessage
import com.denchic45.kts.domain.uivalidator.util.NotEmpty
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ValidatorTest {

    @Test
    fun test() {
        val validator = Validator(
            Condition(
                value = { "lol" },
                rule = CompositeRule(
                    NotEmpty { ErrorMessage.Stroke("Строка пустая!: $it") },
                    Rule(
                        predicate = ({ s: String -> s == "lol" }),
                        errorMessage = { ErrorMessage.Stroke("Эта строка не lol!: $it") }
                    )
                ),
                onError = { print((it as EitherMessage.Stroke).value) }
            )
        )

        Assertions.assertTrue(validator.isValid())
    }

    @Test
    fun testAny() {
        val validator = Validator(
            Condition(
                value = { "1" },
                rule = AnyRule(
                    Rule(
                        predicate = { value -> value != "1" },
                        errorMessage = { ErrorMessage.Stroke("Значение не должно быть равно: 1") }
                    ),
                    NotEmpty { ErrorMessage.Stroke("Не должно быть пустым") },
                    message = { ErrorMessage.Stroke("Все условия не соблюдены!") }
                ),
                onError = { println((it as ErrorMessage.Stroke).value) }
            )
        )
        validator.validate()

        Assertions.assertTrue(validator.isValid())
    }
}

interface CoffeeMachine {
    fun makeSmallCoffee()
    fun makeLargeCoffee()
}

class NormalCoffeeMachine : CoffeeMachine {
    override fun makeSmallCoffee() = println("Normal: Making small coffee")

    override fun makeLargeCoffee() = println("Normal: Making large coffee")
}

//Decorator:
class EnhancedCoffeeMachine(private val coffeeMachine: CoffeeMachine) :
    CoffeeMachine by coffeeMachine {

    // overriding behaviour
    override fun makeLargeCoffee() {
        println("Enhanced: Making large coffee")
    }

    // extended behaviour
    fun makeCoffeeWithMilk() {
        println("Enhanced: Making coffee with milk")
        coffeeMachine.makeSmallCoffee()
        addMilk()
    }

    private fun addMilk() {
        println("Enhanced: Adding milk")
    }
}

class DecoratorTest {

    @Test
    fun testDecorator() {
        val normalMachine = NormalCoffeeMachine()
        val enhancedMachine = EnhancedCoffeeMachine(normalMachine)

        // non-overridden behaviour
        enhancedMachine.makeSmallCoffee()
        // overridden behaviour
        enhancedMachine.makeLargeCoffee()
        // extended behaviour
        enhancedMachine.makeCoffeeWithMilk()
    }
}