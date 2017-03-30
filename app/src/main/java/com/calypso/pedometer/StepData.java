package com.calypso.pedometer;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * 数据库中的表类
 * Created by lenovo on 2017/1/17.
 */
@Table("step")   //标记为表名  使用了第三方框架
public class StepData {
    //@*** 加入的jar包里的功能，方便对于数据库的管理
    //指定自增，每个对象需要一个主键
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

    @Column("today")
    private String today;
    @Column("step")
    private String step;
    @Column("previousStep")
    private String previousStep;

    public void setId(int id) {
        this.id = id;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public int getId(){
        return id;
    }

    public String getPreviousStep() {
        return previousStep;
    }

    public void setPreviousStep(String previousStep) {
        this.previousStep = previousStep;
    }
}
