package org.jeecg.modules.fwbz.energyAnalysis.constant;

public class BusinessConfigConstant {

    /**
     * 成本分析-电专业总量
     */
    public static final String COST_TOTAL_ELECTRICITY_KEY = "cost_analysis:total:electricity";

    /**
     * 成本分析-水专业总量
     */
    public static final String COST_TOTAL_WATER_KEY = "cost_analysis:total:water";

    /**
     * 成本分析-热专业总量
     */
    public static final String COST_TOTAL_HEAT_KEY = "cost_analysis:total:heat";

    /**
     * 成本分析-电-各专业成本
     */
    public static final String COST_CATEGORY_ELECTRICITY_KEY = "cost_analysis:category:electricity";

    /**
     * 成本分析-水-各专业成本
     */
    public static final String COST_CATEGORY_WATER_KEY = "cost_analysis:category:water";

    /**
     * 成本分析-热-各专业成本
     */
    public static final String COST_CATEGORY_HEAT_KEY = "cost_analysis:category:heat";

    /**
     * 碳排放分析-碳流图
     */
    public static final String CARBON_FLOW_DIAGRAM_KEY = "carbon:carbon_flow_diagram";

    /**
     * 碳排放分析-总量，多个点位以英文逗号分隔
     */
    public static final String CARBON_EMISSION_POINT = "carbon:carbon_emission_pointIds";
    /**
     * 能源计量场馆用电量计量规则ids，多个点位以英文逗号分隔
     */
    public static final String METERPOINTDATA_VENUEELECTRICITY = "meterPointData:venueElectricity";


    /**
     * 能源计量用能结构分析计量规则ids，多个点位以英文逗号分隔
     */
    public static final String METERPOINTDATA_ENERGYSTRUCTURE = "meterPointData:energyStructure";
    /**
     * 能源计量-概览-今日用电量
     */
    public static final String ENERGYMETERING_DAY_ELECTRIC = "energyMetering:day:electric";
    public static final String ENERGYMETERING_DAY_WATER = "energyMetering:day:water";
    public static final String ENERGYMETERING_MONTH_ELECTRIC = "energyMetering:month:electric";
    public static final String ENERGYMETERING_MONTH_WATER = "energyMetering:month:water";

    /**
     *运行保障tab页空调机组id
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_CATEGORYID = "operationSupport:tab:air:categoryid";

    /**
     *运行保障tab页空调机组 列表配置项
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_COLUMNS = "operationSupport:tab:air:columns";

    /**
     *运行保障tab页空调机组-计量规则id
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_POINT_ID = "operationSupport:tab:air:pointId";

    /**
     *运行保障tab页空调机组-能耗曲线ids
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_ENERGY_POINTIDS = "operationSupport:tab:air:energyPointIds";

    /**
     *运行保障tab页空调机组-送风温度趋势属性名
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_SUPPLYAIR = "operationSupport:tab:air:supplyAir";



    /**
     *运行保障tab页空调机组-回风温度趋势属性名
     */
    public static final String OPERATIONSUPPORT_TAB_AIR_RETURNAIR = "operationSupport:tab:air:returnAir";
    /**
     *运行保障tab页新风机组id
     */
    public static final String OPERATIONSUPPORT_TAB_FRESH_AIR_CATEGORYID = "operationSupport:tab:freshAir:categoryid";


    /**
     *运行保障tab页新风机组 列表配置项
     */
    public static final String OPERATIONSUPPORT_TAB_FRESH_AIR_COLUMNS = "operationSupport:tab:freshAir:columns";


    /**
     *运行保障tab页新风机组-计量规则id
     */
    public static final String OPERATIONSUPPORT_TAB_FRESHAIR_POINT_ID = "operationSupport:tab:freshAir:pointId";


    /**
     *运行保障tab页新风机组-回风温度趋势属性名
     */
    public static final String OPERATIONSUPPORT_TAB_FRESHAIR_PM25 = "operationSupport:tab:freshAir:pm25";






    /**
     *运行保障tab页配电系统id
     */
    public static final String OPERATIONSUPPORT_TAB_POWER_CATEGORYID = "operationSupport:tab:power:categoryid";


    /**
     *运行保障tab页配电系统 列表配置项
     */
    public static final String OPERATIONSUPPORT_TAB_POWER_COLUMNS = "operationSupport:tab:power:columns";




}
