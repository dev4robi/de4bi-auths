package com.authserver.controller.restcontroller;

import java.util.Map;

import com.authserver.data.ApiResult;
import com.authserver.data.vo.IssueUserJwtVO;
import com.authserver.data.vo.ValidateUserJwtVO;
import com.authserver.service.UsersService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
public class UsersApiController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private UsersService usersSvc;
   
    @GetMapping("/users/api/duplicated/{nickname}")
    public Map<String, Object> userApiCheckEmailDuplicated(@PathVariable String nickname) {
        ApiResult selectResult = usersSvc.selectUserByKey("nickname", nickname);

        if (selectResult.getResult() && selectResult.getDataAsStr("selectedUser") != null) {
            return ApiResult.make(false).toMap(); // 이미 회원정보 있음
        }
        
        return ApiResult.make(true).toMap();
    }

    @PostMapping("/users/api/jwt/issue")
    public Map<String, Object> userApiGetJwt(@RequestBody IssueUserJwtVO issueUserJwtVo) {
        return usersSvc.issueUserJwt(issueUserJwtVo.getAudience(), issueUserJwtVo.getEmail(),
                                     issueUserJwtVo.getPassword(), issueUserJwtVo.getDuration()).toMap();
    }

    @PostMapping("/users/api/jwt/validate")
    public Map<String, Object> userApiGetJwt(@RequestBody ValidateUserJwtVO validateUserJwtVo) {
        return usersSvc.validateUserJwt(validateUserJwtVo.getAudience(), validateUserJwtVo.getUserJwt()).toMap();
    }

/*
    @RequestMapping(value="/users/api/jwt", method={ RequestMethod.GET, RequestMethod.POST })
    public Map<String, Object> userApiGetJwt(
        @ModelAttribute("audience") String audience,
        @ModelAttribute("email") String email,
        @ModelAttribute("password") String password,
        @ModelAttribute("duration") String duration) {
        return usersSvc.issueUserJwt(audience, email, password, Long.parseLong(duration)).toMap();
    }
*/
}