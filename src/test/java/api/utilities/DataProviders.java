package api.utilities;

import org.testng.annotations.DataProvider;

public class DataProviders {

    String filePath = System.getProperty("user.dir") + "/testdata/UserData.xlsx";
    String sheetName = "Users";

    @DataProvider(name = "AllData")
    public Object[][] getAllData() throws Exception {
        ExcelUtility excel = new ExcelUtility(filePath);
        int lastRowIndex = excel.getRowCount(sheetName); // last row index (0-based)
        if (lastRowIndex < 1) return new Object[0][0];

        int headerCellCount = excel.getCellCount(sheetName, 0);
        int dataRows = lastRowIndex; // rows with data = lastRowIndex (assuming header at 0)

        Object[][] data = new Object[dataRows][headerCellCount];

        for (int r = 1; r <= lastRowIndex; r++) { // start at 1 to skip header
            for (int c = 0; c < headerCellCount; c++) {
                String cell = excel.getCellData(sheetName, r, c);
                data[r - 1][c] = (cell == null) ? "" : cell.trim();
            }
        }
        return data;
    }

    @DataProvider(name = "UserNames")
    public Object[][] getUserNames() throws Exception {
        ExcelUtility excel = new ExcelUtility(filePath);
        int lastRowIndex = excel.getRowCount(sheetName);
        if (lastRowIndex < 1) return new Object[0][0];

        Object[][] usernames = new Object[lastRowIndex][1];

        for (int r = 1; r <= lastRowIndex; r++) {
            // Username is column index 1 (because column 0 is ID in your sheet)
            String u = excel.getCellData(sheetName, r, 1);
            if (u == null) u = "";
            usernames[r - 1][0] = u.trim();
        }
        return usernames;
    }
}
