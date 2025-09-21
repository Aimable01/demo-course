package com.rca.demo_course.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

@Slf4j
public class CalculatorModelTest {

    // when, given, return

    // Arrange
    private CalculatorModel calculatorModel;

    @BeforeAll
     static void setupBeforeClass(){
        log.info("Setup before class");
    }

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

    @Test
    @DisplayName("Diving two numbers")
    void testDiv_TwoValidNumbers_returnDivision(){
        // Act
        double res = calculatorModel.divide(10,2);

        // Assert
        Assertions.assertEquals(5,res);
    }
}
