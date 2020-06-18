package com.getconfig.Model

import com.poiji.annotation.*

public class Employee {
    @ExcelRow
    private int rowIndex;

    @ExcelCell(0)
    protected String name = "";

    @ExcelCell(1)
    protected String email = "";

    @ExcelCell(2)
    protected String birth = "";

    @ExcelCell(3)
    protected int salary;

    @ExcelCell(4)
    protected int department;

    @Override
    public String toString() {
        return "Employee{" + rowIndex +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", birth=" + birth +
                ", salary=" + salary +
                ", department='" + department + '\'' +
                '}';
    }
}
