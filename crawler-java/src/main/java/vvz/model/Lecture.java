package vvz.model;

import java.util.ArrayList;
import java.util.List;

public class Lecture {

    public String id;
    public String name;
    public String number;
    public List<Section> sections;

    public Lecture(String id, String name) {
        this.id = id;
        this.name = name;
        this.sections = new ArrayList<Section>();
    }

}
