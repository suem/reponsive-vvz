package vvz.model;

import java.util.ArrayList;
import java.util.List;

public class Program {

    public String id;
    public String name;
    public Department department;
    public List<Section> sections;

    public Program(String id, String name) {
        this.id = id;
        this.name = name;
        this.sections = new ArrayList<Section>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Section> getSections() {
        return sections;
    }

    @Override
    public String toString() {
        return name;
    }
}
