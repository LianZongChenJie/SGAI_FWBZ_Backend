package org.jeecg.module.maintenance.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.module.maintenance.config.GroupInfoConfiguration;
import org.jeecg.module.maintenance.dto.PlanModelDto;
import org.jeecg.module.maintenance.dto.ResultInfo;
import org.jeecg.module.maintenance.dto.WeekOfMonth;
import org.jeecg.module.maintenance.entity.PlanModel;
import org.jeecg.module.maintenance.entity.PlanModelDetail;
import org.jeecg.module.maintenance.mapper.PlanModelMapper;
import org.jeecg.module.maintenance.service.IPlanModelDetailService;
import org.jeecg.module.maintenance.service.IPlanModelService;
import org.jeecg.module.maintenance.util.ExcelUtil;
import org.jeecg.module.maintenance.util.ExportStyler;
import org.jeecg.module.maintenance.util.HeaderUtil;
import org.jeecg.module.maintenance.util.ReflectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.module.maintenance.util.HeaderUtil.getHeader;

@Service
@AllArgsConstructor
public class PlanModelServiceImpl extends ServiceImpl<PlanModelMapper, PlanModel> implements IPlanModelService {

    private final GroupInfoConfiguration groupInfoConfiguration;

    private final IPlanModelDetailService planModelDetailService;

    @Override
    public void exportTemplate(int year, HttpServletResponse response, String labelType) {
        List<ExcelExportEntity> colList = new ArrayList<>(Arrays.asList(
                new ExcelExportEntity("序号"),
                new ExcelExportEntity("计划工作项目(必填)"),
                new ExcelExportEntity("数量(数字)"),
                new ExcelExportEntity("厂家"),
                new ExcelExportEntity("是否关联设备(必填)"),
                /*new ExcelExportEntity("是否关联空间"),*/
                // new ExcelExportEntity(labelType+"维保周期(数字)"),
                new ExcelExportEntity(labelType+"周期(数字)"),
                new ExcelExportEntity("单位"),
                new ExcelExportEntity("建议频次(数字)"),
                new ExcelExportEntity("持续时间(天)(必填)"),
//                new ExcelExportEntity("负责人"),
                new ExcelExportEntity("执行科组(必填)"),
                new ExcelExportEntity("类型(必填)")
        ));
        ExcelExportEntity yearHeader = new ExcelExportEntity("时间安排");
        Map<String, List<String>> monthWeekHeader = HeaderUtil.getTimeHeaderMap(year);
        List<ExcelExportEntity> monthList = new ArrayList<>();
        monthWeekHeader.forEach((k, v) -> {
            ExcelExportEntity monthHeader = new ExcelExportEntity(k);
            monthHeader.setList(v.stream().map(ExcelExportEntity::new).collect(Collectors.toList()));
            monthList.add(monthHeader);
        });
        yearHeader.setList(monthList);
        colList.add(yearHeader);
        // 根据部门id查询人员
        ExportParams params = new ExportParams(year + "年度设备设施"+labelType+"保计划一览表", labelType+"计划");

        //增加表头样式
        params.setStyle(ExportStyler.class);

        // 设置标题样式(ExcelExportStyleBigHeaderImpl重写了easypoi的AbstractExcelExportStyler方法)]
        params.setType(ExcelType.HSSF);
        Workbook workbook = ExcelExportUtil.exportExcel(params, colList,
                new ArrayList<>());
        //添加下拉选
        Sheet sheet = workbook.getSheetAt(0);
        selectList(workbook, 6, 6, new String[]{"天", "周", "月", "季度", "年"});
        selectList(workbook, 4, 4, new String[]{"是", "否"});

        //增加科组下拉选项
        selectList(workbook,9,9, groupInfoConfiguration.getGroupNames().values().toString().replace("[","").replace("]","").split(","));

        //增加维保类型下拉选项
        selectList(workbook,10,10, groupInfoConfiguration.getWeibaoTypeList().toString().replace("[","").replace("]","").split(","));


        //设置合并单元格的边框
        ExcelUtil.setMergedBorder(workbook, BorderStyle.THIN);

        try {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(year + "年设备"+labelType+"模板.xls", "UTF-8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @Override
    public List<ResultInfo> importData(MultipartFile file, String labelType) {
        List<PlanModel> planModelList = new ArrayList<>();
        List<ResultInfo> resultList = new ArrayList<>();
        List<PlanModel> executeList = new ArrayList<>();
        try {

            planModelList = ExcelUtil.readExcelWitchOutCheck(file.getInputStream(),labelType);

            Integer year = planModelList.get(0).getYear();
            //先检查年度，如果存在就都不导入
            if (countByYear(year,labelType) > 0) {
                ResultInfo resultInfo = new ResultInfo();
                resultInfo.setInfo("该年度已有计划模板，不可重复导入");
                resultInfo.setIndex("0");
                resultInfo.setKeyMessage(planModelList.get(0).getName());
                resultList.add(resultInfo);
                return resultList;
            }
            planModelList.forEach(item ->{
                ResultInfo result = checkInfo(item);
                if(result != null){
                    resultList.add(result);
                }else{
                    executeList.add(item);
                }
            });
        } catch (IOException e) {

        }

        for (PlanModel planModel : executeList) {

            setDepartmentIdByName(planModel);
            super.save(planModel);
            ResultInfo resultInfo = new ResultInfo();
            resultInfo.setKeyMessage("第"+planModel.getIndexNum()+"行，导入成功");
            resultInfo.setIndex(planModel.getIndexNum()+"");
            resultInfo.setInfo("无");
            resultList.add(resultInfo);
            for (PlanModelDetail planModelDetail : createModelDetail(planModel)) {
                planModelDetailService.save(planModelDetail);
            }
        }
        ResultInfo total = new ResultInfo() ;
        total.setIndex(0+"");
        total.setInfo("无");
        total.setKeyMessage("总述：共"+planModelList.size()+"条记录，其中导入成功共"+executeList.size()+"条");
        resultList.add(total);
        Collections.sort(resultList,(pre, curr)->{
            if(Integer.parseInt(pre.getIndex()) >Integer.parseInt(curr.getIndex())){
                return 1;
            }else{
                return -1;
            }
        });
        return resultList;
    }

    @Override
    public long countByYear(int year, String labelType) {
        return super.count(new LambdaQueryWrapper<PlanModel>().eq(PlanModel::getYear, year).eq(PlanModel::getLabelType, labelType));
    }

    @Transactional
    @Override
    public void remove(Integer year,String labelType) {
        if(year == null || StrUtil.isEmpty(labelType)){
            throw new JeecgBootException("年份或计划类型不能为空");
        }
        List<Long> planModelIds = listByYearAndLabelType(year,labelType)
                .stream().map(PlanModel::getId).toList();
        super.removeByIds(planModelIds);
    }

    @Override
    public PlanModelDto findByYear(Integer year, String labelType) {
        PlanModelDto planModelDto = new PlanModelDto();
        planModelDto.setTableHeader(getHeader(year,labelType));
        List<PlanModel> modelList = listByYearAndLabelType(year,labelType);
        planModelDto.setPlanModelList(modelList);
        return planModelDto;
    }

    @Override
    public Page<PlanModel> queryPage(PlanModel param) {
        return super.page(new Page<>(param.getPage(), param.getPagesize()), new LambdaQueryWrapper<PlanModel>().eq(PlanModel::getYear, param.getYear()).eq(PlanModel::getLabelType, param.getLabelType()));
    }

    private List<PlanModel> listByYearAndLabelType(Integer year,String labelType){
        if(year == null || StrUtil.isEmpty(labelType)){
            return Collections.emptyList();
        }
        return super.list(new LambdaQueryWrapper<PlanModel>().eq(PlanModel::getYear, year).eq(PlanModel::getLabelType, labelType));
    }


    /**
     * firstRow 開始行號 根据此项目，默认为2(下标0开始)
     * lastRow  根据此项目，默认为最大65535
     * firstCol 区域中第一个单元格的列号 (下标0开始)
     * lastCol 区域中最后一个单元格的列号
     * strings 下拉内容
     */
    public static void selectList(Workbook workbook, int firstCol, int lastCol, String[] strings) {


        Sheet sheet = workbook.getSheetAt(0);
        //  生成下拉列表
        //  只对(x，x)单元格有效
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(2, 65535, firstCol, lastCol);
        //  生成下拉框内容
        DataValidation dataValidation;
        if (sheet instanceof HSSFSheet) {
            DataValidationConstraint dvConstraint = DVConstraint.createExplicitListConstraint(strings);
            dataValidation = new HSSFDataValidation(cellRangeAddressList, dvConstraint);
        } else {
            XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
            XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                    .createExplicitListConstraint(strings);
            dataValidation = dvHelper.createValidation(dvConstraint, cellRangeAddressList);
        }
        //  对sheet页生效
        sheet.addValidationData(dataValidation);

    }


    /**
     *
     * @param planModels 模板导入是字段校验
     * @return
     */
    private ResultInfo checkInfo(PlanModel planModels){
        ResultInfo resultInfo = new ResultInfo();

        String msg = "";
        //非空校验 项目名称
        if(StringUtils.isEmpty(planModels.getName())){
            msg += "'计划工作项目'为空,";
        }
        if(0 == planModels.getDuration()){
            msg +="'持续时间'为空,";
        }
        if(StringUtils.isEmpty(planModels.getDepartment())){
            msg += "执行科组'为空,";
        }
        if(StringUtils.isEmpty(planModels.getWeibaoType())){
            msg +="'类型'为空";
        }

        if(StringUtils.isNotEmpty(msg)){
            resultInfo.setKeyMessage("未导入");
        }else {
            return null;
        }
        resultInfo.setInfo(msg);
        resultInfo.setKeyMessage(planModels.getName());
        resultInfo.setIndex(planModels.getIndexNum()+"");
        return  resultInfo;
    }

    private void setDepartmentIdByName(PlanModel planModel){

        Map<Long, String> map = groupInfoConfiguration.getGroupNames();

        List<Map.Entry<Long, String>> info = map.entrySet().stream().filter(k -> k.getValue().equals(planModel.getDepartment())).collect(Collectors.toList());

        if(info.size() != 0){
            planModel.setDepartmentId(info.get(0).getKey());
        }
    }

    @Transactional
    public List<PlanModelDetail> createModelDetail(PlanModel model) {
        Map<Integer, LocalDate> map = HeaderUtil.getWeekOfMonths(model.getYear()).stream().collect(Collectors.toMap(WeekOfMonth::getIndex, WeekOfMonth::getLocalDate, (k1, k2) -> k1));
        List<PlanModelDetail> detailList = new ArrayList<>();
        for (int i = 1; i < 54; i++) {
            Object value = ReflectUtil.getValue(model, "w" + i);
            if (value != null) {
                PlanModelDetail planModelDetail = new PlanModelDetail();
                planModelDetail.setPlanModelId(model.getId());
                planModelDetail.setStart(map.get(i) == null ? null : map.get(i).atStartOfDay());
                planModelDetail.setEnableFlag(false);
                detailList.add(planModelDetail);
            }
        }
        return detailList;
    }


}
