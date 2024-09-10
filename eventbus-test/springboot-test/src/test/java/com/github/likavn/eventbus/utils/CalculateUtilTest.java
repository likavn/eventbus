package com.github.likavn.eventbus.utils;


import com.github.likavn.eventbus.core.utils.CalculateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculateUtilTest {

    @Test
    public void testCalculate() {
        String expression = "1+2*3/4";
        double v = CalculateUtil.evalExpression(expression);
        Assertions.assertEquals(v, 2.5);
    }
}
