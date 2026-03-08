package com.resumechecker;

import com.resumechecker.model.ResumeRequest;
import com.resumechecker.model.ResumeResponse;
import com.resumechecker.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResumeServiceTest {

    @Autowired
    private ResumeService resumeService;

    private static final String SAMPLE_RESUME =
            "John Doe\n" +
            "Software Engineer\n\n" +
            "Summary\n" +
            "Experienced software engineer with 5 years of experience in Java and Python development.\n\n" +
            "Experience\n" +
            "Senior Developer at TechCorp (2020-2024)\n" +
            "- Developed and implemented microservices using Spring Boot, improving system performance by 40%\n" +
            "- Led a team of 5 developers to deliver 3 major projects on time\n" +
            "- Optimized database queries, reducing response time by 30%\n" +
            "- Managed CI/CD pipelines using Docker and Kubernetes\n\n" +
            "Education\n" +
            "Bachelor of Science in Computer Science - State University (2019)\n\n" +
            "Skills\n" +
            "Java, Python, Spring, React, Docker, Kubernetes, AWS, SQL, Git, Agile, REST APIs\n\n" +
            "Projects\n" +
            "E-commerce Platform: Built a scalable e-commerce platform serving 10000+ users\n" +
            "Automated 50+ test cases reducing QA time by 25%";

    @Test
    void testAnalyzeResumeWithNoJobDescription() {
        ResumeRequest request = new ResumeRequest(SAMPLE_RESUME, "");
        ResumeResponse response = resumeService.analyzeResume(request);

        assertNotNull(response);
        assertTrue(response.getOverallScore() > 0, "Score should be greater than 0");
        assertTrue(response.getOverallScore() <= 100, "Score should not exceed 100");
        assertNotNull(response.getGrade());
        assertNotNull(response.getCategoryScores());
        assertFalse(response.getStrengths().isEmpty(), "Should have at least one strength");
        assertNotNull(response.getSummary());
    }

    @Test
    void testAnalyzeResumeWithJobDescription() {
        String jobDescription = "We are looking for a Java Spring Boot developer with experience in " +
                "microservices, Docker, Kubernetes, AWS and REST APIs. Agile experience required.";

        ResumeRequest request = new ResumeRequest(SAMPLE_RESUME, jobDescription);
        ResumeResponse response = resumeService.analyzeResume(request);

        assertNotNull(response);
        assertTrue(response.getOverallScore() >= 60, "Good resume with matching JD should score >= 60");
        assertNotNull(response.getMatchedKeywords());
        assertFalse(response.getMatchedKeywords().isEmpty(), "Should have matched keywords");
    }

    @Test
    void testEmptyResumeReturnsLowScore() {
        ResumeRequest request = new ResumeRequest("John Doe", "");
        ResumeResponse response = resumeService.analyzeResume(request);

        assertNotNull(response);
        assertTrue(response.getOverallScore() < 50, "Empty resume should score below 50");
        assertFalse(response.getImprovements().isEmpty(), "Should have improvement suggestions");
    }

    @Test
    void testGradeCalculation() {
        ResumeRequest request = new ResumeRequest(SAMPLE_RESUME, "");
        ResumeResponse response = resumeService.analyzeResume(request);

        assertNotNull(response.getGrade());
        assertTrue(response.getGrade().matches("[A-D][+]?"), "Grade should be a valid letter grade");
    }

    @Test
    void testCategoryScoresPresent() {
        ResumeRequest request = new ResumeRequest(SAMPLE_RESUME, "");
        ResumeResponse response = resumeService.analyzeResume(request);

        assertNotNull(response.getCategoryScores());
        assertTrue(response.getCategoryScores().containsKey("Resume Sections"));
        assertTrue(response.getCategoryScores().containsKey("Action Verbs"));
        assertTrue(response.getCategoryScores().containsKey("Quantifiable Achievements"));
        assertTrue(response.getCategoryScores().containsKey("Skills Coverage"));
        assertTrue(response.getCategoryScores().containsKey("Resume Length"));
    }
}
