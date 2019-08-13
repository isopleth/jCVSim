/*
 * Copyright (C) 2017 Jason Leake
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jcvsim.common;

import static java.lang.Double.isNaN;
import java.math.BigDecimal;
import org.apache.commons.math3.util.FastMath;

/**
 * This class provides maths routines. It is here so than NaN etc errors can be
 * detected on values returned from Java Math routines. Functions which can take
 * any number as input will still return NaN if presented with NaN, so it it
 * worth checking this for all of them.
 *
 * @author Jason Leake
 */
public class Maths {

    /**
     * Absolute value
     *
     * @param value
     * @return
     */
    public static final double fabs(double value) {
        double returnValue = Math.abs(value);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in fabs - input value " + value);
        }
        return returnValue;
    }

    /**
     * Power
     *
     * @param value1
     * @param value2
     * @return
     */
    public static double pow(double value1, double value2) {
        double returnValue = Math.pow(value1, value2);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in pow - input value "
                    + value1 + " and " + value2);
        }
        return returnValue;
    }

    /**
     * Natural logarithm
     *
     * @param value
     * @return
     */
    public static double log(double value) {
        double returnValue = Math.log(value);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in log - input value " + value);
        }
        return returnValue;
    }

    /**
     * Exponential
     *
     * @param value
     * @return
     */
    public static double exp(double value) {
        double returnValue = Math.exp(value);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in exp - input value " + value);
        }
        return returnValue;
    }

    /**
     * Square root
     *
     * @param value
     * @return
     */
    public static final double sqrt(double value) {
        double returnValue = Math.sqrt(value);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in sqrt - input value " + value);
        }
        return returnValue;
    }

    /**
     * Arc tangent
     *
     * @param value
     * @return
     */
    public static final double atan(double value) {
        double returnValue = FastMath.atan(value);
        // Was "double returnValue = Math.atan(value);"
        if (isNaN(returnValue)) {
            System.out.println("isnan error in atan - input value " + value);
        }
        return returnValue;
    }

    /**
     * Round to nearest integer
     *
     * @param value
     * @return
     */
    public static final double rint(double value) {
        double returnValue = Math.round(value);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in round - input value " + value);
        }
        return returnValue;
    }

    /**
     * Tangent
     *
     * @param radians
     * @return
     */
    public static final double tan(double radians) {
        double returnValue = Math.tan(radians);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in tan - input value " + radians);
        }
        return returnValue;
    }

    /**
     * Sine
     *
     * @param radians
     * @return
     */
    public static final double sin(double radians) {
        double returnValue = Math.sin(radians);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in sin - input value " + radians);
        }
        return returnValue;
    }

    /**
     * Cosine
     *
     * @param radians
     * @return
     */
    public static final double cos(double radians) {
        double returnValue = Math.cos(radians);
        if (isNaN(returnValue)) {
            System.out.println("isnan error in cos - input value " + radians);
        }
        return returnValue;
    }

    /**
     * Return the mean of an array of values
     *
     * @param values the values
     * @return their mean value
     */
    public static final double getMean(double[] values) {
        double mean = 0.0;
        for (double value : values) {
            mean += value;
        }
        return mean / (double) values.length;
    }

    /**
     * Convert rate per minute to period in seconds
     *
     * @param rate rate per minute
     * @return period in seconds
     */
    public static double periodFromRatePerMinute(double rate) {
        return 60.0 / rate;
    }

    /**
     * Convert period in seconds to rate per minute
     *
     * @param rate rate per minute
     * @return period in seconds
     */
    public static double ratePerMinuteFromPeriod(double rate) {
        return 60.0 / rate;
    }

    /**
     * Convert value to string containing specified number of significant
     * digits. Multiple trailing zeroes are always removed
     *
     * @param value
     * @param numberOfDigits
     * @return string representation of value rounded to the specified number of
     * digits
     */
    public static String toStringSignificantFigures(Double value, int numberOfDigits) {
        BigDecimal roundedValue = BigDecimal.valueOf(value).setScale(numberOfDigits,
                BigDecimal.ROUND_HALF_UP);
        String returnString = roundedValue.toString();
        // Saw off trailing zeroes after the decimal point, leaving just the
        // first one.  i.e. 10.0000000 -> 10.0
        return returnString.replaceAll("(\\.0?\\d*?)0+$", "$1");
    }

}
