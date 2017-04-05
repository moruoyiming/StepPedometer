package com.calypso.pedometer.greendao.entry;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Project：StepPedometer
 * Created：jianz
 * Date：2017/3/29 15:45
 * Summry：
 */
@Entity
public class StepInfo {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "STEPCOUNT")
    private long stepCount;

    @Index(unique = true)
    @Property(nameInDb = "DATE")
    private String date;

    @Property(nameInDb = "CRETETIME")
    private String creteTime;

    @Property(nameInDb = "PREVIOUSSTEP")
    private long previousStepCount;

    @Property(nameInDb = "STEPTOTAL")
    private long stepTotal;

    @Generated(hash = 1717967210)
    public StepInfo(Long id, long stepCount, String date, String creteTime,
            long previousStepCount, long stepTotal) {
        this.id = id;
        this.stepCount = stepCount;
        this.date = date;
        this.creteTime = creteTime;
        this.previousStepCount = previousStepCount;
        this.stepTotal = stepTotal;
    }

    @Generated(hash = 1153084582)
    public StepInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStepCount() {
        return this.stepCount;
    }

    public void setStepCount(long stepCount) {
        this.stepCount = stepCount;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreteTime() {
        return this.creteTime;
    }

    public void setCreteTime(String creteTime) {
        this.creteTime = creteTime;
    }

    public long getPreviousStepCount() {
        return this.previousStepCount;
    }

    public void setPreviousStepCount(long previousStepCount) {
        this.previousStepCount = previousStepCount;
    }

    public long getStepTotal() {
        return this.stepTotal;
    }

    public void setStepTotal(long stepTotal) {
        this.stepTotal = stepTotal;
    }


}
