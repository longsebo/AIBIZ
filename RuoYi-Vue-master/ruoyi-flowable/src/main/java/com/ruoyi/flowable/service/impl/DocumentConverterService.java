package com.ruoyi.flowable.service.impl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Service
public class DocumentConverterService {

    public String convertToHtml(String filePath) throws Exception {
        String fileName = new File(filePath).getName().toLowerCase();
        
        if (fileName.endsWith(".doc")) {
            return convertDocToHtml(filePath);
        } else if (fileName.endsWith(".docx")) {
            return convertDocxToHtml(filePath);
        } else if (fileName.endsWith(".xls")) {
            return convertXlsToHtml(filePath);
        } else if (fileName.endsWith(".xlsx")) {
            return convertXlsxToHtml(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private String convertDocToHtml(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
             HWPFDocument doc = new HWPFDocument(fis)) {
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document document = factory.newDocumentBuilder().newDocument();
            WordToHtmlConverter converter = new WordToHtmlConverter(document);
            
            converter.processDocument(doc);
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private String convertDocxToHtml(String filePath) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        html.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; }");
        html.append("p { margin: 5px 0; line-height: 1.6; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
        html.append("td, th { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("h1 { font-size: 24px; font-weight: bold; margin: 15px 0; }");
        html.append("h2 { font-size: 20px; font-weight: bold; margin: 12px 0; }");
        html.append("h3 { font-size: 18px; font-weight: bold; margin: 10px 0; }");
        html.append("</style></head><body>");

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument doc = new XWPFDocument(fis)) {
            
            for (IBodyElement element : doc.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String text = paragraph.getText();
                    if (text == null || text.trim().isEmpty()) {
                        continue;
                    }
                    
                    String style = getParagraphStyle(paragraph);
                    html.append(style);
                    html.append(escapeHtml(text));
                    html.append("</p>\n");
                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    html.append("<table>\n");
                    for (XWPFTableRow row : table.getRows()) {
                        html.append("<tr>\n");
                        for (XWPFTableCell cell : row.getTableCells()) {
                            html.append("<td>");
                            html.append(escapeHtml(cell.getText()));
                            html.append("</td>");
                        }
                        html.append("</tr>\n");
                    }
                    html.append("</table>\n");
                }
            }
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String getParagraphStyle(XWPFParagraph paragraph) {
        String styleId = paragraph.getStyleID();
        if (styleId != null) {
            if (styleId.contains("Heading1") || styleId.contains("heading1")) {
                return "<h1>";
            } else if (styleId.contains("Heading2") || styleId.contains("heading2")) {
                return "<h2>";
            } else if (styleId.contains("Heading3") || styleId.contains("heading3")) {
                return "<h3>";
            }
        }
        return "<p>";
    }

    private String convertXlsToHtml(String filePath) throws Exception {
        return convertExcelToHtml(filePath, true);
    }

    private String convertXlsxToHtml(String filePath) throws Exception {
        return convertExcelToHtml(filePath, false);
    }

    private String convertExcelToHtml(String filePath, boolean isXls) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>");
        html.append("body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; }");
        html.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
        html.append("td, th { border: 1px solid #ddd; padding: 8px; text-align: left; white-space: pre-wrap; }");
        html.append("th { background-color: #f5f5f5; font-weight: bold; }");
        html.append("</style></head><body>");

        try (FileInputStream fis = new FileInputStream(filePath)) {
            org.apache.poi.ss.usermodel.Workbook workbook = isXls 
                ? new HSSFWorkbook(fis) 
                : new XSSFWorkbook(fis);
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
                html.append("<h3>").append(escapeHtml(sheet.getSheetName())).append("</h3>\n");
                html.append("<table>\n");
                
                int lastRowNum = sheet.getLastRowNum();
                for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    html.append("<tr>\n");
                    int lastCellNum = row.getLastCellNum();
                    for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
                        html.append("<td>");
                        html.append(escapeHtml(getCellValue(cell)));
                        html.append("</td>");
                    }
                    html.append("</tr>\n");
                }
                html.append("</table>\n");
            }
            workbook.close();
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    public boolean isPreviewable(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".doc") || lowerName.endsWith(".docx") ||
               lowerName.endsWith(".pdf") || lowerName.endsWith(".xls") ||
               lowerName.endsWith(".xlsx") || lowerName.endsWith(".jpg") ||
               lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
    }

    public String getPreviewType(String fileName) {
        if (fileName == null) {
            return "download";
        }
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx") ||
            lowerName.endsWith(".xls") || lowerName.endsWith(".xlsx")) {
            return "html";
        } else if (lowerName.endsWith(".pdf")) {
            return "pdf";
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                   lowerName.endsWith(".png")) {
            return "image";
        }
        return "download";
    }
}