package vvz.model;

import java.util.ArrayList;
import java.util.List;

public class Section {

    public String id;
    public String name;
    public Program program;
    public List<Lecture> lectures;

    public Section(String id, String name) {
        this.id = id;
        this.name = name;
        this.lectures = new ArrayList<Lecture>();
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

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    @Override
    public String toString() {
        return name;
    }
}
