# StepPedometer
Android 记步应用软件

* GitHub 项目地址 : [https://github.com/moruoyiming/StepPedometer](https://github.com/moruoyiming/StepPedometer)

#Preview

![image](https://github.com/moruoyiming/StepPedometer/blob/master/pics/Screenshot_2017-05-03-15-34-47-980_com.calypso.pe.png)

#Example

[app-debug.apk](https://github.com/moruoyiming/StepPedometer/blob/master/apks/app-debug.apk)

##技术
 * 运用GreenDao数据库实现数据存储
 * 记步功能调用自带的计步传感器,步行检测传感器,加速传感器 三种检测方式
 * 运用MPAndroidChart展现数据
 * 自定义圆形ProgressBar实现渐变色
 * 自定义View实现数据滚动

##部分代码
```   private void addCountStepListener() {
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            stepSensor = 0;
            Log.v(TAG, "countSensor 计步传感器");
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else if (detectorSensor != null) {
            stepSensor = 1;
            Log.v(TAG, "detector 步行检测传感器");
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            stepSensor = 2;
            Log.v(TAG, "Count sensor not available! 没有可用的传感器，只能用加速传感器了");
            addBasePedoListener();
        }
    }
```

##部分功能并不完善,还在优化当中.欢迎大家Star 及 Mark 也可以分享给小伙伴哦;

#License

Copyright 2016 jianz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.