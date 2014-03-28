package vvz.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonWrapper {

    public static String toJSON(Data data) {
        JSONObject obj = new JSONObject();
        //add departments
        JSONObject departments = new JSONObject();
        data.departments.values().forEach((d) -> departments.put(d.id, wrap(d)));
        obj.put("departments", departments);

        //add programs
        JSONObject programs = new JSONObject();
        data.programs.values().forEach((p) -> programs.put(p.id, wrap(p)));
        obj.put("programs", programs);

        //add sections
        JSONObject sections = new JSONObject();
        data.sections.values().forEach((s) -> sections.put(s.id, wrap(s)));
        obj.put("sections", sections);

        //add lectures
        JSONObject lectures = new JSONObject();
        data.lectures.values().forEach((l) -> lectures.put(l.id, wrap(l)));
        obj.put("lectures", lectures);
        return obj.toJSONString();
    }

    private static JSONObject wrap(Section section) {
        JSONObject obj = new JSONObject();
        obj.put("id", section.id);
        obj.put("name", section.name);
        obj.put("program", section.program.id);
        JSONArray arr = new JSONArray();
        section.lectures.forEach((l) -> arr.add(l.id));
        obj.put("lectures", arr);
        return obj;
    }

    private static JSONObject wrap(Program program) {
        JSONObject obj = new JSONObject();
        obj.put("id", program.id);
        obj.put("name", program.name);
        obj.put("department", program.department.id);
        JSONArray arr = new JSONArray();
        program.sections.forEach((s) -> arr.add(s.id));
        obj.put("sections", arr);
        return obj;
    }

    private static JSONObject wrap(Department department) {
        JSONObject obj = new JSONObject();
        obj.put("id", department.id);
        obj.put("name", department.name);
        JSONArray arr = new JSONArray();
        department.programs.forEach((p) -> arr.add(p.id));
        obj.put("programs", arr);
        return obj;
    }

    private static JSONObject wrap(Lecture lecture) {
        JSONObject obj = new JSONObject();
        obj.put("id", lecture.id);
        obj.put("name", lecture.name);
        obj.put("number", lecture.number);
        JSONArray arr = new JSONArray();
        lecture.sections.forEach((l) -> arr.add(l.id));
        obj.put("sections", arr);

        return obj;
    }


}
