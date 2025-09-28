package com.rca.demo_course.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CalculatorModelTestAB {
    private CalculatorModel calculatorModel;
    @BeforeEach
    void setUp() {
        calculatorModel = new CalculatorModel();
    }
    @Test
    @DisplayName("Adding two valid number")
    void testAdd_TwoValidNumbers_returnSum(){
        //Act
        double sum = calculatorModel.add(4,5);

        //Assert
        Assertions.assertEquals(9,sum);
    }
    @Test
    @DisplayName("Adding negative numbers")
    void testAdd_TwoNegativeNumbers_returnLessThanZero(){
        //act
        double sum = calculatorModel.add(-4,-5);
        //assert
        Assertions.assertEquals(-9,sum);
    }

    @Test
    @DisplayName("Subtracting two numbers")
    void testSubtract_TwoNumbers_returnGreaterThanZero(){
         double result  = calculatorModel.subtract(9, 3);

        Assertions.assertEquals(6,result);
    }
}
