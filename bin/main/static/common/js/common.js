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
            data : (!!reqBody ? JSON.stringify(reqBody) : '')
        })
        .always(function(data_jqXHR, textStatus, jqXHR_errorThrown) {
            if (!!alwaysFunc) return alwaysFunc(data_jqXHR, textStatus, jqXHR_errorThrown);
        })
        .done(function(data, textStatus, jqXHR) {
            if (!!doneFunc) return doneFunc(data, textStatus, jqXHR);
            return true;
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            if (!!failFunc) return failFunc(jqXHR, JSON.stringify(textStatus), errorThrown);
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

        var resultData = apiResult.result_data;

        if (!resultData) {
            console.log("Parameter 'resultData' error! (resultData:" + resultData + ")");
            return null;
        }

        if (!key) {
            return resultData;
        }

        return resultData[key];
    },
    // Get Result message from API Result
    getResultMsg : function(apiResult) {
        return apiResult.result_msg;
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

// SHA-256
function SHA256(s) {
    var chrsz   = 8;
    var hexcase = 0;
  
    function safe_add (x, y) {
        var lsw = (x & 0xFFFF) + (y & 0xFFFF);
        var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
        return (msw << 16) | (lsw & 0xFFFF);
    }
  
    function S (X, n) { return ( X >>> n ) | (X << (32 - n)); }
    function R (X, n) { return ( X >>> n ); }
    function Ch(x, y, z) { return ((x & y) ^ ((~x) & z)); }
    function Maj(x, y, z) { return ((x & y) ^ (x & z) ^ (y & z)); }
    function Sigma0256(x) { return (S(x, 2) ^ S(x, 13) ^ S(x, 22)); }
    function Sigma1256(x) { return (S(x, 6) ^ S(x, 11) ^ S(x, 25)); }
    function Gamma0256(x) { return (S(x, 7) ^ S(x, 18) ^ R(x, 3)); }
    function Gamma1256(x) { return (S(x, 17) ^ S(x, 19) ^ R(x, 10)); }
  
    function core_sha256 (m, l) {
        var K = new Array(
            0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5, 0x3956C25B, 0x59F111F1,
            0x923F82A4, 0xAB1C5ED5, 0xD807AA98, 0x12835B01, 0x243185BE, 0x550C7DC3,
            0x72BE5D74, 0x80DEB1FE, 0x9BDC06A7, 0xC19BF174, 0xE49B69C1, 0xEFBE4786,
            0xFC19DC6, 0x240CA1CC, 0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA,
            0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7, 0xC6E00BF3, 0xD5A79147,
            0x6CA6351, 0x14292967, 0x27B70A85, 0x2E1B2138, 0x4D2C6DFC, 0x53380D13,
            0x650A7354, 0x766A0ABB, 0x81C2C92E, 0x92722C85, 0xA2BFE8A1, 0xA81A664B,
            0xC24B8B70, 0xC76C51A3, 0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070,
            0x19A4C116, 0x1E376C08, 0x2748774C, 0x34B0BCB5, 0x391C0CB3, 0x4ED8AA4A,
            0x5B9CCA4F, 0x682E6FF3, 0x748F82EE, 0x78A5636F, 0x84C87814, 0x8CC70208,
            0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2);

        var HASH = new Array(
            0x6A09E667, 0xBB67AE85, 0x3C6EF372, 0xA54FF53A, 0x510E527F, 
            0x9B05688C, 0x1F83D9AB, 0x5BE0CD19);

        var W = new Array(64);
        var a, b, c, d, e, f, g, h, i, j;
        var T1, T2;
  
        m[l >> 5] |= 0x80 << (24 - l % 32);
        m[((l + 64 >> 9) << 4) + 15] = l;
  
        for (var i = 0; i < m.length; i += 16) {
            a = HASH[0];
            b = HASH[1];
            c = HASH[2];
            d = HASH[3];
            e = HASH[4];
            f = HASH[5];
            g = HASH[6];
            h = HASH[7];
  
            for (var j = 0; j < 64; ++j) {
                if (j < 16) W[j] = m[j + i];
                else W[j] = safe_add(safe_add(safe_add(Gamma1256(W[j - 2]), W[j - 7]), Gamma0256(W[j - 15])), W[j - 16]);
  
                T1 = safe_add(safe_add(safe_add(safe_add(h, Sigma1256(e)), Ch(e, f, g)), K[j]), W[j]);
                T2 = safe_add(Sigma0256(a), Maj(a, b, c));
  
                h = g;
                g = f;
                f = e;
                e = safe_add(d, T1);
                d = c;
                c = b;
                b = a;
                a = safe_add(T1, T2);
            }
  
            HASH[0] = safe_add(a, HASH[0]);
            HASH[1] = safe_add(b, HASH[1]);
            HASH[2] = safe_add(c, HASH[2]);
            HASH[3] = safe_add(d, HASH[3]);
            HASH[4] = safe_add(e, HASH[4]);
            HASH[5] = safe_add(f, HASH[5]);
            HASH[6] = safe_add(g, HASH[6]);
            HASH[7] = safe_add(h, HASH[7]);
        }
        return HASH;
    }
  
    function str2binb (str) {
        var bin = Array();
        var mask = (1 << chrsz) - 1;
        for(var i = 0; i < str.length * chrsz; i += chrsz) {
            bin[i>>5] |= (str.charCodeAt(i / chrsz) & mask) << (24 - i%32);
        }
        return bin;
    }
  
    function Utf8Encode(string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";
  
        for (var n = 0; n < string.length; n++) {
  
            var c = string.charCodeAt(n);
  
            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
  
        }
  
        return utftext;
    }
  
    function binb2hex (binarray) {
        var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
        var str = "";
        var binLen = binarray.length * 4;
        for(var i = 0; i < binLen; ++i) {
            str += hex_tab.charAt((binarray[i >> 2] >> ((3 - i % 4) * 8 + 4)) & 0xF) +
            hex_tab.charAt((binarray[i >> 2] >> ((3 - i % 4) * 8  )) & 0xF);
        }
        return str;
    }
  
    s = Utf8Encode(s);
    return binb2hex(core_sha256(str2binb(s), s.length * chrsz));
}

// 패스워드 조건 검사
function checkPassword(password, passwordCheck) {
    if (!password) {
        alert('비밀번호를 입력해주세요.');
        return false;
    }
    else if (password.length < 8 || password.length > 32) {
        alert('비밀번호는 8~32자 사이여야 합니다.\n(현재: ' + password.length + '자)');
        return false;
    }
    else if (!!passwordCheck && password != passwordCheck) {
        alert('비밀번호 확인이 일치하지 않습니다.');
        return false;
    }

    return true;
}

// 닉네임 조건 검사
function checkNickname(nickname) {
    if (!nickname) {
        alert('닉네임을 입력해주세요.');
        return false;
    }
    else if (nickname.length < 2 || nickname.length > 16) {
        alert('닉네임은 2~16자 사이여야 합니다.\n(현재: ' + nickname.length + '자)');
        return false;
    }

    return true;
}

// 이름 조건 검사
function checkFullName(fullName) {
    if (!fullName) {
        alert('이름을 입력해주세요.');
        return false;
    }
    else if (fullName.length < 1 || fullName.length > 64) {
        alert('이름은 1~64자 사이여야 합니다.\n(현재: ' + fullName.length + '자)');
        return false;
    }

    return true;
}

// 성별 조건 검사
function checkGender(gender) {
    if (!gender) {
        alert('성별을 선택해주세요.');
        return false;
    }

    if (gender != 'M' && gender != 'F') {
        alert('성별값은 M또는 F여야 합니다.\n(현재: ' + gender + ')');
        return false;
    }

    return true;
}

// 생년월일 문자열을 시간(ms)으로 변환
function convertDateOfBirth(dateOfBirth) {
    if (!dateOfBirth) {
        alert('생년월일을 입력해주세요.');
        return null;
    }

    if (dateOfBirth.length < 8) {
        alert('올바른 생년월일값을 입력해주세요.\n(현재: ' + dateOfBirth + ' / 양식: yyyy.MM.dd)');
        return null;
    }

    // Format : yyyy.MM.dd
    var monthIdx = dateOfBirth.indexOf('.');
    var dateIdx = dateOfBirth.lastIndexOf('.');
    var year = null;
    var month = null;
    var date = null;

    try {
        year = parseInt(dateOfBirth.substring(0, monthIdx));
        month = parseInt(dateOfBirth.substring(monthIdx + 1, dateIdx));
        date = parseInt(dateOfBirth.substring(dateIdx + 1, dateOfBirth.length));
    }
    catch {
        alert('올바른 생년월일값을 입력해주세요.\n(현재: ' + dateOfBirth + ' / 양식: yyyy.MM.dd)');
        return null;
    }

    if (year < 0 || year > 9999) {
        alert('올바른 연도를 입력해주세요.\n(현재: ' + year + ")")
        return null;
    }

    if (month < 0 || month > 12) {
        alert('올바른 월을 입력해주세요.\n(현재: ' + year + ")")
        return null;
    }

    if (date < 0 || date > 31) {
        alert('올바른 일을 입력해주세요.\n(현재: ' + date + ")")
        return null;
    }

    var date = null;
    
    if ((date = new Date(dateOfBirth)) == null) {
        alert('생년월일 변환중 오류가 발생했습니다. (입력값: ' + dateOfBirth + ')');
        return null;
    }

    return date.getTime();
}