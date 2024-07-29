/**
 * Copyright 2023-2033, likavn (likavn@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.likavn.eventbus.core.utils;

import lombok.experimental.UtilityClass;

import java.util.Stack;

/**
 * 计算工具
 *
 * @author likavn
 * @date 2024/01/01
 */
@UtilityClass
public class CalculateUtil {

    /**
     * 截断小数点后多余的0
     *
     * @param expression exp
     * @return long
     */
    public long fixEvalExpression(String expression) {
        return (long) evalExpression(expression);
    }

    /**
     * 解析表达式的函数（这里简化实现，只处理加减乘除和括号）
     *
     * @param expression exp
     * @return double
     */
    @SuppressWarnings("all")
    public double evalExpression(String expression) {
        expression = trim(expression);
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        char ch;
        char[] chars = expression.toCharArray();
        for (int i = 0; i < chars.length; ) {
            ch = chars[i];
            if (Character.isDigit(ch)) {
                // 处理数字（这里简化处理，假设数字之间不会有空格）
                StringBuilder numBuilder = new StringBuilder();
                do {
                    numBuilder.append(chars[i++]);
                } while (i < chars.length && (Character.isDigit(chars[i]) || chars[i] == '.'));
                double num = Double.parseDouble(numBuilder.toString());
                values.push(num);
            } else {
                if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                    // 处理运算符（考虑优先级）
                    while (!operators.isEmpty() && hasPrecedence(ch, operators.peek())) {
                        char op = operators.pop();
                        double val2 = values.pop();
                        double val1 = values.pop();
                        values.push(applyOp(val1, val2, op));
                    }
                    operators.push(ch);
                } else if (ch == '(') {
                    // 处理左括号，直接入栈
                    operators.push(ch);
                } else if (ch == ')') {
                    // 处理右括号，计算括号内的表达式
                    while (!operators.isEmpty() && operators.peek() != '(') {
                        char op = operators.pop();
                        double val2 = values.pop();
                        double val1 = values.pop();
                        values.push(applyOp(val1, val2, op));
                    }
                    // 弹出左括号
                    operators.pop();
                }
                i++;
            }
        }

        // 处理剩余的操作符
        while (!operators.isEmpty()) {
            char op = operators.pop();
            double val2 = values.pop();
            double val1 = values.pop();
            values.push(applyOp(val1, val2, op));
        }

        // 栈顶元素即为最终结果
        return values.pop();
    }

    /**
     * 判断运算符优先级
     */
    @SuppressWarnings("all")
    private boolean hasPrecedence(char op1, char op2) {
        if (op1 == '*' || op1 == '/') {
            return false;
        }
        if (op2 == '+' || op2 == '-') {
            return false;
        }
        return true;
    }

    /**
     * 应用运算
     */
    private double applyOp(double a, double b, char op) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new IllegalArgumentException("除数不能为0");
                }
                return a / b;
            default:
                break;
        }
        return 0;
    }

    /**
     * 去除空格
     */
    private String trim(String expression) {
        return expression.replace(" ", "");
    }
}
