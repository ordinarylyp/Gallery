package com.sunmi.imagepicker.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-10-09.
 */
@RunWith(Parameterized.class)
public class UtilsTest {

    private long time;

    public UtilsTest(long time) {
        this.time = time;
    }

    @Parameterized.Parameters
    public static Iterable<Long> data() {
        return Arrays.asList(1508054402001L, Calendar.getInstance().getTimeInMillis());
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("测试开始！");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("测试结束！");
    }


    @Test()
    public void getImageTime() {
        Utils.getImageTime(time);
    }

    @Test()
    public void getVideoDuration() {
        Utils.getImageTime(time);
    }
}