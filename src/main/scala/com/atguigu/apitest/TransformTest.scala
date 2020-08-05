package com.atguigu.apitest

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.streaming.api.scala._

/**
  * Copyright (c) 2018-2028 尚硅谷 All Rights Reserved 
  *
  * Project: FlinkTutorial
  * Package: com.atguigu.apitest
  * Version: 1.0
  *
  * Created by wushengran on 2020/8/5 15:39
  */
object TransformTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // 0.读取数据
    val inputPath = "D:\\Projects\\BigData\\FlinkTutorial\\src\\main\\resources\\sensor.txt"
    val inputStream = env.readTextFile(inputPath)

    // 1.先转换成样例类类型（简单转换操作）
    val dataStream = inputStream
      .map( data => {
        val arr = data.split(",")
        SensorReading(arr(0), arr(1).toLong, arr(2).toDouble)
      } )
    // 2.分组聚合，输出每个传感器当前最小值
    val aggStream = dataStream
      .keyBy("id")    // 根据id进行分组
      .minBy("temperature")

    // 3.需要输出当前最小的温度值，以及最近的时间戳，要用reduce
    val resultStream = dataStream
      .keyBy("id")
//      .reduce( (curState, newData) =>
//        SensorReading( curState.id, newData.timestamp, curState.temperature.min(newData.temperature) )
//      )
      .reduce(new MyReduceFunction)

    //    resultStream.print()

    // 4. 多流转换操作
    // 4.1 分流，将传感器温度数据分成低温、高温两条流
    val splitStream = dataStream
      .split( data => {
        if( data.temperature > 30.0 ) Seq("high") else Seq("low")
      } )
    val highTempStream = splitStream.select("high")
    val lowTempStream = splitStream.select("low")
    val allTempStream = splitStream.select("high", "low")

//    highTempStream.print("high")
//    lowTempStream.print("low")
//    allTempStream.print("all")

    // 4.2 合流，connect
    val warningStream = highTempStream.map( data => (data.id, data.temperature) )
    val connectedStreams = warningStream.connect(lowTempStream)

    // 用coMap对数据进行分别处理
    val coMapResultStream: DataStream[Any] = connectedStreams
      .map(
        waringData => (waringData._1, waringData._2, "warning") ,
        lowTempData => (lowTempData.id, "healthy")
      )
    
    // 4.3 union合流
    val unionStream = highTempStream.union(lowTempStream, allTempStream)

    coMapResultStream.print("coMap")

    env.execute("transform test")
  }
}

class MyReduceFunction extends ReduceFunction[SensorReading]{
  override def reduce(value1: SensorReading, value2: SensorReading): SensorReading =
    SensorReading(value1.id, value2.timestamp, value1.temperature.min(value2.temperature))
}