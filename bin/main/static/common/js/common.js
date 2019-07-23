// AJAX API
var AJAX = {
    apiCall : function(httpMethod, apiURL, reqHeader, reqBody, alwaysFunc, doneFunc, failFunc) {
        if (!httpMethod) {
            console.log("Parameter 'httpMethod' warning! (httpMethod:" + httpMethod + ")");
            console.log("'httpMethod' forcibly changed to 'GET'");
            httpMethod = 'GET';
        }

        if (!apiURL) {
            console.log("Parameter 'apiURL' error! (apiURL:" + httpMethod + ")");
            return false;
        }

        $.ajax({
            method : httpMethod,
            url : apiURL,
            headers: reqHeader,
            type : 'json',
            contentType : 'application/json',
            data : JSON.stringify(reqBody)
        })
        .always(function(data_jqXHR, textStatus, jqXHR_errorThrown) {
            if (!!alwaysFunc) return alwaysFunc(data_jqXHR, textStatus, jqXHR_errorThrown);
        })
        .done(function(data, textStatus, jqXHR) {
            if (!!doneFunc) return doneFunc(data, textStatus, jqXHR);
            return true;
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            if (!!failFunc) return failFunc(jqXHR, textStatus, errorThrown);
            return false;
        });
    },
    // API ResultCode Check
    checkResultSuccess : function(apiResult) {
        if (!apiResult) {
            console.log("Parameter 'apiResult' error! (apiResult:" + apiResult + ")");
            return false;
        }

        return apiResult.result;
    },
    // Get ResultData From API ResultData
    getResultData : function(apiResult, key) {
        if (!apiResult) {
            console.log("Parameter 'apiResult' error! (apiResult:" + apiResult + ")");
            return null;
        }

        if (!key) {
            console.log("Parameter 'key' error! (key:" + key + ")");
            return null;
        }

        var resultData = apiResult.resultData;

        if (!resultData) {
            console.log("Parameter 'resultData' error! (resultData:" + resultData + ")");
            return null;
        }

        return resultData[key];
    }
}

// Add comma(,) each 3point of number string
function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// String.format
if (!String.prototype.format) {
	String.prototype.format = function() {
		var args = arguments;
		return this.replace(/{(\d+)}/g, function(match, number) { 
			return typeof args[number] != 'undefined' ? args[number] : match;
		});
	};
}

// Date.format
Date.prototype.format = function(f) {
    if (!this.valueOf()) return " ";
 
    var weekName = ["일", "월", "화", "수", "목", "금", "토"];
    var d = this;
     
    return f.replace(/(yyyy|yy|MM|dd|E|hh|mm|ss|a\/p)/gi, function($1) {
        switch ($1) {
            case "yyyy": return d.getFullYear();
            case "yy": return (d.getFullYear() % 1000).zf(2);
            case "MM": return (d.getMonth() + 1).zf(2);
            case "dd": return d.getDate().zf(2);
            case "E": return weekName[d.getDay()];
            case "HH": return d.getHours().zf(2);
            case "hh": return ((h = d.getHours() % 12) ? h : 12).zf(2);
            case "mm": return d.getMinutes().zf(2);
            case "ss": return d.getSeconds().zf(2);
            case "a/p": return d.getHours() < 12 ? "AM" : "PM";
            default: return $1;
        }
    });
};
 
String.prototype.string = function(len){var s = '', i = 0; while (i++ < len) { s += this; } return s;};
String.prototype.zf = function(len){return "0".string(len - this.length) + this;};
Number.prototype.zf = function(len){return this.toString().zf(len);};