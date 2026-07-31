package org.jeecg.module.maintenance.util;

import cn.afterturn.easypoi.excel.export.styler.ExcelExportStylerDefaultImpl;
import org.apache.poi.ss.usermodel.*;

public class ExportStyler extends ExcelExportStylerDefaultImpl {

    private static final short FONT_SIZE_TWELVE = 13;
    private static final short FONT_SIZE_ELEVEN = 11;


    public ExportStyler(Workbook workbook) {
        super(workbook);
    }



    /**
     * 基础样式
     *
     * @return
     */
    private CellStyle getBaseCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);   // 下边框
        style.setBorderLeft(BorderStyle.THIN);     // 左边框
        style.setBorderTop(BorderStyle.THIN);      // 上边框
        style.setBorderRight(BorderStyle.THIN);    // 右边框
        style.setAlignment(HorizontalAlignment.CENTER);         // 水平居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);   // 上下居中
        style.setWrapText(true);    // 设置自动换行
        return style;
    }

    /**
     * 字体样式
     *
     * @param size   字体大小
     * @param isBold 是否加粗
     * @return
     */
    private Font getFont(Workbook workbook, short size, boolean isBold) {
        Font font = workbook.createFont();
        font.setFontName("宋体"); // 字体样式
        font.setBold(isBold);    // 是否加粗
        font.setFontHeightInPoints(size);   // 字体大小
        font.setColor(IndexedColors.BLACK.getIndex());
        return font;
    }




    @Override
    public CellStyle getTitleStyle(short color) {

        CellStyle titleStyle = getBaseCellStyle(workbook);
        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFont(getFont(workbook,FONT_SIZE_ELEVEN,true));
        return titleStyle;
    }

    @Override
    public CellStyle getHeaderStyle(short color) {

        CellStyle titleStyle = getBaseCellStyle(workbook);
        titleStyle.setFont(getFont(workbook,FONT_SIZE_TWELVE,true));
//        titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return titleStyle;
    }

}
