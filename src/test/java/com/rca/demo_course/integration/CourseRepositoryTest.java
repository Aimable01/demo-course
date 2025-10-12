package com.rca.demo_course.integration;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Course Repository Integration Tests")
public class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course testCourse1;
    private Course testCourse2;
    private Course testCourse3;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        courseRepository.deleteAll();

        // Create test courses
        testCourse1 = new Course();
        testCourse1.setName("Introduction to Programming");
        testCourse1.setCode("CS101");
        testCourse1.setCredits(3);

        testCourse2 = new Course();
        testCourse2.setName("Data Structures and Algorithms");
        testCourse2.setCode("CS201");
        testCourse2.setCredits(4);

        testCourse3 = new Course();
        testCourse3.setName("Advanced Programming");
        testCourse3.setCode("CS301");
        testCourse3.setCredits(3);
    }

    @Test
    @DisplayName("Should save and find course by ID")
    void testSaveAndFindById() {
        // Given
        Course savedCourse = courseRepository.save(testCourse1);

        // When
        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());

        // Then
        assertTrue(foundCourse.isPresent());
        assertEquals(testCourse1.getName(), foundCourse.get().getName());
        assertEquals(testCourse1.getCode(), foundCourse.get().getCode());
        assertEquals(testCourse1.getCredits(), foundCourse.get().getCredits());
    }

    @Test
    @DisplayName("Should return empty optional when course not found by ID")
    void testFindByIdNotFound() {
        // When
        Optional<Course> foundCourse = courseRepository.findById(999L);

        // Then
        assertFalse(foundCourse.isPresent());
    }

    @Test
    @DisplayName("Should find course by code")
    void testFindByCode() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When
        Optional<Course> foundCourse = courseRepository.findByCode("CS101");

        // Then
        assertTrue(foundCourse.isPresent());
        assertEquals("Introduction to Programming", foundCourse.get().getName());
        assertEquals("CS101", foundCourse.get().getCode());
        assertEquals(3, foundCourse.get().getCredits());
    }

    @Test
    @DisplayName("Should return empty optional when course not found by code")
    void testFindByCodeNotFound() {
        // Given
        courseRepository.save(testCourse1);

        // When
        Optional<Course> foundCourse = courseRepository.findByCode("NONEXISTENT");

        // Then
        assertFalse(foundCourse.isPresent());
    }

    @Test
    @DisplayName("Should find courses by name containing (case insensitive)")
    void testFindByNameContainingIgnoreCase() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("programming");

        // Then
        assertEquals(2, foundCourses.size());
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS101")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS301")));
    }

    @Test
    @DisplayName("Should find courses by name containing with different case")
    void testFindByNameContainingIgnoreCaseDifferentCase() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("DATA");

        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS201", foundCourses.get(0).getCode());
        assertEquals("Data Structures and Algorithms", foundCourses.get(0).getName());
    }

    @Test
    @DisplayName("Should return empty list when no courses match name")
    void testFindByNameContainingIgnoreCaseNotFound() {
        // Given
        courseRepository.save(testCourse1);

        // When
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("nonexistent");

        // Then
        assertTrue(foundCourses.isEmpty());
    }

    @Test
    @DisplayName("Should find courses by exact credits")
    void testFindByCredits() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        List<Course> foundCourses = courseRepository.findByCredits(3);

        // Then
        assertEquals(2, foundCourses.size());
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS101")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS301")));
    }

    @Test
    @DisplayName("Should return empty list when no courses match credits")
    void testFindByCreditsNotFound() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When
        List<Course> foundCourses = courseRepository.findByCredits(5);

        // Then
        assertTrue(foundCourses.isEmpty());
    }

    @Test
    @DisplayName("Should find courses with credits greater than or equal to minimum")
    void testFindByCreditsGreaterThanEqual() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        List<Course> foundCourses = courseRepository.findByCreditsGreaterThanEqual(4);

        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS201", foundCourses.get(0).getCode());
        assertEquals(4, foundCourses.get(0).getCredits());
    }

    @Test
    @DisplayName("Should find courses with credits greater than or equal to minimum (inclusive)")
    void testFindByCreditsGreaterThanEqualInclusive() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        List<Course> foundCourses = courseRepository.findByCreditsGreaterThanEqual(3);

        // Then
        assertEquals(3, foundCourses.size());
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS101")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS201")));
        assertTrue(foundCourses.stream().anyMatch(c -> c.getCode().equals("CS301")));
    }

    @Test
    @DisplayName("Should return empty list when no courses meet minimum credits")
    void testFindByCreditsGreaterThanEqualNotFound() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When
        List<Course> foundCourses = courseRepository.findByCreditsGreaterThanEqual(5);

        // Then
        assertTrue(foundCourses.isEmpty());
    }

    @Test
    @DisplayName("Should return true when course exists by code")
    void testExistsByCode() {
        // Given
        courseRepository.save(testCourse1);

        // When
        boolean exists = courseRepository.existsByCode("CS101");

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when course does not exist by code")
    void testExistsByCodeNotFound() {
        // Given
        courseRepository.save(testCourse1);

        // When
        boolean exists = courseRepository.existsByCode("NONEXISTENT");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should return false when checking existence of null code")
    void testExistsByCodeNull() {
        // Given
        courseRepository.save(testCourse1);

        // When
        boolean exists = courseRepository.existsByCode(null);

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Should update existing course")
    void testUpdateCourse() {
        // Given
        Course savedCourse = courseRepository.save(testCourse1);
        savedCourse.setName("Updated Programming Course");
        savedCourse.setCredits(4);

        // When
        Course updatedCourse = courseRepository.save(savedCourse);

        // Then
        assertEquals("Updated Programming Course", updatedCourse.getName());
        assertEquals(4, updatedCourse.getCredits());
        assertEquals("CS101", updatedCourse.getCode());

        // Verify the course was actually updated in the database
        Optional<Course> foundCourse = courseRepository.findById(savedCourse.getId());
        assertTrue(foundCourse.isPresent());
        assertEquals("Updated Programming Course", foundCourse.get().getName());
        assertEquals(4, foundCourse.get().getCredits());
    }

    @Test
    @DisplayName("Should delete course by ID")
    void testDeleteCourse() {
        // Given
        Course savedCourse = courseRepository.save(testCourse1);
        Long courseId = savedCourse.getId();

        // When
        courseRepository.deleteById(courseId);

        // Then
        Optional<Course> foundCourse = courseRepository.findById(courseId);
        assertFalse(foundCourse.isPresent());
    }

    @Test
    @DisplayName("Should delete all courses")
    void testDeleteAll() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        courseRepository.deleteAll();

        // Then
        List<Course> allCourses = courseRepository.findAll();
        assertTrue(allCourses.isEmpty());
    }

    @Test
    @DisplayName("Should count total number of courses")
    void testCountCourses() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When
        long count = courseRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should reject duplicate course codes due to unique constraint")
    void testDuplicateCourseCodes() {
        // Given
        courseRepository.save(testCourse1);

        Course duplicateCode = new Course();
        duplicateCode.setName("Different Course");
        duplicateCode.setCode("CS101"); // Same code as testCourse1
        duplicateCode.setCredits(2);

        // When & Then - Should throw exception due to unique constraint on code
        assertThrows(Exception.class, () -> {
            courseRepository.save(duplicateCode);
            courseRepository.flush();
        });
    }

    @Test
    @DisplayName("Should reject course with null values due to validation")
    void testCourseWithNullValues() {
        // Given
        Course courseWithNulls = new Course();
        courseWithNulls.setName(null);
        courseWithNulls.setCode(null);
        courseWithNulls.setCredits(null);

        // When & Then - Should throw validation exception
        assertThrows(Exception.class, () -> {
            courseRepository.save(courseWithNulls);
            courseRepository.flush();
        });
    }

    @Test
    @DisplayName("Should perform complex query combining multiple criteria")
    void testComplexQueryScenario() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When - Find all 3+ credit courses that contain "programming" in name
        List<Course> programmingCourses = courseRepository.findByNameContainingIgnoreCase("programming");
        List<Course> highCreditCourses = courseRepository.findByCreditsGreaterThanEqual(3);

        // Find intersection (courses that are both programming and 3+ credits)
        List<Course> complexResult = programmingCourses.stream()
                .filter(highCreditCourses::contains)
                .toList();

        // Then
        assertEquals(2, complexResult.size()); // CS101 and CS301
        assertTrue(complexResult.stream().anyMatch(c -> c.getCode().equals("CS101")));
        assertTrue(complexResult.stream().anyMatch(c -> c.getCode().equals("CS301")));
    }

    @Test
    @DisplayName("Should find all courses in database")
    void testFindAll() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When
        List<Course> allCourses = courseRepository.findAll();

        // Then
        assertEquals(3, allCourses.size());
        assertTrue(allCourses.stream().anyMatch(c -> c.getCode().equals("CS101")));
        assertTrue(allCourses.stream().anyMatch(c -> c.getCode().equals("CS201")));
        assertTrue(allCourses.stream().anyMatch(c -> c.getCode().equals("CS301")));
    }

    @Test
    @DisplayName("Should return empty list when finding all courses in empty database")
    void testFindAllEmpty() {
        // When
        List<Course> allCourses = courseRepository.findAll();

        // Then
        assertTrue(allCourses.isEmpty());
    }

    @Test
    @DisplayName("Should reject course with empty string values due to validation")
    void testCourseWithEmptyStrings() {
        // Given
        Course courseWithEmptyStrings = new Course();
        courseWithEmptyStrings.setName("");
        courseWithEmptyStrings.setCode("");
        courseWithEmptyStrings.setCredits(1);

        // When & Then - Should throw validation exception
        assertThrows(Exception.class, () -> {
            courseRepository.save(courseWithEmptyStrings);
            courseRepository.flush();
        });
    }

    @Test
    @DisplayName("Should reject very long course name due to validation")
    void testVeryLongCourseName() {
        // Given
        String longName = "A".repeat(500); // Very long name (exceeds max 100)
        Course courseWithLongName = new Course();
        courseWithLongName.setName(longName);
        courseWithLongName.setCode("CS999");
        courseWithLongName.setCredits(3);

        // When & Then - Should throw validation exception
        assertThrows(Exception.class, () -> {
            courseRepository.save(courseWithLongName);
            courseRepository.flush();
        });
    }

    @Test
    @DisplayName("Should handle special characters in course name")
    void testSpecialCharactersInCourseName() {
        // Given
        Course courseWithSpecialChars = new Course();
        courseWithSpecialChars.setName("Programming & Web Development: HTML/CSS + JavaScript!");
        courseWithSpecialChars.setCode("CS-102");
        courseWithSpecialChars.setCredits(4);

        // When
        Course savedCourse = courseRepository.save(courseWithSpecialChars);

        // Then
        assertNotNull(savedCourse);
        assertEquals("Programming & Web Development: HTML/CSS + JavaScript!", savedCourse.getName());
        assertEquals("CS-102", savedCourse.getCode());

        // Verify it can be found
        Optional<Course> foundCourse = courseRepository.findByCode("CS-102");
        assertTrue(foundCourse.isPresent());
        assertEquals("Programming & Web Development: HTML/CSS + JavaScript!", foundCourse.get().getName());
    }

    @Test
    @DisplayName("Should reject negative credit values due to validation")
    void testNegativeCredits() {
        // Given
        Course courseWithNegativeCredits = new Course();
        courseWithNegativeCredits.setName("Invalid Course");
        courseWithNegativeCredits.setCode("CS000");
        courseWithNegativeCredits.setCredits(-5);

        // When & Then - Should throw validation exception
        assertThrows(Exception.class, () -> {
            courseRepository.save(courseWithNegativeCredits);
            courseRepository.flush();
        });
    }

    @Test
    @DisplayName("Should handle very large credit values")
    void testVeryLargeCredits() {
        // Given
        Course courseWithLargeCredits = new Course();
        courseWithLargeCredits.setName("Intensive Course");
        courseWithLargeCredits.setCode("CS500");
        courseWithLargeCredits.setCredits(Integer.MAX_VALUE);

        // When
        Course savedCourse = courseRepository.save(courseWithLargeCredits);

        // Then
        assertNotNull(savedCourse);
        assertEquals(Integer.MAX_VALUE, savedCourse.getCredits());
    }

    @Test
    @DisplayName("Should return correct count when database is empty")
    void testCountWhenEmpty() {
        // When
        long count = courseRepository.count();

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should find courses by name containing with partial match")
    void testFindByNameContainingPartialMatch() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When - Search for just "to" which appears in "Introduction"
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("to");

        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS101", foundCourses.get(0).getCode());
    }

    @Test
    @DisplayName("Should find courses by name containing with single character")
    void testFindByNameContainingSingleCharacter() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);
        courseRepository.save(testCourse3);

        // When - Search for single letter that appears in multiple courses
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("a");

        // Then
        assertTrue(foundCourses.size() > 0);
        assertTrue(foundCourses.stream().anyMatch(c -> c.getName().toLowerCase().contains("a")));
    }

    @Test
    @DisplayName("Should handle whitespace in search queries")
    void testFindByNameContainingWithWhitespace() {
        // Given
        courseRepository.save(testCourse1);
        courseRepository.save(testCourse2);

        // When - Search with whitespace
        List<Course> foundCourses = courseRepository.findByNameContainingIgnoreCase("Data Structures");

        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS201", foundCourses.get(0).getCode());
    }

    @Test
    @DisplayName("Should maintain data integrity after multiple operations")
    void testDataIntegrityAfterMultipleOperations() {
        // Given
        Course course = courseRepository.save(testCourse1);
        Long originalId = course.getId();

        // When - Perform multiple updates
        course.setName("First Update");
        courseRepository.save(course);

        course.setName("Second Update");
        courseRepository.save(course);

        course.setCredits(5);
        courseRepository.save(course);

        // Then - Verify the course still has the same ID and latest updates
        Optional<Course> foundCourse = courseRepository.findById(originalId);
        assertTrue(foundCourse.isPresent());
        assertEquals(originalId, foundCourse.get().getId());
        assertEquals("Second Update", foundCourse.get().getName());
        assertEquals(5, foundCourse.get().getCredits());
        assertEquals("CS101", foundCourse.get().getCode());
    }

    @Test
    @DisplayName("Should handle batch save of multiple courses")
    void testBatchSave() {
        // Given
        List<Course> coursesToSave = List.of(testCourse1, testCourse2, testCourse3);

        // When
        List<Course> savedCourses = courseRepository.saveAll(coursesToSave);

        // Then
        assertEquals(3, savedCourses.size());
        assertEquals(3, courseRepository.count());

        // Verify all courses have IDs
        assertTrue(savedCourses.stream().allMatch(c -> c.getId() != null));
    }

    @Test
    @DisplayName("Should find courses by credits with value of 1")
    void testFindByCreditsOne() {
        // Given
        Course oneCreditCourse = new Course();
        oneCreditCourse.setName("One Credit Course");
        oneCreditCourse.setCode("CS000");
        oneCreditCourse.setCredits(1);
        courseRepository.save(oneCreditCourse);
        courseRepository.save(testCourse1);

        // When
        List<Course> foundCourses = courseRepository.findByCredits(1);

        // Then
        assertEquals(1, foundCourses.size());
        assertEquals("CS000", foundCourses.get(0).getCode());
    }

    @Test
    @DisplayName("Should verify course exists after save")
    void testCourseExistsAfterSave() {
        // Given
        Course savedCourse = courseRepository.save(testCourse1);

        // When
        boolean existsById = courseRepository.existsById(savedCourse.getId());
        boolean existsByCode = courseRepository.existsByCode(savedCourse.getCode());

        // Then
        assertTrue(existsById);
        assertTrue(existsByCode);
    }

    @Test
    @DisplayName("Should verify course does not exist after delete")
    void testCourseDoesNotExistAfterDelete() {
        // Given
        Course savedCourse = courseRepository.save(testCourse1);
        Long courseId = savedCourse.getId();
        String courseCode = savedCourse.getCode();

        // When
        courseRepository.deleteById(courseId);

        // Then
        assertFalse(courseRepository.existsById(courseId));
        assertFalse(courseRepository.existsByCode(courseCode));
    }

    @Test
    @DisplayName("Should handle course name at maximum length")
    void testCourseNameAtMaxLength() {
        // Given - Create a course with name exactly at max length (100 chars)
        String maxLengthName = "A".repeat(100);
        Course course = new Course();
        course.setName(maxLengthName);
        course.setCode("CS998");
        course.setCredits(3);

        // When
        Course savedCourse = courseRepository.save(course);

        // Then
        assertNotNull(savedCourse);
        assertEquals(maxLengthName, savedCourse.getName());
        assertEquals(100, savedCourse.getName().length());
    }

    @Test
    @DisplayName("Should handle course code at minimum length")
    void testCourseCodeAtMinLength() {
        // Given - Create a course with code exactly at min length (2 chars)
        Course course = new Course();
        course.setName("Short Code Course");
        course.setCode("AB");
        course.setCredits(2);

        // When
        Course savedCourse = courseRepository.save(course);

        // Then
        assertNotNull(savedCourse);
        assertEquals("AB", savedCourse.getCode());
        assertEquals(2, savedCourse.getCode().length());
    }

    @Test
    @DisplayName("Should handle course with minimum credits")
    void testCourseWithMinimumCredits() {
        // Given
        Course course = new Course();
        course.setName("Minimum Credit Course");
        course.setCode("CS001");
        course.setCredits(1);

        // When
        Course savedCourse = courseRepository.save(course);

        // Then
        assertNotNull(savedCourse);
        assertEquals(1, savedCourse.getCredits());
    }

    @Test
    @DisplayName("Should handle concurrent saves of different courses")
    void testConcurrentSaves() {
        // Given
        Course course1 = new Course();
        course1.setName("Concurrent Course 1");
        course1.setCode("CONC01");
        course1.setCredits(3);

        Course course2 = new Course();
        course2.setName("Concurrent Course 2");
        course2.setCode("CONC02");
        course2.setCredits(4);

        // When
        Course saved1 = courseRepository.save(course1);
        Course saved2 = courseRepository.save(course2);

        // Then
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
        assertEquals(2, courseRepository.count());
    }

    @Test
    @DisplayName("Should find course by partial code match is case sensitive")
    void testFindByCodeCaseSensitive() {
        // Given
        courseRepository.save(testCourse1);

        // When - Try to find with lowercase code
        Optional<Course> foundCourse = courseRepository.findByCode("cs101");

        // Then - Should not find since code is case sensitive
        assertFalse(foundCourse.isPresent());
    }

    @Test
    @DisplayName("Should handle updating course multiple times in same transaction")
    void testMultipleUpdatesInTransaction() {
        // Given
        Course course = courseRepository.save(testCourse1);

        // When - Update multiple fields
        course.setName("Updated Name");
        courseRepository.save(course);

        course.setCredits(5);
        courseRepository.save(course);

        course.setName("Final Name");
        Course finalCourse = courseRepository.save(course);

        // Then
        assertEquals("Final Name", finalCourse.getName());
        assertEquals(5, finalCourse.getCredits());
        assertEquals("CS101", finalCourse.getCode());
    }
}
