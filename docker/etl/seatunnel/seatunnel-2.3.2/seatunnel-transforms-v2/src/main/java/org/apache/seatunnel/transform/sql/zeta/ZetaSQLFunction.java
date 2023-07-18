/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.transform.sql.zeta;

import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.api.table.type.SqlType;
import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.transform.exception.TransformException;
import org.apache.seatunnel.transform.sql.zeta.functions.DateTimeFunction;
import org.apache.seatunnel.transform.sql.zeta.functions.NumericFunction;
import org.apache.seatunnel.transform.sql.zeta.functions.StringFunction;
import org.apache.seatunnel.transform.sql.zeta.functions.SystemFunction;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ZetaSQLFunction {
    // ============================internal functions=====================

    // -------------------------string functions----------------------------
    public static final String ASCII = "ASCII";
    public static final String BIT_LENGTH = "BIT_LENGTH";
    public static final String CHAR_LENGTH = "CHAR_LENGTH";
    public static final String LENGTH = "LENGTH";
    public static final String OCTET_LENGTH = "OCTET_LENGTH";
    public static final String CHAR = "CHAR";
    public static final String CHR = "CHR";
    public static final String CONCAT = "CONCAT";
    public static final String CONCAT_WS = "CONCAT_WS";
    public static final String HEXTORAW = "HEXTORAW";
    public static final String RAWTOHEX = "RAWTOHEX";
    public static final String INSERT = "INSERT";
    public static final String LOWER = "LOWER";
    public static final String LCASE = "LCASE";
    public static final String UPPER = "UPPER";
    public static final String UCASE = "UCASE";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";
    public static final String LOCATE = "LOCATE";
    public static final String INSTR = "INSTR";
    public static final String POSITION = "POSITION";
    public static final String LPAD = "LPAD";
    public static final String RPAD = "RPAD";
    public static final String LTRIM = "LTRIM";
    public static final String RTRIM = "RTRIM";
    public static final String TRIM = "TRIM";
    public static final String REGEXP_REPLACE = "REGEXP_REPLACE";
    public static final String REGEXP_LIKE = "REGEXP_LIKE";
    public static final String REGEXP_SUBSTR = "REGEXP_SUBSTR";
    public static final String REPEAT = "REPEAT";
    public static final String REPLACE = "REPLACE";
    public static final String SOUNDEX = "SOUNDEX";
    public static final String SPACE = "SPACE";
    public static final String SUBSTRING = "SUBSTRING";
    public static final String SUBSTR = "SUBSTR";
    public static final String TO_CHAR = "TO_CHAR";
    public static final String TRANSLATE = "TRANSLATE";

    // -------------------------numeric functions----------------------------
    public static final String ABS = "ABS";
    public static final String ACOS = "ACOS";
    public static final String ASIN = "ASIN";
    public static final String ATAN = "ATAN";
    public static final String COS = "COS";
    public static final String COSH = "COSH";
    public static final String COT = "COT";
    public static final String SIN = "SIN";
    public static final String SINH = "SINH";
    public static final String TAN = "TAN";
    public static final String TANH = "TANH";
    public static final String ATAN2 = "ATAN2";
    public static final String MOD = "MOD";
    public static final String CEIL = "CEIL";
    public static final String CEILING = "CEILING";
    public static final String EXP = "EXP";
    public static final String FLOOR = "FLOOR";
    public static final String LN = "LN";
    public static final String LOG = "LOG";
    public static final String LOG10 = "LOG10";
    public static final String RADIANS = "RADIANS";
    public static final String SQRT = "SQRT";
    public static final String PI = "PI";
    public static final String POWER = "POWER";
    public static final String RAND = "RAND";
    public static final String RANDOM = "RANDOM";
    public static final String ROUND = "ROUND";
    public static final String SIGN = "SIGN";
    public static final String TRUNC = "TRUNC";
    public static final String TRUNCATE = "TRUNCATE";

    // -------------------------time and date functions----------------------------
    public static final String CURRENT_DATE = "CURRENT_DATE";
    public static final String CURRENT_DATE_P = "CURRENT_DATE()";
    public static final String CURRENT_TIME = "CURRENT_TIME";
    public static final String CURRENT_TIME_P = "CURRENT_TIME()";
    public static final String CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";
    public static final String CURRENT_TIMESTAMP_P = "CURRENT_TIMESTAMP()";
    public static final String NOW = "NOW";
    public static final String DATEADD = "DATEADD";
    public static final String TIMESTAMPADD = "TIMESTAMPADD";
    public static final String DATEDIFF = "DATEDIFF";
    public static final String DATE_TRUNC = "DATE_TRUNC";
    public static final String DAYNAME = "DAYNAME";
    public static final String DAY_OF_MONTH = "DAY_OF_MONTH";
    public static final String DAY_OF_WEEK = "DAY_OF_WEEK";
    public static final String DAY_OF_YEAR = "DAY_OF_YEAR";
    public static final String EXTRACT = "EXTRACT";
    public static final String FORMATDATETIME = "FORMATDATETIME";
    public static final String HOUR = "HOUR";
    public static final String MINUTE = "MINUTE";
    public static final String MONTH = "MONTH";
    public static final String MONTHNAME = "MONTHNAME";
    public static final String PARSEDATETIME = "PARSEDATETIME";
    public static final String TO_DATE = "TO_DATE";
    public static final String QUARTER = "QUARTER";
    public static final String SECOND = "SECOND";
    public static final String WEEK = "WEEK";
    public static final String YEAR = "YEAR";

    // -------------------------system functions----------------------------
    public static final String COALESCE = "COALESCE";
    public static final String IFNULL = "IFNULL";
    public static final String NULLIF = "NULLIF";

    private final SeaTunnelRowType inputRowType;
    private final ZetaSQLType zetaSQLType;

    private final List<ZetaUDF> udfList;

    public ZetaSQLFunction(
            SeaTunnelRowType inputRowType, ZetaSQLType zetaSQLType, List<ZetaUDF> udfList) {
        this.inputRowType = inputRowType;
        this.zetaSQLType = zetaSQLType;
        this.udfList = udfList;
    }

    public Object computeForValue(Expression expression, Object[] inputFields) {
        if (expression instanceof NullValue) {
            return null;
        }
        if (expression instanceof DoubleValue) {
            return ((DoubleValue) expression).getValue();
        }
        if (expression instanceof LongValue) {
            long longVal = ((LongValue) expression).getValue();
            if (longVal <= Integer.MAX_VALUE && longVal >= Integer.MIN_VALUE) {
                return (int) longVal;
            } else {
                return longVal;
            }
        }
        if (expression instanceof StringValue) {
            return ((StringValue) expression).getValue();
        }
        if (expression instanceof Column) {
            int idx = inputRowType.indexOf(((Column) expression).getColumnName());
            return inputFields[idx];
        }
        if (expression instanceof Function) {
            Function function = (Function) expression;
            ExpressionList expressionList = function.getParameters();
            List<Object> functionArgs = new ArrayList<>();
            if (expressionList != null) {
                for (Expression funcArgExpression : expressionList.getExpressions()) {
                    functionArgs.add(computeForValue(funcArgExpression, inputFields));
                }
            }
            return executeFunctionExpr(function.getName(), functionArgs);
        }
        if (expression instanceof TimeKeyExpression) {
            return executeTimeKeyExpr(((TimeKeyExpression) expression).getStringValue());
        }
        if (expression instanceof ExtractExpression) {
            ExtractExpression extract = (ExtractExpression) expression;
            List<Object> functionArgs = new ArrayList<>();
            functionArgs.add(computeForValue(extract.getExpression(), inputFields));
            functionArgs.add(extract.getName());
            return executeFunctionExpr(ZetaSQLFunction.EXTRACT, functionArgs);
        }
        if (expression instanceof Parenthesis) {
            Parenthesis parenthesis = (Parenthesis) expression;
            return computeForValue(parenthesis.getExpression(), inputFields);
        }
        if (expression instanceof BinaryExpression) {
            return executeBinaryExpr((BinaryExpression) expression, inputFields);
        }
        if (expression instanceof CastExpression) {
            CastExpression castExpression = (CastExpression) expression;
            Expression leftExpr = castExpression.getLeftExpression();
            Object leftValue = computeForValue(leftExpr, inputFields);
            return executeCastExpr(castExpression, leftValue);
        }
        throw new TransformException(
                CommonErrorCode.UNSUPPORTED_OPERATION,
                String.format("Unsupported SQL Expression: %s ", expression.toString()));
    }

    public Object executeFunctionExpr(String functionName, List<Object> args) {
        switch (functionName.toUpperCase()) {
            case ASCII:
                return StringFunction.ascii(args);
            case BIT_LENGTH:
                return StringFunction.bitLength(args);
            case CHAR_LENGTH:
            case LENGTH:
                return StringFunction.charLength(args);
            case OCTET_LENGTH:
                return StringFunction.octetLength(args);
            case CHAR:
            case CHR:
                return StringFunction.chr(args);
            case CONCAT:
                return StringFunction.concat(args);
            case CONCAT_WS:
                return StringFunction.concatWs(args);
            case HEXTORAW:
                return StringFunction.hextoraw(args);
            case RAWTOHEX:
                return StringFunction.rawtohex(args);
            case INSERT:
                return StringFunction.insert(args);
            case LOWER:
            case LCASE:
                return StringFunction.lower(args);
            case UPPER:
            case UCASE:
                return StringFunction.upper(args);
            case LEFT:
                return StringFunction.left(args);
            case RIGHT:
                return StringFunction.right(args);
            case LOCATE:
            case POSITION:
                return StringFunction.location(functionName, args);
            case INSTR:
                return StringFunction.instr(args);
            case LPAD:
            case RPAD:
                return StringFunction.pad(functionName, args);
            case LTRIM:
                return StringFunction.ltrim(args);
            case RTRIM:
                return StringFunction.rtrim(args);
            case TRIM:
                return StringFunction.trim(args);
            case REGEXP_REPLACE:
                return StringFunction.regexpReplace(args);
            case REGEXP_LIKE:
                return StringFunction.regexpLike(args);
            case REGEXP_SUBSTR:
                return StringFunction.regexpSubstr(args);
            case REPEAT:
                return StringFunction.repeat(args);
            case REPLACE:
                return StringFunction.replace(args);
            case SOUNDEX:
                return StringFunction.soundex(args);
            case SPACE:
                return StringFunction.space(args);
            case SUBSTRING:
            case SUBSTR:
                return StringFunction.substring(args);
            case TO_CHAR:
                return StringFunction.toChar(args);
            case TRANSLATE:
                return StringFunction.translate(args);
            case ABS:
                return NumericFunction.abs(args);
            case ACOS:
                return NumericFunction.acos(args);
            case ASIN:
                return NumericFunction.asin(args);
            case ATAN:
                return NumericFunction.atan(args);
            case COS:
                return NumericFunction.cos(args);
            case COSH:
                return NumericFunction.cosh(args);
            case COT:
                return NumericFunction.cot(args);
            case SIN:
                return NumericFunction.sin(args);
            case SINH:
                return NumericFunction.sinh(args);
            case TAN:
                return NumericFunction.tan(args);
            case TANH:
                return NumericFunction.tanh(args);
            case ATAN2:
                return NumericFunction.atan2(args);
            case MOD:
                return NumericFunction.mod(args);
            case CEIL:
            case CEILING:
                return NumericFunction.ceil(args);
            case EXP:
                return NumericFunction.exp(args);
            case FLOOR:
                return NumericFunction.floor(args);
            case LN:
                return NumericFunction.ln(args);
            case LOG:
                return NumericFunction.log(args);
            case LOG10:
                return NumericFunction.log10(args);
            case RADIANS:
                return NumericFunction.radians(args);
            case SQRT:
                return NumericFunction.sqrt(args);
            case PI:
                return NumericFunction.pi(args);
            case POWER:
                return NumericFunction.power(args);
            case RAND:
            case RANDOM:
                return NumericFunction.random(args);
            case ROUND:
                return NumericFunction.round(args);
            case SIGN:
                return NumericFunction.sign(args);
            case TRUNC:
            case TRUNCATE:
                return NumericFunction.trunc(args);
            case NOW:
                return DateTimeFunction.currentTimestamp();
            case DATEADD:
            case TIMESTAMPADD:
                return DateTimeFunction.dateadd(args);
            case DATEDIFF:
                return DateTimeFunction.datediff(args);
            case DATE_TRUNC:
                return DateTimeFunction.dateTrunc(args);
            case DAYNAME:
                return DateTimeFunction.dayname(args);
            case DAY_OF_MONTH:
                return DateTimeFunction.dayOfMonth(args);
            case DAY_OF_WEEK:
                return DateTimeFunction.dayOfWeek(args);
            case DAY_OF_YEAR:
                return DateTimeFunction.dayOfYear(args);
            case EXTRACT:
                return DateTimeFunction.extract(args);
            case FORMATDATETIME:
                return DateTimeFunction.formatdatetime(args);
            case HOUR:
                return DateTimeFunction.hour(args);
            case MINUTE:
                return DateTimeFunction.minute(args);
            case MONTH:
                return DateTimeFunction.month(args);
            case MONTHNAME:
                return DateTimeFunction.monthname(args);
            case PARSEDATETIME:
            case TO_DATE:
                return DateTimeFunction.parsedatetime(args);
            case QUARTER:
                return DateTimeFunction.quarter(args);
            case SECOND:
                return DateTimeFunction.second(args);
            case WEEK:
                return DateTimeFunction.week(args);
            case YEAR:
                return DateTimeFunction.year(args);
            case COALESCE:
                return SystemFunction.coalesce(args);
            case IFNULL:
                return SystemFunction.ifnull(args);
            case NULLIF:
                return SystemFunction.nullif(args);
            default:
                for (ZetaUDF udf : udfList) {
                    if (udf.functionName().equalsIgnoreCase(functionName)) {
                        return udf.evaluate(args);
                    }
                }
                throw new TransformException(
                        CommonErrorCode.UNSUPPORTED_OPERATION,
                        String.format("Unsupported function: %s", functionName));
        }
    }

    public Object executeTimeKeyExpr(String timeKeyExpr) {
        switch (timeKeyExpr.toUpperCase()) {
            case CURRENT_DATE:
            case CURRENT_DATE_P:
                return DateTimeFunction.currentDate();
            case CURRENT_TIME:
            case CURRENT_TIME_P:
                return DateTimeFunction.currentTime();
            case CURRENT_TIMESTAMP:
            case CURRENT_TIMESTAMP_P:
                return DateTimeFunction.currentTimestamp();
        }
        throw new TransformException(
                CommonErrorCode.UNSUPPORTED_OPERATION,
                String.format("Unsupported TimeKey expression: %s", timeKeyExpr));
    }

    public Object executeCastExpr(CastExpression castExpression, Object arg) {
        String dataType = castExpression.getType().getDataType();
        List<Object> args = new ArrayList<>(2);
        args.add(arg);
        args.add(dataType.toUpperCase());
        if (dataType.equalsIgnoreCase("DECIMAL")) {
            List<String> ps = castExpression.getType().getArgumentsStringList();
            args.add(Integer.parseInt(ps.get(0)));
            args.add(Integer.parseInt(ps.get(1)));
        }
        return SystemFunction.castAs(args);
    }

    private Object executeBinaryExpr(BinaryExpression binaryExpression, Object[] inputFields) {
        if (binaryExpression instanceof Concat) {
            Concat concat = (Concat) binaryExpression;
            Expression leftExpr = concat.getLeftExpression();
            Expression rightExpr = concat.getRightExpression();
            Function function = new Function();
            function.setName(ZetaSQLFunction.CONCAT);
            ExpressionList expressionList = new ExpressionList();
            expressionList.setExpressions(new ArrayList<>());
            expressionList.getExpressions().add(leftExpr);
            expressionList.getExpressions().add(rightExpr);
            function.setParameters(expressionList);
            return computeForValue(function, inputFields);
        }
        Number leftValue =
                (Number) computeForValue(binaryExpression.getLeftExpression(), inputFields);
        Number rightValue =
                (Number) computeForValue(binaryExpression.getRightExpression(), inputFields);
        if (leftValue == null || rightValue == null) {
            return null;
        }
        SeaTunnelDataType<?> resultType = zetaSQLType.getExpressionType(binaryExpression);
        if (resultType.getSqlType() == SqlType.INT) {
            if (binaryExpression instanceof Addition) {
                return leftValue.intValue() + rightValue.intValue();
            }
            if (binaryExpression instanceof Subtraction) {
                return leftValue.intValue() - rightValue.intValue();
            }
            if (binaryExpression instanceof Multiplication) {
                return leftValue.intValue() * rightValue.intValue();
            }
            if (binaryExpression instanceof Division) {
                return leftValue.intValue() / rightValue.intValue();
            }
            if (binaryExpression instanceof Modulo) {
                return leftValue.intValue() % rightValue.intValue();
            }
        }
        if (resultType.getSqlType() == SqlType.DECIMAL) {
            BigDecimal bigDecimal = BigDecimal.valueOf(leftValue.doubleValue());
            if (binaryExpression instanceof Addition) {
                return bigDecimal.add(BigDecimal.valueOf(rightValue.doubleValue()));
            }
            if (binaryExpression instanceof Subtraction) {
                return bigDecimal.subtract(BigDecimal.valueOf(rightValue.doubleValue()));
            }
            if (binaryExpression instanceof Multiplication) {
                return bigDecimal.multiply(BigDecimal.valueOf(rightValue.doubleValue()));
            }
            if (binaryExpression instanceof Division) {
                DecimalType decimalType = (DecimalType) resultType;
                return bigDecimal.divide(
                        BigDecimal.valueOf(rightValue.doubleValue()),
                        decimalType.getScale(),
                        RoundingMode.UP);
            }
            if (binaryExpression instanceof Modulo) {
                List<Object> args = new ArrayList<>();
                args.add(leftValue);
                args.add(rightValue);
                return NumericFunction.mod(args);
            }
        }
        if (resultType.getSqlType() == SqlType.DOUBLE) {
            if (binaryExpression instanceof Addition) {
                return leftValue.doubleValue() + rightValue.doubleValue();
            }
            if (binaryExpression instanceof Subtraction) {
                return leftValue.doubleValue() - rightValue.doubleValue();
            }
            if (binaryExpression instanceof Multiplication) {
                return leftValue.doubleValue() * rightValue.doubleValue();
            }
            if (binaryExpression instanceof Division) {
                return leftValue.doubleValue() / rightValue.doubleValue();
            }
            if (binaryExpression instanceof Modulo) {
                return leftValue.doubleValue() % rightValue.doubleValue();
            }
        }
        if (resultType.getSqlType() == SqlType.BIGINT) {
            if (binaryExpression instanceof Addition) {
                return leftValue.longValue() + rightValue.longValue();
            }
            if (binaryExpression instanceof Subtraction) {
                return leftValue.longValue() - rightValue.longValue();
            }
            if (binaryExpression instanceof Multiplication) {
                return leftValue.longValue() * rightValue.longValue();
            }
            if (binaryExpression instanceof Division) {
                return leftValue.longValue() / rightValue.longValue();
            }
            if (binaryExpression instanceof Modulo) {
                return leftValue.longValue() % rightValue.longValue();
            }
        }
        throw new TransformException(
                CommonErrorCode.UNSUPPORTED_OPERATION,
                String.format("Unsupported SQL Expression: %s ", binaryExpression));
    }
}
