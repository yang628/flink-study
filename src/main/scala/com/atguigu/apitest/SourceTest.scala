package com.atguigu.apitest

import org.apache.flink.streaming.api.scala._

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved 
  *
  * Project: FlinkTutorial
  * Package: com.atguigu.apitest
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/5 11:47
  */

// 定义样例类，温度传感器
case class SensorReading( id: String, timestamp: Long, temperature: Double )

object SourceTest {
  def main(args: Array[String]): Unit = {
    // 创建执行环境
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // 1. 从集合中读取数据
    val dataList = List(
      SensorReading("sensor_1", 1547718199, 35.8),
      SensorReading("sensor_6", 1547718201, 15.4),
      SensorReading("sensor_7", 1547718202, 6.7),
      SensorReading("sensor_10", 1547718205, 38.1)
    )
    val stream1 = env.fromCollection(dataList)
//    env.fromElements(1.0, 35, "hello")

    // 2. 从文件中读取数据
    val inputPath = "D:\\Projects\\BigData\\FlinkTutorial\\src\\main\\resources\\sensor.txt"
    val stream2 = env.readTextFile(inputPath)

    stream2.print()

    // 执行
    env.execute("source test")
  }
}
