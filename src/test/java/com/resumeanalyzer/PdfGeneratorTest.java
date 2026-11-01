package com.resumeanalyzer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PdfGeneratorTest {

    @Test
    void generateSamplePdf() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.setLeading(18f);
                cs.newLineAtOffset(50, 750);
                cs.showText("Alex Morgan");
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("Email: alex.morgan@example.com | Phone: +1-555-0199 | github.com/alexmorgan | linkedin.com/in/alexmorgan");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("PROFESSIONAL SUMMARY");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("Results-driven Software Engineer with 2+ years of experience developing robust Java and Spring Boot applications.");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("EDUCATION");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("Bachelor of Technology in Computer Science, State University, 2024");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("TECHNICAL SKILLS");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("Languages: Java, SQL, Python, JavaScript");
                cs.newLine();
                cs.showText("Frameworks: Spring Boot, Hibernate, JUnit, React");
                cs.newLine();
                cs.showText("Databases & Tools: MySQL, PostgreSQL, Docker, Git, AWS, REST APIs");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("WORK EXPERIENCE");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.showText("Software Engineer Intern - Acme Corp (2023 - 2024)");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("- Engineered scalable RESTful APIs using Spring Boot and MySQL, improving request throughput by 30%.");
                cs.newLine();
                cs.showText("- Optimized relational database queries, reducing query latency from 350ms to 80ms.");
                cs.newLine();
                cs.showText("- Automated deployment pipelines using Docker and Git.");
                cs.newLine();
                cs.newLine();

                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.showText("PROJECTS");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
                cs.showText("E-Commerce Microservices Platform");
                cs.newLine();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.showText("- Developed backend microservices with Spring Boot, Hibernate, and MySQL.");
                cs.newLine();
                cs.showText("- Implemented JWT token authentication and role-based access control.");
                cs.newLine();

                cs.endText();
            }

            File out = new File("sample_resume.pdf");
            doc.save(out);
            assertThat(out.exists()).isTrue();
            assertThat(out.length()).isGreaterThan(0);
            System.out.println("Generated sample_resume.pdf at: " + out.getAbsolutePath());
        }
    }
}
