package api.utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

/**
 * ExcelUtility
 * -------------
 * Utility class for performing common Excel operations using Apache POI.
 * Supported methods:
 * - getRowCount(String sheetName)
 * - getCellCount(String sheetName, int rowNum)
 * - getCellData(String sheetName, int rowNum, int colNum)
 * - setCellData(String sheetName, int rowNum, int colNum, String data)
 * - createRowIfNotExists(String sheetName, int rowNum)
 *
 * Author: PK
 * Version: 1.0
 */
public class ExcelUtility {

    private String filePath;
    private FileInputStream fis;
    private FileOutputStream fos;
    private Workbook workbook;
    private Sheet sheet;
    private Row row;
    private Cell cell;

    // Constructor
    public ExcelUtility(String filePath) {
        this.filePath = filePath;
    }

    // 🔹 Get Row Count
    public int getRowCount(String sheetName) throws IOException {
        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(sheetName);
        int rowCount = sheet.getLastRowNum();
        workbook.close();
        fis.close();
        return rowCount;
    }

    // 🔹 Get Cell Count in a Row
    public int getCellCount(String sheetName, int rowNum) throws IOException {
        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(sheetName);
        row = sheet.getRow(rowNum);
        int cellCount = row.getLastCellNum();
        workbook.close();
        fis.close();
        return cellCount;
    }

    // 🔹 Get Cell Data
    public String getCellData(String sheetName, int rowNum, int colNum) throws IOException {
        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(sheetName);
        row = sheet.getRow(rowNum);
        String data;
        try {
            cell = row.getCell(colNum);
            DataFormatter formatter = new DataFormatter();
            data = formatter.formatCellValue(cell); // Handles all cell types safely
        } catch (Exception e) {
            data = "";
        }
        workbook.close();
        fis.close();
        return data;
    }

    // 🔹 Set Cell Data
    public void setCellData(String sheetName, int rowNum, int colNum, String data) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            workbook = new XSSFWorkbook();
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
        }

        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);

        if (workbook.getSheetIndex(sheetName) == -1)
            workbook.createSheet(sheetName);

        sheet = workbook.getSheet(sheetName);

        if (sheet.getRow(rowNum) == null)
            sheet.createRow(rowNum);

        row = sheet.getRow(rowNum);

        cell = row.createCell(colNum);
        cell.setCellValue(data);

        fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();
        fis.close();
        fos.close();
    }

    // 🔹 (Optional) Create row if not exists (for dynamic writing)
    public void createRowIfNotExists(String sheetName, int rowNum) throws IOException {
        fis = new FileInputStream(filePath);
        workbook = new XSSFWorkbook(fis);
        sheet = workbook.getSheet(sheetName);
        if (sheet.getRow(rowNum) == null) {
            sheet.createRow(rowNum);
        }
        fos = new FileOutputStream(filePath);
        workbook.write(fos);
        workbook.close();
        fis.close();
        fos.close();
    }
}
