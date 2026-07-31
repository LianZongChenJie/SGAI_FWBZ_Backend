package org.jeecg.module.maintenance.util;


import com.alibaba.druid.util.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.RegionUtil;
import org.jeecg.module.maintenance.entity.PlanModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author ppliu
 * created in 2021/8/17 8:52
 */
@SuppressWarnings("deprecation")
public class ExcelUtil {

    /**
     * @param inputStream 输入流，导入信息转化微模板信息，涉及字段转换
     * @param labelType
     * @return
     */
    public static List<PlanModel> readExcelWitchOutCheck(InputStream inputStream, String labelType) {

        List<PlanModel> planModelList = new ArrayList<>();
        try {
            Workbook workbook = new HSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            String header = sheet.getRow(0).getCell(0).getStringCellValue();
            int year = Integer.parseInt(header.substring(0, 4));
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                    }
                }
                int colNum = HeaderUtil.getWeekOfMonths(year).size() + 11;
                PlanModel planModel = new PlanModel();
                planModel.setIndexNum(convertInt(getStringCellValue(row.getCell(0))));
                planModel.setName(getStringCellValue(row.getCell(1)));
                planModel.setCount(convertInt(getStringCellValue(row.getCell(2))));
                planModel.setFactory(getStringCellValue(row.getCell(3)));
                planModel.setAssociatedDevice(convertYes(getStringCellValue(row.getCell(4))));
                planModel.setCycle(getStringCellValue(row.getCell(5)));
                planModel.setUnit(getStringCellValue(row.getCell(6)));
                planModel.setFrequency(getStringCellValue(row.getCell(7)));
                planModel.setDuration(convertInt(getStringCellValue(row.getCell(8))));
                planModel.setDepartment(getStringCellValue(row.getCell(9)));
                planModel.setWeibaoType(getStringCellValue(row.getCell(10)));
                planModel.setYear(year);
                planModel.setLabelType(labelType);
                for(int k = 1 ;k<53 ;k++){
                    Reflections.setFieldValue(planModel,"w"+k,row.getCell((10+k)) == null ? null : row.getCell((10+k)).getStringCellValue());
                }
                if (colNum - 1 == 63) {
                    planModel.setW53(row.getCell(63) == null ? null : row.getCell(63).getStringCellValue());
                }
                planModelList.add(planModel);
            }
        }catch (Exception e){

        }
        return planModelList;
    }

    private static String getStringCellValue(Cell cell) {
        return cell == null ? null : cell.getStringCellValue();
    }

    private static Boolean convertYes(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        return "是".equals(value.trim());
    }

    private static Integer convertInt(String value) {
        try {

            if (StringUtils.isEmpty(value)) {
                return 0;
            }
            return Integer.valueOf(value);
        }catch (Exception e){
            return 0;
        }
    }

    /**
     * 设置合并单元格的边框
     * @param workbook excel实例
     * @param border 边框类型
     */
    public static void setMergedBorder(Workbook workbook,BorderStyle border){

        Sheet sheet = workbook.getSheetAt(0);

        sheet.getMergedRegions().forEach(k ->{
            RegionUtil.setBorderBottom(border,k,sheet);
            RegionUtil.setBorderTop(border,k,sheet);
            RegionUtil.setBorderLeft(border,k,sheet);
            RegionUtil.setBorderRight(border,k,sheet);
        });
    }
}
