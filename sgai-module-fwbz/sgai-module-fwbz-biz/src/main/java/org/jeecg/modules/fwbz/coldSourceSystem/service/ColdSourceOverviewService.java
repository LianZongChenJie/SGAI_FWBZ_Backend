package org.jeecg.modules.fwbz.coldSourceSystem.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 冷源系统「前端字段 key -> 测点(tagId)」映射表服务
 *
 * 维护 FIELD_MAP 供实时订阅推送服务（{@link ColdSourceRealPushService}）做 tagId -> key 反查：
 * 冷源 SDK 订阅回传 tagId，此处提供该 tagId 对应的前端字段 key（一个测点可映射多个 key）。
 *
 * 说明：FIELD_MAP 中的 id 根据点表「文字描述」人工维护，注释中标明了对应描述，仅作参考，可自行修改。
 */
@Slf4j
@Service
public class ColdSourceOverviewService {

    /** 前端字段 key -> 测点ID数组(tagId[])，null 表示该 key 在点表中无对应测点（返回 null） */
    private static final Map<String, List<Long>> FIELD_MAP = buildFieldMap();

    /** 放入 key -> id 数组映射；ids 为空/null 表示该 key 无对应测点（返回 null） */
    private static void put(Map<String, List<Long>> m, String key, Long... ids) {
        if (ids == null || ids.length == 0) {
            m.put(key, null);
            return;
        }
        List<Long> list = new ArrayList<>(ids.length);
        for (Long id : ids) {
            if (id != null) {
                list.add(id);
            }
        }
        m.put(key, list.isEmpty() ? null : list);
    }

    /**
     * 构建前端字段 key -> 测点ID(tagId) 映射表。
     * id 依据点表「描述信息」列人工维护（注释为对应描述，仅作参考）；点表中不存在的点填 null（接口返回 null，前端可兜底）。
     */
    private static Map<String, List<Long>> buildFieldMap() {
        Map<String, List<Long>> m = new LinkedHashMap<>();

        // ================= station 系统总览 =================
        // 系统总功率 = 冷冻总管热量表功率(579) + 12台风冷机组热量表功率(557) + 东会议楼集中(543) + 东会议楼独立风冷(550) + 4号馆(564)
        put(m, "station.totalPower", 579L, 557L, 543L, 550L, 564L);
        put(m, "station.coolingCapacity", null);         // 制冷量（点表无，可自行配置）
        put(m, "station.cop", null);                     // COP（点表无）
        put(m, "station.supplyTemp", 512L);              // 冷冻水供水温度
        put(m, "station.returnTemp", 513L);              // 冷冻水回水温度
        put(m, "station.outdoorTemp", 526L);             // 室外温度
        put(m, "station.outdoorHumidity", 527L);         // 室外湿度
        put(m, "station.wetBulbTemp", 298L);             // 湿球温度
        put(m, "station.dailyEnergy", null);             // 日累计冷量（点表无）
        put(m, "station.powerSavingRate", null);         // 节能率（点表无）
        put(m, "station.loadRate", null);                // 负荷率（点表无）
        put(m, "station.copImprovement", null);          // COP提升率（点表无）
        put(m, "station.forecastEnergy", null);          // 预测能耗（点表无）
        put(m, "station.alarmCount", null);              // 告警数（点表无）
        put(m, "station.autoMode", null);                // 自动模式（点表无）

        // ================= loop 循环水系统 =================
        put(m, "loop.chwSupplyPressure", 514L);          // 冷冻水供水压力
        put(m, "loop.chwReturnPressure", 515L);          // 冷冻水回水压力
        put(m, "loop.chwFlow", 580L);                    // 冷冻总管热量表瞬时流量
        put(m, "loop.cwSupplyTemp", 516L);               // 冷却水供水温度
        put(m, "loop.cwReturnTemp", 517L);               // 冷却水回水温度-旁通阀前
        put(m, "loop.cwPressure", 519L);                 // 冷却水供水压力

        // ================= tower 冷却塔 =================
        put(m, "tower.basinLevel", 525L);                // 冷却塔盘液位

        // ================= makeup 补水系统 =================
        put(m, "makeup.tankLevel", 509L);                // 水箱液位
        put(m, "makeup.pumpRunning", 257L);              // 定压补水运行状态
        put(m, "makeup.pressure", null);                 // 补水压力（点表无）
        put(m, "makeup.softenerRunning", null);          // 软化水运行（点表无）

        // ================= treatment 水处理 =================
        put(m, "treatment.chilledRunning", 275L);        // 全程水处理运行状态
        put(m, "treatment.chilledFault", 276L);          // 全程水处理故障报警
        put(m, "treatment.coolingRunning", 259L);        // 旁通水处理运行状态
        put(m, "treatment.coolingFault", 260L);          // 旁通水处理故障报警

        // ================= dosing 加药 =================
        put(m, "dosing.running", 277L);                  // 加药装置运行状态
        put(m, "dosing.fault", 278L);                    // 加药装置故障报警

        // ================= control 群控参数 =================
        put(m, "control.addJudgeDelay", 285L);           // 加减机判断延时
        put(m, "control.addLoadSetpoint", 286L);         // 加机负荷设定
        put(m, "control.removeLoadSetpoint", 287L);      // 减机负荷设定
        put(m, "control.addTempDelta", 288L);            // 加机温度偏差
        put(m, "control.removeTempDelta", 290L);         // 减机温度偏差
        put(m, "control.chwPumpStartDelay", 292L);       // 冷冻泵开延时
        put(m, "control.chwPumpStopDelay", 295L);        // 冷冻泵关延时
        put(m, "control.cwPumpStartDelay", 293L);        // 冷却泵开延时
        put(m, "control.cwPumpStopDelay", 296L);         // 冷却泵关延时
        put(m, "control.systemEnabled", 590L);           // 系统总启停
        put(m, "control.outputCount", 283L);             // 输出启动数量
        put(m, "control.startMode", 23L);                // 启动数量模式
        put(m, "control.forceCount", 284L);              // 强制设定数量
        put(m, "control.startOrder", 24L);               // 冷机启动顺序
        put(m, "control.chillerOrder", 591L);            // 冷机设定排序

        // ================= towerControl 冷却塔控制 =================
        put(m, "towerControl.approachSetpoint", null);   // 趋近温度设定（点表无）
        put(m, "towerControl.highTempLimit", 503L);      // 回水温度设定高限
        put(m, "towerControl.lowTempLimit", 504L);       // 回水温度设定低限
        put(m, "towerControl.addDelay", 505L);           // 加塔延时设定
        put(m, "towerControl.removeDelay", 506L);        // 减塔延时设定
        put(m, "towerControl.highFrequency", 507L);      // 冷塔频率高限
        put(m, "towerControl.lowFrequency", 508L);       // 冷塔频率低限

        // ================= tower.4 4#冷却塔 =================
        put(m, "tower.4.running", 231L);                 // 4#冷却塔运行状态
        put(m, "tower.4.frequency", 486L);               // 4#冷却塔频率反馈
        put(m, "tower.4.hours", 484L);                   // 4#冷却塔运行时长
        put(m, "tower.4.fault", 232L);                   // 4#冷却塔故障报警
        put(m, "tower.4.controlSource", 233L);           // 4#冷却塔控制源
        put(m, "tower.4.softFault", 230L);               // 4#冷却塔软故障
        put(m, "tower.4.controlMode", 228L);             // 4#冷却塔控制模式
        put(m, "tower.4.forceCommand", 229L);            // 4#冷却塔强制命令
        put(m, "tower.4.forceFrequency", 485L);          // 4#冷却塔强制频率

        // ================= air.station 风冷系统总览 =================
        put(m, "air.station.totalPower", 557L);          // 12台风冷机组热量表功率
        put(m, "air.station.coolingCapacity", 557L);     // 12台风冷机组热量表功率
        put(m, "air.station.cop", null);                 // 风冷COP（点表无）
        put(m, "air.station.dailyEnergy", 556L);         // 12台风冷机组热量表累计冷量
        put(m, "air.station.powerSavingRate", null);     // 节能率（点表无）
        put(m, "air.station.loadRate", null);            // 负荷率（点表无）
        put(m, "air.station.copImprovement", null);      // COP提升率（点表无）
        put(m, "air.station.forecastEnergy", null);      // 预测能耗（点表无）
        put(m, "air.station.alarmCount", null);          // 告警数（点表无）

        // ================= air.loop 风冷循环 =================
        put(m, "air.loop.supplyTemp", 560L);             // 12台风冷机组热量表供水温度
        put(m, "air.loop.returnTemp", 561L);             // 12台风冷机组热量表回水温度
        put(m, "air.loop.supplyPressure", null);         // 供水压力（点表无）
        put(m, "air.loop.returnPressure", null);         // 回水压力（点表无）
        put(m, "air.loop.flow", 559L);                   // 12台风冷机组热量表瞬时流量

        // ================= air.treatment / air.degasser / air.control（点表无） =================
        put(m, "air.treatment.running", null);
        put(m, "air.treatment.fault", null);
        put(m, "air.degasser.running", null);
        put(m, "air.degasser.fault", null);
        put(m, "air.degasser.level", null);
        put(m, "air.control.systemEnabled", null);
        put(m, "air.control.outputCount", null);
        put(m, "air.control.startMode", null);
        put(m, "air.control.forceCount", null);
        put(m, "air.control.startOrder", null);
        put(m, "air.control.pumpOrder", null);

        // ================= distributed 分馆（3 个馆，对应点表热量表） =================
        // no -> {热量表前缀, 功率id, 累计冷量id, 供水温度id, 回水温度id, 瞬时流量id}
        long[][] hallIds = {
                {543, 542, 546, 547, 545},   // 1 东会议楼集中热量表
                {550, 549, 553, 554, 552},   // 2 东会议楼独立风冷热量表
                {564, 563, 567, 568, 566}    // 3 4号馆热量表
        };
        for (int i = 0; i < hallIds.length; i++) {
            String prefix = "distributed." + (i + 1);
            put(m, prefix + ".station.totalPower", hallIds[i][0]);
            put(m, prefix + ".station.coolingCapacity", hallIds[i][0]);
            put(m, prefix + ".station.cop", null);
            put(m, prefix + ".station.dailyEnergy", hallIds[i][1]);
            put(m, prefix + ".station.powerSavingRate", null);
            put(m, prefix + ".station.loadRate", null);
            put(m, prefix + ".station.copImprovement", null);
            put(m, prefix + ".station.forecastEnergy", null);
            put(m, prefix + ".station.alarmCount", null);
            put(m, prefix + ".loop.supplyTemp", hallIds[i][2]);
            put(m, prefix + ".loop.returnTemp", hallIds[i][3]);
            put(m, prefix + ".loop.supplyPressure", null);
            put(m, prefix + ".loop.returnPressure", null);
            put(m, prefix + ".loop.flow", hallIds[i][4]);
            for (int u = 1; u <= 3; u++) {
                put(m, prefix + ".unit." + u + ".running", null);
                put(m, prefix + ".unit." + u + ".fault", null);
                put(m, prefix + ".unit." + u + ".power", null);
                put(m, prefix + ".unit." + u + ".load", null);
                put(m, prefix + ".unit." + u + ".hours", null);
                put(m, prefix + ".unit." + u + ".fanFrequency", null);
                put(m, prefix + ".unit." + u + ".controlSource", null);
                put(m, prefix + ".unit." + u + ".softFault", null);
                put(m, prefix + ".unit." + u + ".controlMode", null);
                put(m, prefix + ".unit." + u + ".forceCommand", null);
                put(m, prefix + ".unit." + u + ".forceFrequency", null);
            }
            for (int p = 1; p <= 2; p++) {
                put(m, prefix + ".pump." + p + ".running", null);
                put(m, prefix + ".pump." + p + ".frequency", null);
                put(m, prefix + ".pump." + p + ".hours", null);
                put(m, prefix + ".pump." + p + ".fault", null);
                put(m, prefix + ".pump." + p + ".controlSource", null);
                put(m, prefix + ".pump." + p + ".softFault", null);
                put(m, prefix + ".pump." + p + ".controlMode", null);
                put(m, prefix + ".pump." + p + ".forceCommand", null);
                put(m, prefix + ".pump." + p + ".forceFrequency", null);
            }
            put(m, prefix + ".treatment.running", null);
            put(m, prefix + ".treatment.fault", null);
            put(m, prefix + ".degasser.running", null);
            put(m, prefix + ".degasser.fault", null);
            put(m, prefix + ".degasser.level", null);
            put(m, prefix + ".makeup.tankLevel", null);
            put(m, prefix + ".makeup.pumpRunning", null);
            put(m, prefix + ".makeup.pressure", null);
            put(m, prefix + ".makeup.softenerRunning", null);
            put(m, prefix + ".control.systemEnabled", null);
            put(m, prefix + ".control.autoMode", null);
        }

        // ================= airUnit 1-12 风冷机组（点表无逐台数据） =================
        for (int no = 1; no <= 12; no++) {
            put(m, "airUnit." + no + ".controlSource", null);
            put(m, "airUnit." + no + ".softFault", null);
            put(m, "airUnit." + no + ".controlMode", null);
            put(m, "airUnit." + no + ".forceCommand", null);
            put(m, "airUnit." + no + ".forceFrequency", null);
            put(m, "airUnit." + no + ".running", null);
            put(m, "airUnit." + no + ".fault", null);
            put(m, "airUnit." + no + ".power", null);
            put(m, "airUnit." + no + ".load", null);
            put(m, "airUnit." + no + ".hours", null);
            put(m, "airUnit." + no + ".fanFrequency", null);
        }

        // ================= airChwPump 1-3 风冷冷水泵（点表无对应） =================
        for (int no = 1; no <= 3; no++) {
            put(m, "airChwPump." + no + ".running", null);
            put(m, "airChwPump." + no + ".frequency", null);
            put(m, "airChwPump." + no + ".hours", null);
            put(m, "airChwPump." + no + ".fault", null);
            put(m, "airChwPump." + no + ".controlSource", null);
            put(m, "airChwPump." + no + ".softFault", null);
            put(m, "airChwPump." + no + ".controlMode", null);
            put(m, "airChwPump." + no + ".forceCommand", null);
            put(m, "airChwPump." + no + ".forceFrequency", null);
        }

        // ================= airHotPump 1-2 风冷热水泵（点表无对应） =================
        for (int no = 1; no <= 2; no++) {
            put(m, "airHotPump." + no + ".running", null);
            put(m, "airHotPump." + no + ".frequency", null);
            put(m, "airHotPump." + no + ".hours", null);
            put(m, "airHotPump." + no + ".fault", null);
            put(m, "airHotPump." + no + ".controlSource", null);
            put(m, "airHotPump." + no + ".softFault", null);
            put(m, "airHotPump." + no + ".controlMode", null);
            put(m, "airHotPump." + no + ".forceCommand", null);
            put(m, "airHotPump." + no + ".forceFrequency", null);
        }

        // ================= chiller 冷水机 1-3 =================
        // 每台机组各字段对应点表描述（参考）：
        // 1#: 运行状态48 总故障53 本地/远程49 软故障41 控制模式39 强制命令40 压机负荷322 运行时长317 蒸发器出水319 蒸发器回水318
        // 2#: 运行状态74 总故障75 本地/远程70 软故障22 控制模式67 强制命令68 1号压机负荷366 运行时长345 蒸发器出水280 蒸发器回水279
        // 3#: 运行状态101 总故障102 本地/远程97 软故障95 控制模式93 强制命令94 1号压机负荷418 运行时长393 蒸发器出水395 蒸发器回水394
        long[] chRunning = {48L, 74L, 101L};
        long[] chFault = {53L, 75L, 102L};
        long[] chControlSource = {49L, 70L, 97L};
        long[] chSoftFault = {41L, 22L, 95L};
        long[] chControlMode = {39L, 67L, 93L};
        long[] chForceCommand = {40L, 68L, 94L};
        long[] chLoad = {322L, 366L, 418L};
        long[] chHours = {317L, 345L, 393L};
        long[] chSupplyTemp = {319L, 280L, 395L};
        long[] chReturnTemp = {318L, 279L, 394L};
        for (int i = 0; i < 3; i++) {
            int no = i + 1;
            put(m, "chiller." + no + ".running", chRunning[i]);
            put(m, "chiller." + no + ".power", null);          // 冷机功率（点表无，可用压机负荷近似）
            put(m, "chiller." + no + ".load", chLoad[i]);
            put(m, "chiller." + no + ".hours", chHours[i]);
            put(m, "chiller." + no + ".supplyTemp", chSupplyTemp[i]);
            put(m, "chiller." + no + ".returnTemp", chReturnTemp[i]);
            put(m, "chiller." + no + ".fault", chFault[i]);
            put(m, "chiller." + no + ".controlSource", chControlSource[i]);
            put(m, "chiller." + no + ".softFault", chSoftFault[i]);
            put(m, "chiller." + no + ".controlMode", chControlMode[i]);
            put(m, "chiller." + no + ".forceCommand", chForceCommand[i]);
            put(m, "chiller." + no + ".forceFrequency", null);
        }

        // ================= chwPump 冷冻泵 1-3 =================
        // 1#: 运行状态123 频率反馈449 运行时长445 故障报警124 控制源125 软故障122 控制模式120 强制命令121 强制频率446
        // 2#: 运行状态129 频率反馈454 运行时长450 故障报警130 控制源131 软故障128 控制模式126 强制命令127 强制频率451
        // 3#: 运行状态135 频率反馈459 运行时长455 故障报警136 控制源137 软故障134 控制模式132 强制命令133 强制频率456
        long[] cwPumpRunning = {123L, 129L, 135L};
        long[] cwPumpFreq = {449L, 454L, 459L};
        long[] cwPumpHours = {445L, 450L, 455L};
        long[] cwPumpFault = {124L, 130L, 136L};
        long[] cwPumpControlSource = {125L, 131L, 137L};
        long[] cwPumpSoftFault = {122L, 128L, 134L};
        long[] cwPumpControlMode = {120L, 126L, 132L};
        long[] cwPumpForceCommand = {121L, 127L, 133L};
        long[] cwPumpForceFreq = {446L, 451L, 456L};
        for (int i = 0; i < 3; i++) {
            int no = i + 1;
            put(m, "chwPump." + no + ".running", cwPumpRunning[i]);
            put(m, "chwPump." + no + ".frequency", cwPumpFreq[i]);
            put(m, "chwPump." + no + ".hours", cwPumpHours[i]);
            put(m, "chwPump." + no + ".fault", cwPumpFault[i]);
            put(m, "chwPump." + no + ".controlSource", cwPumpControlSource[i]);
            put(m, "chwPump." + no + ".softFault", cwPumpSoftFault[i]);
            put(m, "chwPump." + no + ".controlMode", cwPumpControlMode[i]);
            put(m, "chwPump." + no + ".forceCommand", cwPumpForceCommand[i]);
            put(m, "chwPump." + no + ".forceFrequency", cwPumpForceFreq[i]);
        }

        // ================= cwPump 冷却泵 1-3 =================
        // 1#: 运行状态141 频率反馈464 运行时长140 故障报警142 控制源143 软故障139 控制模式460 强制命令138 强制频率461
        // 2#: 运行状态147 频率反馈469 运行时长146 故障报警148 控制源149 软故障145 控制模式465 强制命令144 强制频率466
        // 3#: 运行状态153 频率反馈474 运行时长152 故障报警154 控制源155 软故障151 控制模式470 强制命令150 强制频率471
        long[] cpRunning = {141L, 147L, 153L};
        long[] cpFreq = {464L, 469L, 474L};
        long[] cpHours = {140L, 146L, 152L};
        long[] cpFault = {142L, 148L, 154L};
        long[] cpControlSource = {143L, 149L, 155L};
        long[] cpSoftFault = {139L, 145L, 151L};
        long[] cpControlMode = {460L, 465L, 470L};
        long[] cpForceCommand = {138L, 144L, 150L};
        long[] cpForceFreq = {461L, 466L, 471L};
        for (int i = 0; i < 3; i++) {
            int no = i + 1;
            put(m, "cwPump." + no + ".running", cpRunning[i]);
            put(m, "cwPump." + no + ".frequency", cpFreq[i]);
            put(m, "cwPump." + no + ".hours", cpHours[i]);
            put(m, "cwPump." + no + ".fault", cpFault[i]);
            put(m, "cwPump." + no + ".controlSource", cpControlSource[i]);
            put(m, "cwPump." + no + ".softFault", cpSoftFault[i]);
            put(m, "cwPump." + no + ".controlMode", cpControlMode[i]);
            put(m, "cwPump." + no + ".forceCommand", cpForceCommand[i]);
            put(m, "cwPump." + no + ".forceFrequency", cpForceFreq[i]);
        }

        // ================= tower 冷却塔 1-3 =================
        // 1#: 运行状态159 频率反馈477 运行时长475 故障报警160 控制源161 软故障158 控制模式156 强制命令157 强制频率476
        // 2#: 运行状态183 频率反馈480 运行时长478 故障报警184 控制源185 软故障182 控制模式180 强制命令181 强制频率479
        // 3#: 运行状态207 频率反馈483 运行时长481 故障报警208 控制源209 软故障206 控制模式204 强制命令205 强制频率482
        long[] twRunning = {159L, 183L, 207L};
        long[] twFreq = {477L, 480L, 483L};
        long[] twHours = {475L, 478L, 481L};
        long[] twFault = {160L, 184L, 208L};
        long[] twControlSource = {161L, 185L, 209L};
        long[] twSoftFault = {158L, 182L, 206L};
        long[] twControlMode = {156L, 180L, 204L};
        long[] twForceCommand = {157L, 181L, 205L};
        long[] twForceFreq = {476L, 479L, 482L};
        for (int i = 0; i < 3; i++) {
            int no = i + 1;
            put(m, "tower." + no + ".running", twRunning[i]);
            put(m, "tower." + no + ".frequency", twFreq[i]);
            put(m, "tower." + no + ".hours", twHours[i]);
            put(m, "tower." + no + ".fault", twFault[i]);
            put(m, "tower." + no + ".controlSource", twControlSource[i]);
            put(m, "tower." + no + ".softFault", twSoftFault[i]);
            put(m, "tower." + no + ".controlMode", twControlMode[i]);
            put(m, "tower." + no + ".forceCommand", twForceCommand[i]);
            put(m, "tower." + no + ".forceFrequency", twForceFreq[i]);
        }
        return m;
    }

    /**
     * 当前映射的字段数量（调试用）
     */
    public int getFieldCount() {
        return FIELD_MAP.size();
    }

    /**
     * 全量返回 FIELD_MAP 映射：前端字段 key -> 测点ID数组(tagId[])，null 表示该 key 无对应测点
     *
     * @return 只读视图（防御性拷贝，外部修改不影响内部映射）
     */
    public Map<String, List<Long>> getFieldMap() {
        Map<String, List<Long>> copy = new LinkedHashMap<>();
        FIELD_MAP.forEach((key, ids) -> copy.put(key, ids == null ? null : new ArrayList<>(ids)));
        return copy;
    }
}
