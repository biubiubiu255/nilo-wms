package com.nilo.wms.common.util;

import com.nilo.wms.common.annotation.Excel;
import org.apache.poi.hssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class ExportExcel<T> {

    private HSSFWorkbook wb;

    private HSSFSheet sheet;

    /**
     */
    public ExportExcel(HSSFWorkbook wb) {
        this.wb = wb;
        sheet = this.wb.createSheet();
    }

    class KeyComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer str1, Integer str2) {
            return str1.compareTo(str2);
        }
    }

    class ExcelCellBean {
        String name;
        String field;
        boolean subType;
        String subFieldName;
        Class subClass;
    }

    private Map<Integer, ExcelCellBean> buildSortMap(Class t, Map<Integer, ExcelCellBean> sortMap, boolean subType, String subName) {

        Field[] fields = t.getDeclaredFields();
        for (Field field : fields) {
            Excel excel = field.getAnnotation(Excel.class);
            if (excel != null) {
                if (!excel.subType()) {
                    ExcelCellBean b = new ExcelCellBean();
                    b.field = field.getName();
                    b.name = excel.name();
                    if (subType) {
                        b.subType = true;
                        b.subFieldName = subName;
                        b.subClass = t;
                    }
                    sortMap.put(excel.order(), b);
                } else {
                    Class fieldClass = field.getType();
                    sortMap = buildSortMap(fieldClass, sortMap, true, field.getName());
                }
            }
        }
        return sortMap;
    }


    public int fillData(List<T> list, Class<T> t) {
        Map<Integer, ExcelCellBean> sortMap = new TreeMap<Integer, ExcelCellBean>(
                new KeyComparator());
        //根据注解构建需要导出的字段
        sortMap = buildSortMap(t, sortMap, false, null);

        List<String> header = new ArrayList<>();
        for (Map.Entry<Integer, ExcelCellBean> entry : sortMap.entrySet()) {
            ExcelCellBean c = entry.getValue();
            header.add(c.name);
        }

        List<Map<String, String>> listMap = new ArrayList<>();

        for (T data : list) {
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<Integer, ExcelCellBean> entry : sortMap.entrySet()) {
                ExcelCellBean c = entry.getValue();
                if (!c.subType) {
                    String value = BeanUtils.getProperty(data, c.field);
                    map.put(c.name, value);
                } else {
                    String value = BeanUtils.getProperty(BeanUtils.readField(data, c.subFieldName, c.subClass), c.field);
                    map.put(c.name, value);
                }
            }
            listMap.add(map);
        }

        int rowNum = 0;

        HSSFFont poDateFont = wb.createFont();
        poDateFont.setFontHeightInPoints((short) 10);

        //文字靠左样式
        HSSFCellStyle leftStyle = wb.createCellStyle();
        leftStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        leftStyle.setFont(poDateFont);
        leftStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        leftStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        leftStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        leftStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        //header
        HSSFRow headerRow = sheet.createRow(rowNum);
        int hCellNum = 0;
        for (String headerCol : header) {
            HSSFCell cell = headerRow.createCell(hCellNum);
            cell.setCellValue(headerCol);
            cell.setCellStyle(leftStyle);
            hCellNum++;
        }

        for (Map<String, String> map : listMap) {
            rowNum++;
            HSSFRow proRow = sheet.createRow(rowNum);
            int cellNum = 0;
            for (String headerCol : header) {
                HSSFCell cell = proRow.createCell(cellNum);
                cell.setCellValue(map.get(headerCol));
                cell.setCellStyle(leftStyle);
                cellNum++;
            }
        }

        sheet.setRowBreak(rowNum);
        return rowNum;
    }

    public void export(String path, String fileName) {
        FileOutputStream fileOut = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            fileOut = new FileOutputStream(new File(path + fileName));
            wb.write(fileOut);//把Workbook对象输出到文件workbook.xls中
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public void exportWrite(OutputStream outputStream) {
        try {
            wb.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}