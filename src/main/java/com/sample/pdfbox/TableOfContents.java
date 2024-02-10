package com.sample.pdfbox;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;

public class TableOfContents {
	public static final int FONTSIZE = 12;
	public static final float POINTS_PER_INCH = 72;
	public static final PDRectangle LANDSCAPE_LETTER = new PDRectangle(11f * POINTS_PER_INCH, 8.5f * POINTS_PER_INCH);
	public static final PDFont TABLE_HEADER_FONT = PDType1Font.HELVETICA_BOLD;
	public static final PDFont TABLE_CELL_FONT = PDType1Font.HELVETICA;
	public static final List<String> pageNames = Arrays.asList("Page 1", "Page 2", "Page 3");

	public static void headerText(PDDocument document, PDPage page, String text, PDFont fontMedium,
			PDFont fontRegular) throws IOException {

		float margin = 36;
		int FONTSIZE = 14;
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		contentStream.setStrokingColor(Color.BLACK);

		contentStream.beginText();
		contentStream.setFont(fontMedium, FONTSIZE);
		contentStream.newLineAtOffset(margin, page.getMediaBox().getHeight() - 45);
		contentStream.showText(text);
		contentStream.endText();
		contentStream.close();

	}

	public static void tableOfContentsTable(PDDocument document, PDPage page, PDFont fontMedium, PDFont fontRegular,
			Map<String, PDPage> pageRef) throws IOException {

		List<PDAnnotation> annotations = page.getAnnotations();
		headerText(document, page, "TABLE OF CONTENTS", fontMedium, fontRegular);
		float margin = 36; // X axis start position
		float topMargin = 21;
		float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * topMargin);
		float yStart = yStartNewPage - 30;
		float bottomMargin = topMargin;

		System.out.println("Left Margin " + margin + "\tTop Margin " + topMargin);
		System.out.println("Table width " + tableWidth + "\ttableHeight " + yStartNewPage);

		BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, document, page, false,
				true);
		Row<PDPage> row = null;
		Cell<PDPage> cell = null;
		float rowHeight = 0f;
		float yPositionForNewRow = yStart;
		PDPage jumpPage = null;
		int pageNumber;
		PDRectangle annotationPositionRectangle = null;

		for (String pageName : pageNames) {
			jumpPage = (PDPage) pageRef.get(pageName);
			pageNumber = getPageNumber(document, jumpPage);
			row = table.createRow(0f);
			cell = row.createCell(75, pageName, HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
			cell.setFont(fontRegular);
			cell.setFontSize(FONTSIZE);
			cell = row.createCell(25, String.valueOf(pageNumber), HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE);
			cell.setFont(fontRegular);
			cell.setFontSize(FONTSIZE);
			rowHeight = row.getHeight();
			yPositionForNewRow -= rowHeight;

			annotationPositionRectangle = getPositionRectangle(margin, yPositionForNewRow, tableWidth + margin,
					yPositionForNewRow + rowHeight);
			annotations.add(generateAnnotation(annotationPositionRectangle, jumpPage));

		}

		table.draw();
		annotations.forEach(ann -> ann.constructAppearances(document));
	}

	public static PDRectangle getPositionRectangle(float lowerLeftX, float lowerLeftY, float upperRightX,
			float upperRightY) {
		PDRectangle position = new PDRectangle();
		position.setLowerLeftX(lowerLeftX);
		position.setLowerLeftY(lowerLeftY);
		position.setUpperRightX(upperRightX);
		position.setUpperRightY(upperRightY);
		return position;
	}

	public static int getPageNumber(PDDocument document, PDPage page) {
		PDPageTree allPages = document.getPages();
		int totalNumberOfPages = allPages.getCount();
		PDPage iterabelPage;
		for (int i = 0; i < totalNumberOfPages; i++) {
			iterabelPage = allPages.get(i);
			if (iterabelPage.equals(page))
				return i + 1;
		}
		return -1;
	}

	public static PDAnnotationLink generateAnnotation(PDRectangle position, PDPage page) {
		PDAnnotationLink pageLink = new PDAnnotationLink();

		PDBorderStyleDictionary borderULine = new PDBorderStyleDictionary();
		borderULine.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);
		borderULine.setWidth(0); // remove to show underline
		pageLink.setBorderStyle(borderULine);

		PDActionGoTo actionGoto = new PDActionGoTo();
		PDPageDestination dest = new PDPageXYZDestination();
		dest.setPage(page);
		actionGoto.setDestination(dest);
		pageLink.setAction(actionGoto);
		pageLink.setRectangle(position);

		return pageLink;
	}

	public static void main(String[] args) throws IOException {

		Map<String, PDPage> pageRef = new HashMap<String, PDPage>();

		PDDocument document = new PDDocument();

		PDPage page = new PDPage(LANDSCAPE_LETTER);
		document.addPage(page);
		pageRef.put("toc", page);

		PDPage page1 = new PDPage(LANDSCAPE_LETTER);
		document.addPage(page1);
		String pageName = pageNames.get(0);
		headerText(document, page1,pageName, TABLE_HEADER_FONT, TABLE_CELL_FONT);
		pageRef.put(pageName, page1);

		PDPage page2 = new PDPage(LANDSCAPE_LETTER);
		document.addPage(page2);
		pageName = pageNames.get(1);
		headerText(document, page2,pageName, TABLE_HEADER_FONT, TABLE_CELL_FONT);
		pageRef.put(pageName, page2);

		PDPage page3 = new PDPage(LANDSCAPE_LETTER);
		document.addPage(page3);
		pageName = pageNames.get(2);
		headerText(document, page3,pageName, TABLE_HEADER_FONT, TABLE_CELL_FONT);
		pageRef.put(pageName, page3);

		int pageWidth = (int) page.getTrimBox().getWidth(); // get width of the page
		int pageHeight = (int) page.getTrimBox().getHeight(); // get height of the page
		System.out.println("Width:::" + pageWidth + "\t Height:::" + pageHeight);

		tableOfContentsTable(document, page, TABLE_HEADER_FONT, TABLE_CELL_FONT, pageRef);

		document.save("tableOfContentsReport" + Instant.now().getEpochSecond() + ".pdf");
		document.close();
		System.out.println("tableOfContentsReport Page pdf created");
	}
}
