package org.jeecg.modules.fwbz.complaint.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.fwbz.complaint.dto.ComplaintHandleDTO;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintInfo;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintStatus;
import org.jeecg.modules.fwbz.complaint.entity.ComplaintType;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintStatusMapper;
import org.jeecg.modules.fwbz.complaint.mapper.ComplaintTypeMapper;
import org.jeecg.modules.fwbz.complaint.service.IComplaintInfoService;
import org.jeecg.modules.fwbz.complaint.vo.ComplaintDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

 /**
 * @Description: 投诉建议
 * @Author: jeecg-boot
 * @Date:   2026-08-06
 * @Version: V1.0
 */
@Api(tags="投诉建议")
@RestController
@RequestMapping("/fwbz/complaint")
@Slf4j
public class ComplaintController {
    @Autowired
    private IComplaintInfoService complaintInfoService;

    @Autowired
    private ComplaintTypeMapper complaintTypeMapper;

    @Autowired
    private ComplaintStatusMapper complaintStatusMapper;

    /**
     * 注册Date类型属性编辑器，处理空字符串绑定问题
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    /**
     * 分页列表查询
     *
     * @param complaintInfo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value="投诉建议-分页列表查询", notes="投诉建议-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<ComplaintInfo>> queryPageList(ComplaintInfo complaintInfo,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<ComplaintInfo> queryWrapper = QueryGenerator.initQueryWrapper(complaintInfo, req.getParameterMap());
        queryWrapper.orderByDesc("gmt_create");
        Page<ComplaintInfo> page = new Page<>(pageNo, pageSize);
        IPage<ComplaintInfo> pageList = complaintInfoService.pageWithTypeName(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 新增投诉建议
     *
     * @param complaintInfo
     * @return
     */
    @AutoLog(value = "投诉建议-添加")
    @ApiOperation(value="投诉建议-添加", notes="投诉建议-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody ComplaintInfo complaintInfo) {
        complaintInfoService.save(complaintInfo);
        return Result.OK("添加成功！");
    }

    /**
     * 修改投诉建议
     *
     * @param complaintInfo
     * @return
     */
    @AutoLog(value = "投诉建议-编辑")
    @ApiOperation(value="投诉建议-编辑", notes="投诉建议-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody ComplaintInfo complaintInfo) {
        complaintInfoService.updateById(complaintInfo);
        return Result.OK("编辑成功!");
    }

    /**
     * 处理投诉建议（修改状态并添加处理记录）
     *
     * @param dto
     * @return
     */
    @AutoLog(value = "投诉建议-处理")
    @ApiOperation(value="投诉建议-处理", notes="投诉建议-处理")
    @PostMapping(value = "/handle")
    public Result<String> handle(@RequestBody ComplaintHandleDTO dto) {
        complaintInfoService.handleComplaint(dto);
        return Result.OK("处理成功！");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "投诉建议-通过id删除")
    @ApiOperation(value="投诉建议-通过id删除", notes="投诉建议-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id", required=true) String id) {
        complaintInfoService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "投诉建议-批量删除")
    @ApiOperation(value="投诉建议-批量删除", notes="投诉建议-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids", required=true) String ids) {
        complaintInfoService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询（含处理记录）
     *
     * @param id
     * @return
     */
    @ApiOperation(value="投诉建议-通过id查询", notes="投诉建议-通过id查询（含处理记录）")
    @GetMapping(value = "/queryById")
    public Result<ComplaintDetailVO> queryById(@RequestParam(name="id", required=true) String id) {
        ComplaintDetailVO vo = complaintInfoService.getDetailById(id);
        if (vo == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(vo);
    }

    /**
     * 投诉建议类型下拉
     *
     * @return
     */
    @ApiOperation(value="投诉建议-类型下拉", notes="投诉建议-类型下拉")
    @GetMapping(value = "/typeList")
    public Result<List<ComplaintType>> typeList() {
        List<ComplaintType> list = complaintTypeMapper.selectList(null);
        return Result.OK(list);
    }

    /**
     * 投诉建议状态下拉
     *
     * @return
     */
    @ApiOperation(value="投诉建议-状态下拉", notes="投诉建议-状态下拉")
    @GetMapping(value = "/statusList")
    public Result<List<ComplaintStatus>> statusList() {
        List<ComplaintStatus> list = complaintStatusMapper.selectList(null);
        return Result.OK(list);
    }
}
