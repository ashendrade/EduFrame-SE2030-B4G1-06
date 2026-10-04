package com.eduframepackage.eduframe.quiz.service.certificate;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * BUILDER PATTERN.
 *
 * Builds a certificate PDF step by step (student, quiz, score, code,
 * date...) instead of one constructor with a long parameter list. Each
 * setter returns {@code this} so calls can be chained, and {@link #build()}
 * performs the actual PDF assembly only once every required field has
 * been supplied.
 */
public class CertificatePdfBuilder {

    private String studentName;
    private String quizTitle;
    private String certificateCode;
    private int scorePercentage;
    private String issuedDate;

    public CertificatePdfBuilder withStudentName(String studentName) {
        this.studentName = studentName;
        return this;
    }

    public CertificatePdfBuilder withQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
        return this;
    }

    public CertificatePdfBuilder withCertificateCode(String certificateCode) {
        this.certificateCode = certificateCode;
        return this;
    }

    public CertificatePdfBuilder withScorePercentage(int scorePercentage) {
        this.scorePercentage = scorePercentage;
        return this;
    }

    public CertificatePdfBuilder withIssuedDate(java.time.LocalDateTime dateTime) {
        this.issuedDate = dateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        return this;
    }

    public byte[] build() {
        if (studentName == null || quizTitle == null || certificateCode == null) {
            throw new IllegalStateException("Cannot build certificate PDF: missing required fields");
        }
        try {
            Document document = new Document(PageSize.A4.rotate(), 50, 50, 60, 60);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();

            BaseColor navy = new BaseColor(27, 58, 107);
            BaseColor gold = new BaseColor(196, 155, 60);

            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 34, Font.BOLD, navy);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.DARK_GRAY);
            Font nameFont = new Font(Font.FontFamily.TIMES_ROMAN, 26, Font.BOLDITALIC, BaseColor.BLACK);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 13, Font.NORMAL, BaseColor.DARK_GRAY);
            Font quizFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, navy);
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);

            PdfPTable border = new PdfPTable(1);
            border.setWidthPercentage(100);
            PdfPCell cell = new PdfPCell();
            cell.setBorderWidth(3);
            cell.setBorderColor(gold);
            cell.setPadding(30);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph eyebrow = new Paragraph("EduFrame Learning Platform", subtitleFont);
            eyebrow.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(eyebrow);

            Paragraph spacer1 = new Paragraph(" ");
            cell.addElement(spacer1);

            Paragraph title = new Paragraph("Certificate of Completion", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(title);

            Paragraph spacer2 = new Paragraph(" ");
            cell.addElement(spacer2);

            Paragraph presented = new Paragraph("This certifies that", bodyFont);
            presented.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(presented);

            Paragraph name = new Paragraph(studentName, nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingBefore(10);
            name.setSpacingAfter(10);
            cell.addElement(name);

            Paragraph completed = new Paragraph("has successfully completed the assessment", bodyFont);
            completed.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(completed);

            Paragraph quiz = new Paragraph("\"" + quizTitle + "\"", quizFont);
            quiz.setAlignment(Element.ALIGN_CENTER);
            quiz.setSpacingBefore(8);
            quiz.setSpacingAfter(8);
            cell.addElement(quiz);

            Paragraph score = new Paragraph("with a final score of " + scorePercentage + "%", bodyFont);
            score.setAlignment(Element.ALIGN_CENTER);
            score.setSpacingAfter(25);
            cell.addElement(score);

            Paragraph footer = new Paragraph(
                    "Issued on " + issuedDate + "   |   Certificate ID: " + certificateCode, footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(footer);

            border.addCell(cell);
            document.add(border);

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }
}