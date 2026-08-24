package org.jeecg.modules.fwbz.energyAnalysis.util.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 用能成本计算
 */
public class CalculationUtil {

    public static String calculateMomToString(BigDecimal current, BigDecimal previous) {
        BigDecimal bigDecimal = calculateMom(current, previous);
        if(bigDecimal==null){
            return "-";
        }
        if(bigDecimal.compareTo(BigDecimal.ZERO)>0){
            return "↑" + bigDecimal + "%";
        }else if(bigDecimal.compareTo(BigDecimal.ZERO)<0){
            return "↓" + bigDecimal + "%";
        }else{
            return bigDecimal.toString();
        }
    }



    public static String calculatePercentageToString(Long numerator, Long denominator) {
        return calculatePercentage(numerator, denominator)+"%";

    }

    public static String calculatePercentageToString(BigDecimal numerator, BigDecimal denominator) {
        return calculatePercentage(numerator, denominator)+"%";
    }




    /**
     * 计算环比增长率（返回百分比数值，如 20.5 表示 20.5%）
     *
     * @param current  本期值
     * @param previous 上期值
     * @return 环比增长率，保留2位小数
     */
    public static BigDecimal calculateMom(BigDecimal current, BigDecimal previous) {
        // 1. 判空
        if (current == null || previous == null) {
            return null;
        }

        // 2. 处理上期为0的情况
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;  // 两者都为0，增长率为0
            }
            return null;  // 上期为0，本期>0，增长率无穷大，返回null或特殊值
        }

        // 3. 计算：(current - previous) / previous * 100
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)  // 先除，保留4位小数提高精度
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);  // 最终保留2位小数
    }


    /**
     * 计算百分比：分子 / 分母 * 100
     *
     * @param numerator   分子
     * @param denominator 分母
     * @return 百分比，保留2位小数
     */
    public static BigDecimal calculatePercentage(Long numerator, Long denominator) {
        // 1. 判空
        if (numerator == null || denominator == null) {
            return null;
        }
        // 2. 分母为0处理
        if (denominator == 0) {
            return numerator == 0 ? BigDecimal.ZERO : null;  // 0/0 返回0，非零/0 返回null
        }
        // 3. 计算：(numerator / denominator) * 100
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }



    /**
     * 计算百分比：分子 / 分母 * 100
     *
     * @param numerator   分子
     * @param denominator 分母
     * @return 百分比，保留2位小数
     */
    public static BigDecimal calculatePercentage(BigDecimal numerator, BigDecimal denominator) {
        // 1. 判空
        if (numerator == null || denominator == null) {
            return null;
        }
        // 2. 分母为0处理
        if (denominator.equals(BigDecimal.ZERO)) {
            return numerator.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : null;  // 0/0 返回0，非零/0 返回null
        }
        // 3. 计算：(numerator / denominator) * 100
        return numerator
                .divide(denominator, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
