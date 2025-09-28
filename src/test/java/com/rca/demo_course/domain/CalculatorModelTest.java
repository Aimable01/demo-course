package com.rca.demo_course.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
public class CalculatorModelTest {

    private CalculatorModel calculatorModel;
    @BeforeAll
    static void setUpBeforeClass()  {
        log.info("setUpBeforeClass");
    }

    @BeforeEach
    void setUp() {
        calculatorModel = new CalculatorModel();
    }

    @Test
    @DisplayName("Adding two numbers")
    void testAdd_givenTwoNumbers_thenReturnSum(){
        //Act
        double result = calculatorModel.add(4,6);

        //Assert
        Assertions.assertEquals(10.0,result,"Adding two numbers");
    }

    @Test
    void testAdd_givenNegativeNumbers_thenReturnNegativeSum(){
        double result = calculatorModel.add(-4,-6);
        Assertions.assertEquals(-10,result,"Adding negative numbers");

    }
    @Test
    void testSubtract_givenValidNumbers_thenReturnDifference(){
        double result = calculatorModel.subtract(6,2);
        Assertions.assertEquals(4,result,"Subtract valid numbers");
    }
}
