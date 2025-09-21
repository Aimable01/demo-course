package com.rca.demo_course.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CalculatorModelTest {

    // Arrange
    private CalculatorModel calculatorModel;
    @BeforeEach
    public void setUp() {
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
        //Act
        double sum = calculatorModel.add(-4,-5);
        //Assert
        Assertions.assertEquals(-9,sum,"Adding two numbers");
    }

    @Test
    @DisplayName("Subtracting two numbers")
    void testSubtract_TwoValidNumbers_returnDifference(){

        // Act
        double difference = calculatorModel.subtract(4,5);

        // Assert
        Assertions.assertEquals(-1,difference);

    }

    @Test
    @DisplayName("Multiplying two numbers")
    void testMultiply_TwoValidNumbers_returnProduct(){

        // Act
        double product = calculatorModel.multiply(4,5);

        // Assert
        Assertions.assertEquals(20,product);
    }
}
