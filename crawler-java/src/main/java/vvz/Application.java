package vvz;

import vvz.model.JsonWrapper;
import vvz.model.Lecture;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Application {
    public static void main(String[] args) {

        String sem = "2014S";
        try {
            StructureExtractor ex = new StructureExtractor(sem);
            ex.fetchStructure();
            ex.fetchLectureList();
            ex.populateAllLectures();
//            Lecture l = ex.getData().getOrStore(new Lecture("90519", null));
//            ex.populateLecture(l);
            String json = JsonWrapper.toJSON(ex.getData());
            PrintWriter pw = new PrintWriter(new File(sem + ".json"));
            pw.println(json);
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
