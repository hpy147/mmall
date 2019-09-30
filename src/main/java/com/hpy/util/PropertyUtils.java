package com.hpy.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Author: hpy
 * Date: 2019-09-29
 * Description: <描述>
 */
public class PropertyUtils {

    private static Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    private  static Properties props;

    static {
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertyUtils.class.getClassLoader().getResourceAsStream(fileName), "utf-8"));
        } catch (IOException e) {
            logger.error("加载mmall.properties属性文件失败");
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key.trim());
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return value;
    }



}
