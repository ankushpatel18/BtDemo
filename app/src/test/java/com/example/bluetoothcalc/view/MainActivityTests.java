package com.example.bluetoothcalc.view;

import com.example.bluetoothcalc.Actions;
import com.example.bluetoothcalc.model.CalcRequestModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MainActivityTests {

    @Before
    public void setUp() {
    }

    @Test
    public void calculateNumbersTestDivide() {
        MainActivity act = new MainActivity();
        float inputOne = 12f;
        float inoutTwo = 2f;
        float expectedOutput = inputOne / inoutTwo;
        CalcRequestModel data = new CalcRequestModel(Actions.DIVIDE, inputOne, inoutTwo, -1f);
        CalcRequestModel functionOutput = act.calculateNumbers(data);
        Assert.assertNotNull(functionOutput);
        Assert.assertEquals(expectedOutput, functionOutput.getResult(), 0);
    }

    @Test
    public void calculateNumbersTestDivide_Negative() {
        MainActivity act = new MainActivity();
        float inputOne = 12f;
        float inoutTwo = 2f;
        float expectedOutput = 12f;     //that is wrong output
        CalcRequestModel data = new CalcRequestModel(Actions.DIVIDE, inputOne, inoutTwo, -1f);
        CalcRequestModel functionOutput = act.calculateNumbers(data);
        Assert.assertNotNull(functionOutput);
        Assert.assertEquals(expectedOutput, functionOutput.getResult(), 0);
    }

    @Test
    public void calculateNumbersTestMultiply() {
        MainActivity act = new MainActivity();
        float inputOne = 12f;
        float inoutTwo = 2f;
        float expectedOutput = inputOne * inoutTwo;
        CalcRequestModel data = new CalcRequestModel(Actions.MULTIPLY, inputOne, inoutTwo, -1f);
        CalcRequestModel functionOutput = act.calculateNumbers(data);
        Assert.assertNotNull(functionOutput);
        Assert.assertEquals(expectedOutput, functionOutput.getResult(), 0);
    }

    @Test
    public void calculateNumbersTestMultiply_Negative() {
        MainActivity act = new MainActivity();
        float inputOne = 12f;
        float inoutTwo = 2f;
        float expectedOutput = 12f;     //that is wrong output
        CalcRequestModel data = new CalcRequestModel(Actions.MULTIPLY, inputOne, inoutTwo, -1f);
        CalcRequestModel functionOutput = act.calculateNumbers(data);
        Assert.assertNotNull(functionOutput);
        Assert.assertEquals(expectedOutput, functionOutput.getResult(), 0);
    }
}
