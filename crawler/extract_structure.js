//imports------------------------------------------------------------------------
var request = require('request');
var cheerio = require('cheerio');
var util = require('util');
var _ = require('underscore');
//-----------------------------------------------------------------------------------

var vvz_search_query_page = 'http://vvz.ethz.ch/Vorlesungsverzeichnis/sucheLehrangebot.do?lang=en&semkez=%s&studiengangTyp=%s&deptId=%s&refresh=on';
var data = {levels: {}, departments : {}, programs : {} , sections : {}, lectures : {}};

exports.fetch_structure = function (semester_id, callback) {
    get_structure(semester_id, callback);
}

var callback_counter;
var structure_ready;

function get_structure(semester_id, callback) {
    callback_counter = 0;
    structure_ready = callback;
    var url = util.format(vvz_search_query_page, semester_id, '', '')
    fetch_values(url, 'studiengangTyp', 'name', 'id', function (error, values) {
        if (!error) {
            values.forEach(function (level) {
                //create departments list for level object
                if(!level.departments) level.departments = [];
               //store level
                data.levels[level.id] = level;
                //create url to fetch all departments of this level
                url = util.format(vvz_search_query_page, semester_id, level.id, '');
                fetch_values(url, 'deptId', 'name', 'id', function (error, values) {
                    if (!error) {
                        values.forEach(function (department) {
                            //create levels list for department
                            if(!department.levels) department.levels = [];
                            department.programs = [];
                            //write level/department relationship
                            department.levels.push(level.id);
                            level.departments.push(department.id);
                            //store department
                            if(!data.departments[department.id]) data.departments[department.id] = department;
                            //create url to fetch all programms of this department
                            url = util.format(vvz_search_query_page, semester_id, level.id, department.id);
                            fetch_values(url, 'studiengangAbschnittId', 'name', 'id', function (error, values) {
                                if (!error) {
                                    values.forEach(function (program) {
                                        //write department/program relationship
                                        department.programs.push(program.id);
                                        program.department = department.id;
                                        program.sections = [];
                                        //store program
                                        data.programs[program.id] = program;
                                    })
                                }
                            });
                        });
                    }
                });
            })
        }
    })
}

/**
 * Fetch values from a select/option input field
 * @param url
 * @param select_id name of the select field
 * @param object_name_key name of the json property which contains the name of the option fileds
 * @param callback(error,values)
 */
function fetch_values(url, select_id, object_name_key, id_name_key, callback) {
    callback_counter++;
    request(url, function (error, response, html) {
        var values = null;
        var error = null;
        if (!error && response.statusCode == 200) {
            var $ = cheerio.load(html);
            values = get_select_options($, select_id, object_name_key, id_name_key);
        } else {
            error = 'Failed to load page';
        }
        callback(error, values);
        callback_counter--;
        if (callback_counter <= 0) {
            structure_ready(data);
        }
    });
}

function get_select_options($, select_name, object_name_key, id_name_key) {
    var options = [];
    $('select[name=' + select_name + '] option').each(function (i, element) {
        var element = $(this);
        var opt_name = element.text();
        var opt_id = element.attr("value");
        if (opt_name && opt_id) {
            var opt = {};
            opt[object_name_key] = opt_name;
            opt[id_name_key] = opt_id;
            options.push(opt);
        }
    });
    return options;
}
function collect_data(error, response, html) {
    if (!error && response.statusCode == 200) {
        var $ = cheerio.load(html);
        var semesters = get_select_options($, 'semkez');
        var departments = get_select_options($, 'deptId');
        var latest_semester = semesters[0];
        departments.forEach(function (department) {
            fetch_lectures(latest_semester, department);
        })
    } else console.log('failed to load page with error: ' + error);
}

function _each(object, iterator) {
    for (var key in object) {
        var value = object[key];
        iterator(key, value);
    }
}
