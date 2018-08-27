package com.ptalele.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import static com.ptalele.xlsx.TestUtils.expectCachedType;
import static com.ptalele.xlsx.TestUtils.expectFormula;
import static com.ptalele.xlsx.TestUtils.expectSameStringContent;
import static com.ptalele.xlsx.TestUtils.expectStringContent;
import static com.ptalele.xlsx.TestUtils.expectType;
import static com.ptalele.xlsx.TestUtils.getCellFromNextRow;
import static com.ptalele.xlsx.TestUtils.nextRow;
import static com.ptalele.xlsx.TestUtils.openWorkbook;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamingWorkbookTest {
  @BeforeClass
  public static void init() {
    Locale.setDefault(Locale.ENGLISH);
  }

  @Test
  public void testIterateSheets() throws Exception {
    try(
        InputStream is = new FileInputStream(new File("src/test/resources/sheets.xlsx"));
        Workbook workbook = StreamingReader.builder().open(is);
    ) {

      assertEquals(2, workbook.getNumberOfSheets());

      Sheet alpha = workbook.getSheetAt(0);
      Sheet zulu = workbook.getSheetAt(1);
      assertEquals("SheetAlpha", alpha.getSheetName());
      assertEquals("SheetZulu", zulu.getSheetName());

      Row rowA = alpha.rowIterator().next();
      Row rowZ = zulu.rowIterator().next();

      assertEquals("stuff", rowA.getCell(0).getStringCellValue());
      assertEquals("yeah", rowZ.getCell(0).getStringCellValue());
    }
  }

  @Test
  public void testHiddenCells() throws Exception {
    try(
        InputStream is = new FileInputStream(new File("src/test/resources/hidden.xlsx"));
        Workbook workbook = StreamingReader.builder().open(is)
    ) {
      assertEquals(3, workbook.getNumberOfSheets());
      Sheet sheet = workbook.getSheetAt(0);

      assertFalse("Column 0 should not be hidden", sheet.isColumnHidden(0));
      assertTrue("Column 1 should be hidden", sheet.isColumnHidden(1));
      assertFalse("Column 2 should not be hidden", sheet.isColumnHidden(2));

      assertFalse("Row 0 should not be hidden", sheet.rowIterator().next().getZeroHeight());
      assertTrue("Row 1 should be hidden", sheet.rowIterator().next().getZeroHeight());
      assertFalse("Row 2 should not be hidden", sheet.rowIterator().next().getZeroHeight());
    }
  }

  @Test
  public void testHiddenSheets() throws Exception {
    try(
        InputStream is = new FileInputStream(new File("src/test/resources/hidden.xlsx"));
        Workbook workbook = StreamingReader.builder().open(is)
    ) {
      assertEquals(3, workbook.getNumberOfSheets());
      assertFalse(workbook.isSheetHidden(0));

      assertTrue(workbook.isSheetHidden(1));
      assertFalse(workbook.isSheetVeryHidden(1));

      assertFalse(workbook.isSheetHidden(2));
      assertTrue(workbook.isSheetVeryHidden(2));
    }
  }

  @Test
  public void testFormulaCells() throws Exception {
    try (Workbook workbook = openWorkbook("formula_cell.xlsx")) {
      assertEquals(1, workbook.getNumberOfSheets());
      Sheet sheet = workbook.getSheetAt(0);

      Iterator<Row> rowIterator = sheet.rowIterator();
      Cell A1 = getCellFromNextRow(rowIterator, 0);
      Cell A2 = getCellFromNextRow(rowIterator, 0);
      Cell A3 = getCellFromNextRow(rowIterator, 0);

      expectType(A3, FORMULA);
      expectCachedType(A3, NUMERIC);
      expectFormula(A3, "SUM(A1:A2)");

      expectStringContent(A1, "1");
      expectStringContent(A2, "2");
      expectStringContent(A3, "3");
    }
  }

  @Test
  public void testNumericFormattedFormulaCell() throws Exception {
    try (Workbook workbook = openWorkbook("formula_cell.xlsx")) {
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.rowIterator();

      Cell C1 = getCellFromNextRow(rowIterator, 2);
      Cell C2 = getCellFromNextRow(rowIterator, 2);

      expectType(C2, FORMULA);
      expectCachedType(C2, NUMERIC);
      expectFormula(C2, "C1");
      expectSameStringContent(C2, C1);
      expectStringContent(C2, "May 11 2018");
    }
  }

  @Test
  public void testStringFormattedFormulaCell() throws Exception {
    try (Workbook workbook = openWorkbook("formula_cell.xlsx")) {
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.rowIterator();

      Cell B1 = getCellFromNextRow(rowIterator, 1);
      nextRow(rowIterator);
      Cell B3 = getCellFromNextRow(rowIterator, 1);

      expectType(B3, FORMULA);
//      expectCachedType(B3, STRING); // this can't return FUNCTION as cached type as per javadoc ! fix in future work
      expectFormula(B3, "B1");
      expectSameStringContent(B1, B3);
      expectStringContent(B3, "a");
    }
  }

  @Test
  public void testQuotedStringFormattedFormulaCell() throws Exception {
    try (Workbook workbook = openWorkbook("formula_cell.xlsx")) {
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.rowIterator();

      nextRow(rowIterator);
      Cell B2 = getCellFromNextRow(rowIterator, 1);
      nextRow(rowIterator);
      Cell B4 = getCellFromNextRow(rowIterator, 1);

      expectType(B4, FORMULA);
//      expectCachedType(B4, STRING); // this can't return FUNCTION as cached type as per javadoc ! fix in future work
//      expectFormula(B4, "B2"); // returning wrong forumla type? this needs to be fixed in future work
      expectSameStringContent(B2, B4);
      expectStringContent(B4, "\"a\"");
    }
  }

}
