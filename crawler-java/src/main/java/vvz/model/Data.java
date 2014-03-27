package vvz.model;

import java.util.HashMap;
import java.util.Map;

public class Data {
    public Map<String, Department> departments = new HashMap<String, Department>();
    public Map<String, Program> programs = new HashMap<String, Program>();
    public Map<String, Section> sections = new HashMap<String, Section>();
    public Map<String, Lecture> lectures = new HashMap<String, Lecture>();
    public String semesterId;

    public Department getOrstore(Department obj) {
        if (departments.containsKey(obj.getId())) return departments.get(obj.getId());
        else {
            departments.put(obj.getId(),obj);
            return obj;
        }
    }

    public Section getOrStore(Section obj) {
        if (sections.containsKey(obj.getId())) return sections.get(obj.getId());
        else {
            sections.put(obj.getId(),obj);
            return obj;
        }
    }
    public Lecture getOrStore(Lecture obj) {
        if (lectures.containsKey(obj.id)) return lectures.get(obj.id);
        else {
            lectures.put(obj.id,obj);
            return obj;
        }
    }

    public Program getOrStore(Program obj) {
        if (programs.containsKey(obj.getId())) return programs.get(obj.getId());
        else {
            programs.put(obj.getId(),obj);
            return obj;
        }
    }
}
