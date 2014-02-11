var request = require('request');
var cheerio = require('cheerio');
var util = require('util');
var extract_structure = require('./extract_structure.js');

var vvz_lecture_list = 'http://vvz.ethz.ch/Vorlesungsverzeichnis/sucheLehrangebot.do?lang=en&semkez=%s&seite=0';
var vvz_lecture = 'http://vvz.ethz.ch/Vorlesungsverzeichnis/lerneinheitPre.do?semkez=2014S&lang=en&ansicht=ALLE&lerneinheitId=%s';

var lectures = {};

//extract_structure.fetch_structure('2014S',function(programms){
//    console.log(programms);
//});

//fetch_lectures('2014S');
fetch_lecture(89544,function(lecture){
    console.log(lecture);
});

function fetch_lectures(semester_id) {
    var url = util.format(vvz_lecture_list, semester_id);
    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);
            var links = $("a[href*='lerneinheitId=']");
            var regex = /lerneinheitId=(\d+)/;
            links.each(function (i, link) {
                var link = $(this);
                var regexMatch = regex.exec(link.attr('href'));
                if (regexMatch) {
                    var id = regexMatch[1];
                    fetch_lecture(id);
                }
            });
        } else console.log("failed to load lectures for " + semester_id);
    });
}

function fetch_lecture(lecture_id, callback) {
    var url = util.format(vvz_lecture, lecture_id);
    var lecture = {};
    lecture.id = lecture_id;
    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);

            //title, number general information
            var title = $("h2").first().text().trim();
            var title_regex = /(.*L)(\s*)(.*)/;
            var title_match = title_regex.exec(title);
            if(title_match) {
               lecture.title = title_match[3];
               lecture.number= title_match[1];
            } else lecture.title = title;
            lecture.lecturers = get_row_value($,'Lecturers').split(',').map(function(str){return str.trim()});
            lecture.periodicity = get_row_value($,'Periodicity');
            lecture.language = get_row_value($,'Language of instruction');

            //catalogue data
            lecture.abstract = get_row_value($,'Abstract');
            lecture.objective = get_row_value($,'Objective');
            lecture.content = get_row_value($,'Content');
            lecture.literature = get_row_value($,'Literature');
            lecture.prerequisites = get_row_value($,'Prerequisites / Notice');

            //performance assesment
            //TODO does not work somehow, maybe a bug
            lecture.examination_block = get_row_value($,'In examination block for')
            lecture.credits = /\d+/.exec(get_row_value($,'ECTS credits'))[0];
            lecture.examiners = get_row_value($,'Examiners').split(',').map(function(str){return str.trim()});
            lecture.type = get_row_value($,'Type')
            lecture.examination_language = get_row_value($,'Language of examination')
            lecture.attendance_required = get_row_value($,'Course attendance confirmation required')
            lecture.repetition = get_row_value($,'Repetition')
            lecture.examination_mode = get_row_value($,'Mode of examination')
            lecture.examination_aids = get_row_value($,'Written aids')
            callback(lecture);
        }
    });
}

function get_row_value($, key) {
    var matcher = function(i,elem) {
        var text = $(this).text();
        var regex = new RegExp(key+"$");
        return regex.exec(text);
    }
    var value = $("td:contains('"+key+"')").filter(matcher).first().next().text().trim();
    return value;
}

