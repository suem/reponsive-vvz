package vvz.model;

import java.util.ArrayList;
import java.util.List;

public class Department {

    public String id;
    public String name;
    public List<Program> programs;

    public Department(String id, String name) {
        this.id = id;
        this.name = name;
        this.programs = new ArrayList<Program>();
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

    public List<Program> getPrograms() {
        return programs;
    }

    @Override
    public String toString() {
        return name;
    }
}
