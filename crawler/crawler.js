var request = require('request');
var cheerio = require('cheerio');
var util = require('util');
var extract_structure = require('./extract_structure.js');
var fs = require('fs');
var async = require('async');

var vvz_lecture_list = 'http://vvz.ethz.ch/Vorlesungsverzeichnis/sucheLehrangebot.do?lang=en&semkez=%s&deptId=%s&seite=0';
var vvz_lecture = 'http://vvz.ethz.ch/Vorlesungsverzeichnis/lerneinheitPre.do?semkez=%s&lang=en&ansicht=ALLE&lerneinheitId=%s';

var data = require('./dummy_data.js').data;
var lecture_ids = require('./dummy-lecture-list.js').lectures;

collect('2014S');

function collect(semester_id) {
    console.log('requesting lecture id list');
    fetch_lectures(semester_id,'',function(error,lecture_ids) {
        if(!error) {
            var lecture_count = lecture_ids.length;
            console.log('fetched '+lecture_count+' lecture ids');
            var callbacks = [];
            lecture_ids.forEach(function(id) {
                var fetch_lecture_callback = function(callback) {
                    console.log('requesting lecture '+id);
                    fetch_lecture(semester_id,id,function(error,lecture) {
                        if(!error) {
                            console.log('fetched lecture '+lecture.title);
                        } else console.log('failed to fetch lecture '+id);
                        callback(null,id);
                    });
                }
                callbacks.push(fetch_lecture_callback);
            });
            //call with async library
            async.series(callbacks,function(err,results){
                console.log('fetched all lectures, writing to file');
                fs.writeFileSync('data_'+semester_id+'.json',JSON.stringify(data));
                console.log('done');
            })

        } else console.log('failed to fetch lecture list:'+error);
    });
}

function fetch_lectures(semester_id, department_id, callback) {
    var url = util.format(vvz_lecture_list, semester_id, department_id);
    request(url, function (error, response, html) {
        var lecture_ids = {};
        if (!error && response.statusCode == 200) {
            console.log('successfully loaded lecture list');
            var $ = cheerio.load(html);
            var links = $("a[href*='lerneinheitId=']");
            var regex = /lerneinheitId=(\d+)/;
            links.each(function (i, link) {
                var link = $(this);
                var regexMatch = regex.exec(link.attr('href'));
                if (regexMatch) {
                    var id = regexMatch[1];
                    lecture_ids[id] = id;
                }
            });
        }
        callback(error,Object.keys(lecture_ids));
    });
}

function fetch_lecture(semester_id, lecture_id,callback) {
    var url = util.format(vvz_lecture, semester_id, lecture_id);
    var lecture = {};
    lecture.id = lecture_id;
    data.lectures[lecture.id] = lecture;
    request(url, function (error, response, html) {
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);

            //title, number general information
            var title = $("h2").first().text().trim();
            var title_regex = /(.*L)(\s*)(.*)/;
            var title_match = title_regex.exec(title);
            if (title_match) {
                lecture.title = title_match[3];
                lecture.number = title_match[1];
            } else lecture.title = title;
            lecture.lecturers = get_row_value($, 'Lecturers').split(',').map(function (str) {
                return str.trim()
            });
            lecture.periodicity = get_row_value($, 'Periodicity');
            lecture.language = get_row_value($, 'Language of instruction');

            //catalogue data
            lecture.abstract = get_row_value($, 'Abstract');
            lecture.objective = get_row_value($, 'Objective');
            lecture.content = get_row_value($, 'Content');
            lecture.literature = get_row_value($, 'Literature');
            lecture.prerequisites = get_row_value($, 'Prerequisites / Notice');

            //performance assesment
            //TODO does not work somehow, maybe a bug
            lecture.examination_block = get_row_value($, 'In examination block for');
            var credit_result = /\d+/.exec(get_row_value($, 'ECTS credits'));
            if(credit_result) {
                lecture.credits = credit_result[0];
            }
            lecture.examiners = get_row_value($, 'Examiners').split(',').map(function (str) {
                return str.trim()
            });
            lecture.type = get_row_value($, 'Type');
            lecture.examination_language = get_row_value($, 'Language of examination');
            lecture.attendance_required = get_row_value($, 'Course attendance confirmation required');
            lecture.repetition = get_row_value($, 'Repetition');
            lecture.examination_mode = get_row_value($, 'Mode of examination');
            lecture.examination_aids = get_row_value($, 'Written aids');

            //programs
            fetch_programs($, lecture);
        }
        callback(error,lecture);
    });
}

function fetch_programs($, lecture) {
    var cell = get_cell($, 'Offered in');
    var rows = cell.parents('tbody').find('tr:has(a)');
    var id_regex = /abschnittId=(\d+)/;
    lecture.sections = [];

    rows.each(function () {
        var row = $(this);
        var cells = row.find('td');
        var program_link = cells.eq(0).find('a');
        var section__link = cells.eq(1).find('a');
        var program_id = id_regex.exec(program_link.attr('href'))[1];
        var section_id = id_regex.exec(section__link.attr('href'))[1];
        var section_name = section__link.text();

        var section  = data.sections[section_id];
        if(!data.sections[section_id]) {
            //create new section
            section = {id: section_id, name: section_name, program: program_id, lectures : []};
            data.sections[section_id] = section;
            data.programs[program_id].sections.push(section_id);
        }
        //add this lecture to section
        section.lectures.push(lecture.id);
        //add section to lecture
        lecture.sections.push(section_id);
    });
}

function get_row_value($, key) {
    var value = get_cell($, key).next().text().trim();
    return value;
}

function get_cell($, key) {
    var matcher = function () {
        var text = $(this).text();
        var regex = new RegExp(key + "$");
        return regex.exec(text);
    }
    var row = $("td:contains('" + key + "')").filter(matcher).first();
    return row;
}
