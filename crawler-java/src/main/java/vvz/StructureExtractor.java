package vvz;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import vvz.model.*;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureExtractor {
    private static final Logger log = Logger.getLogger(StructureExtractor.class.getName());
    private static final String STRUCTURE_URL = "http://vvz.ethz.ch/Vorlesungsverzeichnis/sucheLehrangebot.do?lang=en&semkez=%s&deptId=%s&studiengangAbschnittId=%s&bereichAbschnittId=%s&refresh=on";
    private static final String LECTURES_URL = "http://vvz.ethz.ch/Vorlesungsverzeichnis/sucheLehrangebot.do?lang=en&semkez=%s&deptId=%s&studiengangAbschnittId=%s&bereichAbschnittId=%s&seite=0";
    private static final String LECTURE_URL = "http://vvz.ethz.ch/Vorlesungsverzeichnis/lerneinheitPre.do?semkez=%s&lang=en&ansicht=ALLE&lerneinheitId=%s";

    private static final String DEPARTMENT = "deptId";
    private static final String PROGRAM = "studiengangAbschnittId";
    private static final String SECTION = "bereichAbschnittId";

    private Data data;

    public StructureExtractor(String semesterId) {
        this.data = new Data();
        this.data.semesterId = semesterId;
    }

    private String getUrl(String semesterId, String departmentId, String programId, String sectionId) {
        return String.format(STRUCTURE_URL, semesterId, departmentId, programId, sectionId);
    }

    private String getLecturesUrl(String semesterId, String departmentId, String programId, String sectionId) {
        return String.format(LECTURES_URL, semesterId, departmentId, programId, sectionId);
    }

    private String getLectureUrl(String semesterId, String lectureId) {
        return String.format(LECTURE_URL, semesterId, lectureId);
    }

    private Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).timeout(0).get();
    }

    private String getUrlAttribure(String url,String attrname) {
        Pattern idPattern = Pattern.compile("(.*)"+attrname+"=(\\d+)(.*)");
        Matcher m = idPattern.matcher(url);
        if (m.matches()) {
            return m.group(2);
        } else return null;
    }


    public void fetchLectureList() throws IOException {
        String url = getLecturesUrl(data.semesterId, "5", "", "");
        Document doc = getDocument(url);
        Elements linkElements = doc.select("a[href*=lerneinheitId]");
        Pattern idPattern = Pattern.compile("(.*)lerneinheitId=(\\d+)(.*)");
        for (Element linkElement : linkElements) {
            String href = linkElement.attr("href");
            Matcher m = idPattern.matcher(href);
            if (m.matches()) {
                String id = m.group(2);
                Lecture lecture = data.getOrStore(new Lecture(id, ""));
            }
        }
        System.out.printf("Fetched " + data.lectures.size() + " lectures");
    }

    public void populateAllLectures() {
        for (Lecture lecture : data.lectures.values()) {
            try {
                populateLecture(lecture);
            } catch (IOException e) {
                log.warning("Exception while populating lecture " + lecture.id + ", cause: " + e.getCause());
            }
        }
    }

    public void populateLecture(Lecture lecture) throws IOException {
//        log.info("populating lecture "+lecture.id);
//        Document doc = Jsoup.parse(new File("lecture.htm"), "UTF-8");
        Document doc = getDocument(getLectureUrl(data.semesterId, lecture.id));
        if (!setTitleAndNumber(lecture, doc)) log.warning("Failed to parse title and number of lecture: " + lecture.id);
        if (!setSections(lecture, doc)) log.warning("Failed to parse sections of lecture: " + lecture.id);
        log.info("Done populating lecture: " + lecture.name);
    }

    private boolean setSections(Lecture lecture, Document doc) {
        Elements headerElems = doc.select("tr td:matches(Offered in)");
        try {
            Element sectionsElements = headerElems.first().parent().nextElementSibling().nextElementSibling();
            do {
                String programLink = sectionsElements.child(0).child(0).attr("href");
                String sectionLink = sectionsElements.child(1).child(0).attr("href");
                String sectionName = sectionsElements.child(1).child(0).text().trim();
                String programId = getUrlAttribure(programLink, "abschnittId");
                String sectionId = getUrlAttribure(sectionLink,"abschnittId");

                Program program = data.programs.get(programId);
                assert program != null;
                Section section = new Section(sectionId, sectionName);
                section.program = program;
                section = data.getOrStore(section);

                section.lectures.add(lecture);
                lecture.sections.add(section);
                program.sections.add(section);

                sectionsElements = sectionsElements.nextElementSibling();
            } while (sectionsElements != null);

            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean setTitleAndNumber(Lecture lecture, Document doc) {
        Elements elements = doc.select("h2");
        if (elements.size() == 1) {
            Element titleElement = elements.first();
            String text = titleElement.text();
            Pattern p = Pattern.compile("(.*L)(.*)"); //TODO fix this regex
            Matcher m = p.matcher(text);
            if (m.matches()) {
                lecture.number = m.group(1).trim();
                lecture.name = m.group(2).trim();
                return true;
            }
        }
        return false;
    }


    public void fetchStructure() throws IOException {
        Document doc = getDocument(getUrl(data.semesterId, "", "", ""));
        List<OptionElement> depOps = getSelectOptions(doc, DEPARTMENT);
        for (OptionElement depOp : depOps) {
            Department department = data.getOrstore(new Department(depOp.id, depOp.name));
            doc = getDocument(getUrl(data.semesterId, depOp.id, "", ""));
            List<OptionElement> progOps = getSelectOptions(doc, PROGRAM);
            for (OptionElement progOp : progOps) {
                Program program = data.getOrStore(new Program(progOp.id, progOp.name));
                program.setDepartment(department);
                department.getPrograms().add(program);
            }
        }
        log.info("Fetched structure");
    }

    private List<OptionElement> getSelectOptions(Document doc, String selectName) {
        Elements optionElements = doc.select(String.format("select[name=%s] option", selectName));
        List<OptionElement> list = new ArrayList<OptionElement>();
        for (Element optionElement : optionElements) {
            String optionName = optionElement.text();
            String optionId = optionElement.val();
            if (optionName != "" && optionId != "")
                list.add(new OptionElement(optionId, optionName));
        }
        return list;
    }

    public Data getData() {
        return data;
    }
}
